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
	
	public String getContentFromUnit( Annotation annotation, Unit unit ){
		if( unit instanceof Schema){
			//TODO what to do in this case ?
			//return all unit of the schema ?
			return new String("");
		}else{
			return this.getContent().substring( unit.getStart( annotation ), unit.getEnd( annotation ) );
		}
	}

	
	
}
