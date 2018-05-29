package com.democrat.expes;

import org.apache.commons.cli.*;

import java.io.File;
import java.nio.file.NotDirectoryException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpesArgs {

    private static final String file_num_regexpr = "([^\\(\\)]*)\\(([0-9]+)\\)";

    public final String corpus_in;
    public final String out_csv;

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


    public ExpesArgs(String args[], String expe_name) throws NotDirectoryException {
        String[] algos1;
        String out_csvtmp;
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
                " Fichier csv de sortie. (default: "+expe_name+".csv)");
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
                "Paraléliser les opérations en n threads (Défaut 3, non parallélisé)"
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
                .argName("algos")
                .longOpt("algos")
                .desc("Algorithmes à utiliser pour la création des models séparés par des espaces (par défaut: J48 SMO)")
                .numberOfArgs(5).build());

        CommandLineParser commandline = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = commandline.parse(opt, args);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(0);
        }
        corpus_in = cmd.getOptionValue("i");
        out_csvtmp = cmd.getOptionValue("o",expe_name+".csv");

        algos1 = cmd.getOptionValues(",");
        algos = (algos1==null || algos1.length==0) ? new String[]{"J48", "SMO"} : algos1;

        force = cmd.hasOption("F");
        skip_features = cmd.hasOption("skip-features-gen");
        skip_arff = cmd.hasOption("skip-arff-gen");
        skip_models = cmd.hasOption("skip-models-gen");



        num_threads = Integer.parseInt(cmd.getOptionValue("P","3"));
        num_learn_run = Integer.parseInt(cmd.getOptionValue("l","4"));
        num_test_run = Integer.parseInt(cmd.getOptionValue("t","4"));
        train_pos = Integer.parseInt(cmd.getOptionValue("train-pos","1500"));
        train_neg = Integer.parseInt(cmd.getOptionValue("train-neg","1075"));
        test_pos = Integer.parseInt(cmd.getOptionValue("test-pos","2757"));
        test_neg = Integer.parseInt(cmd.getOptionValue("test-neg","3861"));

        if(!force && new File(out_csvtmp).exists()){
            Matcher m= Pattern.compile(file_num_regexpr).matcher(out_csvtmp);
            if(m.find()) // Première copie
                out_csvtmp = out_csvtmp.replace(".csv","(1).csv");
            else{// nième copie
                String name = m.group(1);
                int n = Integer.parseInt(m.group(2)) + 1;
                out_csvtmp = name+"("+n+").csv";
            }
        }

        // Vérification de l'architecture du corpus d'entrée
        out_csv = out_csvtmp;
        File corpfile;
        for (String filename: new String[]{corpus_in})
            if (!(corpfile = new File(corpus_in)).exists() || !corpfile.isDirectory())
                throw new NotDirectoryException(corpus_in + "n'existe pas, n'est pas un répertoire, " +
                        "ou ne correspond pas à l'architecture requise: \n" +
                        inc.getDescription());

    }
}
