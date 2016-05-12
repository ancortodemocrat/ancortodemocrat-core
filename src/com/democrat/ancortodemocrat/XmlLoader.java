package com.democrat.ancortodemocrat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXB;

import com.democrat.ancortodemocrat.element.Annotation;

public class XmlLoader {


	/**
	 * Load all anotation from xml file (.aa Glozz) 
	 * @param xmlFile
	 * @return
	 */
	public static Annotation loadAnnotationFromFile(String xmlFileName) {
		InputStream xmlStream = null;
		try {
			xmlStream = new FileInputStream( new File( xmlFileName ) );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return JAXB.unmarshal(xmlStream, Annotation.class); 

	}

}
