package com.democrat.ancortodemocrat.element;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.positioning.PositioningUnit;

//@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="unit")
public class Unit extends Element {

	private static Logger logger = Logger.getLogger(Annotation.class);

	private MetadataUnit metadata;
	private PositioningUnit positioning;


	/**
	 * Gets the value of the metadata property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link MetadataUnit }
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
	 *     {@link MetadataUnit }
	 *     
	 */

	public void setMetadata(MetadataUnit value) {
		this.metadata = value;
	}



	/**
	 * Gets the value of the positioning property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link PositioningUnit }
	 *     
	 */
	@XmlElement(name="positioning")
	public PositioningUnit getPositioning() {
		return positioning;
	}

	/**
	 * Sets the value of the positioning property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link PositioningUnit }
	 *     
	 */
	public void setPositioning(PositioningUnit value) {
		this.positioning = value;
	}

	public boolean isNew( Annotation annotation ){
		String value = getFeature("NEW");
		if(value != null && value.equalsIgnoreCase( "yes" ) ){
			return true;
		}
		return false;
	}


	public int getStart(  Annotation annotation  ){
		return this.getPositioning().getStart().getSinglePosition().getIndex();
	}

	public int getEnd( Annotation annotation ){
		return this.getPositioning().getEnd().getSinglePosition().getIndex();
	}

	/**
	 * test if this unit is contained in one schema or not
	 * 
	 * @param annotaion
	 * @return
	 */
	public boolean isContainedInSchema(  Annotation annotation  ){
		List<Schema> schemaList = annotation.getSchema();
		for(int s = 0; s < schemaList.size(); s++){
			List<EmbeddedUnit> embeddedUnitList = schemaList.get( s ).getPositioning().getEmbeddedUnit();
			for(int e = 0; e < embeddedUnitList.size(); e++){
				if( embeddedUnitList.get( e ).getId().equalsIgnoreCase( this.getId() )){
					return true;
				}
			}
		}
		return false;
	}


	public boolean isAssociative( Annotation annotation ){
		List<Relation> relationAssociated = annotation.getRelationContaining( this );
		if( relationAssociated.size() > 0){
			for(int r = 0; r < relationAssociated.size(); r++){
				if( relationAssociated.get( r ).getCharacterisation().getType().getValue().contains( "ASSOC" )){
					return true;
				}
			}
		}
		return false;
	}

}
