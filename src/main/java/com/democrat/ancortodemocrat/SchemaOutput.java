package com.democrat.ancortodemocrat;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import com.democrat.ancortodemocrat.element.Annotation;

/**
 * Export xml file
 * @author Alexis Puret
 * @deprecated
 */
public class SchemaOutput extends SchemaOutputResolver{
	
	/**
	 * Actually: current folder
	 */
	public final static String location = ".";

	@Override
	public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
		// TODO Auto-generated method stub
		return new StreamResult(new File(location, suggestedFileName));
	}
	
	public static void generate(){
		try {
			JAXBContext context = JAXBContext.newInstance(Annotation.class);
			context.generateSchema(new SchemaOutput());
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	

}
