package com.democrat.ancortodemocrat;

import java.util.ArrayList;
import java.util.List;

import javax.management.relation.RelationService;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Cluster;
import com.democrat.ancortodemocrat.element.PositioningRelation;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Unit;

public class ConversionWorker {
	
	private static Logger logger = Logger.getLogger(ConversionWorker.class);
	
	private List<Annotation> annotations;
	
	public ConversionWorker( Annotation annotation ){
		this.annotations = new ArrayList<Annotation>();
		this.annotations.add(annotation);
	}
	
	public ConversionWorker( List<Annotation> annotations ){
		this.annotations = annotations;
	}
	
	public void work(){
		for( int a = 0; a < this.annotations.size(); a++ ){
			Annotation annotation = this.annotations.get(a);
			this.convertRelationToChain( annotation );
			this.convertFeature( annotation );
			logger.info("File converted: "+(a + 1)+"/"+this.annotations.size());
		}
	}
	
	/**
	 * Take a list of relation annoted in first mention, 
	 * and convert it in chain
	 * @param relation
	 */
	private void convertRelationToChain( Annotation annotation ){
		List<Relation> newRelations = new ArrayList<Relation>();
		List<Relation> relations = annotation.getRelation();
		
		for(int r = 0; r < relations.size(); r++){
			Relation relation = relations.get( r );
			Relation newRelation = Relation.newInstance( relation );
			
			PositioningRelation positioning = relation.getPositioning();
			if( positioning != null){
				if(positioning.getTerm().size() > 1){
					//we are checking the two pointers in the current relation
					//if one point on the first element (new = yes) 
					//so replace this point with the unit the more closer before
					//so the unit in fist mention of the pre relation of this unit
					Relation preRelation = relation.getPreRelation( annotation );
					if( preRelation == null){
						//first relation
						newRelations.add( newRelation );
						continue;
					}
					Unit unit = preRelation.getUnit( annotation );
					if( positioning.getTerm().get( 0 ).getUnit( annotation ).isNew() ){
						newRelation.getPositioning().getTerm().get( 0 ).setId( unit.getId()  );
					}else if( positioning.getTerm().get( 1 ).getUnit( annotation ).isNew() ){
						newRelation.getPositioning().getTerm().get( 1 ).setId( unit.getId()  );
					}			
					
				}
			}
			
			newRelations.add( newRelation );
		}
		
		annotation.setRelation( newRelations );
	}
	
	/**
	 * recalculates every relation with their term 
	 * to determine these features
	 * <ul>
	 * <li>NB</li>
	 * <li>GENRE</li>
	 * </ul>
	 * @param relations
	 */
	private void convertFeature( Annotation annotation ){
		
	}
	

}
