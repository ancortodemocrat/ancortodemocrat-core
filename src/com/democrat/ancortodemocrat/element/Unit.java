package com.democrat.ancortodemocrat.element;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "metadata",
    "characterisation",
    "positioning"
})
public static class Unit {

    @XmlElement(required = true)
    protected Annotations.Unit.Metadata metadata;
    @XmlElement(required = true)
    protected Annotations.Unit.Characterisation characterisation;
    @XmlElement(required = true)
    protected Annotations.Unit.Positioning positioning;
    @XmlAttribute(name = "id")
    protected String id;

    /**
     * Gets the value of the metadata property.
     * 
     * @return
     *     possible object is
     *     {@link Annotations.Unit.Metadata }
     *     
     */
    public Annotations.Unit.Metadata getMetadata() {
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
    public void setMetadata(Annotations.Unit.Metadata value) {
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
    public Annotations.Unit.Characterisation getCharacterisation() {
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
    public void setCharacterisation(Annotations.Unit.Characterisation value) {
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
    public Annotations.Unit.Positioning getPositioning() {
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
    public void setPositioning(Annotations.Unit.Positioning value) {
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
