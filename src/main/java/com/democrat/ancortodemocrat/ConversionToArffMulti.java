package com.democrat.ancortodemocrat;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Element;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Type;
import com.democrat.ancortodemocrat.element.Unit;
import com.democrat.ancortodemocrat.feature.CalculateFeature;

public class ConversionToArffMulti implements Runnable{


	public final static String ARFF_ATTRIBUTE = "@RELATION coreference\n"+
			"@ATTRIBUTE m1_type {N, PR, NULL}\n"+
			"@ATTRIBUTE m2_type {N, PR, NULL}\n"+
			"@ATTRIBUTE m1_def {INDEF, EXPL, DEF_SPLE, DEF_DEM, NULL, UNK}\n"+
			"@ATTRIBUTE m2_def {INDEF, EXPL, DEF_SPLE, DEF_DEM, NULL, UNK}\n"+
			"@ATTRIBUTE m1_genre {M, F, UNK, NULL}\n"+
			"@ATTRIBUTE m2_genre {M, F, UNK, NULL}\n"+
			"@ATTRIBUTE m1_nombre {SG, PL, UNK, NULL}\n"+
			"@ATTRIBUTE m2_nombre {SG, PL, UNK, NULL}\n"+
			/**
			"@ATTRIBUTE m1_previous string\n"+
			"@ATTRIBUTE m2_previous string\n"+
			"@ATTRIBUTE m1_next string\n"+
			"@ATTRIBUTE m2_next string\n"+
			"@ATTRIBUTE m1_spk string\n"+
			"@ATTRIBUTE m2_spk string\n"+
			 **/
//			 "@ATTRIBUTE m1_new {YES, NO, UNK, NULL}\n"+
//			 "@ATTRIBUTE m2_new {YES, NO, UNK, NULL}\n"+
			 "@ATTRIBUTE m1_en {PERS, FONC, LOC, ORG, PROD, TIME, AMOUNT, EVENT, NO, UNK, NULL}\n"+
			 "@ATTRIBUTE m2_en {PERS, FONC, LOC, ORG, PROD, TIME, AMOUNT, EVENT, NO, UNK, NULL}\n"+
			 "@ATTRIBUTE id_form {YES, NO, NA}\n"+
			 "@ATTRIBUTE id_subform {YES, NO, NA}\n"+
			 "@ATTRIBUTE incl_rate real\n"+
			 "@ATTRIBUTE com_rate real\n"+
			 "@ATTRIBUTE id_def {YES, NO, NA}\n"+
			 "@ATTRIBUTE id_type {YES, NO, NA}\n"+
			 "@ATTRIBUTE id_en {YES, NO, NA}\n"+
			 "@ATTRIBUTE id_genre {YES, NO, UNK}\n" +
			 "@ATTRIBUTE id_nombre {YES, NO, UNK}\n" +
			 "@ATTRIBUTE id_spk {YES, NO, NA}\n" +
			 "@ATTRIBUTE distance_mention real\n" +
			 "@ATTRIBUTE distance_turn real\n" +
			 "@ATTRIBUTE distance_word real\n" + 
			 "@ATTRIBUTE distance_char real\n" +
//			 "@ATTRIBUTE id_new {YES, NO, NA}\n" +
			 "@ATTRIBUTE EMBEDDED {YES, NO, NA}\n" +
			 "@ATTRIBUTE id_previous {YES, NO, NA}\n" +
			 "@ATTRIBUTE id_next {YES, NO, NA}\n" +
			 "@ATTRIBUTE class {DIRECTE, NOT_DIRECTE, INDIRECTE, NOT_INDIRECTE, ANAPHORE, NOT_ANAPHORE}\n" +
			 "@DATA";

	
	public final static String ARFF_ATTRIBUTE2 ="@RELATION coreference\n"+
			"@ATTRIBUTE m1_type {N, PR, NULL}\n"+
			"@ATTRIBUTE m2_type {N, PR, NULL}\n"+
			"@ATTRIBUTE m1_def {INDEF, EXPL, DEF_SPLE, DEF_DEM, NULL, UNK}\n"+
			"@ATTRIBUTE m2_def {INDEF, EXPL, DEF_SPLE, DEF_DEM, NULL, UNK}\n"+
			"@ATTRIBUTE m1_genre {M, F, UNK, NULL}\n"+
			"@ATTRIBUTE m2_genre {M, F, UNK, NULL}\n"+
			"@ATTRIBUTE m1_nombre {SG, PL, UNK, NULL}\n"+
			"@ATTRIBUTE m2_nombre {SG, PL, UNK, NULL}\n"+
			/**
			"@ATTRIBUTE m1_previous string\n"+
			"@ATTRIBUTE m2_previous string\n"+
			"@ATTRIBUTE m1_next string\n"+
			"@ATTRIBUTE m2_next string\n"+
			"@ATTRIBUTE m1_spk string\n"+
			"@ATTRIBUTE m2_spk string\n"+
			 **/
//			 "@ATTRIBUTE m1_new {YES, NO, UNK, NULL}\n"+
//			 "@ATTRIBUTE m2_new {YES, NO, UNK, NULL}\n"+
			 "@ATTRIBUTE m1_en {PERS, FONC, LOC, ORG, PROD, TIME, AMOUNT, EVENT, NO, UNK, NULL}\n"+
			 "@ATTRIBUTE m2_en {PERS, FONC, LOC, ORG, PROD, TIME, AMOUNT, EVENT, NO, UNK, NULL}\n"+
			 "@ATTRIBUTE id_form {YES, NO, NA}\n"+
			 "@ATTRIBUTE id_subform {YES, NO, NA}\n"+
			 "@ATTRIBUTE incl_rate real\n"+
			 "@ATTRIBUTE com_rate real\n"+
			 "@ATTRIBUTE id_def {YES, NO, NA}\n"+
			 "@ATTRIBUTE id_type {YES, NO, NA}\n"+
			 "@ATTRIBUTE id_en {YES, NO, NA}\n"+
			 "@ATTRIBUTE id_genre {YES, NO, UNK}\n" +
			 "@ATTRIBUTE id_nombre {YES, NO, UNK}\n" +
			 "@ATTRIBUTE id_spk {YES, NO, NA}\n" +
			 "@ATTRIBUTE distance_mention real\n" +
			 "@ATTRIBUTE distance_turn real\n" +
			 "@ATTRIBUTE distance_word real\n" + 
			 "@ATTRIBUTE distance_char real\n" +
//			 "@ATTRIBUTE id_new {YES, NO, NA}\n" +
			 "@ATTRIBUTE EMBEDDED {YES, NO, NA}\n" +
			 "@ATTRIBUTE id_previous {YES, NO, NA}\n" +
			 "@ATTRIBUTE id_next {YES, NO, NA}\n" +
			 "@ATTRIBUTE class {DIRECTE, NOT_DIRECTE, INDIRECTE, NOT_INDIRECTE, ANAPHORE, NOT_ANAPHORE}\n" +
			 "@DATA";

