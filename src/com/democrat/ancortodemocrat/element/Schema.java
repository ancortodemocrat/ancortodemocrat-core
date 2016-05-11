package com.democrat.ancortodemocrat.element;

import javax.xml.bind.annotation.XmlElement;

import org.apache.log4j.Logger;

public class Schema extends Unit{
	
	private static Logger logger = Logger.getLogger(Schema.class);

	private PositioningSchema positioning;
	
	
	@XmlElement(name="positioning")
	public PositioningSchema getPositioningSchema() {
		return this.positioning;
	}
	public void setPositioningSchema(PositioningSchema positioning) {
		this.positioning = positioning;
	}
	

}
