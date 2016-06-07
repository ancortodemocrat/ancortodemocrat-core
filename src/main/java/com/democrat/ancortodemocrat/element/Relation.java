package com.democrat.ancortodemocrat.element;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.feature.Feature;
import com.democrat.ancortodemocrat.feature.FeatureSet;
import com.democrat.ancortodemocrat.positioning.PositioningRelation;

//@XmlAccessorType(XmlAccessType.FIELD)


@XmlRootElement(name="relation")
public class Relation extends Element {


	private static Logger logger = Logger.getLogger(Relation.class);


	private MetadataUnit metadata;
	private PositioningRelation positioning = new PositioningRelation();

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


		relation.characterisation = new Characterisation();
		relation.characterisation.setType( new Type( new String( anotherRelation.getCharacterisation().getType().getValue() ) ) );

		List<Feature> features = anotherRelation.getCharacterisation().getFeatureSet().getFeature();
		FeatureSet featureSet = new FeatureSet();
		List<Feature> newFeatures = new ArrayList<Feature>();
		for(int f = 0; f < features.size(); f++){
			newFeatures.add( new Feature( new String( features.get( f ).getName()), new String( features.get( f ).getValue() ) ));
		}
		featureSet.setFeature( newFeatures );
		relation.characterisation.setFeatureSet( featureSet ); 

		PositioningRelation positioning = new PositioningRelation();
		List<Term> terms = anotherRelation.getPositioning().getTerm();

		positioning.setTerm( new ArrayList<Term>());
		for(int t = 0; t < terms.size(); t++){
			positioning.getTerm().add(new Term( new String(terms.get( t ).getId() ) ) );
		}

		relation.positioning = positioning;

		relation.setId( anotherRelation.getId() );
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
	 *     {@link Characterisation }
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
	 * Return the unit where point the relation 
	 * @return
	 */
	public Element getPreElement( Annotation annotation ){
		PositioningRelation position = this.getPositioning();
		if(position != null){
			if(position.getTerm().size() > 1){
				Element firstElement = position.getTerm().get( 0 ).getElement( annotation );
				Element secondElement = position.getTerm().get( 1 ).getElement( annotation );
				if(firstElement instanceof Relation){
					//TODO relation --> relation
					//logger.debug(" relation TO relation idRelation " + this.getId());
					return null;
				}else if(firstElement instanceof Unit && secondElement instanceof Unit){
					Unit firstUnit = (Unit) firstElement;
					Unit secondUnit = (Unit) secondElement;
					if( firstUnit.isNew( annotation ) && secondUnit.isNew( annotation) ){
						//compare two positions
						if( firstUnit.getStart( annotation ) > secondUnit.getStart( annotation ) ){
							return secondUnit;
						}else{
							return firstUnit;
						}
					}
					if( firstUnit.isNew( annotation ) ){
						return firstUnit;
					}else if( secondUnit.isNew( annotation ) ){
						return secondUnit;
					}else{
						//compare two positions
						if( firstUnit.getStart( annotation ) > secondUnit.getStart( annotation ) ){
							return secondUnit;
						}else{
							return firstUnit;
						}
					}
				}
			}
		}
		logger.debug("element null on relation: "+this.getId());
		return null;
	}

	/**
	 * work only in first mention
	 * Return the current unit annoted
	 * @return
	 */
	public Element getElement( Annotation annotation ){
		PositioningRelation position = this.getPositioning();
		if(position != null){
			if(position.getTerm().size() > 1){
				Element firstElement = position.getTerm().get( 0 ).getElement( annotation );
				Element secondElement = position.getTerm().get( 1 ).getElement( annotation );
				if(firstElement instanceof Relation){
					//TODO relation --> relation
					//logger.debug(" relation TO relation idRelation " + this.getId());
					return null;
				}else if(firstElement instanceof Unit && secondElement instanceof Unit){
					Unit firstUnit = (Unit) firstElement;
					Unit secondUnit = (Unit) secondElement;
					if( firstUnit.isNew( annotation ) && secondUnit.isNew( annotation ) ){
						//compare two positions
						if( firstUnit.getStart( annotation ) > secondUnit.getStart( annotation ) ){
							return firstUnit;
						}else{
							return secondUnit;
						}
					}
					if( firstUnit.isNew( annotation ) ){
						return secondUnit;
					}else if( secondUnit.isNew( annotation ) ){
						return firstUnit;
					}else{
						//compare two positions
						if( firstUnit.getStart( annotation ) > secondUnit.getStart( annotation ) ){
							return firstUnit;
						}else{
							return secondUnit;
						}
					}
				}
			}
		}
		logger.debug("element null on relation: "+this.getId());
		return null;
	}


