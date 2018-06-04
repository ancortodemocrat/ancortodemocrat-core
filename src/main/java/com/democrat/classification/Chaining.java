package com.democrat.classification;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;

public class Chaining {

	private Logger logger = Logger.getLogger(Chaining.class);


	/**
	 * @param args Liste des arguments passé dans la ligne de commande
	 */
	public Chaining(String[] args) throws InvalidArffAttributes, IOException {
		scorerTask(new ScorerArgs(args));
	}

	public Chaining(List<String> aux_output, String in_gold, String in_system, String output,
			boolean force) throws IOException, InvalidArffAttributes {

		scorerTask(new ScorerArgs(aux_output,in_gold,in_system,output,force));
	}

	private void scorerTask(ScorerArgs sargs) throws IOException, InvalidArffAttributes {

		String goldArffName = sargs.in_gold;
		String systemArffName = sargs.in_system;
		String arffIdName = sargs.in_gold
				.replace(".arff", ".idff")
				.replace("_GOLD", "");
		String conllGold = sargs.output + "_GOLD.conll";
		String conllSystem = sargs.output + "_SYSTEM.conll";
		String csvMentions = sargs.output + "_conll_to_ancor.csv";

		String csvListOfEdgesGold = sargs.hasListOfEdges() ? sargs.output + "_LOE_GOLD.csv" : null;
		String csvListOfEdgesSystem = sargs.hasListOfEdges() ? sargs.output + "_LOE_SYSTEM.csv" : null;

		String csvListOfMentionsGold = sargs.hasListOfEdges() ? sargs.output + "_LOM_GOLD.csv" : null;
		String csvListOfMentionsSystem = sargs.hasListOfEdges() ? sargs.output + "_LOM_SYSTEM.csv" : null;

		checkWriteDirs(
				sargs.force, conllGold, conllSystem, csvMentions,
				csvListOfEdgesGold, csvListOfEdgesSystem,
				csvListOfMentionsGold, csvListOfMentionsSystem
		);

		ArrayList<HashMap<String, Integer>> goldChains;
		ArrayList<HashMap<String, Integer>> systemChains;



		goldChains = createSet(goldArffName, arffIdName,TypeChains.GOLD_CHAIN);
		systemChains = createSet(systemArffName, arffIdName,TypeChains.SYSTEM_CHAIN);

		HashMap<String,Integer> mention_str_to_int = new HashMap<>();

		logger.info("Ecriture dans "+ csvMentions);
		PrintWriter conll_to_ancor_id = new PrintWriter(new FileOutputStream(csvMentions));
		conll_to_ancor_id.println("CONLL_ID\tAncor_ID");
		int u = 0;
		for(HashMap<String, Integer> h : goldChains){
			for(String s : h.keySet()){
				conll_to_ancor_id.println(u+"\t"+s);
				mention_str_to_int.put(s,u++);
			}
		}
		conll_to_ancor_id.close();

		writeCoNNL(conllGold, csvListOfEdgesGold, csvListOfMentionsGold,
				"unique_doc",
				mention_str_to_int,
				goldChains);

		writeCoNNL(conllSystem, csvListOfEdgesSystem, csvListOfMentionsSystem,
				"unique_doc", mention_str_to_int, systemChains);
	}

	private void checkWriteDirs(boolean force, String... outs) throws FileAlreadyExistsException {
		String DirErrors;
		StringBuilder FileErrors = new StringBuilder("\n");
		File err;
		StringBuilder DirErrorsBuilder = new StringBuilder("\n");
		for(String out : outs) {
			if (out != null && (err = new File(out)).exists()) {
				if (err.isDirectory()) {
					DirErrorsBuilder.append(out).append(" est un répertoire\n");
				} else if (!force) {
					FileErrors.append(out).append(" existe déjà: utilisez -f ou --force pour l'écraser\n");
				}
			}
		}
		DirErrors = DirErrorsBuilder.toString();
		if(DirErrors.length() > 1)
			throw new FileAlreadyExistsException(DirErrors);
		if(FileErrors.length() > 1)
			throw  new FileAlreadyExistsException(FileErrors.toString());
	}

