package com.democrat.ancortodemocrat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.feature.CalculateFeature;
import com.democrat.ancortodemocrat.treetagger.TreeTagger;
import com.democrat.classification.Model;
import com.democrat.classification.Scorer;

import weka.classifiers.Evaluation;
import weka.core.Instances;


public class AncorToDemocrat {

	private static Logger logger = Logger.getLogger(AncorToDemocrat.class);
	public static FileManager fileManager;
	public static TreeTagger treeTagger;
	public final static String[] help = new String[]{};

	public static void main(String[] args) {

		//configure logger
		DOMConfigurator.configure("cfg/log4j-config.xml");


		System.out.println("=====================================================================================================================");
		System.out.println("=====================================================================================================================");

		//cfg TreeTagger
		treeTagger = new TreeTagger();

		fileManager = new FileManager();


		if( args.length > 1){
			if( args[0].equalsIgnoreCase( "feature" )){
				/**
				 * feature
				 * - type de corpus en entrée
				 *    "p" --> en première mention
				 *    "c" --> en chaîne
				 * - chemin du corpus en entrée
				 * - -o chemin de sortie pour le corpus avec ses traits calculés (si non spécifié, generated/feature/nomDuCorpus)
				 **/
				//path corpus unknow
				String outputPath = "";

				Corpus corpus;
				if( args.length > 2 ){
					//test if output argument is present
					corpus = new Corpus( args[ 2 ] );
					if( args.length > 3 ){

						if( args[ 3 ].equalsIgnoreCase( "-o" ) ){
							outputPath = args[ 4 ];
						}else{
							logger.error("Arguement -o manquant pour le nombre d'arguement passé.");
						}
					}
					if( outputPath.isEmpty() ){
						outputPath = "generated/feature/";
						fileManager.mkdir( outputPath );
						outputPath += corpus.getName();
						fileManager.mkdir(outputPath);
					}
					if( args[ 1 ].equalsIgnoreCase( "p" ) ){
						//first mention
						//calculate REF feature 
						generateFeature( corpus, true, outputPath);			
					}else if( args[ 1 ].equalsIgnoreCase( "c" ) ){
						//in chain
						//calculate REF feature 
						generateFeature( corpus, false, outputPath);	
					}else{
						logger.error("Erreur pour Feature, e.g:");
						logger.error("Pour un corpus en chaîne: calculateFeature c C:/Users/buggr/Documents/stage/ancor/corpus_OTG generated/corpus/corpus_OTG_traits_caclules");
						logger.error("Pour un corpus en première mention: calculateFeature p C:/Users/buggr/Documents/stage/ancor/corpus_OTG generated/corpus/corpus_OTG_traits_caclules");
					}
					return;
				}
			}else if( args[ 0 ].equalsIgnoreCase( "arff" ) ||
					args[ 0 ].equalsIgnoreCase( "scorer" ) ){
				//arff command
				/**     
				 * - paramètre de sortie
				 * 			"all" --> toutes les relations
				 * 			"no_assoc" --> toutes les relations sans les associatives, ni les associatives pronominales 
				 * - -i chemin du ou des corpus à extraire, peut aussi être un simple fichier .aa (pour garder l'intégrité d'un texte)
				 * 			(si non spécifié, prend tous les corpus dans generated/feature/) (le(s) corpus doit avoir ses features calculés (précédente commande))
				 * - -q quantité de nombre d'instances positives, et d'instances négatives
				 * - -o nom du fichier .arff qui sera exporté (si non spécifié, generated/arff/dateDuJour_Heure.arff)
				 * - -s en combien de partie le/les corpus doivent être splité(s)
				 * 
				 * 
				 * Cas où c'est scorer on doit avoir en plus
				 * - -m chemin du model
				 * - -r liste des paramètres à supprimer
				 * - -a type de traits à garder NO_ORAL || ONLY_RELATIONAL
				 * - -r et -a peuvent être complémentaire
				 * 
				 **/

				ParamToArff parameter = ParamToArff.ALL;
				String inputPath = "";
				String outputPath = "";
				String modelPath = "";
				List<String> removeAttribute = new ArrayList<String>();
				int split = 0;
				//quantité
				int pos = 0;
				int neg = 0;

				if( args.length > 1 ){
					if( args[ 1 ].equalsIgnoreCase( "all" ) ) {

					}else if( args[ 1 ].equalsIgnoreCase( "no_assoc") ){
						parameter = ParamToArff.NO_ASSOC;

					}else{
						//error first argument
						logger.error( "Premier argument invalide, il doit être égal à 'all' ou 'no_assoc'.");
						logger.error( "Pour plus d'information, invoquez help.");
						return;
					}

					for( int a = 2; a < args.length; a++){
						if(args[ a ].equalsIgnoreCase( "-i" ) ){
							//input path
							if( a + 1 < args.length ){
								inputPath = args[ a + 1 ];
							}else{
								//error missing arguement
								logger.error("Aucun paramètre indiqué après -i.");
							}
						}else if(args[ a ].equalsIgnoreCase( "-q" ) ){
							//quantity of negative, positive instances
							if( a + 2 < args.length ){

								try{
									pos = Integer.valueOf( args[ a + 1] );
									neg = Integer.valueOf( args[ a + 2] );
								}catch(NumberFormatException e){
									logger.error("Deux nombres sont attendus après -q.");
								}
							}else{
								//error missing arguement
								logger.error("Aucun paramètre indiqué après -q.");
							}
						}else if(args[ a ].equalsIgnoreCase( "-o" ) ){
							//ouput path
							if( a + 1 < args.length ){
								outputPath = args[ a + 1 ];
							}else{
								//error missing arguement
								logger.error("Aucun paramètre indiqué après -o.");
							}
						}else if( args[ a ].equalsIgnoreCase( "-s" ) ){
							if( a + 1 < args.length ){
								try{
									split = Integer.valueOf( args[ a + 1 ] );
								}catch(NumberFormatException e){
									logger.error("Un chiffre est attendue après -s.");
								}
							}else{
								//error missing arguement
								logger.error("Aucun paramètre indiqué après -s.");								
							}
						}
						if( args[ 0 ].equalsIgnoreCase( "scorer" ) ){
							if( args[ a ].equalsIgnoreCase( "-m" ) ){
								if( a + 1 < args.length ){
									modelPath = args[ a + 1 ];
								}else{
									//error missing arguement
									logger.error("Aucun paramètre indiqué après -m.");								
								}
							}else if( args[ a ].equalsIgnoreCase( "-r") ){
								int i = 1;
								while( a + i < args.length && ! args[ a + i ].contains("-") ){
									removeAttribute.add( args[ a + i ] );
									i++;
								}
								if( i == 1 ){
									logger.error( "Aucun paramètre indiqué après -r.");
								}
							}else if( args[ a ].equalsIgnoreCase( "-a" ) ){
								if( a + 1 < args.length ){
									if( args[ a + 1 ].equalsIgnoreCase( "NO_ORAL" ) ){
										//on ne prend pas en compte les traits oraux
										removeAttribute.add( "distance_turn" );
										removeAttribute.add( "id_spk" );
									}else if( args[ a + 1 ].equalsIgnoreCase( "ONLY_RELATIONAL" ) ){
										//18 traits en tout que les relationnels
										removeAttribute.add( "m1_type" );
										removeAttribute.add( "m2_type" );
										removeAttribute.add( "m1_def" );
										removeAttribute.add( "m2_def" );
										removeAttribute.add( "m1_genre" );
										removeAttribute.add( "m2_genre" );
										removeAttribute.add( "m1_nombre" );
										removeAttribute.add( "m2_nombre" );
										removeAttribute.add( "m1_new" );
										removeAttribute.add( "m2_new" );
										removeAttribute.add( "m1_en" );
										removeAttribute.add( "m2_en" );
									}
								}
							}
						}
					}

					List<Corpus> corpusList = new ArrayList<Corpus>();

					if( inputPath.isEmpty() ){
						//charger les corpus dans generated/
						String pathFolder = "generated/corpus/";
						ArrayList<String> corpusPathList = fileManager.getFolderFromFolder( new File( pathFolder ) );
						for(int c = 0; c < corpusPathList.size(); c++){
							corpusList.add( new Corpus( "generated/corpus/" + corpusPathList.get( c ) ) );
						}
					}else{
						//tester si c'est un dossier ou non
						//ensuite tester si le dossier contient des corpus ou si le dossier est un corpus
						//sinon fichier
						File fileInput = new File( inputPath );
						if( fileInput.isDirectory() ){
							//dossier
							ArrayList<String> folderList = fileManager.getFolderFromFolder( fileInput );
							boolean isFolderCorpus = false;
							for( int f = 0; f < folderList.size(); f++ ){
								if( folderList.get( f ).equalsIgnoreCase("aa_fichiers" ) ){
									isFolderCorpus = true;
								}
							}
							if( isFolderCorpus ){
								//dossier de corpus
								corpusList.add( new Corpus( fileInput.getAbsolutePath() ) );
							}else{
								//liste de dossier de corpus
								for( int f = 0; f < folderList.size(); f++ ){
									if( ! inputPath.endsWith("\\") && ! inputPath.endsWith("/") ){
										inputPath += "/";
									}
									corpusList.add( new Corpus( inputPath + folderList.get( f ) ) );
								}
							}
						}else{
							//fichier
							Annotation annotation = XmlLoader.loadAnnotationFromFile( inputPath );
							List<Annotation> annotationList = new ArrayList<Annotation>();
							annotationList.add( annotation );
							corpusList.add( new Corpus("/default", annotationList ) );
						}
					}

					//OUTPUTPATH
					if(outputPath.isEmpty() ){
						// sortie par defaut
						DateFormat shortDateFormat = DateFormat.getDateTimeInstance(
								DateFormat.SHORT,
								DateFormat.SHORT);
						String fileName = shortDateFormat.format( new Date() );
						logger.info( fileName );
						fileName = fileName.replace(" ", "-");
						fileName = fileName.replace("/", "_");
						fileName = fileName.replace(":", "H");
						outputPath = "generated/arff/" + fileName;
					}else{
						//tester si le chemin de sortie est un dossier
						File outputFile = new File( outputPath );
						if( outputFile.isDirectory() && args[ 0 ].equalsIgnoreCase( "arff" ) ){
							logger.error("Le fichier de sortie ne doit pas être un dossier: "+outputPath);
							return;
						}else if( ! outputFile.isDirectory() && args[ 0 ].equalsIgnoreCase( "scorer" ) ){
							logger.error("Le fichier de sortie ne doit pas être un fichier: "+outputPath);
							return;							
						}
					}

					if( args[ 0 ].equalsIgnoreCase( "scorer" ) && 
							( ! outputPath.endsWith("\\") || ! outputPath.endsWith( "/" ) ) ){
						outputPath += "/";
					}
					if( split != 0 && pos != 0 ){
						pos = 0;
						neg = 0;
						logger.info("Seul l'option split est prise en compte face à -q.");
					}

					if( args[ 0 ].equalsIgnoreCase( "arff" ) ){
						ConversionToArff conversionToArff = new ConversionToArff(corpusList, pos, neg, parameter, outputPath, split);
						Thread th = new Thread( conversionToArff );
						th.start();
					}else{
						//SCORER
						Scorer.scorerTask( corpusList, modelPath, pos, neg, parameter, outputPath, split, removeAttribute );
					}
				}
			}else if( args[ 0 ].equalsIgnoreCase("chain") ){
				//loading corpus via command line
				List<Corpus> corpusList = new ArrayList<Corpus>();
				for(int a = 1; a < args.length; a++){
					corpusList.add( new Corpus( args[ a ] ) );
				}
				convertCorpus( corpusList );

			}else if( args[ 0 ].equalsIgnoreCase( "ownarff" ) ){
				if(args.length >= 4){
					int pos = 0;
					int neg = 0;
					try{
						pos = Integer.valueOf( args[ 3 ] );
						neg = Integer.valueOf( args[ 4 ] );
					}catch(NumberFormatException e){
						logger.error("nbPositiveInstance et nbNegative doivent être des nombres");
						logger.error("e.g. arff fromFolderName fileName nbPositiveInstance nbNegativeInstance");
					}
					generateOwnArff(args[ 1 ], args[ 2 ], pos, neg );
				}else if(args[ 1 ].equalsIgnoreCase("corpus" ) ){
					generateCorpusArff();
				}else{
					logger.error("e.g. arff fileName nbPositiveInstance nbNegativeInstance");
					logger.error("e.g. arff corpus : to generate all arff file from corpus");
				}
			}
		}else if(args.length == 1){
			//help
			if(args[ 0 ].equalsIgnoreCase( "help" ) ){
				//TODO print all command
			}
		}else{//loading corpus via txt file
			List<String> corpusPath = fileManager.loadPathFile();
			List<Corpus> corpusList = new ArrayList<Corpus>();
			for(String path : corpusPath){
				corpusList.add( new Corpus( path ));
			}

			convertCorpus( corpusList );
		}
	}

