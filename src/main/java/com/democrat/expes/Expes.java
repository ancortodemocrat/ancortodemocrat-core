package com.democrat.expes;

import com.democrat.expes.rjc18.RJC18;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Experience manager
 * java -jar ancor2.jar expes <expe>
 * @author Augustin Voisin-Marras
 */
public class Expes {
    private Experience expe;

    public Expes(String[] args) throws MissingArgumentException, IOException {
        if (args.length < 2)
            throw new MissingArgumentException("Le nom de l'expérience doit être fournit");
        String expe_name = args[1];

        switch (expe_name.split("/")[0].toLowerCase()){
            case RJC18.EXPE_PREFIX:
                expe = (Experience) new RJC18(args, expe_name);
                break;
        }

        Logger logger = Logger.getLogger(Expes.class);
        double start = System.currentTimeMillis();
        expe.run();
        start -= System.currentTimeMillis();
        logger.info("Terminé en "+ -start / 1000 + "secondes");
    }
}
