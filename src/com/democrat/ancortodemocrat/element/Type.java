package com.democrat.ancortodemocrat.element;

import javax.xml.bind.annotation.XmlValue;

public class Type {
	
	private String value;
	
	public Type(){
		
	}

	public Type(String str) {
		this.value = str;
	}

	@XmlValue
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	

}
