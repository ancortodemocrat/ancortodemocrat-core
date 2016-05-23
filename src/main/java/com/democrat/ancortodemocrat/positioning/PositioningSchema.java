package com.democrat.ancortodemocrat.positioning;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;import javax.xml.bind.annotation.XmlRootElement;

import com.democrat.ancortodemocrat.element.EmbeddedUnit;

@XmlRootElement(name="positioning")
public class PositioningSchema {
	
	private List<EmbeddedUnit> embeddedUnit;


	@XmlElement(name="embedded-unit")
	public List<EmbeddedUnit> getEmbeddedUnit() {
		return embeddedUnit;
	}

	public void setEmbeddedUnit(List<EmbeddedUnit> embeddedUnit) {
		this.embeddedUnit = embeddedUnit;
	}
	

}
