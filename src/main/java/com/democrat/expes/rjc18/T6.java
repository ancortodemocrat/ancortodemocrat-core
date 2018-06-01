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
import weka.Run;
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
        eargs = new ExpesArgs(args, RJC18.EXPE_PREFIX+"/"+EXPE_NAME);
        prepareRep();
    }

    @Override
    public void run() {
        try {
            ExecutorService executor;
            HashMap<String, HashMap> class_files = null;
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
                scores(executor);
            }
            // TODO: gestion des résultats
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void scores(ExecutorService executor) throws InterruptedException {
        File rep = new File(CHAINS_DIR);
        Pattern p = Pattern.compile("^([a-zA-Z]+)_([a-zA-Z]+)_([a-zA-Z]+)_([0-9]+)_([0-9]+)_([0-9]+)_GOLD\\.conll");

        //ALGO / train / test / num_test / run_train / run_test
        HashMap<String,HashMap<String, HashMap<String, ArrayList<ArrayList<ArrayList<String>>>>>>
                chainesParAlgo = new HashMap<>();
        for(File fichier : rep.listFiles()){
            Matcher m = p.matcher(fichier.getName());
            if(m.matches()){
                String algo = m.group(1);
                String train = m.group(2);
                String test = m.group(3);
                Integer num_test = Integer.parseInt(m.group(4)) -1;
                Integer run_train = Integer.parseInt(m.group(5));
                Integer run_test = Integer.parseInt(m.group(6));

                if(!chainesParAlgo.containsKey(algo))
                    chainesParAlgo.put(algo,new HashMap<>());

                if(!chainesParAlgo.get(algo).containsKey(train))
                    chainesParAlgo.get(algo).put(train,new HashMap<>());

                if(!chainesParAlgo.get(algo).get(train).containsKey(test)) {
                    ArrayList<ArrayList<ArrayList<String>>> a = new ArrayList<>();
                    for(int i = 0; i < 3; i++) a.add(null);
                    chainesParAlgo.get(algo).get(train).put(test, a);
                }

                if(chainesParAlgo.get(algo).get(train).get(test).get(num_test)==null) {
                    ArrayList<ArrayList<String>> a = new ArrayList<>();
                    for(int i = 0; i < eargs.num_learn_run; i++) a.add(null);
                    chainesParAlgo.get(algo).get(train).get(test).add(num_test,a);
                }

                if(chainesParAlgo.get(algo).get(train).get(test).get(num_test).get(run_train)==null) {
                    ArrayList<String> a = new ArrayList<>();
                    for (int i = 0; i < eargs.num_test_run; i++) a.add(null);
                    chainesParAlgo.get(algo).get(train).get(test).get(num_test).set(run_train, a);
                }

                chainesParAlgo
                        .get(algo)
                        .get(train)
                        .get(test)
                        .get(num_test)
                        .get(run_train)
                        .set(run_test,fichier.getName());

                executor.execute(new JobScorer(fichier.getName()));
            }
            else if (!fichier.getName().contains("_SYSTEM.conll"))
                logger.info("Scores: "+fichier.getName()+" didnt't match to the pattern");

            // TODO: Writing
    }

        executor.shutdown();
        executor.awaitTermination(3600, TimeUnit.SECONDS);
    }

    private void chains(ExecutorService executor) throws InterruptedException {
        File rep = new File(CLASSIF_DIR);
        Pattern p = Pattern.compile("^([a-zA-Z]+)_([a-zA-Z]+)_([a-zA-Z]+)_([0-9]+)_([0-9]+)_([0-9]+)_SYSTEM\\.arff");
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
            else
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

            HashMap<String, HashMap<String, ArrayList< ArrayList<ArrayList<String>>> > > chainesParTrainArff = new HashMap<>();
            chainesParAlgo.put(algo_str,chainesParTrainArff);

            for(String train : corpus_train){
                HashMap<String, ArrayList< ArrayList<ArrayList<String>>> > chainesParTestArff = new HashMap<>();
                chainesParTrainArff.put(train,chainesParTestArff);

                for(String test : corpus_test){
                    ArrayList<ArrayList<ArrayList<String>>> chainesParNumTest = new ArrayList<>();
                    chainesParTestArff.put(test,chainesParNumTest);

                    for (Integer num_test = 1; num_test <= 3; num_test ++){
                        ArrayList<ArrayList<String>> chainesParRunTrain = new ArrayList<>();
                        chainesParNumTest.add(chainesParRunTrain);

                        for(Integer run_train = 0; run_train < eargs.num_learn_run; run_train++){
                            ArrayList<String> chainesParRunTest = new ArrayList<>();
                            chainesParRunTrain.add(chainesParRunTest);

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
                Chaining.scorerTask(in_gold,in_system,output,null,true,true);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Chaining.InvalidArffAttributes invalidArffAttributes) {
                invalidArffAttributes.printStackTrace();
            }
        }
    }

    private class JobScorer implements Runnable {
        private final String system;
        private final String gold;
        private HashMap<String, HashMap<String, Double>> scores;

        public JobScorer(String name) {
            this.gold = CHAINS_DIR+"/"+name;
            this.system = CHAINS_DIR+"/"+name.replace("GOLD","SYSTEM");
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
                        "Recall:\\s\\([\\d.]+\\s\\/\\s[\\d.]+\\)\\s([\\d.]+)%\\s" +
                        "Precision:\\s\\([\\d.]+\\s\\/\\s[\\d.]+\\)\\s([\\d.]+)%\\s" +
                        "F1:\\s([\\d.]+)%");

                String scoreur= null;
                scores = new HashMap();
                    for (Iterator<String> it = output.lines().iterator(); it.hasNext(); ) {
                        String line = it.next();
                        //System.out.println("_____a line is \n"+line+"\n_____");
                        Matcher m = p_metric.matcher(line);
                        //System.out.println("p_metric: "+m.matches());
                        if(m.matches()) {
                            scoreur = m.group(1);
                            if(!scores.containsKey(scoreur)){
                                scores.put(scoreur,new HashMap<>());
                            }
                        }
                        m = p_scores.matcher(line);
                        //System.out.println("p_scores: "+m.matches());
                        if(m.matches()){
                            scores.get(scoreur).put("Recall",Double.parseDouble(m.group(1)));
                            scores.get(scoreur).put("Precision",Double.parseDouble(m.group(2)));
                            scores.get(scoreur).put("F1",Double.parseDouble(m.group(3)));
                        }

                    }
                synchronized (logger) {
                    logger.debug(cmd);
                    logger.debug(scores.toString());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
