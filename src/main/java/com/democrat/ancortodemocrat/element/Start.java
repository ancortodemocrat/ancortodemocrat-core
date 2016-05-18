package com.democrat.ancortodemocrat.element;

//@XmlAccessorType(XmlAccessType.FIELD)

public class Start {

	private SinglePosition singlePosition;
	
	public Start(){
		
	}

    public Start(long l) {
    	this.singlePosition = new SinglePosition( l );
	}

	/**
     * Gets the value of the singlePosition property.
     * 
     * @return
     *     possible object is
     *     {@link SinglePosition }
     *     
     */
    public SinglePosition getSinglePosition() {
        return singlePosition;
    }

    /**
     * Sets the value of the singlePosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link SinglePosition }
     *     
     */
    public void setSinglePosition(SinglePosition value) {
        this.singlePosition = value;
    }
}