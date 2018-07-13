package com.democrat.ancortodemocrat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXB;

import com.democrat.ancortodemocrat.element.Annotation;

/**
 * Loading Xml
 * @author Alexis Puret
 */
public class XmlLoader {


	/**
	 * Load all anotation from xml file (.aa Glozz) 
	 * @param xmlFileName xml file to load
	 * @return Annotation containing xml file data
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
