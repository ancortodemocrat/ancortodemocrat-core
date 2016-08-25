package com.democrat.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

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

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;

public class Toast {

	public static FileManager fileManager;
	private static Logger logger = Logger.getLogger(Toast.class);

	public static void main(String[] args) {


		DOMConfigurator.configure("cfg/log4j-config.xml");


	}

	/**
	 * 
	 * @param corpusList liste des corpus où sont extrait les relations
	 * @param modelPath chemin du fichier model à appliquer pour faire le test
	 * @param positif nombre d'instance positive, sinon 0 et toutes les relations négatives seront prises
	 * @param negatif nombre d'instance négative, sinon 0 et toutes les relations négatives seront prises
	 * @param param Quel type de relation l'on doit prendre
	 * @param outputPath Chemin de sortie des résultats
	 * @param split Si on doit splter le/les corpus indiqué(s), sinon 0 et aucun split est appliqué
	 */
	public static void scorerTask(
			List<Corpus> corpusList,
			String modelPath,
			int positif,
			int negatif,
			ParamToArff param,
			String outputPath,
			int split,
			int nbFolds){

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
		List<Chain>[] listPerSystemFile = new List[ fileArff.size() ];

		//id des mentions qui sont seules, ceci concerne
		//principalement les NOT-COREF
		int lastChainSingleton = 9999;


		logger.info("Création des liste de set..");

		lastChainSingleton = createGoldSet( listPerGoldFile, negativeRelationSelected, negativeRelationSelected, split, fileArff, lastChainSingleton);
		
		// !!								!!
		// !!	 ECRIRE LE FICHIER GOLD 	!!
		// !!								!!
		
		createSystemSet( model, listPerGoldFile, negativeRelationSelected, negativeRelationSelected, split, fileArff, lastChainSingleton);


		//creation des fichiers GOLD et SYST en CONLL
		logger.info("Creation des fichiers CoNLL..");
		int indexUnit = 0;
		//GOLD && SYSTEM
		for(int f = 0; f < fileArff.size(); f++){
			File file = new File( fileArff.get( f ) );
			PrintWriter writer = null;
			PrintWriter writerSystem = null;
			try {

				//création des fichiers
				writer = new PrintWriter(outputPath + file.getName().replace(".arff", "") + "_GOLD.txt", "UTF-8");
				writerSystem = new PrintWriter(outputPath + file.getName().replace(".arff", "") + "_SYSTEM.txt", "UTF8");

				writer.println("#begin document " + file.getName().replace(".arff", "") + ".txt");
				writerSystem.println("#begin document " + file.getName().replace(".arff", "") + ".txt");

				List<Chain> setGoldList = listPerGoldFile[ f ];
				for(Chain chain : setGoldList ){
					//on écrit pour chaque chaine les id des mentions puis l'id de la chaine
					for(int i = 0; i < chain.size(); i++){
						writer.println( chain.getMentionList().get( i ) + "\t" + "(" + chain.getRef() + ")");
					}
				}


				List<Chain> setSystemList = listPerSystemFile[ f ];
				for( Chain chain : setSystemList ){
					//on écrit pour chaque chaine les id des mentions puis l'id de la chaine
					for(int i = 0; i < chain.size(); i++){
						writer.println( chain.getMentionList().get( i ) + "\t" + "(" + chain.getRef() + ")");
					}
				}

/**
				Map<Integer, Integer> check = new HashMap<Integer, Integer>();

				for(int idRef : keySystem){
					for(int i = 0; i < setListSystem.get( idRef ).size(); i++){
						if( ! check.containsKey( setListSystem.get( idRef ).get( i )) ){
							check.put( setListSystem.get( idRef ).get( i ), 1);
						}else{
							check.put(setListSystem.get( idRef ).get( i ), check.get( setListSystem.get( idRef ).get( i ) ) + 1);
						}
					}
				}
				Map<Integer, Integer> checkOther = new HashMap<Integer, Integer>();

				for(int idRef : keyGold){
					for(int i = 0; i < setListGold.get( idRef ).size(); i++){
						if( ! checkOther.containsKey( setListGold.get( idRef ).get( i )) ){
							checkOther.put( setListGold.get( idRef ).get( i ), 1);
						}else{
							checkOther.put( setListGold.get( idRef ).get( i ), checkOther.get( setListGold.get( idRef ).get( i ) ) + 1);
						}
					}
				}

				for(int idMention : check.keySet() ){
					if(check.get( idMention ) != checkOther.get( idMention) ){
						logger.debug( "DIFFFFFF " + idMention );
					}
				}

**/
				writer.println("#end document");
				writerSystem.println("#end document");

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
				if(writerSystem != null){
					writerSystem.close();
				}
			}

		}

		//call scorer
		logger.info("Scorer:");
		for(int f = 0; f < fileArff.size(); f++){
			File file = new File( fileArff.get( f ) );
			PrintWriter writer = null;
			try {
				String results = "";
				results += System.lineSeparator() + "Muc:" + System.lineSeparator();
				results += eval("muc", outputPath + file.getName().replace(".arff", "") + "_GOLD.txt" ,outputPath + file.getName().replace(".arff", "") + "_SYSTEM.txt" );
				results += System.lineSeparator() + "B3:" + System.lineSeparator();
				results +=  eval("bcub", outputPath + file.getName().replace(".arff", "") + "_GOLD.txt" ,outputPath + file.getName().replace(".arff", "") + "_SYSTEM.txt" );
				results += System.lineSeparator() + "ceafe:" + System.lineSeparator();
				results += eval("ceafe", outputPath + file.getName().replace(".arff", "") + "_GOLD.txt" ,outputPath + file.getName().replace(".arff", "") + "_SYSTEM.txt" );
				results += System.lineSeparator() + "blanc:" + System.lineSeparator();
				results += eval("blanc", outputPath + file.getName().replace(".arff", "") + "_GOLD.txt" ,outputPath + file.getName().replace(".arff", "") + "_SYSTEM.txt" );
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
			logger.info( ( f + 1 ) + " / " + fileArff.size( ) );
			int start = f  * positiveRelationSelected.size() / split;
			int end = start + positiveRelationSelected.size() / split;
			Relation[] relationArray = (Relation[]) positiveRelationSelected.keySet().toArray( new Relation[ positiveRelationSelected.size() ] );

			//liste des set à partir des instances choisies pour le fichier arff
			/**
			 * Integer: id de la chaine/set
			 * List<IntegerWithBool>: liste des id des mentions de la chaîne/set, le bool permet
			 * de connaitre si une mention été annotation coréférente à une autre ou non
			 * ce qui permet ensuite de faire un test logique avec les réponses du système
			 * pour reconstruire les chaînes
			 */
			List<Chain> setGoldList = new ArrayList<Chain>();

			//on parcourt les relations en fonction du fichier arff courant
			for(int p = start; p < end; p++){
				Relation relation = relationArray[ p ];
				Annotation annotation = positiveRelationSelected.get( relation );

				int ref;
				if( ! relation.getFeature( "REF" ).equalsIgnoreCase( "null" ) ){
					ref = Integer.valueOf( relation.getFeature( "REF" ) );
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
				currentChain.addMention( new Mention( idMentionElement, false ) );
				//on place l'id pour ensuite pouvoir retrouver cet élément avec les résultats 
				//du system
				relation.getElement( annotation ).setRefGoldChain( ref );
				ref = lastChainSingleton++;
				if( ! containsChain( setGoldList, ref ) ){
					setGoldList.add( new Chain( ref ) );
				}
				currentChain = getChainFromList( setGoldList, ref );
				currentChain.addMention( new Mention( idMentionPreElement, false ) );
				//on place l'id pour ensuite pouvoir retrouver cet élément avec les résultats 
				//du system
				relation.getPreElement( annotation ).setRefGoldChain( ref );
			}
			perFile[ f ] = setGoldList;
		}
		return lastChainSingleton;
	}

	public static int createSystemSet( Model model,
			List<Chain>[] perFile,
			Map<Relation, Annotation> positiveRelationSelected,
			Map<Relation, Annotation> negativeRelationSelected,
			int split,
			List<String> fileArff,
			int lastChainSingleton){




		//test sur le modèle de chaque fichier sortie
		//et on rempli le fichier systems
		for(int f = 0; f < fileArff.size(); f++){
			logger.info("Apprentissage depuis le model sur les instances de " + fileArff.get( f ) );
			Instances instances = loadInstance( fileArff.get( f ) );
			Instances instancesLabeled = model.classifyInstance( instances );
			//même liste mais rempli des nouveaux set calculés par le system
			List<Chain> setListSystem = new ArrayList<Chain>();
			//liste des relations positives déjà traitées
			for(int i = 0; i < instancesLabeled.size(); i++){

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


				List<Chain> system = perFile[ f ];
				if( instances.get( i ).classValue() != instancesLabeled.get( i ).classValue() ){
					Element element = relation.getElement( annotation );
					Element preElement = relation.getPreElement( annotation );
					if( instances.get( i ).classValue() == 0.0D ){
						//cas où c'était COREF et le system dit NOT-COREF
						//on met les deux mentions concerné à FAUX
						for( Chain chain : system ){
							if( chain.containsMention( element.getIdMention() ) &&
									chain.containsMention( preElement.getIdMention() )){
								//on a bien trouvé la chaîne qui conteant les deux mentions
								//dans la liste
								//on met à jour la logique (cf doc

								chain.getMention( element.getIdMention() ).setCoref( false );
								chain.getMention( preElement.getIdMention() ).setCoref( false );

							}
						}
					}else{
						//cas où c'était NOT-COREF et le système dit COREF
						//les deux mentions de la relation sont maintenant
						//contenu danns une seule chaîne
						
						//on supprime les chaînes qui contenaient les deux mentions
						removeChainFromList( system, element.getRefGoldChain() );
						removeChainFromList( system, preElement.getRefGoldChain() );

						
						//et on en crée une qui contient les deux mentions
						Chain currentChain = new Chain( lastChainSingleton++ );
						currentChain.addMention( new Mention( element.getIdMention() ) );
						currentChain.addMention( new Mention( preElement.getIdMention() ) );
						system.add( currentChain );
						
						
					}					
				}else{
					//le système predit la même chose
				}
			}
			float correct = 0.0F;
			float toI = 0.0F;
			float ownToI = 0.0F;
			for(int i = 0; i < instances.size(); i++){
				if(instances.get( i ).classValue() == instancesLabeled.get( i ).classValue()
						/** && instances.get( i ).classValue() == 0.0D **/ ){
					correct++;
				}
				if( instancesLabeled.get( i ).classValue() == 0.0D){
					toI++;
				}
				if( instances.get( i ).classValue() == 0.0D ){
					ownToI++;
				}
			}
			logger.info( "=======================> " + ownToI );
			logger.debug( correct + "/" + instances.size() );
			float rappel = correct / ownToI;
			float precision = correct / toI;
			logger.debug("PRECISION: " + precision);
			logger.debug("RAPPEL: " + rappel );
		}


		return lastChainSingleton;
	}
	
	public static void writeCoNNL( List<Chain>[] chainListPerFile ){
		
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
		try {
			p = Runtime.getRuntime(  ).exec( "scorer.bat " + metric + " " + trueFile + " " + systemFile );
			BufferedReader br = new BufferedReader( new InputStreamReader( p.getInputStream(  ) ) );
			String line = null;
			while ( ( line = br.readLine(  ) ) != null ){
				result += line;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		int indexOfCoreference = result.indexOf( "Coreference:" );
		String output = "";
		try{
			output = result.substring( indexOfCoreference, result.length() ).replace("--", "");
		}catch(StringIndexOutOfBoundsException e ){
			logger.debug(metric + " error: " + result);
		}

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
