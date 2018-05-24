package com.democrat.classification;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;
import com.democrat.ancortodemocrat.ConversionInSet;
import com.democrat.ancortodemocrat.ConversionToArff;
import com.democrat.ancortodemocrat.Corpus;
import com.democrat.ancortodemocrat.FileManager;
import com.democrat.ancortodemocrat.ParamToArff;
import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Element;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Schema;
import com.democrat.ancortodemocrat.element.Unit;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class Scorer {

	public static FileManager fileManager;
	private static Logger logger = Logger.getLogger(Scorer.class);


	/**
	 *  @param command String de la commande exéctuée pour cette fonction
	 * @param corpusList liste des corpus où sont extrait les relations
	 * @param modelPath chemin du fichier model à appliquer pour faire le test
	 * @param positif nombre d'instance positive
	 * @param negatif nombre d'instance négative
	 * @param param Quel type de relation l'on doit prendre
	 * @param outputPath Chemin de sortie des résultats
	 * @param split Si on doit splter le/les corpus indiqué(s), sinon 0 et aucun split est appliqué
	 * @param listRemoveAttribute Liste des attributs à ignorer pour l'apprentissage
	 * @param metriques Chaine contenant toutes les métriques à calculer (séparées par des +)
	 */
	public static void scorerTask(
			String command,
			List<Corpus> corpusList,
			String modelPath,
			int positif,
			int negatif,
			ParamToArff param,
			String outputPath,
			int split,
			List<String> listRemoveAttribute,
			String metriques){

		//chargement du modèle pour tester
		//si c'est un bon ou pas
		Model model = Model.loadModel( modelPath );

		//chargement des corpus
		for(Corpus corpus : corpusList ){
			logger.info("Chargement du corpus "+corpus.getName() );

			corpus.loadAnnotation();

			//on ajoute l'id mention pour ensuite créer les fichiers GOLD et SYSTEM
			int count = 0;
			for(Annotation annotation : corpus.getAnnotation() ){
				List<Unit> unitList = annotation.getUnit();
				for(Unit unit : unitList){
					unit.setIdMention( count++ );
				}
				List<Schema> schemaList = annotation.getSchema();
				for(Schema schema : schemaList){
					schema.setIdMention( count++ );
				}
			}

			corpus.loadText();
		}

		//on s'assure d'abord que le/les corpus on le champ REF ajouté
		//sinon on l'ajoute, il serviera pour extraire
		//les mentions des relations, et créer les chaînes
		//pour le scorer
		setRefIfNeed( corpusList );
		//REF OK


		logger.info("Création des fichiers arff..");
		//première étape séléctionner les pos/neg
		ConversionToArff conversion = new ConversionToArff( corpusList,
				positif, negatif, param, outputPath, split );

		//first step: séléction de toutes les relations du/des corpus avec
		//génération des négatives, en triant selon la ParamToArff.
		logger.info("Génération des instances négatives et positives.");
		conversion.sortInstance();


		logger.info("Séléction des instances négatives et positives.");
		//second step: séléction de p positive, et n negative comme voulue
		conversion.selectInstance();

		//on écrit le fichier arff
		logger.info("Ecriture du fichier arff.");
		conversion.writeInstance();

		//on récupère les instances voulues
		//pour plus tard on sait que les instances positives sont
		//écrite en premier temps, dans l'ordre de la liste
		//et ensuite les négatives relations
		Map<Relation, Annotation> positiveRelationSelected = conversion.getPositiveRelationSelected();
		Map<Relation, Annotation> negativeRelationSelected = conversion.getNegativeRelationSelected();
		//liste des fichiers arff générés
		List<String> fileArff = conversion.getFileOuput();

		if( split == 0 ){
			split = 1;
		}

		List<Chain>[] listPerGoldFile = new List[ fileArff.size() ];


		logger.info("Création des listes de chaînes..");

		createGoldSet( listPerGoldFile, positiveRelationSelected, negativeRelationSelected, split, fileArff);

		//
		// ECRIRE LE FICHIER CoNLL GOLD
		//
		logger.info("Ecriture du fichier CoNNL Gold.");
		writeCoNNL( fileArff, listPerGoldFile, outputPath, "_GOLD.txt" );

		//même liste de chaîne que pour GOLD mais elle sera modifée et contiendra donc les chaînes de sortie
		//du system
		createSystemSet( model, listPerGoldFile,
				positiveRelationSelected, negativeRelationSelected,
				split, fileArff, listRemoveAttribute);


		//
		// ECRIRE LE FICHIER CoNLL system
		//
		logger.info("Ecriture du fichier CoNNL System.");
		writeCoNNL( fileArff, listPerGoldFile, outputPath, "_SYSTEM.txt" );

		logger.info("Scorer:");
		for(int f = 0; f < fileArff.size(); f++){
			File file = new File( fileArff.get( f ) );
			PrintWriter writer = null;
			try {
				String results = command + System.lineSeparator();
				results += "POS: " + positif;
				results += System.lineSeparator() + "NEG: " + negatif;
				results += System.lineSeparator() + "MODEL: " + model.getPath();
				results += System.lineSeparator() + "PARAM: " + param;
				results += System.lineSeparator() + "CORPUS:" + System.lineSeparator();
				for( Corpus corpus : corpusList ){
					results += "- " + corpus.getName() + System.lineSeparator();
				}
				results += "ATTRIBUTES REMOVED:" + System.lineSeparator();
				for(String attr : listRemoveAttribute ){
					results += "- " + attr + System.lineSeparator();
				}
				/*
				results += System.lineSeparator() + "Muc:" + System.lineSeparator();
				results += eval("muc", outputPath + file.getName().replace(".arff", "") + "_GOLD.txt" ,outputPath + file.getName().replace(".arff", "") + "_SYSTEM.txt" );
				results += System.lineSeparator() + "B3:" + System.lineSeparator();
				results +=  eval("bcub", outputPath + file.getName().replace(".arff", "") + "_GOLD.txt" ,outputPath + file.getName().replace(".arff", "") + "_SYSTEM.txt" );
				results += System.lineSeparator() + "ceafe:" + System.lineSeparator();
				results += eval("ceafe", outputPath + file.getName().replace(".arff", "") + "_GOLD.txt" ,outputPath + file.getName().replace(".arff", "") + "_SYSTEM.txt" );
				results += System.lineSeparator() + "blanc:" + System.lineSeparator();
				results += eval("blanc", outputPath + file.getName().replace(".arff", "") + "_GOLD.txt" ,outputPath + file.getName().replace(".arff", "") + "_SYSTEM.txt" );
				*/

				results += eval(metriques, outputPath + file.getName().replace(".arff", "") + "_GOLD.txt" ,outputPath + file.getName().replace(".arff", "") + "_SYSTEM.txt" );
				logger.info( results );
				//on écrit les résultats dans un fichier
				writer = new PrintWriter( outputPath + file.getName().replace(".arff", "") + "_RESULTS.txt", "UTF-8" );
				writer.println( results );
				writer.close();
				logger.info( ( f + 1 ) + " / " + fileArff.size() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * Rempli pour chaque fichier (chain_per_fic)
	 * une liste de chaîne qui sont créees à partir des listes
	 * des relations données
	 * @param chain_per_fic Tableau des listes des chaînes qui sont à créer
	 * @param corefRelationSelected Liste des relations positives pour tous les fichiers
	 * @param not_corefRelationSelected Liste des relations négatives pour tous les fichiers
	 * @param split En combien de partie les fichiers ont été découpé
	 * @param arffLines Les des fichiers
	 * @return
	 */
	public static void createGoldSet( List<Chain>[] chain_per_fic,
			Map<Relation, Annotation> corefRelationSelected,
			Map<Relation, Annotation> not_corefRelationSelected,
			int split,
			List<String> arffLines){

		int lastChainId = -1;

		for(int f = 0; f < arffLines.size(); f++){
			int start = f  * corefRelationSelected.size() / split;
			int end = start + corefRelationSelected.size() / split;
			Relation[] relationArray = (Relation[]) corefRelationSelected.keySet().toArray( new Relation[ corefRelationSelected.size() ] );

			//liste des set à partir des instances choisies pour le fichier arff
			/**
			 * Integer: id de la chaine/set
			 * List<IntegerWithBool>: liste des id des mentions de la chaîne/set, le bool permet
			 * de connaitre si une mention été annotation coréférente à une autre ou non
			 * ce qui permet ensuite de faire un test logique avec les réponses du système (cf. Mention DOC)
			 * pour reconstruire les chaînes
			 */

			HashSet<Element> mentions = new HashSet<>();
			HashMap<Element, Map.Entry<Element, Double>> corefs = new HashMap<>();

			//on parcourt les relations en fonction du fichier arff courant
			for(int p = start; p < end; p++){
				Relation relation = relationArray[ p ];

				Annotation annotation = corefRelationSelected.get( relation );

				Element element = relation.getElement( annotation );
				Element antecedent = relation.getPreElement( annotation );

				mentions.add(element);
				mentions.add(antecedent);

				//P(COREF) = 1 car gold. On met 2 pour préciser qu'il s'agit de gold
				corefs.put(element,new AbstractMap.SimpleEntry<Element, Double>(antecedent,2d));
			}

			//de même pour les instances négatives
			relationArray = (Relation[]) not_corefRelationSelected.keySet().toArray( new Relation[ not_corefRelationSelected.size() ] );
			start = f * not_corefRelationSelected.size() / split;
			end = start + not_corefRelationSelected.size() / split;
			for( int l = start; l < end; l++){
				Relation relation = relationArray[ l ];
				Annotation annotation = not_corefRelationSelected.get( relation );

				Element element = relation.getElement( annotation );
				Element antecedent = relation.getPreElement( annotation );

				mentions.add(element);
				mentions.add(antecedent);
			}

			List<Chain> l = constructChains(corefs,mentions,TypeChains.GOLD_CHAIN);

			if(chain_per_fic[f] == null) {
				chain_per_fic[f] = l;
			} else {
				chain_per_fic[f].clear();
				chain_per_fic[f].addAll(l);
			}

			logger.info( ( f + 1 ) + " / " + arffLines.size( ) );
		}
	}

	
	/**
	 * 
	 * @param perFile Tableau des listes des chaînes qui sont à modifier en fonction des résultats de l'apprentissage avec le model
	 * @param positiveRelationSelected Liste des relations positives pour tous les fichiers
	 * @param negativeRelationSelected Liste des relations négatives pour tous les fichiers
	 * @param split En combien de partie les fichiers ont été découpé
	 * @param fileArff Les des fichiers
	 * @param model Model qui servira d'apprentissage pour classer les relations
	 * @param removeAttribute Liste des attributes qui ne sont pas à prendre en compte pour l'apprentissage
	 * @return
	 */
	public static void createSystemSet( Model model,
			List<Chain>[] perFile,
			Map<Relation, Annotation> positiveRelationSelected,
			Map<Relation, Annotation> negativeRelationSelected,
			int split,
			List<String> fileArff,
			List<String> removeAttribute){



		Remove remove;
		//test sur le modèle de chaque fichier sortie
		//et on rempli le fichier systems
		for(int f = 0; f < fileArff.size(); f++){
			logger.info("Scoring en cours sur le fichier " + fileArff.get( f ) );


			Instances instancesGold = loadInstance( fileArff.get( f ) );
			Instances instancesSysteme = loadInstance( fileArff.get( f ) ); //model.classifyInstance( instances );

			if( removeAttribute.size() > 0 ){


				BufferedReader reader = null;
				try {
					reader = new BufferedReader( new FileReader( fileArff.get( f ) ) );
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
					remove.setInputFormat( instancesGold );
					instancesGold = Filter.useFilter( instancesGold, remove );
					

					remove.setInputFormat( instancesSysteme );
					instancesSysteme = Filter.useFilter( instancesSysteme, remove );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			model.classifyInstance( instancesSysteme );
			Instances instancesProba = new Instances(instancesSysteme);
			instancesProba.insertAttributeAt(new Attribute("P(CLASS)"),
					instancesProba.numAttributes()-1);

			model.classifyInstanceProba(instancesProba);

			ArffSaver saver = new ArffSaver();
			saver.setInstances(instancesProba);
			try {
				saver.setFile(new File(fileArff.get(f).replace(".arff","_probas.arff")));
				saver.writeBatch();
			} catch (IOException e) {
				e.printStackTrace();
			}
		//Sélection de l'antécédent

			HashMap<Element,Map.Entry<Element,Double>> pre_possibles = new HashMap<>();
			HashSet<Element> singletons = new HashSet<>();
			for(int i = 0; i < instancesSysteme.size(); i++){

				int start = f * ( positiveRelationSelected.size() + negativeRelationSelected.size() ) / split;
				int relationId = start + i;
				Relation relation;
				Annotation annotation;
				if(relationId >= positiveRelationSelected.size() ){
					Relation[] relationArray = (Relation[]) negativeRelationSelected.keySet().toArray( new Relation[ negativeRelationSelected.size() ] );
					relation = relationArray[ relationId - positiveRelationSelected.size() ];
					annotation = negativeRelationSelected.get( relation );
				}else{
					Relation[] relationArray = (Relation[]) positiveRelationSelected.keySet().toArray( new Relation[ positiveRelationSelected.size() ] );
					relation = relationArray[ relationId ];
					annotation = positiveRelationSelected.get( relation );
				}



				Element element = relation.getElement( annotation );
				Element antecedent = relation.getPreElement( annotation );

				singletons.add(element);
				singletons.add(antecedent);

				Double proba = instancesProba.get(i).value(instancesProba.numAttributes()-2);

				// Conversion proba = P(CLASS) -> proba = P(COREF)
				proba = instancesProba.get(i).classValue()==0d? proba : 1 - proba;
				if(proba > 0d) {
					if (!pre_possibles.containsKey(element)) {
						pre_possibles.put(element,
								new AbstractMap.SimpleEntry<Element, Double>(antecedent,proba));
					}
					// Si proba sup ou égal (si égalité, on prend le plus proche, les instances sont dans l'ordre)
					else if (pre_possibles.get(element).getValue() <= proba ){
						pre_possibles.put(element,
								new AbstractMap.SimpleEntry<Element, Double>(antecedent,proba));
					}

				}

			}

			List l = constructChains(pre_possibles, singletons,TypeChains.SYSTEM_CHAIN);
			// Construction des chaînes
			if(perFile[f] == null) {
				perFile[f] = l;
			} else {
				perFile[f].clear();
				perFile[f].addAll(l);
			}
		}
	}

	private enum TypeChains{ GOLD_CHAIN, SYSTEM_CHAIN }

	/**
	 * Construit une collection de chaînes a partir d'un ensemble de coréférences et de singletons
	 * @param paires_coref Paires coréférentes : < Mention, < Antécédent, ScoreClassif > >
	 * @param mentions Ensemble de toutes les mentions du corpus impliquées dans la classif
	 * @return	Collection de Chaines
	 */
	private static List<Chain> constructChains(
			HashMap<Element, Map.Entry<Element, Double>> paires_coref,
			HashSet<Element> mentions,
			TypeChains typeChains) {

		// Construction des chaines
		ArrayList<Chain> chaines = new ArrayList<>();
		int ref = 0;

		// 1: Chaines
		for (Map.Entry<Element,Map.Entry<Element,Double>> e : paires_coref.entrySet() ){
			Element element = e.getKey();
			Element antecedent = e.getValue().getKey();

			// Tri des singletons: element et antecedent ne sont pas des singletons
			mentions.remove(element);
			mentions.remove(antecedent);

			boolean nouvelle_chaine = true;
			for (Chain chain : chaines){
				// Si l'antécédent appartient à cette chaîne, on ajoute l'élément coref
				if(chain.containsMention(antecedent.getIdMention())
						&& !chain.containsMention(element.getIdMention())){
					chain.addMention(new Mention(element.getIdMention()));
					nouvelle_chaine = false;

					if(typeChains == TypeChains.GOLD_CHAIN) element.setRefGoldChain(chain.getRef());
				}

				// Si l'élément coref appartient à cette chaîne, on ajoute son antécédent
				// (Garantie d'un seul antécédent par mention.)
				// permet d'éviter la création de deux chaines si élément déclarée avant antécédent
				if(chain.containsMention(element.getIdMention())
						&& !chain.containsMention(antecedent.getIdMention())){
					chain.addMention(new Mention(antecedent.getIdMention()));
					nouvelle_chaine = false;

					if(typeChains == TypeChains.GOLD_CHAIN) element.setRefGoldChain(chain.getRef());
				}
			}
			if (nouvelle_chaine){
				Chain ch  = new Chain(ref++);
				ch.addMention(new Mention(element.getIdMention()));
				ch.addMention(new Mention(antecedent.getIdMention()));
				chaines.add(0,ch);
				if(typeChains == TypeChains.GOLD_CHAIN) element.setRefGoldChain(ch.getRef());
				if(typeChains == TypeChains.GOLD_CHAIN) antecedent.setRefGoldChain(ch.getRef());
			}
		}
		// mentions ne contient plus que les singletons
		// 2: Singletons
		for(Element singl : mentions){
			Chain ch = new Chain(ref++);
			ch.addMention(new Mention(singl.getIdMention()));
			chaines.add(ch);
			if(typeChains == TypeChains.GOLD_CHAIN) singl.setRefGoldChain(ch.getRef());
		}
		return chaines;
	}

	public static void writeCoNNL( List<String> fileArff, 
			List<Chain>[] chainListPerFile,
			String outputPath,
			String fileName ){

		for(int f = 0; f < fileArff.size(); f++){
			File file = new File( fileArff.get( f ) );
			PrintWriter writer = null;
			try {

				//création des fichiers
				writer = new PrintWriter(outputPath + file.getName().replace(".arff", "") + fileName, "UTF-8");
				//writerSystem = new PrintWriter(outputPath + file.getName().replace(".arff", "") + "_SYSTEM.txt", "UTF8");

				writer.println("#begin document " + file.getName().replace(".arff", "") + ".txt");

				List<Chain> list = chainListPerFile[ f ];
				for(Chain chain : list ){
					//on écrit pour chaque chaine les id des mentions puis l'id de la chaine
					for(int i = 0; i < chain.size(); i++){
						writer.println( chain.getMentionList().get( i ).getId() + "\t" + "(" + chain.getRef() + ")");
					}
				}

				writer.println("#end document");

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if( writer != null ){
					writer.close();
				}
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

}
