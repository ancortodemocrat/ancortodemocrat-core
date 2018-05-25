package com.democrat.classification;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import com.democrat.ancortodemocrat.ConversionInSet;
import com.democrat.ancortodemocrat.Corpus;
import com.democrat.ancortodemocrat.FileManager;
import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Unit;

import weka.core.Instance;
import weka.core.Instances;

public class Scorer {

	public static FileManager fileManager;
	private static Logger logger = Logger.getLogger(Scorer.class);


	/**
	 *  @param args Liste des arguments passé dans la ligne de commande
	 */
	public static void scorerTask(
			String[] args
			) throws InvalidArffAttributes, IOException {

		ScorerArgs sargs = new ScorerArgs(args);


		String goldArffName = sargs.in_gold;
		String systemArffName = sargs.in_system;
		String arffIdName = sargs.in_gold.replace(".arff",".idff");
		String conllGold = sargs.output + "_GOLD.conll";
		String conllSystem = sargs.output + "_SYSTEM.conll";
		String csvMentions = sargs.output + "_conll_to_ancor.csv"; // Contient lien id conll / id mention
		String csvGold = sargs.csv? sargs.output + "_GOLD.csv" : null;
		String csvSystem = sargs.csv? sargs.output + "_SYSTEM.csv" : null;

		if(sargs.csv)
			checkWriteDirs(sargs.force, conllGold, conllSystem, csvMentions, csvGold, csvSystem);
		else
			checkWriteDirs(sargs.force, conllGold, conllSystem, csvMentions);

		ArrayList<ArrayList<String>> goldChains;
		ArrayList<ArrayList<String>> systemChains;



		goldChains = createSet( goldArffName, arffIdName,TypeChains.GOLD_CHAIN);
		systemChains = createSet( systemArffName, arffIdName,TypeChains.SYSTEM_CHAIN);

		HashMap<String,Integer> mention_str_to_int = new HashMap<>();

		logger.info("Ecriture dans "+csvMentions);
		PrintWriter conll_to_ancor_id = new PrintWriter(new FileOutputStream(csvMentions));
		conll_to_ancor_id.println("CONLL_ID\tAncor_ID");
		int u = 0;
		for(ArrayList<String> h : goldChains){
			for(String s : h){
				conll_to_ancor_id.println(u+"\t"+s);
				mention_str_to_int.put(s,u++);
			}
		}
		conll_to_ancor_id.close();

		writeCoNNL(conllGold, csvGold, goldChains,
				mention_str_to_int, TypeChains.GOLD_CHAIN,
				"unique_doc");
		writeCoNNL(conllSystem, csvSystem, systemChains,
				mention_str_to_int, TypeChains.SYSTEM_CHAIN,
				"unique_doc");
	}

	private static void checkWriteDirs(boolean force, String... outs) throws FileAlreadyExistsException {
		String DirErrors = "\n";
		String FileErrors = "\n";
		File err;
		for(String out : outs) {
			if (out != null && (err = new File(out)).exists()) {
				if (err.isDirectory()) {
					DirErrors += out + " est un répertoire\n";
				} else if (!force) {
					FileErrors += out + " existe déjà: utilisez -f ou --force pour l'écraser\n";
				}
			}
		}
		if(DirErrors.length() > 1)
			throw new FileAlreadyExistsException(DirErrors);
		if(FileErrors.length() > 1)
			throw  new FileAlreadyExistsException(FileErrors);
	}

