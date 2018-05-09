package com.democrat.classification;

import org.apache.log4j.Logger;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

public class Classification {
    private static Logger logger = Logger.getLogger(Classification.class);

    /**
     * Appeler depuis la command model <b>classify</b>
     * @param commandArgs
     */
    public Classification(String[] commandArgs) {
        //first arg equals to model
        commandArgs[0] = "";
        try {
            if (commandArgs.length < 3) {
                throw new NoSuchFieldException("ancor2 <model_file> <arff_file>");
            }
            String model_file = commandArgs[1];
            String arff_file = commandArgs[2];
            ArffLoader loader = new ArffLoader();

            loader.setSource(new FileInputStream(new File(arff_file)));
            Instances instances = loader.getDataSet();
            instances.insertAttributeAt(new Attribute("P(CLASS)"),
                    instances.numAttributes()-1);

            logger.info("Loaded "+instances.numInstances()+" instances");

            Model model = Model.loadModel(model_file);

            logger.info("Classification");
            model.classifyInstanceProba(instances);
            String dst = arff_file.replace(".arff","_classified.arff");
            logger.info("Saving results to " + dst);
            ArffSaver saver = new ArffSaver();
            saver.setInstances(instances);
            try {
                saver.setFile(new File(dst));
                saver.writeBatch();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