	private static Logger logger = Logger.getLogger(ConversionToArffDirecte.class);

	/**
	 * Liste des corpus dans laquelle
	 * les instances doivent être séléctionnées
	 */
	private List<Corpus> corpusList = new ArrayList<Corpus>();

	/**
	 * Paramètres de l'utilisateur
	 * qui indique combien d'instances
	 * positives et négatives doivent être
	 * extraites.
	 */
	private int positif;
	private int negatif;

	public static int countPositiveRelation = 0;
	public static int countNegativeRelation = 0;

	public static int countDirecteRelation = 0;
	public static int countNotDirecteRelation = 0;
	
	public static int countIndirecteRelation = 0;
	public static int countNotIndirecteRelation = 0;
	
	public static int countAnaphoreRelation = 0;
	public static int countNotAnaphoreRelation = 0;
	
	public static int countAssocRelation = 0;
	public static int countNotAssocRelation = 0;


	/**
	 * Listes des instances séléctionnées 
	 * filtrage grâce aux paramètres
	 * C'est donc les listes finales des instances attendues
	 */
	public Map<Relation, Annotation> positiveRelationSelected = new HashMap<Relation, Annotation>();
	public Map<Relation, Annotation> negativeRelationSelected = new HashMap<Relation, Annotation>();


	private ParamToArff param;

	private String outputPath;

	private String file1;
	private String file2;
	private String file3;


	private List<String> fileOuput = new ArrayList<String>();
	private int split;

	public ConversionToArffMulti(Corpus corpus){
		this.corpusList.add( corpus );
	}

	/**
	 * 
	 * @param positif nombre d'instance de relation positive voulue
	 * @param negatif nombre d'instance de négative voulue
	 * @param param Paramètre indiquant si on garde ou non les associatives
	 * @param outputPath chemin de sortie du fichier arff
	 */
	private ConversionToArffMulti(int positif, int negatif, ParamToArff param, String outputPath, int split){
		this.positif = positif;
		this.negatif = negatif;
		this.param = param;
		this.outputPath = outputPath;
		this.split = split;

	}