	/**
	 *
	 * @return
	 */
	public static ArrayList<ArrayList<String>> createSet
			(String arff,String arffId, TypeChains t)
			throws IOException, InvalidArffAttributes {

		Instances instances = loadInstance(arff);

		TypeChains type;
		if (instances.attribute(instances.numAttributes()-2).name().equals("P(CLASS)"))
			type = TypeChains.SYSTEM_CHAIN;
		else
			type = TypeChains.GOLD_CHAIN;

		if (!type.equals(t))
			throw new InvalidArffAttributes(
					arff+" is "+type.toString()+ " while " + t.toString() + "excepted");

		BufferedReader idff_reader = new BufferedReader(new FileReader(arffId));

		HashSet<String> mentions = new HashSet<>();
		SortedMap<String, Map.Entry<String, Double>> corefs = new TreeMap<>();

		// Recensement des mentions
		for(int i = 0; i < instances.numInstances(); i++){
			Instance instance = instances.instance(i);
			String[] line_id = idff_reader.readLine().split("\t");
			String rel_id = line_id[0];
			String antecedent = line_id[1];
			String element = line_id[2];

			mentions.add(element);
			mentions.add(antecedent);
		}

		//Best-First
		idff_reader = new BufferedReader(new FileReader(arffId));
		for(int i = 0; i < instances.numInstances(); i++){
			Instance instance = instances.instance(i);
			if(instance.classValue()==0.d) { // Si COREF
				String[] line_id = idff_reader.readLine().split("\t");
				String antecedent = line_id[1];
				String element = line_id[2];
				if(type==TypeChains.GOLD_CHAIN) { // Cas Gold
					corefs.put(element, new AbstractMap.SimpleEntry<String, Double>(antecedent, 1d));
				}else { // Cas System
					Double proba = instance.value(instance.numAttributes()-2);

					assert (proba > 0.5d); // P(COREF) > 0.5 si COREF

					// Si proba sup ou égal (si égalité, on prend le plus proche, les instances sont dans l'ordre)
					if (!corefs.containsKey(element)
						|| corefs.get(element).getValue() <= proba ){

						corefs.put(element,
								new AbstractMap.SimpleEntry<String, Double>(antecedent,proba));
					}
				}
			}
		}

		// Construction des chaines
		return constructChains(corefs,mentions,type);


	}


	private enum TypeChains{ GOLD_CHAIN, SYSTEM_CHAIN }

	/**
	 * Construit une collection de chaînes a partir d'un ensemble de coréférences et de singletons
	 * @param paires_coref Paires coréférentes : < Mention, < Antécédent, ScoreClassif > >
	 * @param mentions Ensemble de toutes les mentions du corpus impliquées dans la classif
	 * @return	Collection de Chaines
	 */
	private static ArrayList<ArrayList<String>> constructChains(
			SortedMap<String, Map.Entry<String, Double>> paires_coref,
			HashSet<String> mentions,
			TypeChains typeChains) {
		logger.info("Construction chaine "+typeChains);
		logger.trace("|M| = "+mentions.size()+" mentions");

		logger.trace("|Co| = "+paires_coref.size()+" corefs");
		// Construction des chaines
		ArrayList<ArrayList<String>> chaines = new ArrayList<>();
		int ref = 0;
		if(logger.isTraceEnabled()){
			Set<String> intersect = new HashSet(mentions);
			intersect.retainAll(paires_coref.keySet());
			logger.trace("|M \u2229 Co| = "+ intersect.size());
			if (intersect.size() == paires_coref.size())
				logger.trace("|M \u2229 Co| = |Co| <=> Co \u2286 M");
		}

		boolean antecedent_perdu = false;
		boolean mention_perdue = false;
		logger.trace("Construction des chaines");
		// 1: Chaines
		for (Map.Entry<String,Map.Entry<String,Double>> e : paires_coref.entrySet() ){
			String element = e.getKey();
			String antecedent = e.getValue().getKey();

			// Tri des singletons: element et antecedent ne sont pas des singletons
			mention_perdue = mentions.remove(element);

			antecedent_perdu = mentions.remove(antecedent);

			boolean nouvelle_chaine = true;
			for (ArrayList chain : chaines){
				// Si l'antécédent appartient à cette chaîne, on ajoute l'élément coref
				if(chain.contains(antecedent)){
					mention_perdue=false;
					antecedent_perdu=false;
					if(!chain.contains(element)){
						chain.add(element);
						nouvelle_chaine = false;
					}
				}else if(chain.contains(element)){
					mention_perdue=false;
					antecedent_perdu=false;
					if(!chain.contains(antecedent)){
						chain.add(antecedent);
						nouvelle_chaine = false;
					}
				}
			}
			if (nouvelle_chaine){
				ArrayList<String> ch  = new ArrayList<>();
				ch.add(element);
				ch.add(antecedent);
				chaines.add(ch);
				mention_perdue=false;
				antecedent_perdu=false;
			}
			assert(mention_perdue==true && antecedent_perdu==true);
		}

		if(logger.isTraceEnabled()) {
			logger.trace("|Ch| = "+chaines.size()+" chaines");
			logger.trace("|S| = "+mentions.size()+" singletons");
			int nbmention = 0;
			for (List<String> ch : chaines) {
				nbmention += ch.size();
			}
			logger.trace("|Mch| = "+nbmention+" mentions dans chaines");
			logger.trace("|Mch \u222A S| = "+(nbmention + mentions.size()) +" mentions ");
		}
		// mentions ne contient plus que les singletons
		logger.trace("Ajout des singletons en tant que chaines à 1 elément");
		// 2: Singletons
		for(String singl : mentions){
			ArrayList<String> ch = new ArrayList<>();
			ch.add(singl);
			chaines.add(ch);
			//if(typeChains == TypeChains.GOLD_CHAIN) singl.setRefGoldChain(ch.getRef());
		}
		if(logger.isDebugEnabled()) {
			logger.trace("|Cl| = "+chaines.size()+" clusters ( Cl = Ch \u222A S )");
			int nbmention = 0;
			for (List<String> ch : chaines) {
				nbmention += ch.size();
			}
			logger.trace("|M| = "+nbmention+" mentions");
		}

		return chaines;
	}

