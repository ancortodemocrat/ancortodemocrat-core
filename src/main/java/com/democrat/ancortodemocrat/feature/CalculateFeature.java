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
	private String outputPath;

	public CalculateFeature( Corpus corpus, String outputPath ){
		this.corpus = corpus;
		this.outputPath = outputPath;
	}


	private void work(){
		logger.debug("Start calculate feature: [" + corpus.getName() +"]");


		//TODO: May be parallelized
		for( int a = 0; a < this.corpus.getAnnotation().size(); a++ ){

			logger.trace("[" + corpus.getName() +"] Calculate new features for : "+(a + 1)+"/"+this.corpus.getAnnotation().size() + " : " + this.corpus.getAnnotation().get( a ).getFileName() );
			Annotation annotation = this.corpus.getAnnotation().get( a );
			this.calculateNewFeature( annotation );
			calculateFeatureOnRelation( annotation );
		}


		logger.info("[" + corpus.getName() +"] features calculated !");
		corpus.setDone( true );


	}

	public void calculateFeatureOnRelation( Annotation annotation, Relation relation ){
		Text text = this.corpus.getText( annotation.getFileName() );
		if(text == null){
			return;
		}
		Element element = relation.getElement( annotation );
		Element preElement = relation.getPreElement( annotation );
		if( element instanceof Unit && preElement instanceof Unit ){
			String mention = text.getContentFromUnit( annotation, (Unit) element );
			String preMention = text.getContentFromUnit( annotation, (Unit) preElement );


			//ID_NOMBRE
			if(relation.getFeature( "NOMBRE" ).equals( "UNK") ){
				String nbElement = element.getFeature( "NB" );
				String nbPreElement = preElement.getFeature( "NB" );
				if(nbElement.equals( nbPreElement ) ){
					relation.setFeature("NOMBRE", "YES");
				}else{
					relation.setFeature("NOMBRE", "NO");
				}
			}
			//ID_GENRE
			if(relation.getFeature( "GENRE" ).equals( "UNK") ){
				String genreElement = element.getFeature( "GENRE" );
				String genrePreElement = preElement.getFeature( "GENRE" );
				if(genreElement.equals( genrePreElement ) ){
					relation.setFeature("GENRE", "YES");
				}else{
					relation.setFeature("GENRE", "NO");
				}
			}

			//ID_TYPE
			String typeElement = element.getCharacterisation().getType().getValue();
			String typePreElement = preElement.getCharacterisation().getType().getValue();
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
			//if one token of the smallest is include in the biggest
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


			//id_new
			if( ((Unit) element).isNew( annotation ) && ((Unit) preElement).isNew( annotation ) ){
				relation.setFeature("ID_NEW", "YES");
			}else{
				relation.setFeature("ID_NEW", "NO");			
			}

			//embedded
			int posAstart = ((Unit) preElement).getStart( annotation );
			int posBstart = ((Unit) element).getStart( annotation );
			int posAend = ((Unit) preElement).getEnd(annotation);
			if( posBstart > posAstart && posBstart < posAend){
				relation.setFeature("EMBEDDED", "YES");
			}else{
				relation.setFeature("EMBEDDED", "NO");				
			}

			//id_previous
			//logger.debug("previousTOKEN: "+preElement.getFeature( "previous_token" ) + " - " + element.getFeature("previous_token")); 
			if( preElement.getFeature("previous_token").equalsIgnoreCase("null") ||
					element.getFeature("previous_token").equalsIgnoreCase("null") ){
				relation.setFeature("ID_PREVIOUS", "NA");
			}
			else if( preElement.getFeature("previous_token").equalsIgnoreCase( element.getFeature( "previous_token" ) ) ){
				relation.setFeature("ID_PREVIOUS", "YES");
			}else{
				relation.setFeature("ID_PREVIOUS", "NO");				
			}


			//id_next
			if( preElement.getFeature("next_token").equalsIgnoreCase("null" ) || 
					element.getFeature("next_token").equalsIgnoreCase("null" ) ){
				relation.setFeature("ID_NEXT", "NA");
			}else if( preElement.getFeature("next_token").equalsIgnoreCase( element.getFeature( "next_token" ) ) ){
				relation.setFeature("ID_NEXT", "YES");
			}else{
				relation.setFeature("ID_NEXT", "NO");				
			}

			
			if( preElement.getFeature( "EN" ).equalsIgnoreCase( element.getFeature( "EN" ) ) ){
				relation.setFeature( "id_en", "YES");
			}else{
				relation.setFeature( "id_en", "NO");
			}

		}
	}

	/**
	 * get the next token of the unit, just the token before the mention
	 * same thing for next token
	 * @param annotation
	 */
	private void calculateToken( Annotation annotation ){
		Text text = this.corpus.getText( annotation.getFileName() );
		if( text == null){
			logger.error("FILE MISSING "+annotation.getFileName() );
			return;
		}
		//calculate for every unit the previous and next token
		List<Unit> unitList = annotation.getUnit();
		for( int u = 0; u < unitList.size(); u++){
			if( ! unitList.get( u ).getId().contains("TXT_") ){
				unitList.get( u ).setFeature("previous_token", this.getPreToken(annotation, text, unitList.get( u ) ) );
				unitList.get( u ).setFeature("next_token", this.getNextToken(annotation, text, unitList.get( u ) ) );
			}
		}

	}

	/**
	 * Retourne le token/mot juste après l'unité indiqué
	 * @param annotation annnotation contenant l'unité
	 * @param text Text correspondant à l'unité de cette annotation
	 * @param unit unité de correspondance
	 * @return
	 */
	private String getNextToken( Annotation annotation, Text text, Unit unit ){
		if(text == null){
			return null;
		}
		Turn turn = text.getTurnCorresponding(annotation, unit);
		if( turn != null ){
			String contentTurn = turn.getContent();

			int positionStart = unit.getEnd( annotation );
			String contentTurnSplitted = text.getContent().substring( positionStart, text.getContent().length() );
			if( contentTurnSplitted.length() > 1 ){
				return splitMention( contentTurnSplitted )[0];
			}else{
				//next turn
				Turn nextTurn = text.getNextTurn( turn );
				if( nextTurn != null){
					String[] contentNextSplitted = splitMention( nextTurn.getContent() );
					return contentNextSplitted[ 0 ];
				}
			}
		}		
		return null;
	}

	
	/**
	 * Retourne le token/mot juste après l'unité indiqué
	 * @param annotation annnotation contenant l'unité
	 * @param text Text correspondant à l'unité de cette annotation
	 * @param unit unité de correspondance
	 * @return
	 */
	private String getPreToken( Annotation annotation, Text text, Unit unit ){
		if(text == null){
			return null;
		}

		Turn turn = text.getTurnCorresponding(annotation, unit);
		String contentUnit = text.getContentFromUnit(annotation, unit);
		if( turn != null ){
			int posMention = turn.getContent().indexOf( contentUnit );
			if( posMention == 0 ){
				//mention start the turn, check the previous turn
				Turn previousTurn = text.getPreviousTurn( turn );
				if( previousTurn == null ){
					return "";
				}
				String[] contentSplitted = splitMention( previousTurn.getContent() );
				return contentSplitted[ contentSplitted.length - 1 ];
			}else{
				//mention not start the turn, get the pre token
				String contentWithoutMention = turn.getContent().substring(0, posMention );
				String[] contentSplitted = splitMention( contentWithoutMention );
				//just take the last element of the contentSplitted
				return contentSplitted[ contentSplitted.length - 1 ];
			}
		}
		//logger.debug("PRE TOKEN NULL");
		return null;
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
		calculateToken( annotation );
		calculateSpeaker( annotation );
	}


	private void calculatePreviousNextToken( Annotation annotation, Relation relation ){
		Text text = this.corpus.getText( annotation.getFileName() );
		if(text == null){
			logger.error("MISSING FILE: "+ annotation.getFileName() + ".ac");
			return;
		}

		Element element = relation.getElement( annotation );
		Element preElement = relation.getPreElement( annotation );
		if( element != null && preElement != null ){
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

	}


	/**
	 * set on each unit, the spk id
	 * comparing with Turn of mention
	 * @param annotation
	 */
	private void calculateSpeaker( Annotation annotation ){
		Text text = this.corpus.getText( annotation.getFileName() );
		if(text == null){
			return;
		}
		Trans trans = text.toTrans();
		List<Unit> unitList = annotation.getUnit();
		if(trans == null){
			return;
		}
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
		if( this.corpus.getAnnotation().size() == 0 ){
			this.corpus.loadAnnotation();
			this.corpus.loadText();
		}
		this.work();
		this.corpus.export( outputPath );
	}

}
