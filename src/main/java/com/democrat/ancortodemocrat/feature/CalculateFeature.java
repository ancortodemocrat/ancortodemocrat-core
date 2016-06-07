package com.democrat.ancortodemocrat.feature;

import java.util.List;

import org.apache.log4j.Logger;

import com.democrat.ancor.speech.Trans;
import com.democrat.ancor.speech.Turn;
import com.democrat.ancortodemocrat.ConversionToArff;
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
	
	public void calculateFeatureOnRelation( Annotation annotation, Relation relation ){
		Text text = this.corpus.getText( annotation.getFileName() );
		Element element = relation.getElement( annotation );
		Element preElement = relation.getPreElement( annotation );
		if( element instanceof Unit && preElement instanceof Unit ){
			String mention = text.getContentFromUnit( annotation, (Unit) element );
			String preMention = text.getContentFromUnit( annotation, (Unit) preElement );

			
			//ID_TYPE
			String typeElement = element.getCharacterisation().getType().getValue();
			String typePreElement = element.getCharacterisation().getType().getValue();
			if( typeElement.equals( typePreElement ) ){
				relation.setFeature( "ID_TYPE", "YES");
			}else{
				relation.setFeature( "ID_TYPE", "NO");
			}
			
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
				preSpk = ((Schema)preElement).getFeature( annotation, "SPK" );
			}

			if( spk.equals( preSpk ) ){
				relation.setFeature("ID_SPK", "YES");
			}else{
				relation.setFeature("ID_SPK", "NO");
			}

			String[] mentionSplitted = this.splitMention( mention );
			String[] preMentionSplitted = this.splitMention( preMention );

			//COM_RATE && INCL_RATE
			float countSimilarity = 0.0F;
			for(int m = 0; m < mentionSplitted.length; m++){

				for(int p = 0; p < preMentionSplitted.length; p++){
					if(mentionSplitted[ m ].equalsIgnoreCase( preMentionSplitted[ p ] ) ){
						countSimilarity++;
					}
				}
			}
			if(mentionSplitted.length > preMentionSplitted.length){
				//Mention is the largest
				float length = mentionSplitted.length;
				float preLength = preMentionSplitted.length;
				float inclRate = countSimilarity / preLength;
				float rate = countSimilarity / length;
				relation.setFeature("COM_RATE", rate + "");
				relation.setFeature("INCL_RATE", inclRate + "");
			}else{
				//premention is the largest
				float length = preMentionSplitted.length;
				float rate = countSimilarity / length;
				float preLength = mentionSplitted.length;
				float inclRate = countSimilarity / preLength;
				relation.setFeature("COM_RATE", rate + "");	
				relation.setFeature("INCL_RATE", inclRate + "");				
			}

			//distance_word && distance_char && turn_distance
			int wordDistance = 0;
			int charDistance = 0;
			int turnDistance = 0;
			Trans trans = text.toTrans();
			List<Turn> turnList = trans.getEpisode().getSection().getTurn();
			boolean counted = false;


			for(int t = 0; t < turnList.size(); t++){
				Turn turn = turnList.get( t );

				//logger.debug("turnContent " + turn.getContent() );
				if( counted ){
					turnDistance++;
				}

				if( !counted && text.isCorresponding( annotation, turn, (Unit) preElement ) ){
					//first mention found, start count
					//logger.debug("word ===> " + text.getContentFromUnit(annotation, (Unit) preElement));

					if( ! text.isCorresponding( annotation, turn, (Unit) element ) ){
						int start = ((Unit) preElement).getEnd( annotation );
						int end = text.indexOf( turn ) + turn.getContent().length();
						wordDistance += this.splitMention( text.getContent().substring(start , end) ).length;
						charDistance += text.getContent().substring(start , end).length();
					}

					counted = true;
				}


				if( counted && text.isCorresponding( annotation, turn, (Unit) element ) ){
					//last mention found, end of counting

					if( text.isCorresponding( annotation, turn, (Unit) preElement ) ){
						//same turn for two elements
						int start = ((Unit) preElement).getEnd( annotation );
						int end = ((Unit) element).getStart( annotation );

						charDistance += end - start;
						if(charDistance > 0){
							wordDistance += this.splitMention( text.getContent().substring(start, end) ).length;

						}
					}else{
						int indexOfTurn = text.indexOf( turn );
						int indexOfUnit = ((Unit) element).getStart( annotation );
						charDistance += indexOfUnit - indexOfTurn;
						wordDistance += this.splitMention( text.getContent().substring(indexOfTurn, indexOfUnit) ).length;
					}

					break;						
				}else if( counted && ! text.isCorresponding( annotation, turn, (Unit) preElement ) ){
					//count word of the turn
					wordDistance += this.splitMention( turn.getContent() ).length;
					//logger.debug("++ "+turn.getContent() );
					//count char of the turn
					charDistance += turn.getContent().length();
				}else{
					//logger.debug("-- "+turn.getContent());
				}
			}
			relation.setFeature("DISTANCE_WORD", wordDistance + "");
			relation.setFeature("DISTANCE_CHAR", charDistance + "");
			relation.setFeature("DISTANCE_TURN", turnDistance + "");


			//ID_DEF
			String def = element.getFeature("DEF");
			String preDef = preElement.getFeature("DEF");

			if( element instanceof Schema){
				def = ((Schema)element).getFeature( annotation, "DEF" );
			}
			if( preElement instanceof Schema){
				preDef = ((Schema)preElement).getFeature( annotation, "DEF" );
			}
			if( def.equals( preDef ) ){
				relation.setFeature("ID_DEF", "YES");
			}else{
				relation.setFeature("ID_DEF", "NO");					
			}


			//distance mention
			int startRelation = ((Unit) preElement).getStart( annotation );
			int endRelation = ((Unit) element).getStart( annotation );
			List<Unit> unitList = annotation.getUnit();
			int countMention = 1;
			for(Unit unit : unitList){
				if( ! unit.getId().contains("TXT_IMPORTER") &&
						! unit.equals( preElement ) &&
						! unit.equals( element )){
					int startUnit = unit.getStart( annotation );
					if( startUnit > startRelation &&
							startUnit < endRelation){
						countMention++;
					}
				}
			}
			relation.setFeature("DISTANCE_MENTION", countMention + "");


		}
	}


	private void calculateFeatureOnRelation( Annotation annotation ){
		List<Relation> relationList = annotation.getRelation();
		for(Relation relation : relationList){
			calculateFeatureOnRelation( annotation, relation );
		}
	}

	private void calculateNewFeature( Annotation annotation ) {

		List<Relation> relationList = annotation.getRelation();

		for(int r = 0; r < relationList.size(); r++){

			calculatePreviousNextToken( annotation, relationList.get( r ) );
		}
		calculateSpeaker( annotation );
	}


	private void calculatePreviousNextToken( Annotation annotation, Relation relation ){
		Text text = this.corpus.getText( annotation.getFileName() );

		Element element = relation.getElement( annotation );
		Element preElement = relation.getPreElement( annotation );
		if( element instanceof Unit && preElement instanceof Unit ){
			
			if( ! ((Unit) element).isNew( annotation ) || ! ((Unit) preElement).isNew( annotation ) ){
				preElement.setFeature("NEXT", text.getContentFromUnit( annotation , (Unit) element ) );
			}
			element.setFeature( "PREVIOUS", text.getContentFromUnit( annotation , (Unit) preElement ) );

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
	 * set on each unit, the spk id
	 * comparing with Turn of mention
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
		if(sentence.length() > 0){
			if(sentence.charAt( 0 ) == ' '){
				sentence = sentence.substring( 1 );
			}
		}
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
		ConversionToArff conversionToArff = new ConversionToArff( this.corpus );
		Thread th = new Thread( conversionToArff );
		th.start();
	}

}
