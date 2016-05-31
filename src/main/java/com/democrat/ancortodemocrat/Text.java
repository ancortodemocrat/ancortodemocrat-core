package com.democrat.ancortodemocrat;

import java.io.StringReader;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.democrat.ancor.speech.Trans;
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

	
	/**
	 * convert the text with xml attribute to speech 
	 * @return
	 */
	public Trans toTrans(){
		int index = this.content.indexOf("<Trans");

		return JAXB.unmarshal(new StringReader( this.content.substring(index, this.content.length() ) ), Trans.class);
	}
	
	
}
