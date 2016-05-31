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
			ConversionInSet.toSetFromChain( annotation );
		}


		logger.info("[" + corpus.getName() +"] features calculated !");
		corpus.setDone( true );


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
			

			if(element.getId().equals(preElement.getId() )){
				logger.debug("YOLO" +element.getId());
				logger.debug("other: "+relation.getOtherElement(annotation, element));
				logger.debug("elementPOS "+((Unit) element).getStart(annotation) );
				logger.debug("otherElement "+(((Unit) relation.getOtherElement(annotation, element)).getStart(annotation)));
			}
			
			element.setFeature( "previous", text.getContentFromUnit( annotation , (Unit) preElement ) );
			preElement.setFeature("next", text.getContentFromUnit( annotation , (Unit) element ) );
			
			if( ((Unit) preElement).isNew( annotation ) ){
				//case where the unit hasn't a previous element
				preElement.setFeature("previous", "^");
			}
			//if the unit has only one relation, so he's the last
			//or a associative mention
			if( annotation.getRelationContaining( (Unit) element ).size() == 1 ){
				element.setFeature("next", "$");
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
						unitList.get( u ).setFeature("spk", turnList.get( t ).getSpeaker() );
					}
				}
				
			}
		}

	}

	@Override
	public void run() {
		this.corpus.loadAnnotation();
		this.corpus.loadText();
		this.work();
		this.corpus.export();

	}

}
