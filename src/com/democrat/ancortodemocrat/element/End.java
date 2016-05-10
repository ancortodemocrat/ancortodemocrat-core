package com.democrat.ancortodemocrat.element;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

//@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "singlePosition"
})
public class End {

    @XmlElement(required = true)
    protected SinglePosition singlePosition;

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