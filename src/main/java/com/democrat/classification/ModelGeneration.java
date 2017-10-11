package com.democrat.classification;

import org.apache.log4j.Logger;
import weka.classifiers.AbstractClassifier;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;

/**
 * Created by bug on 29/09/17.
 *  Some code here coming from source of Weka
 */
public class ModelGeneration {


    private static Logger logger = Logger.getLogger(ModelGeneration.class);

    /**
     * Appeler depuis la command model <b>classifier</b>
     * @param commandArgs
     */
    public ModelGeneration(String[] commandArgs){
        //first arg equals to model
        commandArgs[0] = "";
        try {
            if (commandArgs.length == 1) {
                throw new Exception("No class name given");
            }
            String className = commandArgs[1];
            commandArgs[1] = "";
            Class<?> theClass = Class.forName(className);

            // some classes expect a fixed order of the args, i.e., they don't
            // use Utils.getOption(...) => create new array without first two
            // empty strings (former "java" and "<classname>")
            Vector<String> argv = new Vector<String>();
            for (int i = 2; i < commandArgs.length; i++) {
                argv.add(commandArgs[i]);
            }

            this.execute(theClass, argv.toArray(new String[argv.size()]));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }


    }

    /**
     * @param theClass
     * @return Retourne la méthode <b>main</b> si elle est bien accessible
     */
    private Method getMainMethod(Class<?> theClass) {
        Class<?>[] argTemplate = { String[].class };
        Method mainMethod = null;
        try {
            mainMethod = theClass.getMethod("main", argTemplate);
            if (((mainMethod.getModifiers() & Modifier.STATIC) == 0)
                    || (mainMethod.getModifiers() & Modifier.PUBLIC) == 0) {
                return null;
            }
        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
            return null;
        }
        return mainMethod;
    }

    private void execute(Class<?> theClass, String[] args){
        Method mainMethod;
        if((mainMethod = getMainMethod(theClass)) == null){
            logger.error("La classe " + theClass.getName() + " n'est pas une classe valide pour l'apprentissage.");
            return;
        }

        PrintStream outOld = null;
        PrintStream outNew = null;
        String outFilename = null;
        // is the output redirected?
        if (args.length > 2) {
            String action = args[args.length - 2];
            if (action.equals(">")) {
                outOld = System.out;
                try {
                    outFilename = args[args.length - 1];
                    // since file may not yet exist, command-line completion doesn't
                    // work, hence replace "~" manually with home directory
                    if (outFilename.startsWith("~")) {
                        outFilename =
                                outFilename.replaceFirst("~", System.getProperty("user.home"));
                    }
                    outNew = new PrintStream(new File(outFilename));
                    System.setOut(outNew);
                    args[args.length - 2] = "";
                    args[args.length - 1] = "";
                    // some main methods check the length of the "args" array
                    // -> removed the two empty elements at the end
                    String[] newArgs = new String[args.length - 2];
                    System.arraycopy(args, 0, newArgs, 0,
                            args.length - 2);
                    args = newArgs;
                } catch (Exception e) {
                    System.setOut(outOld);
                    outOld = null;
                }
            }
        }

        try {
            Object[] argsInvoke = { args };
            mainMethod.invoke(null, argsInvoke);

        } catch (Exception ex) {
            System.err.println("Problem invoking model from Weka: " + ex.getMessage());
        }

        // restore old System.out stream
        if (outOld != null) {
            outNew.flush();
            outNew.close();
            System.setOut(outOld);
            System.out.println("Finished redirecting output to '" + outFilename
                    + "'.");
        }

    }


}
