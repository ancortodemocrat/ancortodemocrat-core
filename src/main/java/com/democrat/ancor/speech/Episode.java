package com.democrat.ancor.speech;

import javax.xml.bind.annotation.XmlElement;

public class Episode {
	
	private Section section;

	@XmlElement(name="Section")
	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}
	
	
	

}
