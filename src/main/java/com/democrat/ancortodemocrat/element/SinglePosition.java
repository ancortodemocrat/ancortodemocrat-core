package com.democrat.ancortodemocrat.element;

import javax.xml.bind.annotation.XmlAttribute;

//@XmlAccessorType(XmlAccessType.FIELD)

public class SinglePosition {

    private Long index;

    public SinglePosition(long l) {
    	this.index = l;
	}

	/**
     * Gets the value of the index property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    @XmlAttribute
    public Long getIndex() {
        return index;
    }

    /**
     * Sets the value of the index property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setIndex(Long value) {
        this.index = value;
    }

}
