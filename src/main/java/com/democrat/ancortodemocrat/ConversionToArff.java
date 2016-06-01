package com.democrat.ancortodemocrat;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Element;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Unit;
import com.democrat.ancortodemocrat.feature.Feature;

public class ConversionToArff implements Runnable{

	private Corpus corpus;
	private final String arffAttribute = "@RELATION coreference\n"+
			"@ATTRIBUTE m1_type {N, PR, UNK, NULL}\n"+
			"@ATTRIBUTE m2_type {N, PR, UNK, NULL}\n"+
			"@ATTRIBUTE m1_def {INDEF, EXPL, DEF_SPLE, DEF_DEM, UNK}\n"+
			"@ATTRIBUTE m2_def {INDEF, EXPL, DEF_SPLE, DEF_DEM, UNK}\n"+
			"@ATTRIBUTE m1_genre {M, F, UNK, NULL}\n"+
			"@ATTRIBUTE m2_genre {M, F, UNK, NULL}\n"+
			"@ATTRIBUTE m1_nombre {SG, PL, UNK, NULL}\n"+
			"@ATTRIBUTE m2_nombre {SG, PL, UNK, NULL}\n"+
			"@ATTRIBUTE m1_previous string\n"+
			"@ATTRIBUTE m2_previous string\n"+
			"@ATTRIBUTE m1_next string\n"+
			"@ATTRIBUTE m2_next string\n"+
			"@ATTRIBUTE m1_spk string\n"+
			"@ATTRIBUTE m2_spk string\n"+
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
			"@ATTRIBUTE class {COREF, NOT_COREF}\n" +
			"@DATA";


	public ConversionToArff(Corpus corpus){


		this.corpus = corpus;

	}

	public void work(){

		PrintWriter writer = null;
		try {
			writer = new PrintWriter("generated/arff/" + corpus.getName() + "coreference.arff", "UTF-8");
			for(Annotation annotation : corpus.getAnnotation()){

				for( Relation relation : annotation.getRelation() ){
					String line = "";

					Element element = relation.getElement( annotation );
					Element preElement = relation.getPreElement( annotation );
					if( element instanceof Unit && preElement instanceof Unit ){
						//m1_type
						line += preElement.getCharacterisation().getType().getValue();
						//m2_type
						line += element.getCharacterisation().getType().getValue();
						//m1_def
						line += preElement.getFeature( "DEF" );
						//m2_def
						line += element.getFeature( "DEF" );
						//m1_genre
						line += preElement.getFeature( "GENRE" );
						//m2_genre
						line += element.getFeature( "GENRE" );
						
						//m1_nombre
						line += preElement.getFeature( "NB" );
						//m2_nombre
						line += element.getFeature( "NB" );
						
						//m1_previous
						line += preElement.getFeature( "PREVIOUS" );
						//m2_previous
						line += element.getFeature( "PREVIOUS" );
						
						//m1_next
						line += preElement.getFeature( "NEXT" );
						//m2_next
						line += element.getFeature( "NEXT" );
						
						//m1_spk
						line += preElement.getFeature( "SPK" );
						//m2_spk
						line += element.getFeature( "SPK" );
						
						//m1_new
						line += preElement.getFeature( "NEW" );
						//m2_new
						line += element.getFeature( "NEW" );
						
						//m1_en
						line += preElement.getFeature( "EN" );
						//m2_en
						line += element.getFeature( "NEW" );
						
						

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
