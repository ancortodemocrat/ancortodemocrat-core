package com.democrat.ancortodemocrat;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Element;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Unit;

public class ConversionToArff implements Runnable{

	private Corpus corpus;
	private final static String arffAttribute = "@RELATION coreference\n"+
			"@ATTRIBUTE m1_type {N, PR, UNK, NULL}\n"+
			"@ATTRIBUTE m2_type {N, PR, UNK, NULL}\n"+
			"@ATTRIBUTE m1_def {INDEF, EXPL, DEF_SPLE, DEF_DEM, UNK}\n"+
			"@ATTRIBUTE m2_def {INDEF, EXPL, DEF_SPLE, DEF_DEM, UNK}\n"+
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
			"@ATTRIBUTE id_genre {YES, NO, NA}\n" +
			"@ATTRIBUTE id_nombre {YES, NO, NA}\n" +
			"@ATTRIBUTE id_spk {YES, NO, NA}\n" +
			"@ATTRIBUTE distance_mention real\n" +
			"@ATTRIBUTE distance_turn real\n" +
			"@ATTRIBUTE distance_word real\n" + 
			"@ATTRIBUTE distance_char real\n" +
			"@ATTRIBUTE class {COREF, NOT_COREF}\n" +
			"@DATA";


	public ConversionToArff(Corpus corpus){


		this.corpus = corpus;

	}

	public static void convert(Corpus corpus){

		PrintWriter writer = null;
		try {
			writer = new PrintWriter("generated/arff/" + corpus.getName() + "_coreference.arff", "UTF-8");
			writer.println( arffAttribute );
			for(Annotation annotation : corpus.getAnnotation()){

				for( Relation relation : annotation.getRelation() ){
					String line = "";

					Element element = relation.getElement( annotation );
					Element preElement = relation.getPreElement( annotation );
					if( element instanceof Unit && preElement instanceof Unit ){
						//m1_type
						line += preElement.getCharacterisation().getType().getValue();
						line += " ";
						//m2_type
						line += element.getCharacterisation().getType().getValue();
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
						
						//CLASS !!!!
						line += "COREF";
						

						writer.println( line );
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


	@Override
	public void run() {
		// TODO Auto-generated method stub

	}





}
