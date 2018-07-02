package com.democrat.expes.rjc18;

import com.democrat.ancortodemocrat.AncorToDemocrat;
import com.democrat.ancortodemocrat.ConversionToArff;
import com.democrat.ancortodemocrat.Corpus;
import com.democrat.ancortodemocrat.ParamToArff;
import com.democrat.classification.Chaining;
import com.democrat.classification.Classification;
import com.democrat.classification.ModelGeneration;
import com.democrat.expes.Experience;
import com.democrat.expes.ExpesArgs;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


import org.odftoolkit.odfdom.dom.OdfSchemaDocument;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.dom.style.props.OdfTableCellProperties;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeAutomaticStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.*;
import weka.classifiers.Classifier;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.democrat.ancortodemocrat.AncorToDemocrat.generateFeature;

public class T6 implements Experience{
    private static Logger logger = Logger.getLogger(AncorToDemocrat.class);

    public static final String EXPE_NAME = "t6";
    private static final String WORKING_DIR = "/tmp/"+ RJC18.EXPE_PREFIX+"/"+EXPE_NAME;
    private static final String CORPUS_DIR = WORKING_DIR+"/corpus";
    private static final String FEATURES_DIR = WORKING_DIR+"/features";
    private static final String ARFF_DIR = WORKING_DIR+"/arff";
    private static final String MODELS_DIR = WORKING_DIR+"/models";
    private static final String CLASSIF_DIR = WORKING_DIR + "/classif";
    private static final String CHAINS_DIR = WORKING_DIR + "/chaines";

    private final String[] corpus_train = {"corpus_ESLO_apprentissage", "corpus_OTG_apprentissage"};
    private final String[] corpus_test = {"corpus_ESLO_test", "corpus_OTG_test", "corpus_UBS_test"};

    private final ExpesArgs eargs;


    public T6(String[] args) throws IOException {
        eargs = new ExpesArgs(args, RJC18.EXPE_PREFIX+"_"+EXPE_NAME);
        prepareRep();
    }