	public ConversionToArffMulti(Corpus corpus, int positif, int negatif, ParamToArff param, String outputPath, int split){
		this(positif, negatif, param, outputPath, split );
		this.corpusList.add( corpus );
	}

	public ConversionToArffMulti(List<Corpus> corpusList, int positif, int negatif, ParamToArff param,  String outputPath, int split){
		this(positif, negatif, param, outputPath, split );
		this.corpusList = corpusList;
	}


	public List<String> getFileOuput() {
		return fileOuput;
	}

	/**
	 * Pour une relation donnée, écrit dans un string la liste
	 * des valeurs de ses traits, et renvoie cette ligne.
	 * @param annotation Annotation nécessaire qui contient la relation pour récupérer les traits
	 * @param relation Relation concernée où les traits seront extraits
	 * @return
	 */
	private String makeRelation( Annotation annotation, Relation relation ){
		String line = "";
		Element element = relation.getElement( annotation );
		Element preElement = relation.getPreElement( annotation );
		if( element instanceof Unit && preElement instanceof Unit ){
			//m1_type
			if( preElement.getCharacterisation().getType().getValue().equals("default") ){
				line += "N";
			}else{
				line += preElement.getCharacterisation().getType().getValue();
			}
			line += " ";
			//m2_type
			if( element.getCharacterisation().getType().getValue().equals("default") ){
				line += "N";
			}else{
				line += element.getCharacterisation().getType().getValue();
			}
			line += " ";

			//m1_def
			line += preElement.getFeature( "DEF" );
			line += " ";
			//m2_def
			line += element.getFeature( "DEF" );

			line += " ";

			//m1_genre
			line += preElement.getFeature( "GENRE" );
			line += " ";
			//m2_genre
			line += element.getFeature( "GENRE" );
			line += " ";

			//m1_nombre
			line += preElement.getFeature( "NB" );
			line += " ";
			//m2_nombre
			line += element.getFeature( "NB" );
			line += " ";

//			//m1_new
//			line += preElement.getFeature( "NEW" );
//			line += " ";
//			//m2_new
//			line += element.getFeature( "NEW" );
//			line += " ";


			//m1_en
			line += preElement.getFeature( "EN" );
			line += " ";
			//m2_en
			line += element.getFeature( "EN" );
			line += " ";

			//id_form
			line += relation.getFeature( "ID_FORM" );
			line += " ";

			//id_subform
			line += relation.getFeature( "ID_SUBFORM" );
			line += " ";

			//incl_rate
			line += relation.getFeature( "INCL_RATE" );
			line += " ";

			//com_rate
			line += relation.getFeature( "COM_RATE" );
			line += " ";

			//ID_DEF
			line += relation.getFeature( "ID_DEF" );
			line += " ";

			//ID_TYPE
			line += relation.getFeature( "ID_TYPE" );
			line += " ";

			//id_en
			line += relation.getFeature( "ID_EN" );
			line += " ";

			//ID_GENRE
			line += relation.getFeature( "GENRE" );
			line += " ";

			//ID_NOMBRE
			line += relation.getFeature( "NOMBRE" );
			line += " ";

			//ID_SPK
			line += relation.getFeature( "ID_SPK" );
			line += " ";

			//distance_mention
			line += relation.getFeature( "DISTANCE_MENTION" );
			line += " ";

			//distance_turn
			line += relation.getFeature( "DISTANCE_TURN" );
			line += " ";

			//distance word
			line += relation.getFeature( "DISTANCE_WORD" );
			line += " ";

			//Distance_char 
			line += relation.getFeature( "DISTANCE_CHAR" );
			line += " ";

//			//id_new
//			line += relation.getFeature( "ID_NEW" );
//			line += " ";

			//embedded
			line += relation.getFeature( "EMBEDDED" );
			line += " ";

			//id_previous
			line += relation.getFeature( "ID_PREVIOUS" );
			line += " ";

			//id_next
			line += relation.getFeature( "ID_NEXT" );
			line += " ";

		}
		return line;
	}

	private void work( ){

		//first step: séléction de toutes les relations du/des corpus avec
		//génération des négatives, en triant selon la ParamToArff.
		sortInstance();
		//second step: séléction de p positive, et n negative comme voulue
//		System.out.println("pos: " + this.positif + " neg: " + this.negatif + " selPos: " + this.positiveRelationSelected.size() + " negSel " + this.negativeRelationSelected.size() );
		this.selectInstance();
		//puis écriture des instances
		this.writeInstance();

	}


	public Map<Relation, Annotation> getPositiveRelationSelected() {
		return positiveRelationSelected;
	}

