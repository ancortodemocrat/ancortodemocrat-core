package com.democrat.ancor.speech;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="Turn")
public class Turn {
	
	private String speaker;
	private float startTime;
	private float endTime;
	private Sync sync;
	private String content;
	private List<Who> whoList;
	
	
	
	@XmlAttribute
	public String getSpeaker() {
		return speaker;
	}
	public void setSpeaker(String speaker) {
		this.speaker = speaker;
	}
	public float getStartTime() {
		return startTime;
	}

	@XmlAttribute
	public void setStartTime(float startTime) {
		this.startTime = startTime;
	}
	
	@XmlAttribute
	public float getEndTime() {
		return endTime;
	}
	public void setEndTime(float endTime) {
		this.endTime = endTime;
	}
	
	@XmlValue
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}