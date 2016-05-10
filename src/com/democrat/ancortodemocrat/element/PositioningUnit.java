package com.democrat.ancortodemocrat.element;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


//@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"start",
		"end"
})
@XmlRootElement(name="positioning")
public class PositioningUnit {

	@XmlElement(required = true)
	protected Start start;
	@XmlElement(required = true)
	protected End end;

	/**
	 * Gets the value of the start property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Start }
	 *     
	 */
	public Start getStart() {
		return start;
	}

	/**
	 * Sets the value of the start property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link Start }
	 *     
	 */
	public void setStart(Start value) {
		this.start = value;
	}

	/**
	 * Gets the value of the end property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link End }
	 *     
	 */
	public End getEnd() {
		return end;
	}

	/**
	 * Sets the value of the end property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link End }
	 *     
	 */
	public void setEnd(End value) {
		this.end = value;
	}

}
