package com.democrat.ancortodemocrat.element;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

//@XmlAccessorType(XmlAccessType.FIELD)


@XmlRootElement(name="relation")
public class Relation {


	private static Logger logger = Logger.getLogger(Relation.class);


	private MetadataUnit metadata;
	private Characterisation characterisation;
	private PositioningRelation positioning;
	private String id;
	
	public Relation(){
		
	}
	
	/**
	 * copy of anotherRelation
	 * with unmutable object
	 * @param anotherRelation
	 * @return
	 */
	public static Relation newInstance (Relation anotherRelation){
		Relation relation = new Relation();
		
		MetadataUnit metadata = anotherRelation.getMetadata();
		relation.metadata = metadata;
		
		Characterisation characterisation = anotherRelation.getCharacterisation();
		//TODO load unmutable characterisation
		relation.characterisation = characterisation;
		
		PositioningRelation positioning = new PositioningRelation();
		List<Term> terms = anotherRelation.getPositioning().getTerm();

		positioning.setTerm( new ArrayList<Term>());
		for(int t = 0; t < terms.size(); t++){
			positioning.getTerm().add(new Term( new String(terms.get( t ).getId() ) ) );
		}
		
		relation.positioning = positioning;
		
		relation.id = anotherRelation.id;
		return relation;
	}

	/**
	 * Gets the value of the metadata property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Annotations.Relation.Metadata }
	 *     
	 */
	@XmlElement(name="metadata")
	public MetadataUnit getMetadata() {
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
	public void setMetadata(MetadataUnit value) {
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
	@XmlElement(name="characterisation")
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
	@XmlElement(name="positioning")
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
	@XmlAttribute
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
		if(position != null){
			if(position.getTerm().size() > 1){
				Unit unit = position.getTerm().get( 1 ).getUnit( annotation );
				if(unit.isNew() ){
					//this one is good
					return unit;
				}else{
					return position.getTerm().get( 0 ).getUnit( annotation );
				}
			}
		}
		return null;
	}

	/**
	 * work only in first mention
	 * Return the current unit annoted
	 * @return
	 */
	public Unit getUnit( Annotation annotation ){
		PositioningRelation position = this.getPositioning();
		if(position != null){
			if(position.getTerm().size() > 1){
				Unit unit = position.getTerm().get( 0 ).getUnit( annotation );
				if(unit.isNew() ){
					//the other is the good
					return position.getTerm().get( 1 ).getUnit( annotation );
				}else{
					return unit;
				}
			}
		}
		return null;
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
		logger.debug("==> position: "+position);
		List<Relation> relations = annotation.getRelation();
		for(int r = 0; r < relations.size(); r++){

			if(relationWanted != null){
				logger.debug("comparaison: "+relationWanted.getUnit( annotation ).getPositioning().getStart().getSinglePosition().getIndex());

				long currentPosition = relations.get( r ).getUnit( annotation ).getPositioning().getStart().getSinglePosition().getIndex();
				logger.debug("currentPosition "+currentPosition);
			}
			if(relations.get( r ).equals( this )){
				logger.debug("next");
				continue;
			}

			if( relations.get( r ).getPreUnit( annotation ).getId().equals( idNewUnit )){
				//termid == idNewunit
				//these two units have the same parent (first mention)
				if( relations.get( r ).getUnit( annotation ) instanceof Schema){
					logger.debug("yoloy");
					//TODO manage with schema, first element/ NEW
					continue;
				}
				long currentPosition = relations.get( r ).getUnit( annotation ).getPositioning().getStart().getSinglePosition().getIndex();
				if(relationWanted == null && position - currentPosition > 0){
					relationWanted = relations.get( r );
					logger.debug(":___plus proche: "+currentPosition);
				}else if(position - currentPosition > 0 &&
						(position - currentPosition) < 
						(position - relationWanted.getUnit( annotation ).getPositioning().getStart().getSinglePosition().getIndex()) ){
					//the more closer and just before, not after
					logger.debug("plus proche: "+(currentPosition));
					relationWanted = relations.get( r );
				}
			}else{

				logger.debug("don't point new");
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

	@Override
	public String toString() {
		String str = "Relation [id=" + id + "]";
		str += System.lineSeparator();
		str += "    "+this.metadata;
		str += System.lineSeparator();
		str += "    "+this.characterisation;
		str += System.lineSeparator();
		str += "    "+this.getPositioning();
		str += System.lineSeparator();

		return str;
	}



}