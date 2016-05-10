package com.democrat.ancortodemocrat.element;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

//@XmlAccessorType(XmlAccessType.FIELD)
public class Unit {

	private MetadataUnit metadata;
	private Characterisation characterisation;
	private PositioningUnit positioning;
	private String id;

	/**
	 * Gets the value of the metadata property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Annotations.Unit.Metadata }
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
	 *     {@link Annotations.Unit.Metadata }
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
	 *     {@link Annotations.Unit.Characterisation }
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
	 *     {@link Annotations.Unit.Characterisation }
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
	 *     {@link Annotations.Unit.Positioning }
	 *     
	 */
	public PositioningUnit getPositioningUnit() {
		return positioning;
	}

	/**
	 * Sets the value of the positioning property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link Annotations.Unit.Positioning }
	 *     
	 */
	public void setPositioningUnit(PositioningUnit value) {
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



}
