package com.democrat.ancor.speech;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="Sync")
public class Sync {
	
	private float time;
	
	
	@XmlAttribute
	public float getTime() {
		return time;
	}
	public void setTime(float time) {
		this.time = time;
	}
	
	
	

}
