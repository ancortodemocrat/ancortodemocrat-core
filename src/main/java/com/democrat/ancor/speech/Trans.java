package com.democrat.ancor.speech;

import javax.xml.bind.annotation.XmlElement;

public class Trans {
	
	
	private Episode episode;

	@XmlElement(name="Episode")
	public Episode getEpisode() {
		return episode;
	}

	public void setEpisode(Episode episode) {
		this.episode = episode;
	}
	
	
	

}
