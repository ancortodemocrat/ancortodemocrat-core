package com.democrat.classification;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

public class Classification {
    private static Logger logger = Logger.getLogger(Classification.class);

    /**
     * Appeler depuis la command model <b>classify</b>
     * @param args
     */
    public Classification(String[] args) {
        try {
            ClassifArgs cargs = new ClassifArgs(args);
            if (args.length < 3) {
                throw new NoSuchFieldException("ancor2 <model_file> <arff_file>");
            }

            ArffLoader loader = new ArffLoader();

            loader.setSource(new FileInputStream(new File(cargs.in_arff)));
            Instances instances = loader.getDataSet();


            //Remove remove;
            /*if( removeAttribute.size() > 0 ){


                BufferedReader reader = null;
                try {
                    reader = new BufferedReader( new FileReader( arff ) );
                } catch (FileNotFoundException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
                String line = "";

                int[] indices = new int[ 30 - removeAttribute.size() ];
                int i = 0;
                int index = 0;
                // on crée la liste des indices à utiliser en enlevant ceux de l'utilisateur
                try {
                    while( ( line = reader.readLine() ) != null ){
                        if(line.toLowerCase().contains( "@data" ) ){
                            break;
                        }
                        String attributeName = line.split( " " )[ 1 ];
                        if( line.contains( "@ATTRIBUTE" ) && ! line.contains( "class" ) ){
                            if( removeAttribute.contains( attributeName ) ){
                                indices[ index++ ]  = i;
                            }
                            i++;
                        }
                    }
                    System.out.println(indices.toString());
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }finally{
                    if( reader != null ){
                        try {
                            reader.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                remove = new Remove();

                remove.setAttributeIndicesArray( indices );
                remove.setInvertSelection( false );
                try {
                    remove.setInputFormat( instances );
                    instances = Filter.useFilter( instances, remove );
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }*/
            //TODO


            logger.info("Loaded "+instances.numInstances()+" instances");

            Model model = Model.loadModel(cargs.model);

            logger.info("Classification");
            instances = model.classifyInstanceProba(instances);


            // Vérifie que le fichier existe
            if(new File(cargs.out_arff).exists() && ! cargs.force)
                throw new FileAlreadyExistsException(cargs.out_arff + "existe déjà: utiliser -f ou --force pour écraser ce fichier");

            logger.info("Saving results to " + cargs.out_arff);
            ArffSaver saver = new ArffSaver();
            saver.setInstances(instances);
            try {
                saver.setFile(new File(cargs.out_arff));
                saver.writeBatch();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NoSuchFieldException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClassifArgs {
        private final String in_arff;
        private final String out_arff;
        private final String model;
        private final boolean force;

        public ClassifArgs(String[] args) {
            Options opt = new Options();

            Option ina = new Option(
                    "i",
                    "in-arff",
                    true,
                    "Input gold .arff file");
            ina.setRequired(true);
            opt.addOption(ina);
            Option outd= new Option(
                    "o",
                    "out-arff",
                    true,
                    "Output file for the system .arff");
            outd.setRequired(true);
            opt.addOption(outd);
            Option mod = new Option(
                    "m",
                    "model",
                    true,
                    "Model to use to classify");
            mod.setRequired(true);
            opt.addOption(mod);
            opt.addOption(
                    "f",
                    "force",
                    false,
                    "Force overwrite existing system .arff file"
            );

            CommandLineParser commandline = new GnuParser();
            CommandLine cmd = null;
            try {
                cmd = commandline.parse(opt, args);
            } catch (ParseException e) {
                e.printStackTrace();
                System.exit(0);
            }
            in_arff = cmd.getOptionValue("i");
            out_arff = cmd.getOptionValue("o");
            model = cmd.getOptionValue("m");
            force = cmd.hasOption("f");
        }
    }
}
