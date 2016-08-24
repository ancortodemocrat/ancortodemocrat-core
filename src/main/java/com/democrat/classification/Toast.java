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



		Map<Integer, List<Integer>>[] mapPerFileGold = new Map[ fileArff.size() ];
		Map<Integer, List<Integer>>[] mapPerFileSystem = new Map[ fileArff.size() ];
		int lastChainSingleton = 9999;
		//remplissage de la map
		logger.info("Création des liste de set..");
		for(int f = 0; f < fileArff.size(); f++){
			logger.info( ( f + 1 ) + " / " + fileArff.size( ) );
			int start = f  * positiveRelationSelected.size() / split;
			int end = start + positiveRelationSelected.size() / split;
			Relation[] relationArray = (Relation[]) positiveRelationSelected.keySet().toArray( new Relation[ positiveRelationSelected.size() ] );



			//liste des set à partir des instances choisies pour le fichier arff
			/**
			 * Integer: id de la chaine/set
			 * List<Integer>: liste des id des mentions de la chaîne/set
			 */
			Map<Integer, List<Integer>> setListGold = new HashMap<Integer, List<Integer>>();

			//on parcourt les relations en fonction du fichier dans laquelle 
			//le for est
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
				if( ! setListGold.containsKey( ref ) ){
					setListGold.put( ref, new ArrayList<Integer>() );
				}
				//on s'assure que les id des mentions n'ont pas déjà été ajoutées
				if( ! setListGold.get( ref ).contains( idMentionElement ) ){
					setListGold.get( ref ).add( idMentionElement );
				}
				if( ! setListGold.get( ref ).contains( idMentionPreElement ) ){
					setListGold.get( ref ).add( idMentionPreElement );
				}
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
				if( ! setListGold.containsKey( ref ) ){
					setListGold.put( ref, new ArrayList<Integer>() );
				}
				//on s'assure que les id des mentions n'ont pas déjà été ajoutées
				if( ! setListGold.get( ref ).contains( idMentionElement ) ){
					setListGold.get( ref ).add( idMentionElement );
				}
				lastChainSingleton++;
				if( ! setListGold.get( ref ).contains( idMentionPreElement ) ){
					setListGold.get( ref ).add( idMentionPreElement );
					lastChainSingleton++;
				}
			}
			mapPerFileGold[ f ] = setListGold;
		}



		//lastChainSingleton = 9999;
		//on fait ensuite le test sur le modèle de chaque fichier sortie
		//et on remplie le fichier systems
		for(int f = 0; f < fileArff.size(); f++){
			logger.info("Apprentissage depuis le model sur les instances de " + fileArff.get( f ) );
			Instances instances = loadInstance( fileArff.get( f ) );
			Instances instancesLabeled = model.classifyInstance( instances );
			//même liste mais rempli des nouveaux set calculés par le system
			Map<Integer, List<Integer>> setListSystem = new HashMap<Integer, List<Integer>>();
			//liste des relations positives déjà traitées
			List<Relation> relationSetPositive = new ArrayList<Relation>();
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


				//fileListInstance[ f ][ i ] = instancesLabeled.get( i ).classValue();
				if( instances.get( i ).classValue() != instancesLabeled.get( i ).classValue() ){
					//cas où c'était COREF et le system dit NOT-COREF
					if( instances.get( i ).classValue() == 0.0D ){
						int idMentionElement = relation.getElement( annotation ).getIdMention();
						int idMentionPreElement = relation.getPreElement( annotation ).getIdMention();
						//on vérifie si un des éléments de la relation a déjà été mis ou non
						//si c'est le cas ça veut dire que l'element est présent sur au moins deux relations
						//différentes et que les deux relations faisait partie de la même chaîne
						//mais le système à décider que la relation actuelle n'est pas coréférente et donc
						//n'appartient plus à cette chaîne
						//donc l'autre élément va être mis sur une nouvelle chaîne mais on touche pas à 
						//l'élément en commun
						
						List<Integer> idMentionList = new ArrayList<Integer>();
						idMentionList.add( idMentionElement );
						setListSystem.put( lastChainSingleton++ , idMentionList);

						List<Integer> idPreMentionList = new ArrayList<Integer>();
						idPreMentionList.add( idMentionPreElement );
						setListSystem.put( lastChainSingleton++ , idPreMentionList);
					}else{
						//cas où c'était NOT-COREF et le système dit COREF
						int idMentionElement = relation.getElement( annotation ).getIdMention();
						int idMentionPreElement = relation.getPreElement( annotation ).getIdMention();

						List<Integer> idMentionList = new ArrayList<Integer>();
						idMentionList.add( idMentionElement );
						idMentionList.add( idMentionPreElement );
						setListSystem.put( lastChainSingleton++ , idMentionList);

						relationSetPositive.add( relation );
					}					
				}else{
					//le système predit la même chose
					//on va chercher dans les set GOLD ce qui a été mis
					int idMentionElement = relation.getElement( annotation ).getIdMention();
					int idMentionPreElement = relation.getPreElement( annotation ).getIdMention();

					Map<Integer, List<Integer>> gold = mapPerFileGold[ f ];
					Set<Integer> goldKey = gold.keySet();
					boolean done = false;
					for(int refKey : goldKey){
						if( gold.get( refKey ).contains( idMentionElement ) && gold.get( refKey ).contains( idMentionPreElement ) ){
							//la relation est trouvée avec ses deux elements
							//on la met aussi dans le setListSystem
							if( ! setListSystem.containsKey( refKey ) ){
								setListSystem.put( refKey, new ArrayList<Integer>() );
							}
							if( ! setListSystem.get( refKey ).contains( idMentionElement ) ) {
								setListSystem.get( refKey ).add( idMentionElement );	
							}
							if( ! setListSystem.get( refKey ).contains( idMentionPreElement ) ){
								setListSystem.get( refKey ).add( idMentionPreElement );								
							}

							relationSetPositive.add( relation );
							done = true;
						}
					}
					if( ! done ){
						logger.debug( "ERROR ");
					}

				}
			}
			mapPerFileSystem[ f ] = setListSystem;
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

				Map<Integer, List<Integer>> setListGold = mapPerFileGold[ f ];
				Set<Integer> keyGold = setListGold.keySet();
				for(int idRef : keyGold){
					//on écrit pour chaque chaine les id des mentions puis l'id de la chaine
					for(int i = 0; i < setListGold.get( idRef ).size(); i++){
						writer.println( setListGold.get( idRef ).get( i ) + "\t" + "(" + idRef + ")");
					}
				}


				Map<Integer, List<Integer>> setListSystem = mapPerFileSystem[ f ];
				Set<Integer> keySystem = setListSystem.keySet();
				for(int idRef : keySystem){
					//on écrit pour chaque chaine les id des mentions puis l'id de la chaine
					for(int i = 0; i < setListSystem.get( idRef ).size(); i++){
						writerSystem.println( setListSystem.get( idRef ).get( i ) + "\t" + "(" + idRef + ")");
					}
				}


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
