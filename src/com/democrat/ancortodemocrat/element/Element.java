package com.democrat.ancortodemocrat.element;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public class Element {

	
	private String id;


	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Element [id=" + id + "]";
	}
	

	@XmlAttribute(name="id")
	public String getId() {
		return this.id;
	}
	
	
}