	/**
	 * Création d'un ensemble de chaines
	 * Chaque chaine est constituée d'un ensemble de mention auxquelles on associe le nombre d'antécédents possibles
	 * avant le traitement best-first
	 *
	 * @param arff Fichier arff à traiter
	 * @param arffId Fichier idff contenant les id des relations et mentions du fichier arff
	 * @param t Type de traitement (S'agit-il d'un GOLD ou SYSTEM?)
	 * @return Liste de clusters associant mentions d'une même chaîne à leur nombre d'antécédents possibles
	 * @throws IOException Exception levée lors de la lecture des fichiers
	 * @throws InvalidArffAttributes Le fichier arff ne correspond pas au type demandé (GOLD/SYSTEM)
	 */
	private ArrayList<HashMap<String, Integer>> createSet
	(String arff, String arffId, TypeChains t)
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

		HashSet<String> mentions = new HashSet<>(); // L'ordre importe peu, garantie mentions uniques
		SortedMap<String, HashMap<String, Double>> corefs = new TreeMap<>();

		// Recensement des mentions
		for(int i = 0; i < instances.numInstances(); i++){
			String[] line_id = idff_reader.readLine().split("\t");
			String antecedent = line_id[1];
			String element = line_id[2];

			mentions.add(antecedent);
			mentions.add(element);
		}

