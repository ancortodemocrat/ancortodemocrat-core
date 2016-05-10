package com.democrat.ancortodemocrat.element;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

//@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "type",
    "featureSet"
})
public class Characterisation {

    @XmlElement(required = true)
    protected String type;
    @XmlElement(required = true)
    protected FeatureSet featureSet;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the featureSet property.
     * 
     * @return
     *     possible object is
     *     {@link Annotations.Relation.Characterisation.FeatureSet }
     *     
     */
    public FeatureSet getFeatureSet() {
        return featureSet;
    }

    /**
     * Sets the value of the featureSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link Annotations.Relation.Characterisation.FeatureSet }
     *     
     */
    public void setFeatureSet(FeatureSet value) {
        this.featureSet = value;
    }
}
