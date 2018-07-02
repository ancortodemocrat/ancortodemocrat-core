package com.democrat.expes;

import com.democrat.ancortodemocrat.AncorToDemocrat;
import org.apache.commons.cli.*;

import java.io.File;
import java.nio.file.NotDirectoryException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpesArgs {

    private static final String file_num_regexpr = "([^\\(\\)]*)\\(([0-9]+)\\)";

    public final String corpus_in;

    public final String[] algos;

    public final int num_learn_run;
    public final int num_test_run;
    public final int num_threads;
    public final int train_pos;
    public final int train_neg;
    public final int test_pos;
    public final int test_neg;

    public final boolean force;
    public final boolean skip_features;
    public final boolean skip_arff;
    public final boolean skip_models;
    public final boolean skip_chains;
    public final boolean skip_classif;
    public final boolean skip_scorers;
    public final String[] scorers;
    public final String out_ods;
    public final boolean skip_output;


    public ExpesArgs(String args[], String expe_name) throws NotDirectoryException {
        String[] algos1;
        Options opt = new Options();

        Option inc = new Option(
                "i",
                "corpus-in",
                true,
                "Corpus d'entrée.Contient les répertoires {aa_fichiers, ac_fichiers}");
        inc.setArgs(1);
        inc.setRequired(true);
        opt.addOption(inc);

        Option outf= new Option(
                "o",
                "out",
                true,
                " Fichier odt de sortie. (default: "+expe_name+".ods)");
        outf.setArgs(1);
        outf.setRequired(false);
        opt.addOption(outf);

        Option lrn = new Option(
                "l",
                "num-learn-run",
                true,
                "Nombre de runs de learn.");
        lrn.setArgs(1);
        lrn.setType(Integer.TYPE);
        lrn.setRequired(false);
        opt.addOption(lrn);

        Option tst = new Option(
                "t",
                "num-test-run",
                true,
                "Nombre de runs de learn."
        );
        tst.setArgs(1);
        tst.setType(Integer.TYPE);
        tst.setRequired(false);
        opt.addOption(tst);

        Option ow = new Option(
                "F",
                "overwrite",
                false,
                "Ecrase le fichier de sortie s'il existe" +
                        "(par défaut, out renommé: out_name_(n+1).csv)"
        );
        ow.setRequired(false);
        opt.addOption(ow);

        Option ll = new Option(
                "P",
                "parallel",
                true,
                "Paraléliser les opérations en n threads (Défaut 3)"
        );
        ll.setArgs(1);
        ll.setType(Integer.TYPE);
        ll.setRequired(false);
        opt.addOption(ll);

        opt.addOption(Option.builder()
                .argName("train-pos")
                .longOpt("train-pos")
                .desc("Nombre d'instances positives pour le train (défaut: 1500 ").build());
        opt.addOption(Option.builder()
                .argName("train-neg")
                .longOpt("train-neg")
                .desc("Nombre d'instances négatives pour le train (défaut: 1075 ").build());

        opt.addOption(Option.builder()
                .argName("test-pos")
                .longOpt("test-pos")
                .desc("Nombre d'instances positives pour le test (défaut: 2757 ").build());
        opt.addOption(Option.builder()
                .argName("test-neg")
                .longOpt("test-neg")
                .desc("Nombre d'instances négatives pour le test (défaut: 3861 ").build());

        opt.addOption(Option.builder()
                .argName("skip-features-gen")
                .longOpt("skip-features-gen")
                .desc("Passer la génération des features (utilisation de ceux déjà présents)").build());

        opt.addOption(Option.builder()
                .argName("skip-arff-gen")
                .longOpt("skip-arff-gen")
                .desc("Passer la génération des arff (utilisation de ceux déjà présents)").build());

        opt.addOption(Option.builder()
                .argName("skip-models-gen")
                .longOpt("skip-models-gen")
                .desc("Passer la génération des models (utilisation de ceux déjà présents)").build());

        opt.addOption(Option.builder()
                .argName("skip-chains-gen")
                .longOpt("skip-chains-gen")
                .desc("Passer la génération des chaines (utilisation de celles déjà présentes)").build());

        opt.addOption(Option.builder()
                .argName("skip-classif")
                .longOpt("skip-classif")
                .desc("Passer la classification").build());

        opt.addOption(Option.builder()
                .argName("skip-scorers")
                .longOpt("skip-scorers")
                .desc("Passer le calcul des scores").build());

        opt.addOption(Option.builder()
                .argName("skip-output")
                .longOpt("skip-output")
                .desc("Passer la génération du fichier de sortie").build());

        opt.addOption(Option.builder()
                .argName("algos")
                .longOpt("algos")
                .desc("Algorithmes à utiliser pour la création des models (par défaut: J48 SMO)")
                .numberOfArgs(5).build());

        opt.addOption(Option.builder()
                .argName("scorers")
                .longOpt("scorers")
                .desc("Scorers à utiliser parmis: all, muc, bcub, ceafm, ceafe, blanc(par défaut: muc, bcub)")
                .numberOfArgs(5).build());

        opt.addOption(Option.builder()
                .argName("help")
                .longOpt("help")
                .desc("Print this help")
                .hasArg(false).build());

        CommandLineParser commandline = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = commandline.parse(opt, args);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(0);
        }
        corpus_in = cmd.getOptionValue("i");
        String out_odstmp = cmd.getOptionValue("o", expe_name + ".ods");

        algos1 = cmd.getOptionValues(",");
        algos = (algos1==null || algos1.length==0) ? new String[]{"J48", "SMO"} : algos1;

        String[] scorers1 = cmd.getOptionValues("scorers");
        scorers = (scorers1==null || algos1.length==0) ? new String[]{"muc","bcub"} : scorers1;

        if(cmd.hasOption("help")){
            AncorToDemocrat.documentation(opt);
        }
        force = cmd.hasOption("F");
        skip_features = cmd.hasOption("skip-features-gen");
        skip_arff = cmd.hasOption("skip-arff-gen");
        skip_models = cmd.hasOption("skip-models-gen");
        skip_chains = cmd.hasOption("skip-chains-gen");
        skip_classif = cmd.hasOption("skip-classif");
        skip_scorers = cmd.hasOption("skip-scorers");
        skip_output = cmd.hasOption("skip-output");



        num_threads = Integer.parseInt(cmd.getOptionValue("P","3"));
        num_learn_run = Integer.parseInt(cmd.getOptionValue("l","4"));
        num_test_run = Integer.parseInt(cmd.getOptionValue("t","4"));
        train_pos = Integer.parseInt(cmd.getOptionValue("train-pos","1500"));
        train_neg = Integer.parseInt(cmd.getOptionValue("train-neg","1075"));
        test_pos = Integer.parseInt(cmd.getOptionValue("test-pos","2757"));
        test_neg = Integer.parseInt(cmd.getOptionValue("test-neg","3861"));

        if(!force && new File(out_odstmp).exists()){
            Matcher m= Pattern.compile(file_num_regexpr).matcher(out_odstmp);
            if(!m.find()) // Première copie
                out_odstmp = out_odstmp.replace(".csv","(1).csv");
            else{// nième copie
                String name = m.group(1);
                int n = Integer.parseInt(m.group(2)) + 1;
                out_odstmp = name+"("+n+").csv";
            }
        }

        // Vérification de l'architecture du corpus d'entrée
        out_ods = out_odstmp;
        File corpfile;
        for (String filename: new String[]{corpus_in})
            if (!(corpfile = new File(corpus_in)).exists() || !corpfile.isDirectory())
                throw new NotDirectoryException(corpus_in + "n'existe pas, n'est pas un répertoire, " +
                        "ou ne correspond pas à l'architecture requise: \n" +
                        inc.getDescription());

    }

}
