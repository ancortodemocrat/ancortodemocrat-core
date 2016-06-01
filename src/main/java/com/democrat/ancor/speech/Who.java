package com.democrat.ancor.speech;

import javax.xml.bind.annotation.XmlAttribute;

public class Who {
	
	private int id;
	private String content;
	
	
	@XmlAttribute
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	

}
