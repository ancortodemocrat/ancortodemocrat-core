package com.democrat.ancortodemocrat;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Write to an xml file
 * @author Alexis Puret
 */
public class XmlWriter {

	/**
	 * Write object to xml file
	 * @param obj object to write to xml
	 * @param xmlFileName output xml file name
	 */
	public static void writeXml(Object obj, String xmlFileName){
		try {

			File file = new File( xmlFileName );
			JAXBContext jaxbContext = JAXBContext.newInstance( obj.getClass() );
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(obj, file);

		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
