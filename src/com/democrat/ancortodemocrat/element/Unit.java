package com.democrat.ancortodemocrat.element;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.log4j.Logger;

//@XmlAccessorType(XmlAccessType.FIELD)
public class Unit {
	

	private static Logger logger = Logger.getLogger(Annotation.class);

	private MetadataUnit metadata;
	private Characterisation characterisation;
	private PositioningUnit positioning;
	private String id;

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
	 * Gets the value of the characterisation property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Characterisation }
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
	
	public boolean isNew(){
		String value = getFeature("NEW");
		if(value != null && value.equalsIgnoreCase( "yes") ){
			return true;
		}
		return false;
	}

	public String getFeature(String featureName){
		Characterisation charact = this.getCharacterisation();
		List<Feature> features = charact.getFeatureSet().getFeature();
		for(int f = 0; f < features.size(); f++){
			if(features.get( f ).getName().equalsIgnoreCase( featureName )){
				return features.get( f ).getValue();
			}
		}
		return null;
	}


}
