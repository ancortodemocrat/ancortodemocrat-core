package com.democrat.ancortodemocrat.element;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="metadata")
public class MetadataAnnotation {

    private String corpusHashcode;


    /**
     * Gets the value of the corpusHashcode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @XmlAttribute(name="corpusHashcode")
    public String getCorpusHashcode() {
        return corpusHashcode;
    }

    /**
     * Sets the value of the corpusHashcode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCorpusHashcode(String value) {
        this.corpusHashcode = value;
    }

}