package com.democrat.ancortodemocrat.element;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class PositioningSchema {
	
	private List<EmbeddedUnit> embeddedUnits;

	@XmlElement(name="embedded-unit")
	public List<EmbeddedUnit> getEmbeddedUnits() {
		return embeddedUnits;
	}

	public void setEmbeddedUnits(List<EmbeddedUnit> embeddedUnits) {
		this.embeddedUnits = embeddedUnits;
	}
	
	

}
