package com.democrat.ancortodemocrat;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Schema;
import com.democrat.ancortodemocrat.element.Unit;

public class Text {
	
	
	private String content;
	private String fileName;
	
	public Text(String content){
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * Get the content of one unit,
	 * check from the start and end positionning of the unit
	 * @param annotation useful to read the unit position
	 * @param unit the unit you want the text
	 * @return
	 */
	public String getContentFromUnit( Annotation annotation, Unit unit ){
		if( unit instanceof Schema){
			return getContentFromUnit( annotation, ((Schema) unit).getUnitWhereFeatureNotNull( annotation ) );
		}else{
			return this.getContent().substring( unit.getStart( annotation ), unit.getEnd( annotation ) );
		}
	}

	
	
}
