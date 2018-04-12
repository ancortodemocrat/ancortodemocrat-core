package com.democrat.classification;

import org.apache.log4j.Logger;

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
                throw new Exception("ancor2 <model_file> <arff_file>");
            }
            String model = commandArgs[1];
            commandArgs[1] = "";

            // some classes expect a fixed order of the args, i.e., they don't
            // use Utils.getOption(...) => create new array without first two
            // empty strings (former "java" and "<classname>")
            Vector<String> argv = new Vector<String>();
            for (int i = 2; i < commandArgs.length; i++) {
                argv.add(commandArgs[i]);
            }


        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
}
