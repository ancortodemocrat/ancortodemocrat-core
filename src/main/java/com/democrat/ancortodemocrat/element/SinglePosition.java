package com.democrat.ancortodemocrat.element;

import javax.xml.bind.annotation.XmlAttribute;

//@XmlAccessorType(XmlAccessType.FIELD)

public class SinglePosition {

    private int index;

    public SinglePosition(int l) {
    	this.index = l;
	}
    
    public SinglePosition(){
    	
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
    public int getIndex() {
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
    public void setIndex(int value) {
        this.index = value;
    }

}
