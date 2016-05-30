package com.democrat.ancortodemocrat.feature;

import java.util.List;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.ConversionInSet;
import com.democrat.ancortodemocrat.ConversionWorker;
import com.democrat.ancortodemocrat.Corpus;
import com.democrat.ancortodemocrat.Text;
import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Unit;

public class CalculateFeature implements Runnable {
	
	private static Logger logger = Logger.getLogger(CalculateFeature.class);
	
	private Corpus corpus;

	public CalculateFeature( Corpus corpus ){
		this.corpus = corpus;
	}
	
	
	private void work(){
		

		logger.info("Start converting: [" + corpus.getName() +"]");


		for( int a = 0; a < this.corpus.getAnnotation().size(); a++ ){

			logger.info("[" + corpus.getName() +"] Calculate new features for : "+(a + 1)+"/"+this.corpus.getAnnotation().size() + " : " + this.corpus.getAnnotation().get( a ).getFileName() );
			Annotation annotation = this.corpus.getAnnotation().get( a );
			this.calculateNewFeature( annotation );
			ConversionInSet.toSetFromChain( annotation );
		}


		logger.info("[" + corpus.getName() +"] done !");
		corpus.setDone( true );
		
		
	}

	private void calculateNewFeature( Annotation annotation ) {
		
		List<Unit> unitList = annotation.getUnit();
		
		for(int u = 0; u < unitList.size(); u++){

			calculatePreviousToken( annotation, unitList.get( u ) );
			calculateNextToken( annotation, unitList.get( u ) );
			calculateSpeaker( annotation, unitList.get( u ) );
		}
	}
	
	
	private void calculatePreviousToken( Annotation annotation, Unit unit ){
		List<Relation> relationList = annotation.getRelationContaining( unit );
		Text text = this.corpus.getText( annotation.getFileName() );
		
		if( relationList.size() == 1 ){
			if( relationList.get( 0 ).getElement( annotation ).equals( unit ) ){
				//the previous is the pre element of the relation
				unit.setFeature("previous", text.getContentFromUnit( annotation, (Unit) relationList.get( 0 ).getPreElement( annotation ) ) );
			}else{
				//to find the previous, we should take the pre-relation
				//and take the element
				//or we are in first unit of the coreference, so we put $
				
				if( unit.isNew( annotation ) ){
					//first unit
					unit.setFeature("previous", "^");
				}else{
					unit.setFeature("previous", text.getContentFromUnit( annotation, (Unit) relationList.get( 0 ).getPreRelation( annotation ).getElement( annotation ) ) );
				}
			}
		}
		//TODO else, unit has many relation, (association, or we are in first mention )
		
	}
	
	private void calculateNextToken( Annotation annotation, Unit unit ){
		List<Relation> relationList = annotation.getRelationContaining( unit );
		Text text = this.corpus.getText( annotation.getFileName() );
		
		if( relationList.size() == 1 ){
			if( relationList.get( 0 ).getElement( annotation ).equals( unit ) ){
				//the next is the pre element of the relation
				//unit.setFeature("previous", text.getContentFromUnit( annotation, (Unit) relationList.get( 0 ).getPreElement( annotation ) ) );
				//recalculate the relation
			}else{
				//to find the next unit, we should take the element of the relation
				unit.setFeature("previous", text.getContentFromUnit( annotation, (Unit) relationList.get( 0 ).getElement( annotation ) ) );
			}
		}else{
			//range every relation (remove the associatives relations)
			
		}
		//TODO else, unit has many relation, (association, or we are in first mention )
		
	}
	

	private void calculateSpeaker( Annotation annotation, Unit unit ){
		
	}

	@Override
	public void run() {
		this.work();
		this.corpus.export();
		
	}

}