	public static void writeCoNNL(String conll, String csv,
								  ArrayList<ArrayList<String>> chaines,
								  HashMap<String, Integer> mention_str_to_int,
								  TypeChains type, String document_name){
		logger.info("Ecriture dans "+conll+" et "+csv+":");
		PrintWriter conll_writer = null;
		PrintWriter csv_writer = null;
		try {

			//création des fichiers
			conll_writer = new PrintWriter(conll, "UTF-8");
			if (csv != null) {
				csv_writer = new PrintWriter(csv, "UTF-8");
				csv_writer.println("Source\tTarget");
			}


			conll_writer.println("#begin document " + document_name);

			int id_chain = 0;
			int ref = -1;
			int nbmention = 0;
			int nbchains= 0;
			for(ArrayList<String> chaine : chaines ){
				//on écrit pour chaque chaine les id des mentions puis l'id de la chaine
				nbchains++;
				for(String mention : chaine){
					nbmention++;
					conll_writer.println( mention_str_to_int.get(mention) + "\t" + "(" + id_chain + ")");
					if (csv_writer != null){
						if (ref==-1)
							ref=mention_str_to_int.get(mention);
						else
							csv_writer.println(mention_str_to_int.get(mention)+"\t"+ref);
					}
				}
				ref = -1;
				id_chain++;
			}
			conll_writer.println("#end document");
			logger.debug(nbmention+" mentions");
			logger.debug(nbchains+" chaines");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (conll_writer != null) {
				conll_writer.close();
			}if (csv_writer != null) {
				csv_writer.close();
			}
		}
	}

	public static void removeChainFromList( List<Chain> chainList, int ref ){
		for( int c = 0; c < chainList.size(); c++ ){
			if( chainList.get( c ).getRef() == ref ){
				chainList.remove( c );
				break;
			}
		}
	}

	public static boolean containsChain( List<Chain> chainList, int ref ){
		for( Chain chain : chainList ){
			if( chain.getRef() == ref ){
				return true;
			}
		}	
		return false;
	}

	public static Chain getChainFromList( List<Chain> chainList, int ref ){
		for( Chain chain : chainList ){
			if( chain.getRef() == ref ){
				return chain;
			}
		}	
		return null;
	}


	public static void setRefIfNeed( List<Corpus> corpusList ){
		//on parcours chaque unit de chaque corpus pour voir si un unit contient ref ou non,
		//si un unit ne contient pas de ref, ref à ajouter.
		for(int c = 0; c < corpusList.size(); c++){
			List<Annotation> annotationList = corpusList.get( c ).getAnnotation();
			boolean containsREF = false;
			for(int a = 0; a < annotationList.size(); a++){
				List<Unit> unitList = annotationList.get( a ).getUnit();
				annotationList.get( a ).removeTxtImporter();
				for(int u = 0; u < unitList.size(); u++){
					if( ! unitList.get( u ).getFeature("REF").equalsIgnoreCase("NULL") ){
						//REF présent
						containsREF = true;
						break;
					}
				}
			}
			if( ! containsREF ){
				//on demande à l'utilisateur de quel type est le corpus
				//si il est en première mention ou en chaîne
				boolean answered = false;
				String paramUser = "";
				while( ! answered ){
					Scanner sc = new Scanner( System.in );
					logger.info("Le corpus " + corpusList.get( c ).getName() + " a été trouvé sans champ REF.");
					logger.info("Ce champ est nécessaire,");
					logger.info("le corpus est en première mention (p) ?");
					logger.info("Ou il est en chaîne (c) ?");

					String line = sc.nextLine();
					if(line.toLowerCase().contains("c") || line.toLowerCase().contains("p") ){
						answered = true;
						logger.info("Ajout du champ REF sur " + corpusList.get( c ).getName() + ".");
						paramUser = line.toLowerCase();
					}
				}
				if( paramUser.contains( "c" ) ){
					for(int a = 0; a < annotationList.size(); a++){
						logger.info("Ajout du champ REF.");
						ConversionInSet.toSetFromChain( annotationList.get( a ) );
					}
				}else{
					for(int a = 0; a < annotationList.size(); a++){
						logger.info("Ajout du champ REF.");
						ConversionInSet.toSetFromFirstMention( annotationList.get( a ) );
					}					
				}
			}
		}
	}


