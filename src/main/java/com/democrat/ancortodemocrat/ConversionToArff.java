package com.democrat.ancortodemocrat;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Element;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Unit;
import com.democrat.ancortodemocrat.feature.CalculateFeature;

public class ConversionToArff implements Runnable{
	
	private static Logger logger = Logger.getLogger(ConversionToArff.class);

	private Corpus corpus;
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
			 "@ATTRIBUTE m1_new {YES, NO, UNK, NULL}\n"+
			 "@ATTRIBUTE m2_new {YES, NO, UNK, NULL}\n"+
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
			 "@ATTRIBUTE id_new {YES, NO, NA}\n" +
			 "@ATTRIBUTE EMBEDDED {YES, NO, NA}\n" +
			 "@ATTRIBUTE class {COREF, NOT_COREF}\n" +
			 "@DATA";



	public static int positiveRelation = 0;
	public static int negativeRelation = 0;

	public ConversionToArff(Corpus corpus){
		this.corpus = corpus;
	}

	private String writeFeatures( Annotation annotation, Relation relation ){
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

			//m1_previous
			//line += preElement.getFeature( "PREVIOUS" );
			//line += " ";
			//m2_previous
			//line += element.getFeature( "PREVIOUS" );
			//line += " ";

			//m1_next
			//line += preElement.getFeature( "NEXT" );
			//line += " ";
			//m2_next
			//line += element.getFeature( "NEXT" );
			//line += " ";

			//m1_spk
			//line += preElement.getFeature( "SPK" );
			//line += " ";
			//m2_spk
			//line += element.getFeature( "SPK" );
			//line += " ";

			//m1_new
			line += preElement.getFeature( "NEW" );
			line += " ";
			//m2_new
			line += element.getFeature( "NEW" );
			line += " ";


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
			
			//id_new
			line += relation.getFeature( "ID_NEW" );
			line += " ";
			
			//embedded
			line += relation.getFeature( "EMBEDDED" );
			line += " ";

		}
		return line;
	}

	private void work( ){

		PrintWriter writer = null;
		try {
			writer = new PrintWriter("generated/arff/" + corpus.getName() + "_coreference.arff", "UTF-8");
			writer.println( ARFF_ATTRIBUTE );
			writer.println("");
			for(Annotation annotation : corpus.getAnnotation()){
				annotation.removeTxtImporter();
				for( Relation relation : annotation.getRelation() ){

					//for positive class
					String line = writeFeatures( annotation, relation );
					if( ! line.isEmpty() ){
						line += "COREF";
						writer.println( line );
						positiveRelation++;
						//for negative class
						generateNegativeRelation( corpus, annotation, relation, writer);
					}

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



	private void generateNegativeRelation(Corpus corpus, Annotation annotation, Relation relation, PrintWriter writer) {
		int negativeRelationToGenerate = AncorToDemocrat.randomNumber(1, 3);
		List<Unit> unitList = annotation.getUnit();
		for(int turn = 0; turn < negativeRelationToGenerate; turn++){
			boolean done = false;
			int attempt = 0;
			while( ! done ){
				int unitIdRandom = AncorToDemocrat.randomNumber(0, unitList.size() - 1 );
				if( ! unitList.contains( unitIdRandom ) ){
					//negative relation generated
					if( ! unitList.get( unitIdRandom ).getFeature( "REF" ).equals( relation.getFeature( "REF" ) ) ){
						//the unit is not in the chain of relation
						// we create a new relation between the unit and preElement of the relation
						Unit unit = (Unit) relation.getElement( annotation );
						Relation newRelation = new Relation();
						newRelation.addUnit( unit );
						newRelation.addUnit( unitList.get( unitIdRandom ) );
						
						CalculateFeature calculateFeature = new CalculateFeature( corpus );
						calculateFeature.calculateFeatureOnRelation(annotation, newRelation);
						
						//then calculate feature of the new relation
						String line = writeFeatures( annotation, newRelation );
						if( ! line.isEmpty() ){
							line += "NOT_COREF";
							writer.println( line );
							negativeRelation++;
						}
						done = true;
					}
				}
				if( attempt > 10 ){
					//safe case
					break;
				}
				attempt++;
			}
		}
	}


	@Override
	public void run() {
		this.work( );
		logger.info("[" + this.corpus.getName() + "] arff file writed");
		logger.info("COREF: "+positiveRelation);
		logger.info("NOT-COREF: "+negativeRelation);
	}





}
