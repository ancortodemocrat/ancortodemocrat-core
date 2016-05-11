package com.democrat.ancortodemocrat;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class XmlWriter {


	public static void writeXml(Object obj, String xmlFileName){
		try {

			File file = new File( xmlFileName );
			JAXBContext jaxbContext = JAXBContext.newInstance( obj.getClass() );
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(obj, file);
			//jaxbMarshaller.marshal(obj, System.out);

		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
