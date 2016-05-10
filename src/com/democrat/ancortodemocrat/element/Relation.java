package com.democrat.ancortodemocrat.element;
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "metadata",
    "characterisation",
    "positioning"
})
public static class Relation {

    @XmlElement(required = true)
    protected Annotations.Relation.Metadata metadata;
    @XmlElement(required = true)
    protected Annotations.Relation.Characterisation characterisation;
    @XmlElement(required = true)
    protected Annotations.Relation.Positioning positioning;
    @XmlAttribute(name = "id")
    protected String id;

    /**
     * Gets the value of the metadata property.
     * 
     * @return
     *     possible object is
     *     {@link Annotations.Relation.Metadata }
     *     
     */
    public Annotations.Relation.Metadata getMetadata() {
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
    public void setMetadata(Annotations.Relation.Metadata value) {
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
    public Annotations.Relation.Characterisation getCharacterisation() {
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
    public void setCharacterisation(Annotations.Relation.Characterisation value) {
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
    public Annotations.Relation.Positioning getPositioning() {
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
    public void setPositioning(Annotations.Relation.Positioning value) {
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