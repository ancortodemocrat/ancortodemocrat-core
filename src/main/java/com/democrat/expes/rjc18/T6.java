package com.democrat.expes.rjc18;

import com.democrat.ancortodemocrat.AncorToDemocrat;
import com.democrat.ancortodemocrat.ConversionToArff;
import com.democrat.ancortodemocrat.Corpus;
import com.democrat.ancortodemocrat.ParamToArff;
import com.democrat.classification.ModelGeneration;
import com.democrat.expes.Experience;
import com.democrat.expes.Expes;
import com.democrat.expes.ExpesArgs;
import com.democrat.expes.RJC18T6;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.omg.PortableInterceptor.NON_EXISTENT;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;

import java.io.File;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.democrat.ancortodemocrat.AncorToDemocrat.generateFeature;

public class T6 implements Experience{
    private static Logger logger = Logger.getLogger(AncorToDemocrat.class);

    public static final String EXPE_NAME = "t6";
    private static final String WORKING_DIR = "/tmp/"+ RJC18.EXPE_PREFIX+"/"+EXPE_NAME;
    private static final String CORPUS_DIR = WORKING_DIR+"/corpus";
    private static final String FEATURES_DIR = WORKING_DIR+"/features";
    private static final String ARFF_DIR = WORKING_DIR+"/arff";
    private static final String MODELS_DIR = WORKING_DIR+"/models";

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
            // TODO: gestion des résultats
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void models(ExecutorService executor) {
        HashMap<String, Class<? extends Classifier> > algos = new HashMap<>();

        algos.put("SMO",weka.classifiers.functions.SMO.class);
        algos.put("J48",weka.classifiers.trees.J48.class);

        for(String s : eargs.algos) {
            if (algos.containsKey(s)) {
                Class<? extends Classifier> algo = algos.get(s);
                for (String train : corpus_train)
                    for(int i = 0; i < eargs.num_learn_run; i++) {
                        executor.execute(new JobModelGen(algo,
                                ARFF_DIR + "/" + train + "_"+i+".arff",
                                ARFF_DIR + "/" +
                                        train.replace("apprentissage", "test_1_") +
                                        i+".arff",
                                MODELS_DIR + "/" + train + "_"+ i +".model"));
                    }

            }
            else
                throw new IllegalArgumentException(
                        s + " n'est pas reconnu par le système (" + algos.toString() + ")");
        }
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
                executor.execute(new JobFeature(CORPUS_DIR+"/"+corp+i,
                    FEATURES_DIR+"/"+corp+i));

        executor.shutdown();
        executor.awaitTermination(3600, TimeUnit.SECONDS);
    }

    private void prepareRep() throws IOException {
        if(!eargs.skip_models){
            FileUtils.deleteDirectory(new File(MODELS_DIR));
            new File(MODELS_DIR);
        }
        if(!eargs.skip_arff){
            FileUtils.deleteDirectory(new File(ARFF_DIR));
            new File(ARFF_DIR).mkdirs();
        }
        if(!eargs.skip_features){
            File dst;
            (dst=new File(CORPUS_DIR)).mkdirs();

            FileUtils.deleteDirectory(new File(CORPUS_DIR));
            FileUtils.deleteDirectory(new File(FEATURES_DIR));

            new File(FEATURES_DIR).mkdirs();
            FileUtils.copyDirectory(new File(eargs.corpus_in),dst);
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
}
