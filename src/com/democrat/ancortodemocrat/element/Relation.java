package com.democrat.ancortodemocrat.element;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

//@XmlAccessorType(XmlAccessType.FIELD)


@XmlRootElement(name="relation")
public class Relation {

    private MetadataUnit metadata;
    private Characterisation characterisation;
    private PositioningRelation positioning;
    private String id;

    /**
     * Gets the value of the metadata property.
     * 
     * @return
     *     possible object is
     *     {@link Annotations.Relation.Metadata }
     *     
     */
    public MetadataUnit getMetadataUnit() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link Annotations.Relation.Metadata }
     *     
     */
    public void setMetadataUnit(MetadataUnit value) {
        this.metadata = value;
    }

    /**
     * Gets the value of the characterisation property.
     * 
     * @return
     *     possible object is
     *     {@link Annotations.Relation.Characterisation }
     *     
     */
    public Characterisation getCharacterisation() {
        return characterisation;
    }

    /**
     * Sets the value of the characterisation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Annotations.Relation.Characterisation }
     *     
     */
    public void setCharacterisation(Characterisation value) {
        this.characterisation = value;
    }

    /**
     * Gets the value of the positioning property.
     * 
     * @return
     *     possible object is
     *     {@link Annotations.Relation.Positioning }
     *     
     */
    public PositioningRelation getPositioning() {
        return positioning;
    }

    /**
     * Sets the value of the positioning property.
     * 
     * @param value
     *     allowed object is
     *     {@link Annotations.Relation.Positioning }
     *     
     */
    public void setPositioning(PositioningRelation value) {
        this.positioning = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }
    
    
    /**
     * Return the unit where point the relation 
     * @return
     */
    public Unit getPreUnit( Annotation annotation ){
    	PositioningRelation position = this.getPositioning();
    	Unit lastUnit = null;
    	if(position != null){
        	lastUnit = position.getTerm().get(0).getUnit( annotation );
    		for(int t = 1; t < position.getTerm().size(); t++){
    			Unit currentUnit = position.getTerm().get(t).getUnit( annotation );
    			PositioningUnit positionUnit = currentUnit.getPositioning();
    			if(positionUnit.getStart().getSinglePosition().getIndex() < lastUnit.getPositioning().getStart().getSinglePosition().getIndex()){
    				//we compare the position in the file (text source) of the two units, if the current is upper than the last
    				//so we switch
    				lastUnit = currentUnit;
    			}
    		}
    	}
    	return lastUnit;
    }
    
    /**
     * Return the current unit annoted
     * @return
     */
    public Unit getUnit( Annotation annotation ){
    	PositioningRelation position = this.getPositioning();
    	Unit lastUnit = null;
    	if(position != null){
        	lastUnit = position.getTerm().get(0).getUnit( annotation );
    		for(int t = 1; t < position.getTerm().size(); t++){
    			Unit currentUnit = position.getTerm().get(t).getUnit( annotation );
    			PositioningUnit positionUnit = currentUnit.getPositioning();
    			if(positionUnit.getStart().getSinglePosition().getIndex() > lastUnit.getPositioning().getStart().getSinglePosition().getIndex()){
    				//we compare the position in the file (text source) of the two units, if the current is lower than the last
    				//so we switch
    				lastUnit = currentUnit;
    			}
    		}
    	}
    	return lastUnit;
    }
    
    /**
     * work only in first mention
     * if you are in chain, use {@link Cluster.class}
     * return the pre relation
     * if return null, this relation is the first
     * @param annotation
     * @return
     */
    public Relation getPreRelation( Annotation annotation ){
    	long position = this.getUnit( annotation ).getPositioning().getStart().getSinglePosition().getIndex();
    	String idNewUnit = this.getPreUnit( annotation ).getId();
    	
    	Relation relationWanted = null;
    	
    	//the pre relation is the relation who have one term:
    	//term id == idNewUnit
    	//and her position is the lower than this one (the unit)
    	List<Relation> relations = annotation.getRelation();
    	for(int r = 0; r < relations.size(); r++){
    		if( relations.get( r ).getPreUnit( annotation ).getId().equals( idNewUnit )){
    			//termid == idNewunit
    			//these two units have the same parent (first mention)
    			long currentPosition = relations.get( r ).getUnit( annotation ).getPositioning().getStart().getSinglePosition().getIndex();
    			if(relationWanted == null && position - currentPosition > 0){
    				relationWanted = relations.get( r );
    			}else if(position - currentPosition > 0 &&
    				position - currentPosition < relationWanted.getUnit( annotation ).getPositioning().getStart().getSinglePosition().getIndex() ){
    				//the more closer and just before, not after
    				relationWanted = relations.get( r );
    			}
    		}
    		
    	}
    	return relationWanted;
    }
    
  
    
    public boolean containsUnit(Unit unit){
    	PositioningRelation position = this.getPositioning();
    	if(position != null){
    		if(unit.getId() == position.getTerm().get( 0 ).getId() ||
    				unit.getId() == position.getTerm().get( 1 ).getId() ){
    			//this relation refer this unit
    			return true;
    		}
    	}
    	return false;
    	
    }

}