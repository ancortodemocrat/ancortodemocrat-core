package com.democrat.ancortodemocrat.element;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a list of mentions for one subject
 * based one the field NEW
 * the first one is/should be the NEW unit
 */
public class Cluster {

	private List<Relation> relations;

	/**
	 * init a cluster with the FIRST relation 
	 * @param relation
	 */
	public Cluster( Relation relation ){
		this.relations = new ArrayList<Relation>();
		this.relations.add( relation );
	}

	public Cluster( List<Relation> relations ){
		this.relations = relations;
	}

	public List<Cluster> findFromAnnotation( Annotation annotation ){
		List<Cluster> clusters = new ArrayList<Cluster>();

		//we found every new (field) to determine the first relation 
		//this one will be the first element of the cluster
		for(Unit unit : annotation.getUnit()){
			if( unit.isNew( annotation ) ){
				//we search the first relation, the closer of unit
				//we iterate every relation containing termID == unitID
				List<Relation> relations = annotation.getRelation();
				Relation relationMoreCloser = null;
				for(int r = 0; r < relations.size(); r++){
					if(relations.get( r ).containsUnit( unit )){
						//relation with the unit concerned
						Element currentElement = relations.get( r ).getElement( annotation );
						if( relationMoreCloser == null){
							relationMoreCloser = relations.get( r );
						}else if(currentElement instanceof Unit){
							Element elementOfRelation = relationMoreCloser.getElement( annotation );
							if(((Unit) currentElement).getStart( annotation ) < 
									((Unit) elementOfRelation).getStart( annotation ) ){
								//if this relation is before (in txt source) than the last founded
								//switched
								relationMoreCloser = relations.get( r );
							}
						}
					}
				}
				//first relation founded if relationMoreCloser not null
				if(relationMoreCloser != null){
					clusters.add( new Cluster( relationMoreCloser ) );
				}
			}
		}

		return clusters;
	}

	public List<Relation> getRelation() {
		return relations;
	}

	public void setRelations(List<Relation> relations) {
		this.relations = relations;
	}



}