	/**
	 * work only in first mention
	 * return the pre relation, it's the relation the more closer than this one
	 * just before
	 * if return null, this relation is the first
	 * <strong>be careful, this method ignore the associative mention</strong>
	 * @param annotation
	 * @return
	 */
	public Relation getPreRelation( Annotation annotation ){
		Element element = this.getElement( annotation );
		if(element == null){
			return null;
		}
		long position = 0;
		if(element instanceof Relation){
			//TODO relation to relation check
			return (Relation) element;
		}else{ //UNIT OR SCHEMA
			position = ((Unit) element).getStart( annotation );
		}

		Element preElement = this.getPreElement( annotation ) ;
		if(preElement instanceof Relation){
			//TODO check relation to relation
			return (Relation) preElement;
		}
		//after that the preElement is a Unit
		String idNewUnit = preElement.getId();
		Relation relationWanted = null;

		//the pre relation is the relation who have one term:
		//term id == idNewUnit
		//and her position is the lower than this one (the unit)
		//and is not a associative relation
		List<Relation> relations = annotation.getRelation();
		for(int r = 0; r < relations.size(); r++){

			if(relations.get( r ).equals( this ) || relations.get( r ).getElement( annotation ) == null){
				continue;
			}
			Element currentPreElement = relations.get( r ).getPreElement( annotation );
			if( currentPreElement instanceof Relation ){
				//TODO manage: relation --> relation
				continue;
			}
			if( relations.get( r ).getCharacterisation().getType().getValue().contains( "ASSOC" )){
				continue;
			}
			//here currentPrelement can be casted to Unit
			if( currentPreElement.getId().equals( idNewUnit )){
				//termid == idNewunit
				//these two units have the same parent (first mention)
				Unit currentUnit = (Unit) relations.get( r ).getElement( annotation );
				long currentPosition = (currentUnit).getStart( annotation );
				if(relationWanted == null && position - currentPosition > 0){
					relationWanted = relations.get( r );
				}else if(position - currentPosition > 0 &&
						(position - currentPosition) < 
						(position - ((Unit) relationWanted.getElement( annotation )).getStart( annotation )) ){
					//the more closer and just before, not after
					relationWanted = relations.get( r );
				}
			}

		}
		return relationWanted;
	}



	public boolean containsUnit(Annotation annotation, Unit unit){
		PositioningRelation position = this.getPositioning();
		if(position != null){
			if(unit.getId().equalsIgnoreCase( position.getTerm().get( 0 ).getId() ) ||
					unit.getId().equalsIgnoreCase( position.getTerm().get( 1 ).getId() ) ){
				//this relation refer this unit
				return true;
			}
		}
		return false;

	}

	@Override
	public String toString() {
		String str = "Relation [id=" + this.getId() + "]";
		str += System.lineSeparator();
		str += "    "+this.metadata;
		str += System.lineSeparator();
		str += "    "+this.characterisation;
		str += System.lineSeparator();
		str += "    "+this.getPositioning();
		str += System.lineSeparator();

		return str;
	}




	/**
	 * return the other element of the relation
	 * @param element
	 * @return
	 */
	public Element getOtherElement( Annotation annotation, Element element ){
		List<Term> termList = this.getPositioning().getTerm();
		if(termList.size() == 2){
			if( termList.get( 0 ).getId().equals( element.getId() )){
				return annotation.getElementById( termList.get( 1 ).getId() );
			}else{
				return annotation.getElementById( termList.get( 0 ).getId() );
			}
		}
		return null;
	}

	/**
	 * test on each element of the releation if one
	 * contains the FEATURE NEW
	 * @param annotation
	 * @return boolean
	 */
	public boolean containsNew( Annotation annotation ){

		List<Term> list = this.getPositioning().getTerm();
		for(Term term : list){
			if(term.getElement( annotation ) instanceof Unit){
				if(((Unit) term.getElement( annotation )).isNew( annotation )){
					return true;
				}
			}
		}
		return false;
	}
	
	public void addUnit( Unit unit ){
		if(this.positioning.getTerm().size() >= 2){
			logger.debug("Relation cannot have more two units");
			return;
		}else{
			this.positioning.getTerm().add(new Term( unit.getId() ) );			
		}
	}

}