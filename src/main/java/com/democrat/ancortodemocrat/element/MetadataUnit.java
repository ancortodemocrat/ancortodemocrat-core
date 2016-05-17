package com.democrat.ancortodemocrat.element;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement(name="metadata")
public class MetadataUnit {


    private String author;
    private long creationDate;
    private String lastModifier;
    private long lastModificationDate;

    /**
     * Gets the value of the author property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthor(String value) {
        this.author = value;
    }

    /**
     * Gets the value of the creationDate property.
     * 
     */
    @XmlElement(name="creation-date")
    public long getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the value of the creationDate property.
     * 
     */
    public void setCreationDate(long value) {
        this.creationDate = value;
    }

    /**
     * Gets the value of the lastModifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastModifier() {
        return lastModifier;
    }

    /**
     * Sets the value of the lastModifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastModifier(String value) {
        this.lastModifier = value;
    }

    /**
     * Gets the value of the lastModificationDate property.
     * 
     */
    public long getLastModificationDate() {
        return lastModificationDate;
    }

    /**
     * Sets the value of the lastModificationDate property.
     * 
     */
    public void setLastModificationDate(long value) {
        this.lastModificationDate = value;
    }

}