	public Map<Relation, Annotation> getNegativeRelationSelected() {
		return negativeRelationSelected;
	}

	public void sortInstance(){
		for(Corpus corpus : corpusList){
			logger.info("corpus ==> " + corpus.getName() );
			for(Annotation annotation : corpus.getAnnotation()){
				annotation.removeTxtImporter();

				for( Relation relation : annotation.getRelation() ){
//					if(this.param.equals( ParamToArff.NO_ASSOC )){
						//test si la relation est une associative ou non
						//si c'est le cas, next
						if(relation.getCharacterisation().getType().getValue().toLowerCase().contains( "assoc" ) ){
							continue;
						}
					//}
//					else if(this.param.equals( ParamToArff.DIRECTE )){
//					}
					if( relation.getPreElement( annotation ) == null ||
							relation.getElement( annotation ) == null ){
						continue;
					}
					
					if( relation != null && relation instanceof Relation ){
						this.positiveRelationSelected.put(relation, annotation);
						generateNegativeRelation( corpus, annotation, relation );
					}
					//if( negativeRelation != null){
					//this.negativeRelationSelected.put(negativeRelation, annotation );
					//}
					//for positive class
					//String line = makeRelation( annotation, relation );
					/**if( ! line.isEmpty() ){
						line += "COREF";
						//writer.println( line );
						//this.positiveRelation.add( line );

						countPositiveRelation++;
						//for negative class
					}**/
				}
			}
		}
	}

	/**
	 * Séléctionne les instances positives et négatives selon les paramètres
	 * dans la liste de ceux trouvées dans les corpus
	 */
	public void selectInstance(){
		if( this.positif > this.positiveRelationSelected.size() || this.negatif > this.negativeRelationSelected.size() ){
			if( this.positif > this.positiveRelationSelected.size() ){
				logger.info("Trop d'instances positives attendues par rapport au corpus, restriction au maxium " + this.positiveRelationSelected.size() + ".");
			}
			if( this.negatif > this.negativeRelationSelected.size() ){
				logger.info("Trop d'instances négatives (" + this.negatif + ") attendues par rapport au corpus, restriction au maxium " + this.negativeRelationSelected.size() + ".");				
			}
			logger.info("Respect des pourcentages, positifs/négatifs.");
			float sum = this.positif + this.negatif;
			float percentPositive = this.positif / sum;
			float percentNegative = this.negatif / sum;

			this.positif = (int) ( percentPositive * this.positiveRelationSelected.size() );
			this.negatif = (int) ( percentNegative * this.negativeRelationSelected.size() );
			percentPositive *= 100;
			percentNegative *= 100;
			logger.info(percentPositive + "% soit " + ( this.positif ) + " instances positives");
			logger.info(percentNegative + "% soit " + this.negatif + " instances négatives");
		}

		if(this.positif == 0){
			//on prend tout 
			//this.positiveRelationSelected = this.positiveRelation;
			//this.negativeRelationSelected = this.negativeRelation;
		}else{
			//séléction aléatoire de p positive, et n négative
			int random = 0;
			ArrayList<Integer> nbGenerated = new ArrayList<Integer>();
			//on fait une liste temporaire pour ne garder que le nombre stricte
			//de positive et négative relation
			Map<Relation, Annotation> tmpPositiveRelation = new HashMap<Relation, Annotation>();
			//Relation[] relationArray = (Relation[]) this.positiveRelationSelected.keySet().toArray(new Relation[ this.positiveRelationSelected.size() ]);
			List<Relation> relationList = new ArrayList<Relation>( this.positiveRelationSelected.keySet() );
			for(int p = 0; p < this.positif; p++){
				random = AncorToDemocrat.randomNumber( 0, this.positiveRelationSelected.size() - 1);
				if( nbGenerated.contains( random ) ){
					while( nbGenerated.contains( random ) ){
						random = AncorToDemocrat.randomNumber( 0, positiveRelationSelected.size() - 1);
					}
				}
				nbGenerated.add( random );
				tmpPositiveRelation.put(relationList.get( random ), this.positiveRelationSelected.get( relationList.get( random ) ));
				//this.positiveRelationSelected.add( positiveRelationSelected.get( random ) );

			}
			//et on assigne cette liste temporaire à la liste total
			this.positiveRelationSelected = new HashMap<Relation, Annotation>( tmpPositiveRelation );
			tmpPositiveRelation.clear();
			nbGenerated.clear();
			//de même pour les négatifs
			Map<Relation, Annotation> tmpNegativeRelation = new HashMap<Relation, Annotation>();
			//relationArray = (Relation[]) this.negativeRelationSelected.keySet().toArray( new Relation[ this.negativeRelationSelected.size() ]);
			relationList = new ArrayList<Relation>( this.negativeRelationSelected.keySet() );
			for(int p = 0; p < this.negatif; p++){
				random = AncorToDemocrat.randomNumber( 0, this.negativeRelationSelected.size() - 1);
				if( nbGenerated.contains( random ) ){
					while( nbGenerated.contains( random ) ){
						random = AncorToDemocrat.randomNumber( 0, negativeRelationSelected.size() - 1);
					}
				}
				nbGenerated.add( random );

				tmpNegativeRelation.put(relationList.get( random ), this.negativeRelationSelected.get( relationList.get( random ) ));
				//this.positiveRelationSelected.add( positiveRelationSelected.get( random ) );

			}
			this.negativeRelationSelected = tmpNegativeRelation;
		}
	}

