package com.democrat.ancortodemocrat;

import java.util.ArrayList;
import java.util.List;

import javax.management.relation.RelationService;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Cluster;
import com.democrat.ancortodemocrat.element.Relation;

public class ConversionWorker {
	
	private static Logger logger = Logger.getLogger(ConversionWorker.class);
	
	private List<Annotation> annotations;
	private Annotation currentAnnotation;
	
	public ConversionWorker( Annotation annotation ){
		this.annotations = new ArrayList<Annotation>();
		this.annotations.add(annotation);
		this.work();
	}
	
	public ConversionWorker( List<Annotation> annotations ){
		this.annotations = annotations;
		this.work();
	}
	
	public void work(){
		for( int a = 0; a < this.annotations.size(); a++ ){
			logger.info("File converted: "+a+"/"+this.annotations.size());
			Annotation annotation = this.annotations.get(a);
			this.currentAnnotation = annotation;
			this.convertRelationToChain( annotation.getRelation() );
			this.convertFeature( annotation.getRelation() );
		}
	}
	
	/**
	 * Take a list of relation annoted in first mention, 
	 * and convert it in chain
	 * @param relation
	 */
	private List<Relation> convertRelationToChain( Cluster cluster ){
		List<Relation> newRelations = new ArrayList<Relation>();
		List<Relation> relations = cluster.getRelation();
		
		for(int r = 0; r < relations.size(); r++){
			Relation relation = relations.get( r );
			//e.g 
			//we test if (isNew, relation between these two unit, relation between pre unit)
			if( relation.getPreUnit( this.currentAnnotation ).isNew() ){
				//(YES, -, -)
				//we don't care the type doesnt change
				relations.add( relation );
			}else if( relation)
		}
		
		return newRelations;
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
	private void convertFeature( List<Relation> relations ){
		
	}
	

}
