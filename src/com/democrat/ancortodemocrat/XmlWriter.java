package com.democrat.ancortodemocrat;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class XmlWriter {


	public static void writeXml(Class clas, String xmlFileName){
		try {

			File file = new File( xmlFileName );
			JAXBContext jaxbContext = JAXBContext.newInstance( clas );
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(clas, file);
			//jaxbMarshaller.marshal(clas, System.out);

		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