	public void writeInstance(){
		PrintWriter writer = null;
		PrintWriter writer2 = null;
		PrintWriter writer3 = null;
		PrintWriter writer4 = null;


		String fileName;
		DateFormat shortDateFormat = DateFormat.getDateTimeInstance(
				DateFormat.SHORT,
				DateFormat.SHORT);
		fileName = shortDateFormat.format( new Date() );
		logger.info( fileName );
		fileName = fileName.replace(" ", "_");
		fileName = fileName.replace("/", "_");
		fileName = fileName.replace(":", "H");
		
		String fileName2;
		
		fileName2 = shortDateFormat.format( new Date() );
		logger.info( fileName2 );
		fileName2 = fileName2.replace(" ", "_");
		fileName2 = fileName2.replace("/", "_");
		fileName2 = fileName2.replace(":", "H");
		
		String fileName3;
		
		fileName3 = shortDateFormat.format( new Date() );
		logger.info( fileName3 );
		fileName3 = fileName3.replace(" ", "_");
		fileName3 = fileName3.replace("/", "_");
		fileName3 = fileName3.replace(":", "H");
		
		String fileName4;
		
		fileName4 = shortDateFormat.format( new Date() );
		logger.info( fileName4 );
		fileName4 = fileName4.replace(" ", "_");
		fileName4 = fileName4.replace("/", "_");
		fileName4 = fileName4.replace(":", "H");
		
		String fileName5;
		
		fileName5 = shortDateFormat.format( new Date() );
		logger.info( fileName5 );
		fileName5 = fileName5.replace(" ", "_");
		fileName5 = fileName5.replace("/", "_");
		fileName5 = fileName5.replace(":", "H");
		


		if(this.positif == 0){
			//tout prendre
			if( split > 0 ){
				//sauf si on doit splitter le/les corpus

				for(int f = 1; f < split + 1; f++){
					try {
						writer = new PrintWriter(this.outputPath + fileName + "_" + f + ".arff", "UTF-8");
						this.fileOuput.add( this.outputPath + fileName + "_" + f + ".arff" );
						writer.println( ARFF_ATTRIBUTE );
						writer.println("");

						//écriture instances positives
						int start = (f - 1) * this.positiveRelationSelected.size() / split;
						int end = start + this.positiveRelationSelected.size() / split;
						
						Relation[] relationArray = (Relation[]) this.positiveRelationSelected.keySet().toArray( new Relation[ this.positiveRelationSelected.size() ] );
						Relation[] relationArray2 = (Relation[]) this.negativeRelationSelected.keySet().toArray( new Relation[ this.negativeRelationSelected.size() ] );
						
						
						for( int l = start; l < end; l++){
							
							Type type_relation = relationArray[l].getCharacterisation().getType();
							String string_type_relation = type_relation.getValue();
							logger.info("Type : " + string_type_relation);
							
							if(string_type_relation.equals("DIRECTE")){
								countDirecteRelation++;
								
								int idElement = relationArray[ l ].getElement( this.positiveRelationSelected.get( relationArray[ l ] ) ).getIdMention();
								int idPreElement = relationArray[ l ].getPreElement( this.positiveRelationSelected.get( relationArray[ l ] ) ).getIdMention();
								String line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ l ] ), relationArray[ l ] );
								writer.println( line + "DIRECTE" );
						}
						
							else{
								countNotDirecteRelation++;
								
								relationArray2[ l ].getElement( this.negativeRelationSelected.get( relationArray2[ l ] ) ).getIdMention();
								relationArray2[ l ].getPreElement( this.negativeRelationSelected.get( relationArray2[ l ] ) ).getIdMention();
								String line2 = this.makeRelation(this.negativeRelationSelected.get( relationArray2[ l ] ), relationArray2[ l ] );
								writer.println( line2 + "NOT_DIRECTE" );		
							}
						}
						
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						if(writer != null){
							writer.close();
						}
					}
				}
			}
			
			
			else{
				
				try {
					this.outputPath += fileName;
//					this.outputPath += fileName2;
//					this.outputPath += fileName3;

					writer = new PrintWriter(this.outputPath + ".arff", "UTF-8");
					this.fileOuput.add( this.outputPath + ".arff" );
					writer.println( ARFF_ATTRIBUTE );
					writer.println("");
					
					writer2 = new PrintWriter(this.outputPath + ".arff", "UTF-8");
					this.fileOuput.add( this.outputPath + ".arff" );
					writer2.println( ARFF_ATTRIBUTE );
					writer2.println("");
					
					writer3 = new PrintWriter(this.outputPath + ".arff", "UTF-8");
					this.fileOuput.add( this.outputPath + ".arff" );
					writer3.println( ARFF_ATTRIBUTE );
					writer3.println("");
					
					

					Relation[] relationArray = (Relation[]) this.positiveRelationSelected.keySet().toArray( new Relation[ this.positiveRelationSelected.size() ] );
					Relation[] relationArray2 = (Relation[]) this.negativeRelationSelected.keySet().toArray( new Relation[ this.negativeRelationSelected.size() ] );

					for(int p = 0; p < this.positiveRelationSelected.size(); p++){
						Type type_relation = relationArray[p].getCharacterisation().getType();
						String string_type_relation = type_relation.getValue();
						
						logger.info("Type : " + string_type_relation);

						if(string_type_relation.equals("DIRECTE")){
							countDirecteRelation++;
							
							int idElement = relationArray[ p ].getElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							int idPreElement = relationArray[ p ].getPreElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							String line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ p ] ), relationArray[ p ] );
							writer.println( line + "DIRECTE" );		
						}else{
							countNotDirecteRelation++;
							
							relationArray[ p ].getElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							relationArray[ p ].getPreElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							String line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ p ] ), relationArray[ p ] );
							writer.println( line + "NOT_DIRECTE" );		

						}		
						
						if(string_type_relation.equals("INDIRECTE")){
							
							int idElement = relationArray[ p ].getElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							int idPreElement = relationArray[ p ].getPreElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							String line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ p ] ), relationArray[ p ] );
							writer2.println( line + "INDIRECTE" );		
						}else{
							
							relationArray[ p ].getElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							relationArray[ p ].getPreElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							String line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ p ] ), relationArray[ p ] );
							writer2.println( line + "NOT_INDIRECTE" );		

						}	
						
						if(string_type_relation.equals("ANAPHORE")){
							
							int idElement = relationArray[ p ].getElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							int idPreElement = relationArray[ p ].getPreElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							String line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ p ] ), relationArray[ p ] );
							writer.println( line + "ANAPHORE" );		
						}else{
							
							relationArray[ p ].getElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							relationArray[ p ].getPreElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							String line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ p ] ), relationArray[ p ] );
							writer3.println( line + "NOT_ANAPHORE" );		

						}		
						
					}
					
					
					for(int n = 0; n < this.negativeRelationSelected.size(); n++){
							countNotDirecteRelation++;
							countNotIndirecteRelation++;
							countNotAnaphoreRelation++;
							countNotAnaphoreRelation++;

//							relationArray2[ n ].getElement( this.negativeRelationSelected.get( relationArray2[ n ] ) ).getIdMention();
//							relationArray2[ n ].getPreElement( this.negativeRelationSelected.get( relationArray2[ n ] ) ).getIdMention();
							String line2 = this.makeRelation(this.negativeRelationSelected.get( relationArray2[ n ] ), relationArray2[ n ] );
							writer.println( line2 + "NOT_DIRECTE" );
							writer2.println( line2 + "NOT_INDIRECTE" );
							writer3.println( line2 + "NOT_ANAPHORE" );

						}		
				}
	
					/**Relation[] relationArray = (Relation[]) this.positiveRelationSelected.keySet().toArray();
					for(int p = 0; p < this.positiveRelationSelected.size(); p++){
						String line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ p ] ), relationArray[ p ] );
						writer.println( line + "COREF" );			
					}**/
					/**relationArray = (Relation[]) this.negativeRelationSelected.keySet().toArray();
					for(int n = 0; n < this.negativeRelationSelected.size(); n++){
						String line = this.makeRelation(this.negativeRelationSelected.get( relationArray[ n ] ), relationArray[ n ] );
						writer.println( line + "NOT_COREF" );
					}**/

				 catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					if(writer != null){
						writer.close();
					}
				}
			}
		}
	
		
		
		else{
			//les instances sont déjà séléctionnées, juste besoin de les écrire
			try {
//				this.outputPath += fileName;
//				this.outputPath += fileName2;
//				this.outputPath += fileName3;
//				this.outputPath += fileName4;
//				this.outputPath += fileName5;

				writer = new PrintWriter(this.outputPath + fileName + "_" + "directe" + ".arff", "UTF-8");
				this.fileOuput.add( this.outputPath + ".arff" );
				writer.println( ARFF_ATTRIBUTE );
				writer.println("");
				
				writer2 = new PrintWriter(this.outputPath + fileName + "_" + "indirecte" + ".arff", "UTF-8");
				this.fileOuput.add( this.outputPath + ".arff" );
				writer2.println( ARFF_ATTRIBUTE );
				writer2.println("");
				
				writer3 = new PrintWriter(this.outputPath + fileName + "_" + "anaphore" + ".arff", "UTF-8");
				this.fileOuput.add( this.outputPath + ".arff" );
				writer3.println( ARFF_ATTRIBUTE );
				writer3.println("");

				writer4 = new PrintWriter(this.outputPath + fileName + "_" + "coref" + ".arff", "UTF-8");
				this.fileOuput.add( this.outputPath + ".arff" );
				writer4.println( ARFF_ATTRIBUTE );
				writer4.println("");
				
//				writer5 = new PrintWriter(this.outputPath + fileName + "_" + "assoc" + ".arff", "UTF-8");
//				this.fileOuput.add( this.outputPath + ".arff" );
//				writer5.println( ARFF_ATTRIBUTE );
//				writer5.println("");
				
				Relation[] relationArray = (Relation[]) this.positiveRelationSelected.keySet().toArray( new Relation[ this.positiveRelationSelected.size() ] );
				Relation[] relationArray2 = (Relation[]) this.negativeRelationSelected.keySet().toArray( new Relation[ this.negativeRelationSelected.size() ] );

				for(int p = 0; p < this.positiveRelationSelected.size(); p++){
															
					Type type_relation = relationArray[p].getCharacterisation().getType();
					String string_type_relation = type_relation.getValue();
					
					logger.info("Type : " + string_type_relation);
					String line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ p ] ), relationArray[ p ] );
					int idElement = relationArray[ p ].getElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
					int idPreElement = relationArray[ p ].getPreElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();

					if(string_type_relation.equals("DIRECTE")){
						countDirecteRelation++;
						countNotIndirecteRelation++;
						countNotAnaphoreRelation++;

						idElement = relationArray[ p ].getElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
						idPreElement = relationArray[ p ].getPreElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
						line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ p ] ), relationArray[ p ] );
						writer.println( line + "DIRECTE" );	
						writer2.println( line + "NOT_INDIRECTE");
						writer3.println( line + "NOT_ANAPHORE");