    @Override
    public void run() {
        try {
            ExecutorService executor;
            ArrayList<JobScorer> jobs = new ArrayList<>();
            if(!eargs.skip_features) {
                logger.info("Calcul des Features");
                executor = Executors.newFixedThreadPool(eargs.num_threads);
                features(executor);
            }
            if(!eargs.skip_arff) {
                logger.info("Calcul des arff");
                executor = Executors.newFixedThreadPool(eargs.num_threads);
                arff(executor);
            }
            if(!eargs.skip_models) {
                logger.info("Calcul des models");
                executor = Executors.newFixedThreadPool(eargs.num_threads);
                models(executor);
            }
            if(!eargs.skip_classif){
                logger.info("Classification");
                executor = Executors.newFixedThreadPool(eargs.num_threads);
                classif(executor);
            }
            if(!eargs.skip_chains){
                logger.info("Calcul des chaines");
                executor = Executors.newFixedThreadPool(eargs.num_threads);
                chains(executor);
            }
            if(!eargs.skip_scorers){
                logger.info("Calcul des scores");
                executor = Executors.newFixedThreadPool(eargs.num_threads);

                HashMap<String, HashMap<String, HashMap<String, ArrayList<ArrayList<ArrayList<JobScorer>>>>>>
                        jpa = scores(executor, jobs);
                if(!eargs.skip_output){
                    writeODS(jobs, jpa);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private HashMap<String, HashMap<String, HashMap<String, ArrayList<ArrayList<ArrayList<JobScorer>>>>>>
    scores(ExecutorService executor, ArrayList<JobScorer> jobs) throws Exception
    {
        File rep = new File(CHAINS_DIR);
        Pattern p = Pattern.compile("^([a-zA-Z0-9]+)_([a-zA-Z]+)_([a-zA-Z]+)_([0-9]+)_([0-9]+)_([0-9]+)_GOLD\\.conll");

        //ALGO / train / test / num_test / run_train / run_test
        HashMap<String,HashMap<String, HashMap<String, ArrayList<ArrayList<ArrayList<JobScorer>>>>>>
                jobParAlgo = new HashMap<>();
        for(String algo : eargs.algos){
            jobParAlgo.put(algo, new HashMap<>());
            for(String cTrain : new String[]{"ESLO","OTG"}){
                jobParAlgo.get(algo).put(cTrain,new HashMap<>());
                for(String cTest : new String[]{"ESLO","OTG","UBS"}){
                    jobParAlgo.get(algo).get(cTrain).put(cTest,new ArrayList<>());
                    for(int numTest = 0; numTest < 3; numTest++){
                        jobParAlgo.get(algo).get(cTrain).get(cTest).add(new ArrayList<>());
                        for(int run_train = 0; run_train < eargs.num_learn_run; run_train++){
                            jobParAlgo.get(algo).get(cTrain).get(cTest).get(numTest).add(new ArrayList<>());
                            for(int run_test = 0; run_test < eargs.num_test_run; run_test++)
                                jobParAlgo.get(algo).get(cTrain).get(cTest).get(numTest).get(run_train).add(null);
                        }
                    }
                }
            }
        }


        for(File fichier : rep.listFiles()){
            Matcher m = p.matcher(fichier.getName());
            if(m.matches()){
                String algo = m.group(1);
                String train = m.group(2);
                String test = m.group(3);
                Integer num_test = Integer.parseInt(m.group(4)) -1;
                Integer run_train = Integer.parseInt(m.group(5));
                Integer run_test = Integer.parseInt(m.group(6));

                JobScorer jbs = new JobScorer(fichier.getName());
                jobs.add(jbs);
                jobParAlgo
                        .get(algo)
                        .get(train)
                        .get(test)
                        .get(num_test)
                        .get(run_train)
                        .set(run_test,jbs);

                executor.execute(jbs);
            }
            else if (fichier.getName().contains("_GOLD.conll"))
                logger.info("Scores: "+fichier.getName()+" didnt't match to the pattern");
        }
        executor.shutdown();
        executor.awaitTermination(3600, TimeUnit.SECONDS);
        return jobParAlgo;
    }

    private void writeODS(ArrayList<JobScorer> jobs,
                          HashMap<String, HashMap<String, HashMap<String, ArrayList<ArrayList<ArrayList<JobScorer>>>>>>
                                  jpa) throws Exception
    {
        logger.info("Ecriture du fichier ods");
        SpreadsheetDocument sheet = SpreadsheetDocument.newSpreadsheetDocument();

        Table table = sheet.getSheetByIndex(0);
        table.setTableName("Données Brutes");
        ecrireDonneesBrutes(table, jobs, sheet);

        table = sheet.addTable();
        table.setTableName("T6");
        ecrireDonneesMiseEnForme(table, jpa, sheet);

        sheet.save(eargs.out_ods);
    }

    private void ecrireDonneesMiseEnForme(Table table,
                                          HashMap<String, HashMap<String, HashMap<String, ArrayList<ArrayList<ArrayList<JobScorer>>>>>>
                                                  jobs,
                                          SpreadsheetDocument sheet) {
        table.appendColumns(10);
        final int valeur_height = 3 + 1; // Test 1, 2, 2, Moyenne
        final int metrique_height = 3 * valeur_height; // Recall, Precision, F1
        final int algo_height = eargs.scorers.length * (metrique_height + 1) - 1; // séparation entre métriques

        table.appendRows((algo_height + 1) * eargs.algos.length); // + titres

        String[] titles = new String[]{"ALGO", "METRIQUE", "Valeur", "NumTest"};
        String[][] traintest = new String[][]{{"ESLO", "OTG"}, {"ESLO", "UBS"}, {"OTG", "ESLO"}, {"OTG", "UBS"}};
        int num_algo = -1;
        int i=0;
        for (String tit : titles)
            table.getCellByPosition(i++, 0).setDisplayText(tit);
        for (String algo : eargs.algos) {
            num_algo++;
            int line = num_algo * (algo_height + 1); // titres
            table.getCellRangeByPosition(0, line + 1,
                    0, line + 1 + algo_height - 1).merge();
            table.getCellByPosition(0, line + 1).setDisplayText(algo.toUpperCase());
            i = 4;
            for (String[] cas : traintest)
                table.getCellByPosition(i++, line).setDisplayText(cas[0] + " → " + cas[1]);

            for (String metrique : eargs.scorers) {
                line++; // 1: ligne suivant les titres, N: marge entre metriques
                table.getCellRangeByPosition(1, line, 1, line + metrique_height - 1)
                        .merge();
                table.getCellByPosition(1, line).setDisplayText(metrique.toUpperCase());
                int num_metrique = -1;
                for (String metrique_valeur : new String[]{"Recall", "Precision", "F1"}) {
                    num_metrique++;
                    table.getCellRangeByPosition(2, line, 2, line + valeur_height - 1)
                            .merge();
                    table.getCellByPosition(2,line).setDisplayText(metrique_valeur);
                    Double[] moyennes = new Double[]{0d,0d,0d,0d};
                    for (int num_test = 0; num_test < 3; num_test++) { // Chaque test
                        table.getCellByPosition(3,line).setDisplayText(String.valueOf(num_test));
                        int numcol = 3;
                        for (String[] tt : traintest) { // pour chaque couple train -> test
                            numcol++;
                            ArrayList<ArrayList<JobScorer>> somejobs = jobs
                                    .get(algo.toUpperCase())
                                    .get(tt[0]) // CTrain
                                    .get(tt[1]) //CTest
                                    .get(num_test);
                            // Moyenne des num_runs test/train
                            double result = 0d;
                            for (int run_train = 0; run_train < eargs.num_learn_run; run_train++)
                                for (int run_test = 0; run_test < eargs.num_test_run; run_test++)
                                    //ALGO / CTrain / CTest / num_test / run_train / run_test
                                    try{
                                        assert  (somejobs != null);
                                        assert (somejobs.get(run_train) != null);
                                        assert (somejobs.get(run_train).get(run_test) != null);
                                        assert (somejobs.get(run_train).get(run_test).scores != null);
                                        assert (somejobs.get(run_train).get(run_test).scores.get(metrique) != null);
                                        assert (somejobs.get(run_train).get(run_test).scores.get(metrique).get(metrique_valeur) != null);

                                        result+= somejobs
                                                .get(run_train)
                                                .get(run_test)
                                                .scores
                                                .get(metrique)
                                                .get(metrique_valeur);
                                    }catch(NullPointerException e){
                                        e.printStackTrace();
                                    }catch (AssertionError e){
                                        logger.error(e.getMessage(),e);
                                    }
                            result /= eargs.num_learn_run * eargs.num_test_run;
                            // valeur traintest par numtest
                            table.getCellByPosition(numcol, line).setDoubleValue(result);
                            moyennes[numcol-4]+=result/3;
                        }
                        line++;
                    }
                    table.getCellByPosition(3,line).setDisplayText("Moyenne");
                    for (int col = 4; col <= 7; col++)
                        table.getCellByPosition(col,line).setDoubleValue(moyennes[col-4]);
                    line++;
                }

            }

        }
    }

    private void ecrireDonneesBrutes(Table table, ArrayList<JobScorer> jobs, OdfSchemaDocument sheet) throws Exception {

        OdfOfficeAutomaticStyles styles = sheet.getContentDom().getOrCreateAutomaticStyles();
        OdfStyle heading = styles.newStyle(OdfStyleFamily.TableCell);
        heading.setProperty(OdfTableCellProperties.BackgroundColor, "#dddddd");

        table.appendColumns(15+3*eargs.scorers.length);
        table.appendRows(2);


        table.getRowByIndex(0).setDefaultCellStyle(heading);
        table.getRowByIndex(1).setDefaultCellStyle(heading);

        table.getCellRangeByPosition(0,0,7,0).merge(); // Fichier
        table.getCellByPosition(0,0).setDisplayText("Identifiant fichier");
        int numcolheader = 0;
        table.getCellByPosition(numcolheader++,1).setDisplayText("Algo");
        table.getCellByPosition(numcolheader++,1).setDisplayText("Corpus Train");
        table.getCellByPosition(numcolheader++,1).setDisplayText("Corpus Test");
        table.getCellByPosition(numcolheader++,1).setDisplayText("num test");
        table.getCellByPosition(numcolheader++,1).setDisplayText("run train");
        table.getCellByPosition(numcolheader++,1).setDisplayText("run test");
        table.getCellByPosition(numcolheader++,1).setDisplayText("train size");
        table.getCellByPosition(numcolheader++,1).setDisplayText("test size");

        String[]  rpf = {"Recall", "Precision", "F1"};
        for(String metrique : eargs.scorers){
            table.getCellRangeByPosition(numcolheader ,0,
                    numcolheader+ 2,0).merge();
            table.getCellByPosition(numcolheader,0)
                    .setDisplayText(metrique.toUpperCase());
            for(int i = 0; i < 3; i++){
                table.getCellByPosition(numcolheader +i,1)
                        .setDisplayText(rpf[i]);
            }

            numcolheader += 3;
        }

        table.getCellRangeByPosition(numcolheader,0,numcolheader,1).merge();
        table.getCellByPosition(numcolheader++,0).setDisplayText("% Singletons GOLD");

        table.getCellRangeByPosition(numcolheader,0,numcolheader,1).merge();
        table.getCellByPosition(numcolheader++,0).setDisplayText("% Singletons SYSTEM");



        //ALGO / train / test / num_test / run_train / run_test
        int numRow = 1;
        int max_num_ant=0;
        for (JobScorer job : jobs) {
            if (job != null) {
                int num_col = 0;
                numRow++;

                String[] ids = job.name.split("_");

                BufferedReader test_arff = new BufferedReader(new FileReader(CHAINS_DIR+"/"+job.name)); // nombre mentions
                double nb_test = test_arff.lines().count();
                BufferedReader train_arff = new BufferedReader(new FileReader(
                        ARFF_DIR+"/corpus_"+ids[1]+"_apprentissage_"+ids[4]+".idff"));
                HashSet<String> mentions = new HashSet<>();
                String line = train_arff.readLine();
                while((line=train_arff.readLine()) != null){
                    String[] mentions_ids = line.split("\t");
                    mentions.add(mentions_ids[1]);
                    mentions.add(mentions_ids[2]);
                }

                double nb_train = mentions.size();

                table.getCellByPosition(num_col++, numRow)
                        .setDisplayText(ids[0]); // ALGO
                table.getCellByPosition(num_col++, numRow)
                        .setDisplayText(ids[1]); // Train_corp
                table.getCellByPosition(num_col++, numRow)
                        .setDisplayText(ids[2]);//Test_corp
                table.getCellByPosition(num_col++, numRow)
                        .setDoubleValue(Double.valueOf(ids[3])); // num_test
                table.getCellByPosition(num_col++, numRow)
                        .setDoubleValue(Double.valueOf(ids[4])); // Run train

                table.getCellByPosition(num_col++, numRow)
                        .setDoubleValue(Double.valueOf(ids[5])); // Run test
                table.getCellByPosition(num_col++, numRow)
                        .setDoubleValue(Double.valueOf(nb_train)); // train size
                table.getCellByPosition(num_col++, numRow)
                        .setDoubleValue(Double.valueOf(nb_test)); // test size

                //Scorers
                for (String scr : eargs.scorers) {
                    HashMap<String, Double> sco = job.scores.get(scr.toLowerCase());
                    table.getCellByPosition(num_col++, numRow).setPercentageValue(
                            sco.get("Recall") / 100);
                    table.getCellByPosition(num_col++, numRow).setPercentageValue(
                            sco.get("Precision") / 100);
                    table.getCellByPosition(num_col++, numRow).setPercentageValue(
                            sco.get("F1") / 100);
                }
                // % singletons
                Double sing = job.singletons.get("GOLD");
                table.getCellByPosition(num_col++, numRow).setPercentageValue(sing / 100);

                sing = job.singletons.get("SYSTEM");
                table.getCellByPosition(num_col++, numRow).setPercentageValue(sing / 100);

                for (Double i : job.num_antecedents)
                    table.getCellByPosition(num_col++, numRow).setDoubleValue(i);
                if (job.num_antecedents.size() > max_num_ant) {
                    max_num_ant = job.num_antecedents.size();
                }
            }
        }

        table.appendColumns(max_num_ant);
        // Mise en place du header num antécédents en fonction du max de num_antecedents
        table.getCellRangeByPosition(numcolheader, 0, numcolheader + max_num_ant, 0).merge();
        table.getCellByPosition(numcolheader, 0)
                .setDisplayText("répartition nombre d'antécédents");

        for (int i = 0; i <= max_num_ant; i++) {
            table.getCellByPosition(numcolheader++, 1)
                    .setDoubleValue(Double.valueOf(i));
        }

        table.getRowByIndex(0).setUseOptimalHeight(true);
        table.getRowByIndex(1).setUseOptimalHeight(true);
        for (Iterator<Column> it = table.getColumnIterator(); it.hasNext(); )
            it.next().setUseOptimalWidth(true);

    }

    private void chains(ExecutorService executor) throws InterruptedException {
        File rep = new File(CLASSIF_DIR);
        Pattern p = Pattern.compile("^([a-zA-Z0-9]+)_([a-zA-Z]+)_([a-zA-Z]+)_([0-9]+)_([0-9]+)_([0-9]+)_SYSTEM\\.arff");
        for(File fichier : rep.listFiles()){
            Matcher m = p.matcher(fichier.getName());
            if(m.matches()){

                String in_system = fichier.getAbsolutePath();
                String out = in_system
                        .replace(CLASSIF_DIR,CHAINS_DIR)
                        .replace("_SYSTEM.arff","");
                String in_gold = in_system.replace("SYSTEM","GOLD");
                executor.execute(new JobChaining(in_gold,in_system,out));
            }
            else if (fichier.getName().contains("_SYSTEM.arff"))
                logger.info("Chains: "+ fichier.getName()+" didnt't match to the pattern");
        }

        executor.shutdown();
        executor.awaitTermination(3600, TimeUnit.SECONDS);
    }

    private void classif(ExecutorService executor) throws InterruptedException, IOException {
        int nbop = 0;

        //ALGO / train / test / num_test / run_train / run_test
        HashMap<String,HashMap<String, HashMap<String, ArrayList<ArrayList<ArrayList<String>>>>>>
                chainesParAlgo = new HashMap<>();

        for(String algo_str : eargs.algos){
            for(String train : corpus_train){
                for(String test : corpus_test){
                    for (Integer num_test = 1; num_test <= 3; num_test ++){
                        for(Integer run_train = 0; run_train < eargs.num_learn_run; run_train++){
                            for(Integer run_test = 0; run_test < eargs.num_test_run; run_test++){

                                nbop++;
                                executor.execute(new JobClassif(algo_str,train,test,
                                        num_test,run_train,run_test));
                            }
                        }
                    }

                }
            }
        }
        executor.shutdown();
        executor.awaitTermination(3600,TimeUnit.SECONDS);
        logger.info(nbop+" fichiers de classification générés");
    }

    private void models(ExecutorService executor) throws InterruptedException {
        HashMap<String, Class<? extends Classifier> > algos = new HashMap<>();

        algos.put("SMO",weka.classifiers.functions.SMO.class);
        algos.put("J48",weka.classifiers.trees.J48.class);

        for(String s : eargs.algos) {
            if (algos.containsKey(s)) {
                logger.info("Génération des models pour "+s);
                Class<? extends Classifier> algo = algos.get(s);
                for (String train : corpus_train)
                    for(int i = 0; i < eargs.num_learn_run; i++) {
                        executor.execute(new JobModelGen(algo,
                                ARFF_DIR + "/" + train + "_"+i+".arff",
                                ARFF_DIR + "/" +
                                        train.replace("apprentissage", "test_1_") +
                                        i+".arff",
                                MODELS_DIR + "/" +
                                        train.replace("corpus_","")
                                            .replace("_apprentissage","")
                                        + "_"+s+"_"+ i +".model"));
                    }

            }
            else
                throw new IllegalArgumentException(
                        s + " n'est pas reconnu par le système (" + algos.toString() + ")");
        }
        executor.shutdown();
        executor.awaitTermination(3600,TimeUnit.SECONDS);
    }

    private void arff(ExecutorService executor) throws InterruptedException {
        for(int n_run = 0; n_run < eargs.num_learn_run; n_run++)
            for(String corp : corpus_train)
                executor.execute(new JobArff(FEATURES_DIR+"/"+corp,
                        ARFF_DIR+"/"+corp+"_"+n_run,
                        eargs.train_pos, eargs.train_neg));

        for(int n_run = 0; n_run < eargs.num_test_run; n_run++)
            for(String corp : corpus_test)
                for(int i = 1; i < 4; i++ ) // 3 tests
                    executor.execute(new JobArff(FEATURES_DIR+"/"+corp+"_"+i,
                            ARFF_DIR+"/"+corp+"_"+i+"_"+n_run,
                            eargs.train_pos, eargs.train_neg));

        executor.shutdown();
        executor.awaitTermination(3600, TimeUnit.SECONDS);
    }

    private void features(ExecutorService executor) throws InterruptedException {
        // Lancement des threads (Tous les runs utilisent les mêmes features)
        for(String corp : corpus_train)
            executor.execute(new JobFeature(CORPUS_DIR+"/"+corp,
                    FEATURES_DIR+"/"+corp));
        for(String corp : corpus_test)
            for(int i = 1; i < 4; i++ ) // 3 tests
                executor.execute(new JobFeature(CORPUS_DIR+"/"+corp+"_"+i,
                    FEATURES_DIR+"/"+corp+"_"+i));

        executor.shutdown();
        executor.awaitTermination(3600, TimeUnit.SECONDS);
    }

    private void prepareRep() throws IOException {
        if(!eargs.skip_models){
            FileUtils.deleteDirectory(new File(MODELS_DIR));
            new File(MODELS_DIR).mkdirs();
        }
        if(!eargs.skip_arff){
            FileUtils.deleteDirectory(new File(ARFF_DIR));
            new File(ARFF_DIR).mkdirs();
        }
        if(!eargs.skip_features){
            FileUtils.deleteDirectory(new File(CORPUS_DIR));
            FileUtils.deleteDirectory(new File(FEATURES_DIR));

            File dst;
            (dst=new File(CORPUS_DIR)).mkdirs();

            new File(FEATURES_DIR).mkdirs();
            FileUtils.copyDirectory(new File(eargs.corpus_in),dst);
        }
        if(!eargs.skip_chains){
            FileUtils.deleteDirectory(new File(CHAINS_DIR));
            new File(CHAINS_DIR).mkdirs();
        }
        if (!eargs.skip_classif) {
            FileUtils.deleteDirectory(new File(CLASSIF_DIR));
            new File(CLASSIF_DIR).mkdirs();
        }
    }


    /**
     * JobFeature est la classe qui calcule tous les features par corpus
     * Elle implémente Runnable de sorte à travailler sur les différents corpus en même temps
     */
    private class JobFeature implements Runnable{

        private final String corpus_dst;
        private final String corpus_src;

        /**
         *
         * @param corpus_src corpus source (répertoire contenant répertoires aa_fichiers et ac_fichiers)
         * @param corpus_dst dossier d'enregistrement du corpus (contiendra les répertoires aa_fichiers et ac_fichiers)
         */
        JobFeature(String corpus_src, String corpus_dst){
            this.corpus_src = corpus_src;
            this.corpus_dst = corpus_dst;
        }

        @Override
        public void run() {
            generateFeature(new Corpus(corpus_src),true, corpus_dst);
        }
    }

    private class JobArff implements Runnable {


        private final String corpus;
        private final String out;
        private int pos;
        private int neg;


        JobArff(String corpus, String out, int pos, int neg){
            this.corpus = corpus;
            this.out = out;
            this.pos = pos;
            this.neg = neg;
            logger.debug(out);
        }

        @Override
        public void run() {
            new ConversionToArff(new Corpus(corpus),
                    pos, neg, ParamToArff.NO_ASSOC,
                    out, 0, ConversionToArff.SUFFIX.NONE)
                    .run();
        }
    }

    private class JobModelGen implements Runnable {

        private final Class<? extends Classifier> classif;
        private final String train;
        private final String test;
        private final String out;

        JobModelGen(Class<? extends Classifier> classif, String train, String test, String out){
            this.classif = classif;
            this.train = train;
            this.test = test;
            this.out = out;
        }
        @Override
        public void run() {
            new ModelGeneration(classif,train,test,out);

        }
    }

    private class JobClassif implements Runnable {

        private final String in_arff;
        private final String out_arff;
        private final String model_src;

        public JobClassif(String algo_str, String train, String test,
                          Integer num_test, Integer run_train, Integer run_test) throws IOException {


            out_arff = String.join("_",
                    CLASSIF_DIR+"/"+algo_str,
                    train
                            .replace("corpus_","")
                            .replace("_apprentissage","")
                    ,test
                            .replace("corpus_","")
                            .replace("_test",""),
                    num_test.toString(),run_train.toString(),
                    run_test.toString(),
                    "SYSTEM.arff");

            in_arff = ARFF_DIR+
                    "/"+test+
                    "_"+num_test+
                    "_"+run_test+
                    ".arff";


            model_src = MODELS_DIR + "/" +
                    train.replace("corpus_","")
                            .replace("_apprentissage","")
                    + "_"+algo_str+"_"+ run_train +".model";

            FileUtils.copyFile(
                    new File(in_arff),
                    new File(out_arff.replace("SYSTEM","GOLD")));
            FileUtils.copyFile(
                    new File(in_arff.replace(".arff",".idff")),
                    new File(out_arff.replace("_SYSTEM.arff",".idff")));
        }

        @Override
        public void run() {
            logger.debug("run: "+out_arff);
            new Classification(in_arff,out_arff,model_src,true);
        }
    }

    private class JobChaining implements Runnable{

        private String output;
        private String in_gold;
        private String in_system;

        JobChaining(String in_gold, String in_system, String output){

            this.output = output;
            this.in_gold = in_gold;
            this.in_system = in_system;
        }

        @Override
        public void run() {
            try {
                new Chaining( Arrays.asList(new String[]{"loe", "lom"}),
                        in_gold, in_system, output, true);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Chaining.InvalidArffAttributes invalidArffAttributes) {
                invalidArffAttributes.printStackTrace();
            }
        }
    }

    private class JobScorer implements Runnable{
        private final String system;
        private final String gold;
        private final String lom_gold;
        private final String lom_system;
        private final String name;
        protected HashMap<String, HashMap<String, Double>> scores;
        protected HashMap<String,Double> singletons;
        protected ArrayList<Double> num_antecedents;

        public JobScorer(String name) {
            this.name=name;
            this.gold = CHAINS_DIR+"/"+name;
            this.system = CHAINS_DIR+"/"+name.replace("GOLD","SYSTEM");

            this.lom_gold = CHAINS_DIR+"/"+name.replace("GOLD.conll","LOM_GOLD.csv");
            this.lom_system = CHAINS_DIR+"/"+name.replace("GOLD.conll","LOM_SYSTEM.csv");
        }

        @Override
        public void run() {
            try {
                String cmd = "perl reference-coreference-scorers/scorer.pl "+
                        String.join("+",eargs.scorers)+" "+gold+" "+system;
                Process p = Runtime.getRuntime().exec(cmd);

                BufferedReader output =  new BufferedReader(new InputStreamReader(p.getInputStream()));


                //String line = null;
                Pattern p_metric = Pattern.compile("^METRIC\\s([a-zA-Z]+):");
                Pattern p_scores = Pattern.compile("^Coreference:\\s" +
                        "Recall:\\s\\([\\d\\.]+\\s\\/\\s[\\d\\.]+\\)\\s([\\d\\.]+)%\\s" +
                        "Precision:\\s\\([\\d\\.]+\\s\\/\\s[\\d\\.]+\\)\\s([\\d\\.]+)%\\s" +
                        "F1:\\s([\\d\\.]+)%");

                String scoreur= null;
                scores = new HashMap();
                for (Iterator<String> it = output.lines().iterator(); it.hasNext(); ) {
                    String line = it.next();
                    Matcher m = p_metric.matcher(line);
                    if(m.matches()) {
                        scoreur = m.group(1);
                        if(!scores.containsKey(scoreur)){
                            scores.put(scoreur,new HashMap<>());
                        }
                    }
                    m = p_scores.matcher(line);
                    if(m.matches()){
                        scores.get(scoreur).put("Recall",Double.parseDouble(m.group(1)));
                        scores.get(scoreur).put("Precision",Double.parseDouble(m.group(2)));
                        scores.get(scoreur).put("F1",Double.parseDouble(m.group(3)));
                    }

                }
                singletons = new HashMap<>();
                singletons.put("GOLD",count_singletons(lom_gold,false));
                num_antecedents = new ArrayList<>();
                singletons.put("SYSTEM",count_singletons(lom_system,true));

                synchronized (logger) {
                    logger.trace(cmd);
                    logger.trace(scores.toString());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private Double count_singletons(String lom_gold, boolean count_num_antecedents) throws IOException {
            BufferedReader lom = new BufferedReader(new FileReader(lom_gold));
            lom.readLine(); // Headers
            String line=null;
            String columns[];
            HashSet<Integer> singletons= new HashSet<>();
            HashSet<Integer> chains = new HashSet<>();
            double num_mentions=0;
            while ((line=lom.readLine()) != null){
                columns = line.split("\t");
                num_mentions += 1d;
                int num_ant = Integer.parseInt(columns[3]);//NUM_ANTECEDENTS_BEFORE_FEST_FIRST
                if(num_ant==0){
                    Integer num_ch = Integer.parseInt(columns[2]);
                    if(singletons.contains(num_ch)){
                        chains.add(num_ch);
                        singletons.remove(num_ch);
                    } else if (!chains.contains(num_ch)){
                        singletons.add(num_ch);
                    }
                }
                if(count_num_antecedents){
                    for(int i = num_antecedents.size(); i <= num_ant; i++)
                        num_antecedents.add(0d);

                    num_antecedents.set(num_ant,
                            num_antecedents.get(num_ant)+1d);
                }
            }

            //for (int i = 0; count_num_antecedents && i < num_antecedents.size(); i++)
            //    num_antecedents.set(i,num_antecedents.get(i) *100d/num_mentions);

            return Double.valueOf(singletons.size()) * 100d / num_mentions;
        }

    }
}
