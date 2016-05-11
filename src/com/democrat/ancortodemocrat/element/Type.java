package com.democrat.ancortodemocrat.element;

import javax.xml.bind.annotation.XmlValue;

public class Type {
	
	private String value;

	@XmlValue
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	

}