//						writer5.println( line + "NOT_ASSOC");
						writer4.println( line + "COREF");
					}else{
//						if(string_type_relation.equals("ASSOC") || string_type_relation.equals("ASSOC_PRONOM") ){
//							countAssocRelation++;
//							countNotIndirecteRelation++;
//							countNotDirecteRelation++;
//							countNotAnaphoreRelation++;
//							
//							idElement = relationArray[ p ].getElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
//							idPreElement = relationArray[ p ].getPreElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
//							line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ p ] ), relationArray[ p ] );
//							writer.println( line + "NOT_DIRECTE" );		
//							writer2.println( line + "NOT_INDIRECTE" );	
//							writer3.println( line + "NOT_ANAPHORE");
//							writer5.println( line + "ASSOC");
//							writer4.println( line + "NOT_COREF");
//						}
						 if(string_type_relation.equals("INDIRECTE")){
							countIndirecteRelation++;
							countNotDirecteRelation++;
							countNotAnaphoreRelation++;
							idElement = relationArray[ p ].getElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							idPreElement = relationArray[ p ].getPreElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ p ] ), relationArray[ p ] );
							writer.println( line + "NOT_DIRECTE" );		
							writer2.println( line + "INDIRECTE" );	
							writer3.println( line + "NOT_ANAPHORE");
