package com.democrat.ancortodemocrat;

import java.io.InputStream;

import javax.xml.bind.JAXB;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.PositioningUnit;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Unit;

public class XmlLoader {


	/**
	 * Load all anotation from xml file (.aa Glozz) 
	 * @param xmlFile
	 * @return
	 */
	public static Annotation loadAnnotationFromFile(String xmlFile) {

		InputStream xmlStream = AncorToDemocrat.class.getClassLoader().getResourceAsStream(xmlFile);

		return JAXB.unmarshal(xmlStream, Annotation.class); 

	}

}
