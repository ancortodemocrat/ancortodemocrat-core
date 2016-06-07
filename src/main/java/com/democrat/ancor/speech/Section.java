package com.democrat.ancor.speech;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Section")
public class Section {
	
	
	@XmlElement(name="Turn")
	private List<Turn> turnList;
	
	
	public List<Turn> getTurn() {
		if(turnList == null){
			turnList = new ArrayList<Turn>();
		}
		return turnList;
	}

	public void setTurn(List<Turn> turn) {
		this.turnList = turn;
	}
	
	

}
