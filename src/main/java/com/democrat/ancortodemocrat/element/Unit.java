package com.democrat.ancortodemocrat.element;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

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

	
	public long getStart(  Annotation annotation  ){
		return this.getPositioning().getStart().getSinglePosition().getIndex();
	}
	
	

}