	public static String eval( String metric, String trueFile, String systemFile ){
		Process p;
		String result = "";
		String command = "perl scorer.pl " + metric + " " + trueFile + " " + systemFile;
		System.out.println(command);
		try {
			p = Runtime.getRuntime(  ).exec(command);
			BufferedReader br = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
			//p.waitFor();
			String line = null;
			while ( ( line = br.readLine(  ) ) != null ){ // attente d'écritures
				result += line+"<newline>";
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		int indexOfCoreference = result.indexOf( "Coreference:" );
		String output = "";
		while( -1 < (indexOfCoreference=result.indexOf( "Coreference:" ))) {
			try {
				output += result.substring(indexOfCoreference, result.length()).replace("--", "");
			} catch (StringIndexOutOfBoundsException e) {
				logger.debug(metric + " error: " + result);
			}
		}
		output.replaceAll("<newline>","\n");
		return output;
	}


	public static Instances loadInstance(String arffFile){
		BufferedReader reader = null;
		Instances instances = null;
		try {
			reader = new BufferedReader( new FileReader( arffFile ) );
			instances = new Instances( reader );

			instances.setClassIndex( instances.numAttributes() - 1 );
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return instances;
	}

	public static class InvalidArffAttributes extends Exception {
		public InvalidArffAttributes(String s) {
			super(s);
		}
	}

	private static class ScorerArgs {
		private final String in_gold, in_system;
		private final String output;
		private final String[] scorers;
		private final boolean force;
		private final boolean csv;

		public ScorerArgs(String[] args) {
			Options opt = new Options();

			Option gold = new Option(
					"k",
					"gold-arff",
					true,
					"Input gold arff filename");
			gold.setRequired(true);
			opt.addOption(gold);

			Option system= new Option(
					"r",
					"system-arff",
					true,
					"Input system arff filename");
			system.setRequired(true);
			opt.addOption(system);

			Option out = new Option(
					"o",
					"output-chains",
					true,
					"Represents chains output : -o /path/to/output/couple_name \n" +
							"Will write gold to /path/to/output/couple_name_GOLD.txt \n" +
							"System to /path/to/output/couple_name_SYSTEM.txt\n" +
							"Conll mention ids to Ancor id to /path/to/output/couple_name_conll_to_andor.csv\n" +
							"couple_name may be gold arff name");
			out.setRequired(true);
			opt.addOption(out);

			Option sco = new Option(
					"s",
					"scorers",
					true,
					"Scorers to use (separated with spaces): muc, bcub, ceafe, ceafm, blanc ");
			sco.setArgs(5);
			sco.setRequired(false);
			opt.addOption(sco);

			opt.addOption(
					"f",
					"force",
					false,
					"Force overwrite existing GOLD and SYSTEM chains file"
			);
			opt.addOption(
					"c",
					"csv",
					false,
					"Additionnal csv output of gold and system chains\n" +
							"path/to/output/couple_name_GOLD.csv\n" +
							"path/to/output/couple_name_SYSTEM.csv\n"
			);
			CommandLineParser commandline = new GnuParser();
			CommandLine cmd = null;
			try {
				cmd = commandline.parse(opt, args);
			} catch (ParseException e) {
				e.printStackTrace();
				System.exit(0);
			}
			in_gold = cmd.getOptionValue("k");
			in_system = cmd.getOptionValue("r");
			output = cmd.getOptionValue("o");
			scorers = cmd.getOptionValues("s");
			force = cmd.hasOption("f");
			csv = cmd.hasOption("c");

		}
	}
}
