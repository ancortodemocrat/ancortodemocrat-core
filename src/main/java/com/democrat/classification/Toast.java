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
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.democrat.ancortodemocrat.ConversionInSet;
import com.democrat.ancortodemocrat.ConversionToArff;
import com.democrat.ancortodemocrat.Corpus;
import com.democrat.ancortodemocrat.FileManager;
import com.democrat.ancortodemocrat.ParamToArff;
import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Relation;
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
		conversion.sortInstance();

		//second step: séléction de p positive, et n negative comme voulue
		conversion.selectInstance();

		//on écrit le fichier arff
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
		//Liste pour chaque fichier arff généré
		//des instances pour le fichier GOLD au format CoNLL
		Double[][] fileListInstance = new Double[ fileArff.size() ][ ( positiveRelationSelected.size() / split ) + ( negativeRelationSelected.size() / split )];


		//on fait ensuite le test sur le modèle de chaque fichier sortie
		for(int f = 0; f < fileArff.size(); f++){
			logger.info("Apprentissage depuis le model sur les instances de " + fileArff.get( f ) );
			Instances instances = loadInstance( fileArff.get( f ) );
			Instances instancesLabeled = model.classifyInstance( instances );
			for(int i = 0; i < instancesLabeled.size(); i++){
				fileListInstance[ f ][ i ] = instancesLabeled.get( i ).classValue();	
				//logger.debug( instances.get( i ).classValue() + " class " + instancesLabeled.get( i ).classValue() + "  " + instancesLabeled.classAttribute().value( (int) instancesLabeled.get( i ).classValue()  ));
			}
		}

		//creation des fichiers GOLD et SYST en CONLL
		logger.info("Creation des fichiers CoNLL..");
		int indexUnit = 0;
		int lastChainSingleton = 0;
		//GOLD && SYSTEM
		for(int f = 0; f < fileArff.size(); f++){
			File file = new File( fileArff.get( f ) );
			PrintWriter writer = null;
			PrintWriter writerSystem = null;
			try {
				writer = new PrintWriter(outputPath + file.getName().replace(".arff", "") + "_GOLD.txt", "UTF-8");
				writerSystem = new PrintWriter(outputPath + file.getName().replace(".arff", "") + "_SYSTEM.txt", "UTF8");

				writer.println("#begin document " + file.getName().replace(".arff", "") + ".txt");
				writerSystem.println("#begin document " + file.getName().replace(".arff", "") + ".txt");

				int start = f  * positiveRelationSelected.size() / split;
				int end = start + positiveRelationSelected.size() / split;
				Relation[] relationArray = (Relation[]) positiveRelationSelected.keySet().toArray( new Relation[ positiveRelationSelected.size() ] );
				for(int p = start; p < end; p++){
					Relation relation = relationArray[ p ];
					Annotation annotation = positiveRelationSelected.get( relation );

					String element = "";
					String preElement = "";
					boolean refNotFound = false;
					if( relation.getElement( annotation ).getFeature( "REF" ).equalsIgnoreCase("NULL") ){
						if( relation.getFeature( "REF" ).equalsIgnoreCase("null")){
							element = ( indexUnit++ ) + "\t" + "(" + ( lastChainSingleton ) + ")" ;
							refNotFound = true;
						}else{
							element = ( indexUnit++ ) + "\t" + "(" + ( relation.getFeature( "REF" ) ) + ")" ;							
						}
					}else{
						element = ( indexUnit++ ) + "\t" + "(" + relation.getElement( annotation ).getFeature( "REF" ) + ")";
					}
					if( relation.getPreElement( annotation ).getFeature( "REF" ).equalsIgnoreCase("NULL") ){
						if( relation.getFeature( "REF" ).equalsIgnoreCase("null")){
							if( refNotFound ){
								preElement = ( indexUnit++ ) + "\t" + "(" + ( lastChainSingleton++ ) + ")" ;
							}else{
								preElement = ( indexUnit++ ) + "\t" + "(" + ( ++lastChainSingleton ) + ")" ;								
							}
						}else{
							preElement = ( indexUnit++ ) + "\t" + "(" + ( relation.getFeature( "REF" ) ) + ")" ;							
						}
					}else{
						preElement =  ( indexUnit++ ) + "\t" + "(" + relation.getPreElement( annotation ).getFeature( "REF" ) + ")";						
					}
					writer.println( element );
					writer.println( preElement );

					//on écrit le fichier system en même temps
					if( fileListInstance[ f ][ p ] == 1.0D ){
						//le classifieur l'a mis NOT_COREF alors qu'il a été donné COREF par GOLD
						writerSystem.println( ( indexUnit - 2 ) + "\t" + "(" + ( lastChainSingleton++ ) + ")" );
						writerSystem.println( ( indexUnit - 1 ) + "\t" + "(" + ( lastChainSingleton++ ) + ")" );

					}else{
						//Le classifieur l'a mis COREF comme GOLD on écrit la même chose
						writerSystem.println( element );
						writerSystem.println( preElement );
					}

				}

				//écriture instances négatives
				relationArray = (Relation[]) negativeRelationSelected.keySet().toArray( new Relation[ negativeRelationSelected.size() ] );
				start = f * negativeRelationSelected.size() / split;
				end = start + negativeRelationSelected.size() / split;
				for( int l = start; l < end; l++){
					Relation relation = relationArray[ l ];
					Annotation annotation = negativeRelationSelected.get( relation );
					String element = "";
					String preElement = "";
					boolean refNotFound = false;
					if( relation.getElement( annotation ).getFeature( "REF" ).equalsIgnoreCase("NULL") ){
						if( relation.getFeature( "REF" ).equalsIgnoreCase("null")){
							element = ( indexUnit++ ) + "\t" + "(" + ( lastChainSingleton ) + ")" ;
							refNotFound = true;
						}else{
							element = ( indexUnit++ ) + "\t" + "(" + ( relation.getFeature( "REF" ) ) + ")" ;							
						}
					}else{
						element = ( indexUnit++ ) + "\t" + "(" + relation.getElement( annotation ).getFeature( "REF" ) + ")";
					}
					if( relation.getPreElement( annotation ).getFeature( "REF" ).equalsIgnoreCase("NULL") ){
						if( relation.getFeature( "REF" ).equalsIgnoreCase("null")){
							if( refNotFound ){
								preElement = ( indexUnit++ ) + "\t" + "(" + ( lastChainSingleton++ ) + ")" ;
							}else{
								preElement = ( indexUnit++ ) + "\t" + "(" + ( ++lastChainSingleton ) + ")" ;								
							}
						}else{
							preElement = ( indexUnit++ ) + "\t" + "(" + ( relation.getFeature( "REF" ) ) + ")" ;							
						}
					}else{
						preElement =  ( indexUnit++ ) + "\t" + "(" + relation.getPreElement( annotation ).getFeature( "REF" ) + ")";						
					}
					writer.println( element );
					writer.println( preElement );


					//on écrit le fichier system en même temps
					if( fileListInstance[ f ][ l ] == 0.0D ){
						//le classifieur l'a mis COREF alors qu'il a été donné NOT_COREF par GOLD
						writerSystem.println( ( indexUnit - 2 ) + "\t" + "(" + ( lastChainSingleton++ ) + ")" );
						writerSystem.println( ( indexUnit - 1 ) + "\t" + "(" + ( lastChainSingleton++ ) + ")" );

					}else{
						//Le classifieur l'a bien mis NOT_COREF
						writerSystem.println( element );
						writerSystem.println( preElement );
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
		for(int f = 0; f < fileArff.size(); f++){
			File file = new File( fileArff.get( f ) );
			try {
				logger.info( PerlEval("muc", outputPath + file.getName().replace(".arff", "") + "_GOLD.txt" ,outputPath + file.getName().replace(".arff", "") + "_SYSTEM.txt"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}



	public static String PerlEval( String metric, String trueFile, String systemFile ) throws Exception{
		Process p = Runtime.getRuntime(  ).exec( "scorer.bat "+metric+" "+trueFile+" "+systemFile );
		BufferedReader br = new BufferedReader( new InputStreamReader( p.getInputStream(  ) ) );
		String line = null;
		List<String> resu = new ArrayList<String>(  );
		while ( ( line = br.readLine(  ) ) != null ){
			resu.add( line );
		}
		logger.debug( resu );
		String last = resu.get( resu.size(  )-2 );
		StringTokenizer st = new StringTokenizer( last );
		List<String> exemple = new ArrayList<String>(  );
		while ( st.hasMoreTokens(  ) ){
			exemple.add( st.nextToken(  ) );
		}
		String resultat = exemple.get( exemple.size(  )-1 );
		int index = resultat.indexOf( "%" );
		return resultat.substring( 0, index );
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
