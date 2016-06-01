package com.democrat.ancortodemocrat.feature;

import java.util.List;

import org.apache.log4j.Logger;

import com.democrat.ancor.speech.Trans;
import com.democrat.ancor.speech.Turn;
import com.democrat.ancortodemocrat.ConversionInSet;
import com.democrat.ancortodemocrat.ConversionWorker;
import com.democrat.ancortodemocrat.Corpus;
import com.democrat.ancortodemocrat.Text;
import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Element;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Schema;
import com.democrat.ancortodemocrat.element.Unit;

public class CalculateFeature implements Runnable {

	private static Logger logger = Logger.getLogger(CalculateFeature.class);

	private Corpus corpus;

	public CalculateFeature( Corpus corpus ){
		this.corpus = corpus;
	}


	private void work(){
		logger.info("Start calculate feature: [" + corpus.getName() +"]");


		for( int a = 0; a < this.corpus.getAnnotation().size(); a++ ){

			logger.info("[" + corpus.getName() +"] Calculate new features for : "+(a + 1)+"/"+this.corpus.getAnnotation().size() + " : " + this.corpus.getAnnotation().get( a ).getFileName() );
			Annotation annotation = this.corpus.getAnnotation().get( a );
			this.calculateNewFeature( annotation );
			calculateFeatureOnRelation( annotation );
		}


		logger.info("[" + corpus.getName() +"] features calculated !");
		corpus.setDone( true );


	}

	public void calculateFeatureOnRelation( Annotation annotation ){
		List<Relation> relationList = annotation.getRelation();
		Text text = this.corpus.getText( annotation.getFileName() );
		for(Relation relation : relationList){
			Element element = relation.getElement( annotation );
			Element preElement = relation.getPreElement( annotation );
			if( element instanceof Unit && preElement instanceof Unit ){
				String mention = text.getContentFromUnit( annotation, (Unit) element );
				String preMention = text.getContentFromUnit( annotation, (Unit) preElement );

				//ID_FORM
				if(mention.equalsIgnoreCase( preMention ) ){
					relation.setFeature("ID_FORM", "YES");
				}else{
					relation.setFeature("ID_FORM", "NO");
				}

				//ID_SUBFORM
				if( mention.length() > preMention.length() ){
					if( mention.contains( preMention ) ){
						relation.setFeature("ID_SUBFORM", "YES");
					}else{
						relation.setFeature("ID_SUBFORM", "NO");
					}
				}else{
					if( preMention.contains( mention ) ){
						relation.setFeature("ID_SUBFORM", "YES");
					}else{
						relation.setFeature("ID_SUBFORM", "NO");
					}
				}
				
				//ID_SPK
				String spk = element.getFeature("SPK");
				String preSpk = preElement.getFeature("SPK");
				
				if( element instanceof Schema){
					spk = ((Schema)element).getFeature( annotation, "SPK" );
				}
				if( preElement instanceof Schema){
					spk = ((Schema)preElement).getFeature( annotation, "SPK" );
				}
				
				if( spk.equals( preSpk ) ){
					relation.setFeature("ID_SPK", "YES");
				}else{
					relation.setFeature("ID_SPK", "NO");
				}

				String[] mentionSplitted = this.splitMention( mention );
				String[] preMentionSplitted = this.splitMention( preMention );
				
				//COM_RATE
				int countSimilarity = 0;
				for(int m = 0; m < mentionSplitted.length; m++){
					
					for(int p = 0; p < preMentionSplitted.length; p++){
						if(mentionSplitted[ m ].equalsIgnoreCase( preMentionSplitted[ p ] ) ){
							countSimilarity++;
						}
					}
				}
				if(mentionSplitted.length > preMentionSplitted.length){
					float rate = countSimilarity / mentionSplitted.length;
					relation.setFeature("COM_RATE", rate + "");
				}else{
					float rate = countSimilarity / preMentionSplitted.length;
					relation.setFeature("COM_RATE", rate + "");					
				}
				

			}
		}
	}

	private void calculateNewFeature( Annotation annotation ) {

		List<Relation> relationList = annotation.getRelation();

		for(int r = 0; r < relationList.size(); r++){

			calculatePreviousNextToken( annotation, relationList.get( r ) );
			calculateSpeaker( annotation );
		}
	}


	private void calculatePreviousNextToken( Annotation annotation, Relation relation ){
		Text text = this.corpus.getText( annotation.getFileName() );

		Element element = relation.getElement( annotation );
		Element preElement = relation.getPreElement( annotation );
		if( element instanceof Unit && preElement instanceof Unit ){
			element.setFeature( "PREVIOUS", text.getContentFromUnit( annotation , (Unit) preElement ) );
			preElement.setFeature("NEXT", text.getContentFromUnit( annotation , (Unit) element ) );

			if( ((Unit) preElement).isNew( annotation ) ){
				//case where the unit hasn't a previous element
				preElement.setFeature("PREVIOUS", "^");
			}
			//if the unit has only one relation, so he's the last
			//or a associative mention
			if( annotation.getRelationContaining( (Unit) element ).size() == 1 ){
				element.setFeature("NEXT", "$");
			}
		}

	}


	/**
	 * 
	 * @param annotation
	 */
	private void calculateSpeaker( Annotation annotation ){
		Text text = this.corpus.getText( annotation.getFileName() );
		Trans trans = text.toTrans();
		List<Unit> unitList = annotation.getUnit();
		List<Turn> turnList = trans.getEpisode().getSection().getTurn();

		for(int u = 0 ; u < unitList.size(); u++){
			if( unitList.get( u ) instanceof Unit ){
				String contentOfUnit = text.getContentFromUnit( annotation, unitList.get( u ) );
				//find turn corresponding
				for(int t = 0; t < turnList.size(); t++){
					if( turnList.get( t ).getContent().contains( contentOfUnit ) ){
						//found
						//just check the position is good
						int startOfUnit = unitList.get( u ).getStart( annotation );
						int endOfUnit = unitList.get( u ).getStart( annotation );
						int indexOfTurn = text.indexOf( turnList.get( t ) );
						if(  indexOfTurn <= startOfUnit &&
								indexOfTurn + turnList.get( t ).getContent().length() >= endOfUnit ){
							unitList.get( u ).setFeature("SPK", turnList.get( t ).getSpeaker() );
						}
					}
				}

			}
		}

	}
	


	private String[] splitMention( String sentence ){
		sentence = sentence.replace(", ", " , ");
		sentence = sentence.replace(".", " .");
		sentence = sentence.replace("  ", " ");
		return sentence.split(" ");
	}

	@Override
	public void run() {
		this.corpus.loadAnnotation();
		this.corpus.loadText();
		this.work();
		this.corpus.export();

	}

}
