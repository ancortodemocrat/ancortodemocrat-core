package com.democrat.ancortodemocrat.element;

import javax.xml.bind.annotation.XmlElement;


//@XmlAccessorType(XmlAccessType.FIELD)

import javax.xml.bind.annotation.XmlRootElement;

import com.democrat.ancortodemocrat.feature.FeatureSet;

@XmlRootElement(name = "characterisation")
public class Characterisation {

    private Type type;
    private FeatureSet featureSet;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @XmlElement(name="type")
    public Type getType() {
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
    public void setType(Type value) {
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
    @XmlElement(name="featureSet")
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
