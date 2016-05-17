package com.democrat.ancortodemocrat.element;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;import javax.xml.bind.annotation.XmlRootElement;

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
