package com.democrat.ancortodemocrat.element;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="embedded-unit")
public class EmbeddedUnit {
	
	private String id;

	@XmlElement
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	

}
