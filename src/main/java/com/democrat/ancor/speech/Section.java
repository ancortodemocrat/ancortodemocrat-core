package com.democrat.ancor.speech;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Section")
public class Section {
	
	private List<Turn> turn;

	@XmlElement(name="Turn")
	public List<Turn> getTurn() {
		if(turn == null){
			turn = new ArrayList<Turn>();
		}
		return turn;
	}

	public void setTurn(List<Turn> turn) {
		this.turn = turn;
	}
	
	

}
