package com.democrat.classification;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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

		//id des mentions qui sont seules, ceci concerne
		//principalement les NOT-COREF
		int lastChainSingleton = 9999;


		logger.info("Création des listes de chaînes..");

		lastChainSingleton = createGoldSet( listPerGoldFile, positiveRelationSelected, negativeRelationSelected, split, fileArff, lastChainSingleton);

		// DEBUG
		//création liste copie de gold
		/**List<Chain>[] goldFileDEBUG = new List[ listPerGoldFile.length ];
		for( int g = 0; g < listPerGoldFile.length; g++ ){
			List<Chain> currentChainList = listPerGoldFile[ g ];
			List<Chain> copyList = new ArrayList<Chain>();
			for(Chain chain : currentChainList){
				Chain newChain = new Chain( chain.getRef() );
				for( Mention mention : chain.getMentionList() ){
					newChain.addMention( new Mention( mention.getId() ) );
				}
				copyList.add( newChain );
			}
			goldFileDEBUG[ g ] = copyList;

		}
		**/

		//
		// ECRIRE LE FICHIER CoNLL GOLD
		//
		logger.info("Ecriture du fichier CoNNL Gold.");
		writeCoNNL( fileArff, listPerGoldFile, outputPath, "_GOLD.txt" );

		//même liste de chaîne que pour GOLD mais elle sera modifée et contiendra donc les chaînes de sortie
		//du system
		createSystemSet( model, listPerGoldFile, positiveRelationSelected, negativeRelationSelected, split, fileArff, lastChainSingleton, listRemoveAttribute);


		//
		// ECRIRE LE FICHIER CoNLL system
		//
		logger.info("Ecriture du fichier CoNNL System.");
		writeCoNNL( fileArff, listPerGoldFile, outputPath, "_SYSTEM.txt" );

		/**
		 * DEBUG COMPARAISON DES DEUX LISTES
		 */
		/**
		Map<Integer, Integer> countMentionGOLD = new HashMap<Integer, Integer>();
		for( int g = 0; g < goldFileDEBUG.length; g++ ){
			List<Chain> chainSystemList = goldFileDEBUG[ g ];
			for( Chain chain : chainSystemList ){
				for( Mention mention : chain.getMentionList() ){
					if( ! countMentionGOLD.containsKey( mention.getId() ) ){
						countMentionGOLD.put( mention.getId() , 1);
					}else{
						countMentionGOLD.put( mention.getId(), countMentionGOLD.get( mention.getId() ) + 1 );
					}
				}
			}
		}
		Map<Integer, Integer> countMentionSYSTEM = new HashMap<Integer, Integer>();
		for( int g = 0; g < listPerGoldFile.length; g++ ){
			List<Chain> chainSystemList = listPerGoldFile[ g ];
			for( Chain chain : chainSystemList ){
				for( Mention mention : chain.getMentionList() ){
					if( ! countMentionSYSTEM.containsKey( mention.getId() ) ){
						countMentionSYSTEM.put( mention.getId() , 1);
					}else{
						countMentionSYSTEM.put( mention.getId(), countMentionSYSTEM.get( mention.getId() ) + 1 );
					}
				}
			}
		}
		**/
		//DEBUG ON CHECK SI YA UNE DIFF
		/**
		Set<Integer> keyGold = countMentionGOLD.keySet();
		for( int idMention : keyGold ){
			if( countMentionGOLD.get( idMention ) != countMentionSYSTEM.get( idMention ) ){
				//logger.debug("ERROR DIFF: idMENTION " + idMention + " - " + countMentionGOLD.get( idMention ) + " __ " + countMentionSYSTEM.get( idMention ) );
			}
		}
		**/

		//call scorer
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
	 * Rempli pour chaque fichier (perFile)
	 * une liste de chaîne qui sont créees à partir des listes
	 * des relations données
	 * @param perFile Tableau des listes des chaînes qui sont à créer
	 * @param positiveRelationSelected Liste des relations positives pour tous les fichiers
	 * @param negativeRelationSelected Liste des relations négatives pour tous les fichiers
	 * @param split En combien de partie les fichiers ont été découpé
	 * @param fileArff Les des fichiers
	 * @param lastChainSingleton ID des nouvelles chaînes pour les relations négatives
	 * @return
	 */
	public static int createGoldSet( List<Chain>[] perFile,
			Map<Relation, Annotation> positiveRelationSelected,
			Map<Relation, Annotation> negativeRelationSelected,
			int split,
			List<String> fileArff,
			int lastChainSingleton){


		for(int f = 0; f < fileArff.size(); f++){
			int start = f  * positiveRelationSelected.size() / split;
			int end = start + positiveRelationSelected.size() / split;
			Relation[] relationArray = (Relation[]) positiveRelationSelected.keySet().toArray( new Relation[ positiveRelationSelected.size() ] );

			//liste des set à partir des instances choisies pour le fichier arff
			/**
			 * Integer: id de la chaine/set
			 * List<IntegerWithBool>: liste des id des mentions de la chaîne/set, le bool permet
			 * de connaitre si une mention été annotation coréférente à une autre ou non
			 * ce qui permet ensuite de faire un test logique avec les réponses du système (cf. Mention DOC)
			 * pour reconstruire les chaînes
			 */
			List<Chain> setGoldList = new ArrayList<Chain>();

			//on parcourt les relations en fonction du fichier arff courant
			for(int p = start; p < end; p++){
				Relation relation = relationArray[ p ];
				Annotation annotation = positiveRelationSelected.get( relation );

				int ref;
				if( ! relation.getFeature( "ref" ).equalsIgnoreCase( "null" ) ){
					ref = Integer.valueOf( relation.getFeature( "ref" ) );
				}else{
					ref = lastChainSingleton++;
				}
				int idMentionElement = relation.getElement( annotation ).getIdMention();
				int idMentionPreElement = relation.getPreElement( annotation ).getIdMention();
				if( ! containsChain( setGoldList, ref ) ){
					setGoldList.add( new Chain( ref ) );
				}
				Chain currentChain = getChainFromList( setGoldList, ref );
				currentChain.addMention( new Mention( idMentionElement ) );
				currentChain.addMention( new Mention( idMentionPreElement ) );
				//on place l'id pour ensuite pouvoir retrouver cet élément avec les résultats 
				//du system
				relation.getElement( annotation ).setRefGoldChain( ref );
				relation.getPreElement( annotation ).setRefGoldChain( ref );
			}

			//de même pour les instances négatives
			relationArray = (Relation[]) negativeRelationSelected.keySet().toArray( new Relation[ negativeRelationSelected.size() ] );
			start = f * negativeRelationSelected.size() / split;
			end = start + negativeRelationSelected.size() / split;
			for( int l = start; l < end; l++){
				Relation relation = relationArray[ l ];
				Annotation annotation = negativeRelationSelected.get( relation );

				int ref = lastChainSingleton++;

				int idMentionElement = relation.getElement( annotation ).getIdMention();
				int idMentionPreElement = relation.getPreElement( annotation ).getIdMention();
				//on ajoute les mentions à deux chaines differentes
				if( ! containsChain( setGoldList, ref ) ){
					setGoldList.add( new Chain( ref ) );
				}
				Chain currentChain = getChainFromList( setGoldList, ref );
				currentChain.addMention( new Mention( idMentionElement ) );
				//on place l'id pour ensuite pouvoir retrouver cet élément avec les résultats 
				//du system
				relation.getElement( annotation ).setRefGoldChain( ref );

				ref = lastChainSingleton++;
				if( ! containsChain( setGoldList, ref ) ){
					setGoldList.add( new Chain( ref ) );
				}
				currentChain = getChainFromList( setGoldList, ref );
				currentChain.addMention( new Mention( idMentionPreElement ) );
				//on place l'id pour ensuite pouvoir retrouver cet élément avec les résultats 
				//du system
				relation.getPreElement( annotation ).setRefGoldChain( ref );
			}
			perFile[ f ] = setGoldList;
			logger.info( ( f + 1 ) + " / " + fileArff.size( ) );
		}
		return lastChainSingleton;
	}

	
	/**
	 * 
	 * @param perFile Tableau des listes des chaînes qui sont à modifier en fonction des résultats de l'apprentissage avec le model
	 * @param positiveRelationSelected Liste des relations positives pour tous les fichiers
	 * @param negativeRelationSelected Liste des relations négatives pour tous les fichiers
	 * @param split En combien de partie les fichiers ont été découpé
	 * @param fileArff Les des fichiers
	 * @param lastChainSingleton ID des nouvelles chaînes pour les relations négatives
	 * @param model Model qui servira d'apprentissage pour classer les relations
	 * @param removeAttribute Liste des attributes qui ne sont pas à prendre en compte pour l'apprentissage
	 * @return
	 */
	public static int createSystemSet( Model model,
			List<Chain>[] perFile,
			Map<Relation, Annotation> positiveRelationSelected,
			Map<Relation, Annotation> negativeRelationSelected,
			int split,
			List<String> fileArff,
			int lastChainSingleton,
			List<String> removeAttribute){



		Remove remove;
		//test sur le modèle de chaque fichier sortie
		//et on rempli le fichier systems
		for(int f = 0; f < fileArff.size(); f++){
			logger.info("Apprentissage depuis le model sur les instances de " + fileArff.get( f ) );


			Instances instancesGold = loadInstance( fileArff.get( f ) );
			Instances instancesSysteme = loadInstance( fileArff.get( f ) ); //model.classifyInstance( instances );

			//IDentifiants
			BufferedReader id_reader = null;
			try {
				id_reader = new BufferedReader(
						new FileReader(
								fileArff.get(f).replace(".arff",".idff")));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

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

			List<Chain> system = perFile[ f ];
			String relation_xml_id="";

			//BEST FIRST
			for(int i = 0; i < instancesSysteme.size(); i++){
				try {
					relation_xml_id = id_reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
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
				Element preElement = relation.getPreElement( annotation );

				//System.out.println(relation_xml_id);
				//System.out.println("elem: "+element.getId() + " pre: "+preElement.getId());

				// .classValue() 0=COREF, 1=NOT_COREF
				if( instancesGold.get( i ).classValue() != instancesSysteme.get( i ).classValue() ){
					if( instancesGold.get( i ).classValue() == 0.0D ){
						//instancesSystem.get( i ).classValue() == 1.0D
						//cas où c'était COREF et le system dit NOT-COREF
						//on met les deux mentions concerné à FAUX
						//logger.debug( i + "] COREF ==> NOT-COREF !! " + element.getIdMention() + " -- " + preElement.getIdMention() + "] IDR ("  + element.getRefGoldChain() + ")");
						for( Chain chain : system ){
							if( chain.containsMention( element.getIdMention() ) &&
									chain.containsMention( preElement.getIdMention() )){
								//on a bien trouvé la chaîne qui conteant les deux mentions
								//dans la liste
								//on met à jour la logique (cf. Mention DOC)

								chain.getMention( element.getIdMention() ).setCoref( false );
								chain.getMention( preElement.getIdMention() ).setCoref( false );
							}
						}
					}else{
						//cas où c'était NOT-COREF et le système dit COREF
						//les deux mentions de la relation sont maintenant
						//contenu danns une seule chaîne

						//logger.debug( i + "] IDR element (" + element.getRefGoldChain() + ") preElement (" + preElement.getRefGoldChain() + ") NOT-COREF ==> COREF !! " + element.getIdMention() + " -- " + preElement.getIdMention());


						//on supprime les chaînes qui contenaient les deux mentions
						//les chaînes doivent contenir une seule mention
						for( int c = 0; c < system.size(); c++ ){
							if( system.get( c ).containsMention( element.getIdMention() ) &&
									system.get( c ).getMentionList().size() == 1 ){
								//logger.debug( "REMOVED REF " + system.get( c ).getRef() + " metionID " + element.getIdMention() + " REF SETT " + element.getRefGoldChain() );
								system.remove( c );

								break;
							}
						}
						for( int c = 0; c < system.size(); c++ ){
							if( system.get( c ).containsMention( preElement.getIdMention() ) &&
									system.get( c ).getMentionList().size() == 1 ){
								//logger.debug( "REMOVED REF " + system.get( c ).getRef() + " metionID " + preElement.getIdMention() + " REF SETT " + preElement.getRefGoldChain()  );
								system.remove( c );
								break;
							}
						}


						//et on en crée une qui contient les deux mentions
						Chain currentChain = new Chain( lastChainSingleton++ );
						currentChain.addMention( new Mention( element.getIdMention() ) );
						currentChain.addMention( new Mention( preElement.getIdMention() ) );
						system.add( currentChain );

					}					
				}else{
					//le système predit la même chose
					//on met juste à jour la logique de coref
					for( Chain chain : system ){
						if( chain.containsMention( element.getIdMention() ) &&
								chain.containsMention( preElement.getIdMention() )){
							//on a bien trouvé la chaîne qui conteant les deux mentions
							//dans la liste
							//on met à jour la logique (cf. Mention DOC)

							chain.getMention( element.getIdMention() ).setCoref( true );
							chain.getMention( preElement.getIdMention() ).setCoref( true );
							break;
						}
					}
				}
			}
			//on enlève ensuite les mentions des chaînes qui ont
			// été défini NOT-COREF par le system cad isCoref == false pour une mention
			// dans une chaîne
			List<Chain> futurChainList = new ArrayList<Chain>();
			for( Chain chain : system ){
				List<Mention> mentionList = chain.getMentionList();
				for( int m = 0; m < mentionList.size(); m++ ){
					if( ! mentionList.get( m ).isCoref() ){
						//on l'ajoute à une chaîne seule
						Chain newChain = new Chain( lastChainSingleton ++ );
						newChain.addMention( mentionList.get( m ) );
						futurChainList.add( newChain );
						//mention supprimée
						mentionList.remove( m );
						m--;						
					}
				}
			}
			system.addAll( futurChainList ); 
		}


		return lastChainSingleton;
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
