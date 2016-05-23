package com.democrat.ancortodemocrat.positioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.democrat.ancortodemocrat.element.EmbeddedUnit;
import com.democrat.ancortodemocrat.element.End;
import com.democrat.ancortodemocrat.element.Start;

@XmlRootElement(name="positioning")
public class PositioningUnit {

	private Start start;
	private End end;
	private List<EmbeddedUnit> embeddedUnit = new ArrayList<EmbeddedUnit>();

	/**
	 * Gets the value of the start property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Start }
	 *     
	 */
	@XmlElement
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
	@XmlElement
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

	@Override
	public String toString() {
		String str = "PositioningUnit";
		
		str += System.lineSeparator();
		
		str += "    " + this.getStart();
		
		return str;
	}

	@XmlElement(name="embedded-unit")
	public List<EmbeddedUnit> getEmbeddedUnit() {
		return embeddedUnit;
	}

	public void setEmbeddedUnit(List<EmbeddedUnit> embeddedUnit) {
		this.embeddedUnit = embeddedUnit;
	}
	
	

}