		//Best-First
		idff_reader = new BufferedReader(new FileReader(arffId));
		for(int i = 0; i < instances.numInstances(); i++){
			Instance instance = instances.instance(i);
			if(instance.classValue()==0.d) { // Si COREF
				String[] line_id = idff_reader.readLine().split("\t");
				String antecedent = line_id[1];
				String element = line_id[2];

				// Gold n'a pas le champ P(CLASS)
				// CLASS=COREF, P(CLASS)=P(COREF)
				Double proba = type==TypeChains.GOLD_CHAIN? 2.d : instance.value(instance.numAttributes()-2);


				assert (proba > 0.5d); // P(COREF) > 0.5 si COREF

				if(!corefs.containsKey(element))
					corefs.put(element,new HashMap<>());
				corefs.get(element).put(antecedent,proba);

				// l'antécédent best-first est getAntecedent( corefs.get(element) ),
				// trié sur le score de chaque antécédent possible dans la map (score de classif)

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
	 * @return	Collection de Chaines (une chaîne est un dictionnaire dont les clés sont les mentions de la chaîne,
	 * et les valeurs sont le nombre d'antécédents possibles avant best-first de cette mention)
	 */
	private ArrayList<HashMap<String,Integer>>
	constructChains(
			SortedMap<String, HashMap<String, Double> > paires_coref,
			HashSet<String> mentions,
			TypeChains typeChains
	) {
		logger.info("Construction chaine "+typeChains);
		logger.trace("|M| = "+mentions.size()+" mentions");

		logger.trace("|Co| = "+paires_coref.size()+" corefs");
		// Construction des chaines
		ArrayList<HashMap<String,Integer>> chaines = new ArrayList<>();
		if(logger.isTraceEnabled()){
			Set<String> intersect = new HashSet<>(mentions);
			intersect.retainAll(paires_coref.keySet());
			logger.trace("|M \u2229 Co| = "+ intersect.size());
			if (intersect.size() == paires_coref.size())
				logger.trace("|M \u2229 Co| = |Co| <=> Co \u2286 M");
		}

		logger.trace("Construction des chaines");
		// 1: Chaines
		for (String element: paires_coref.keySet()){
			HashMap<String,Double> map_antecedents = paires_coref.get(element);
			String antecedent = getAntecedent(map_antecedents);
			int nb_antecedents = map_antecedents.size();

			// Tri des singletons: element et antecedent ne sont pas des singletons
			mentions.remove(element);
			mentions.remove(antecedent);
			boolean nouvelle_chaine = true;

			// On va enregistrer où la dernière mention a été enregistrée.
			// Si on place element dans une chaine puis antecedent dans une autre,
			// cette variable nous permettra de rassembler ces deux chaînes.
			HashMap<String,Integer> last_mention_chain = null;
			HashSet<HashMap> remove_chains = new HashSet<>();
			//Parcours des chaines
			for (HashMap<String,Integer> chain : chaines){
				// Si l'antécédent appartient à cette chaîne, on ajoute l'élément coref
				if(chain.containsKey(antecedent)){
					nouvelle_chaine = false;
					if(!chain.containsKey(element) || chain.get(element) == 0){ // ne contient pas element ou element sans antécédents
						chain.put(element,nb_antecedents);
						if(last_mention_chain == null)
							last_mention_chain = chain;
						else {
							// Union des deux ensembles
							chain.putAll(last_mention_chain);
							// On supprimera last_mention_chain plus tard pour éviter ConcurrentException
							remove_chains.add(last_mention_chain);
						}
					}
				}else if(chain.containsKey(element)){
					nouvelle_chaine = false;
					if(!chain.containsKey(antecedent)){
						chain.put(antecedent,0);
						if(chain.get(element) != nb_antecedents){
							chain.put(element, nb_antecedents);
							if(last_mention_chain == null)
								last_mention_chain = chain;
							else{
								// Union des deux ensembles
								chain.putAll(last_mention_chain);
								// On supprimera last_mention_chain plus tard pour éviter ConcurrentException
								remove_chains.add(last_mention_chain);
							}
						}
					}
				}
			}
			// Sinon nouvelle chaîne
			if (nouvelle_chaine){
				HashMap<String,Integer> ch  = new HashMap<>();
				ch.put(element,nb_antecedents);
				ch.put(antecedent,0);
				chaines.add(ch);
			}

			// On supprime les chaines de trop (issues de la fusion de deux chaines)
			chaines.removeAll(remove_chains);
		}

		if(logger.isTraceEnabled()) {
			logger.trace("|Ch| = "+chaines.size()+" chaines");
			logger.trace("|S| = "+mentions.size()+" singletons");
			int nbmention = 0;
			double nb_ant_moy = 0;
			for (HashMap<String,Integer> ch : chaines) {
				nbmention += ch.size();
				for(Integer i : ch.values()) nb_ant_moy += ((double) i) / (ch.size() * chaines.size());
			}
			logger.trace("|Mch| = "+nbmention+" mentions dans chaines");
			logger.trace("avg(nb_antecedents) = "+nb_ant_moy);
			logger.trace("|Mch \u222A S| = "+(nbmention + mentions.size()) +" mentions ");
		}
		// mentions ne contient plus que les singletons
		logger.trace("Ajout des singletons en tant que chaines à 1 elément");
		// 2: Singletons
		for(String singl : mentions){
			HashMap<String,Integer> ch = new HashMap<>();
			ch.put(singl,0);
			chaines.add(ch);
			//if(typeChains == TypeChains.GOLD_CHAIN) singl.setRefGoldChain(ch.getRef());
		}
		if(logger.isTraceEnabled()) {
			logger.trace("|Cl| = "+chaines.size()+" clusters ( Cl = Ch \u222A S )");
			int nbmention = 0;
			double nb_ant_moy = 0;
			for (HashMap<String,Integer> ch : chaines) {
				nbmention += ch.size();
				logger.trace("###########################");
				logger.trace(ch.toString());
				for(Integer i : ch.values()) nb_ant_moy += ((double) i) / (ch.size() * chaines.size());
			}
			logger.trace("|M| = "+nbmention+" mentions");
			logger.trace("avg(nb_antecedents+singl) = "+nb_ant_moy);
		}

		return chaines;
	}

	@SuppressWarnings("ComparatorCombinators")
	private String getAntecedent(HashMap<String, Double> map_antecedents) {
		return Collections.max(
				map_antecedents.entrySet(),
				(t, t1) -> Double.compare(t.getValue(),t1.getValue())
		).getKey();
	}

	private void writeCoNNL(
			String conll, String csvloe, String csvlom, String document_name,
			HashMap<String, Integer> mention_str_to_int,
			ArrayList<HashMap<String, Integer>> chaines){

		logger.info("Ecriture dans "+conll+" , "+csvloe+" , "+csvlom+":");
		PrintWriter conll_writer = null;
		PrintWriter loe_writer = null;
		PrintWriter lom_writer = null;
		try {

			//création des fichiers
			conll_writer = new PrintWriter(conll, "UTF-8");
			if (csvloe != null) {
				loe_writer = new PrintWriter(csvloe, "UTF-8");
				loe_writer.println("Source\tTarget");
			}
			if (csvlom != null) {
				lom_writer = new PrintWriter(csvlom, "UTF-8");
				lom_writer.println("ANCOR_ID\tCONLL_ID\tCHAIN_ID\tNUM_ANTECEDENTS_BEFORE_FEST_FIRST");
			}

			conll_writer.println("#begin document " + document_name);

			int id_chain = 0;
			int ref = -1;
			int nbmention = 0;
			int nbchains= 0;
			for(HashMap<String, Integer> chaine : chaines ){
				//on écrit pour chaque chaine les id des mentions puis l'id de la chaine
				nbchains++;
				for(String mention : chaine.keySet()){
					nbmention++;
					conll_writer.println( mention_str_to_int.get(mention) + "\t" + "(" + id_chain + ")");
					if (loe_writer != null){
						if (ref==-1)
							ref=mention_str_to_int.get(mention);
						else
							loe_writer.println(mention_str_to_int.get(mention)+"\t"+ref);
					}
					if(lom_writer != null){
						lom_writer.println(String.join(
								"\t", mention, // ANCOR_ID
								mention_str_to_int.get(mention).toString() , // CONLL_ID
								Integer.toString(id_chain), // CHAIN_ID
								chaine.get(mention).toString())); // NUM_ANTECEDENTS
					}
				}
				ref = -1;
				id_chain++;
			}
			conll_writer.println("#end document");
			logger.debug(nbmention+" mentions");
			logger.debug(nbchains+" chaines");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			if (conll_writer != null) {
				conll_writer.close();
			}if (loe_writer != null) {
				loe_writer.close();
			}if (lom_writer != null) {
				lom_writer.close();
			}
		}
	}


	private Instances loadInstance(String arffFile){
		BufferedReader reader = null;
		Instances instances = null;
		try {
			reader = new BufferedReader( new FileReader( arffFile ) );
			instances = new Instances( reader );

			instances.setClassIndex( instances.numAttributes() - 1 );
		} catch (Throwable e) {
			e.printStackTrace();
		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return instances;
	}

	public class InvalidArffAttributes extends Exception {
		InvalidArffAttributes(String s) {
			super(s);
		}
	}

	private class ScorerArgs {

		private final String in_gold;
		private final String in_system;
		private final String output;
		private final boolean force;
		private final List<String> aux_output;
		private final Options opt;

		/**
		 * Gestion des paramètres pour la classe Chaining		 *
		 * @param aux_output Formats de sortie auxiliaires
		 * @param in_gold Fichier arff Gold (idff doit être présent à côté)
		 * @param in_system Fichier arff System (idff doit être présent à côté)
		 * @param output Nom générique des fichiers de sortie
		 * @param force Ecraser les fichiers de sortie déjà présents
		 */
		public ScorerArgs(
				List<String> aux_output,
				String in_gold, String in_system, String output,
				boolean force){

			this.in_gold = in_gold;
			this.in_system = in_system;
			this.output = output;
			this.force = force;
			this.aux_output = aux_output;
			opt = null;
		}

		public ScorerArgs(String[] args) {
			opt = new Options();

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
			opt.addOption(
					"f",
					"force",
					false,
					"Force overwrite existing GOLD and SYSTEM chains file"
			);
			opt.addOption(Option.builder()
					.argName("aux-output")
					.longOpt("aux-output")
					.numberOfArgs(2)
					.desc("Auxilliary outputs of gold and system chains. May be one or many of following:\n" +
							"loe: list of edges\n" +
							"lom: list of mentions (Mention num, Cluster num, number of pre-best-first antecedents")
					.build());

			opt.addOption(Option.builder()
					.argName("help").longOpt("help").desc("Print this help").hasArg(false).build());


			CommandLineParser commandline = new DefaultParser();
			CommandLine cmd = null;
			try {
				cmd = commandline.parse(opt, args);
			} catch (ParseException e) {
				documentation();
				System.exit(0);
			}
			in_gold = cmd.getOptionValue("k");
			in_system = cmd.getOptionValue("r");
			output = cmd.getOptionValue("o");
			force = cmd.hasOption("f");
			aux_output = Arrays.asList(cmd.hasOption("aux-output") ? cmd.getOptionValues("aux-output") : new String[]{});
			if(cmd.hasOption("help"))
				documentation();
		}

		boolean hasListOfEdges() {
			return aux_output.contains("loe");
		}

		private void documentation(){
			String header = "Generate chains from input classified instances";
			String footer = "Please submit issues to https://gitlab.com/augustinvoima/ancor2/issues";
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar ancor2.jar", header, opt, footer, true);
			System.exit(1);
		}
	}
}