	public static void generateCorpusArff(){
		//loading corpus
		List<String> corpusPath = fileManager.loadPathFile();
		List<Corpus> corpusList = new ArrayList<Corpus>();
		for(String path : corpusPath){
			corpusList.add( new Corpus( path ));
		}

		//loading annotation and text of corpus
		for(Corpus corpus : corpusList){
			logger.info("Loading annotation on: " + corpus.getName() );
			corpus.loadAnnotation();
			logger.info("Loading text on: " + corpus.getName() );
			corpus.loadText();

		}


		//conversion each corpus
		for(Corpus corpus : corpusList){
			ConversionToArff conversionToArff = new ConversionToArff( corpus );
			Thread th = new Thread( conversionToArff );
			th.start();
		}
	}

	/**
	 * select randomly nb positive instance, and
	 * nb negative instance from other arff file
	 * and generate one file with the name,
	 * 
	 * @param folderName chemin d'entrée
	 * @param fileName Nom du fichier de sortie, si null ou vide, le nom sera 'coreferences'
	 * @param nbPos doit être > 0
	 * @param nbNeg doit être > 0
	 */
	public static void generateOwnArff(String folderName, String fileName, int nbPos, int nbNeg){
		final String defaultFileName = "coreferences";

		if( nbPos < 0 || nbNeg < 0 ){
			logger.error("nbPos et nbNeg doit être > 0");
			return;
		}


		if( fileName.isEmpty() || fileName == null){
			fileName = defaultFileName;
		}


		List<String> posInstanceList = new ArrayList<String>();
		List<String> negInstanceList = new ArrayList<String>();
		PrintWriter writer = null;
		try {

			//load all arff file
			//if no arff file
			//error
			//TODO help user with command
			ArrayList<String> fileList = fileManager.getFileFromFolder(new File( folderName ), "arff");
			if( fileList.size() == 0 ){
				logger.error("Pas de fichier arff trouvé dans ce dossier: "+folderName);		
				return;
			}
			writer = new PrintWriter("generated/" + fileName + ".arff", "UTF-8");
			//loading arff files
			for(String file : fileList){
				BufferedReader br = null;
				try {
					br = new BufferedReader(new FileReader( folderName + file ) );
					String line;
					while ((line = br.readLine()) != null) {
						if( ! line.startsWith("@DATA") && ! line.startsWith("@ATTRIBUTE" ) ){
							if( line.endsWith(" COREF") ){
								posInstanceList.add( line );
							}else if( line.endsWith("NOT_COREF" ) ){
								negInstanceList.add( line );
							}
						}
					}
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					if(br != null){
						try {
							br.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}



			//writing new arff file
			writer.println(ConversionToArff.ARFF_ATTRIBUTE);

			ArrayList<Integer> nbGenerated = new ArrayList<Integer>();


			int random = 0;
			//select the intances
			for(int p = 0; p < nbPos; p++){
				random = AncorToDemocrat.randomNumber( 0, posInstanceList.size() - 1);
				if( nbGenerated.contains( random ) ){
					while( nbGenerated.contains( random ) ){
						random = AncorToDemocrat.randomNumber( 0, posInstanceList.size() - 1);						
					}
				}
				nbGenerated.add( random );	
				writer.println( posInstanceList.get( random ) );

			}
			nbGenerated.clear();
			for(int n = 0; n < nbNeg; n++){
				random = AncorToDemocrat.randomNumber( 0, negInstanceList.size() - 1);
				if( nbGenerated.contains( random ) ){
					while( nbGenerated.contains( random ) ){
						random = AncorToDemocrat.randomNumber( 0, negInstanceList.size() - 1);						
					}
				}
				nbGenerated.add( random );				
				writer.println( negInstanceList.get( random ) );
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if( writer != null){
				writer.close();
			}
		}

		logger.info(fileName + " arff created with "+nbPos+" positive(s) instance(s) and "+nbNeg+" negative(s) instance(s).");

	}

	/**
	 * Calcule de nouveaux traits pour chaque relations, unités du corpus
	 * @param isFirstMention Si le corpus est annoté en première mention ou non
	 * @param outputPath chemin de sortie du corpus avec les nouveaux traits.
	 * 
	 */
	public static void generateFeature( Corpus corpus, boolean isFirstMention, String outputPath ){
		logger.info("Loading annotation on: " + corpus.getName() );
		corpus.loadAnnotation();
		logger.info("Loading text on: " + corpus.getName() );
		corpus.loadText();
		List<Annotation> annotationList = corpus.getAnnotation();
		for(int a = 0; a < annotationList.size(); a++){
			if( ! isFirstMention ){
				ConversionInSet.toSetFromChain( annotationList.get( a ) );
			}else{
				ConversionInSet.toSetFromFirstMention( annotationList.get( a ) );
			}
		}


		CalculateFeature calculate = new CalculateFeature( corpus, outputPath );
		Thread th = new Thread( calculate );
		th.start();
	}

	/**
	 * Convertie des corpus annoté en première mention vers un typage en chaîne
	 * @param corpusList Liste des corpus qui seront convertis en chaîne
	 */
	public static void convertCorpus(List<Corpus> corpusList){
		//loading annotation of corpus
		for(Corpus corpus : corpusList){
			logger.info("Loading annotation on: " + corpus.getName() );
			corpus.loadAnnotation();
			logger.info("Loading text on: " + corpus.getName() );
			corpus.loadText();

		}

		List<ConversionWorker> conversionWorkerList = new ArrayList<ConversionWorker>();

		//conversion each corpus
		for(Corpus corpus : corpusList){
			ConversionWorker conversionWorker = new ConversionWorker( corpus );
			conversionWorkerList.add( conversionWorker );
			conversionWorker.start();
		}
	}


	/**
	 * 
	 * @param min including
	 * @param max including
	 * @return
	 */
	public static int randomNumber(int min, int max){
		return min + (int)(Math.random() * ((max - min) + 1));
	}


}
