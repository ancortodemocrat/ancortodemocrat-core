package com.democrat.ancortodemocrat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Element;
import com.democrat.ancortodemocrat.element.Relation;

public class ConversionInSet implements Runnable{

	
	
	/**
	 * Add element REF to each relation/mention from first mention
	 * or chain type
	 * @param annotation Annotation 
	 * @param fromFirstMention if the annotation is in first mention or not
	 */
	public ConversionInSet( Annotation annotation, boolean fromFirstMention ){
		
	}
	
	
	private void toSetFromFirstMention( Annotation annotation ){
		
		List<Relation> relationList = annotation.getRelation();
		List<String> idRef = new ArrayList<String>();
		
		for(int r = 0; r < relationList.size(); r++){
			//first unit with new element:
			//because each relation go to the first mention
			Element element = relationList.get( r ).getPreElement( annotation );
			if( ! idRef.contains( element.getId() ) ){
				//found new set ==> new ref id
				idRef.add( element.getId() );
				relationList.get( r ).setFeature( "REF", idRef.size()+"" );
			}
			//add the REF feature on each element of the relation
			relationList.get( r ).getElement( annotation ).setFeature( "REF" , idRef.size()+"" );
			relationList.get( r ).getPreElement( annotation ).setFeature( "REF" , idRef.size()+"" );
		}
		
	}
	
	private void toSetFromChain( Annotation annotation ){
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
