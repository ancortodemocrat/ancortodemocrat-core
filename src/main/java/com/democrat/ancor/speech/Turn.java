package com.democrat.ancor.speech;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Turn")
public class Turn {
	
	private String speaker;
	private float startTime;
	private float endTime;
	
	
	private List<String> text;
	
	
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
	
	@XmlMixed
    public List<String> getText() {
		if(text == null){
			text = new ArrayList<String>();
		}
        return text;
    }
	
	public void setText(List<String> text){
		this.text = text;
	}
	
	public String getContent() {
		String content = "";
		for(int t = 0; t < this.getText().size(); t++){
			content += this.text.get( t );
			if( t < this.text.size() - 1){
				content += " ";
			}
		}
		return content;
	}
	
}