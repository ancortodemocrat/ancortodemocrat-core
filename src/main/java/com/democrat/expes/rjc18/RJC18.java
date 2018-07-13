package com.democrat.expes.rjc18;

import com.democrat.expes.Experience;

import java.io.IOException;

/**
 * Parent class for the experiments made for TALN-CORIA RJC 2018
 *
 * @author Augustin Voisin-Marras *
 */
public class RJC18 implements Experience {
    public static final String EXPE_PREFIX = "rjc18";
    private Experience expe;

    public RJC18(String[] args, String expe_name) throws IOException {
        String[] expe_qual = expe_name.split("/");
        if(expe_qual.length < 2)
            runDefault();
        else{
            switch (expe_qual[1].toLowerCase()){
                case T6.EXPE_NAME:
                    expe = new T6(args);
                    break;
            }
        }
    }

    private void runDefault() {
    }

    @Override
    public void run() {
        expe.run();
    }
}