//							writer5.println( line + "NOT_ASSOC");
							writer4.println( line + "COREF");
						}
						else if(string_type_relation.equals("ANAPHORE")){
							countNotDirecteRelation++;
							countNotIndirecteRelation++;
							countAnaphoreRelation++;
							idElement = relationArray[ p ].getElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							idPreElement = relationArray[ p ].getPreElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
							line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ p ] ), relationArray[ p ] );
							writer3.println( line + "ANAPHORE" );	
							writer.println( line + "NOT_DIRECTE");
							writer2.println( line + "NOT_INDIRECTE");
//							writer5.println( line + "NOT_ASSOC");
							writer4.println( line + "COREF");

						}
					}

					
				}
				
				
				for(int n = 0; n < this.negativeRelationSelected.size(); n++){
						countNotAssocRelation++;
						countNotDirecteRelation++;
						countNotIndirecteRelation++;
						countNotAnaphoreRelation++;
//						
//						relationArray2[ n ].getElement( this.negativeRelationSelected.get( relationArray2[ n ] ) ).getIdMention();
//						relationArray2[ n ].getPreElement( this.negativeRelationSelected.get( relationArray2[ n ] ) ).getIdMention();
						String line2 = this.makeRelation(this.negativeRelationSelected.get( relationArray2[ n ] ), relationArray2[ n ] );
						writer.println( line2 + "NOT_DIRECTE" );		
						writer2.println( line2 + "NOT_INDIRECTE" );
						writer3.println( line2 + "NOT_ANAPHORE" );
						writer4.println( line2 + "NOT_COREF" );
//						writer5.println( line2 + "NOT_ASSOC");

					}	
			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if(writer != null){
					writer.close();
					writer2.close();
					writer3.close();
					writer4.close();
//					writer5.close();
				}
			}
		}
	}	


	/**
	 * Génère entre une à trois relations négatives, en prenant comme unité, le premier élément 
	 * de la relation.
	 * @param corpus Corpus sur lequel la relation est basée
	 * @param annotation Annotation qui contient la relation
	 * @param relation relation à partir de laquelle sera generée entre 1 à 3 relation négative
	 */
	private void generateNegativeRelation(Corpus corpus, Annotation annotation, Relation relation) {
		int negativeRelationToGenerate = AncorToDemocrat.randomNumber(1, 3);
		List<Unit> unitList = annotation.getUnit();
		for(int turn = 0; turn < negativeRelationToGenerate; turn++){
			boolean done = false;
			int attempt = 0;
			while( ! done ){
				int unitIdRandom = AncorToDemocrat.randomNumber(0, unitList.size() - 1 );
				//if( ! unitList.contains( unitIdRandom ) ){
				//negative relation generated
				if( ! unitList.get( unitIdRandom ).getFeature( "REF" ).equals( relation.getFeature( "REF" ) ) ){
					//the unit is not in the chain of relation
					// we create a new relation between the unit and preElement of the relation
					Unit unit = (Unit) relation.getElement( annotation );
					if( unit != null ){
						Relation newRelation = new Relation();
						newRelation.addUnit( unit );
						newRelation.addUnit( unitList.get( unitIdRandom ) );

						CalculateFeature calculateFeature = new CalculateFeature( corpus, "" );
						calculateFeature.calculateFeatureOnRelation(annotation, newRelation);

						//then calculate feature of the new relation
						/**String line = makeRelation( annotation, newRelation );
					if( ! line.isEmpty() ){
						line += "NOT_COREF";
						//writer.println( line );
						this.negativeRelation.add( line );
						countNegativeRelation++;
					}**/
						//return newRelation;
						this.negativeRelationSelected.put(newRelation, annotation );
						done = true;
					}
				}
				//}
				if( attempt > 15 ){
					//safe case
					break;
				}
				attempt++;
			}
		}
		//return null;
	}

	@Override
	public void run() {
		//charger chaque corpus..

		for(Corpus corpus : this.corpusList ){
			logger.info("Chargement du corpus " + corpus.getName() );
			corpus.loadAnnotation();
			corpus.loadText();
		}

		this.work( );
		logger.info("[" + this.outputPath + "] arff file writed!");
		logger.info("COREF: " + this.positiveRelationSelected.size() );
		logger.info("NOT-COREF: " + this.negativeRelationSelected.size() );
		logger.info("DIRECTE: " + countDirecteRelation );
		logger.info("NOT-DIRECTE: " + countNotDirecteRelation );
		logger.info("INDIRECTE: " + countIndirecteRelation );
		logger.info("NOT-INDIRECTE: " + countNotIndirecteRelation );
		logger.info("ANAPHORE: " + countAnaphoreRelation );
		logger.info("NOT-ANAPHORE: " + countNotAnaphoreRelation );
		logger.info("ASSOC: " + countAssocRelation );
		logger.info("NOT-ASSOC: " + countNotAssocRelation );

		

	}

}
