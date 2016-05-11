package com.democrat.ancortodemocrat.element;

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
    

}