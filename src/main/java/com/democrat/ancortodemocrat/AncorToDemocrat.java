package com.democrat.ancortodemocrat;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.xml.sax.SAXException;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.feature.CalculateFeature;
import com.democrat.ancortodemocrat.treetagger.TreeTagger;


public class AncorToDemocrat {

	private static Logger logger = Logger.getLogger(AncorToDemocrat.class);
	public static FileManager fileManager;
	public static TreeTagger treeTagger;

	public static void main(String[] args) {
		
		//configure logger
		DOMConfigurator.configure("cfg/log4j-config.xml");


		System.out.println("=====================================================================================================================");
		System.out.println("=====================================================================================================================");

		//cfg TreeTagger
		treeTagger = new TreeTagger();

		fileManager = new FileManager();
		
		
		if( args.length > 1){
			if( args[0].equalsIgnoreCase( "generateFeature" )){
				for(int a = 1; a < args.length; a++){
					generateFeature( args[ a ] );
				}
				return;
			}
		}else{
			convertCorpus();
		}



		//trying generate xsd schema and verify one xml .aa from glozz
		//SchemaOutput.generate();
		/**
		JAXBContext context = null;
		try {
			context = JAXBContext.newInstance(Annotation.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean toast = xmlValidation(context, "test.xml");
		System.out.println("==>"+toast);
		 **/

	}

	/**
	 * 
	 */
	public static void generateFeature(String corpusPath){
		Corpus corpus = new Corpus( corpusPath );
		CalculateFeature calculate = new CalculateFeature( corpus );
		Thread th = new Thread( calculate );
		th.start();
	}
	
	public static void convertCorpus(){

		//loading corpus
		List<String> corpusPath = fileManager.loadPathFile();
		List<Corpus> corpusList = new ArrayList<Corpus>();
		for(String path : corpusPath){
			corpusList.add( new Corpus( path ));
		}

		//loading annotation of corpus
		for(Corpus corpus : corpusList){
			logger.info("Loading annotation on: " + corpus.getName() );
			corpus.loadAnnotation();
			logger.info("Loading text on: " + corpus.getName() );
			corpus.loadText();

		}

		List<ConversionWorker> conversionWorkerList = new ArrayList<ConversionWorker>();

		//conversion each corpus
		for(Corpus corpus : corpusList){
			ConversionWorker conversionWorker = new ConversionWorker( corpus );
			conversionWorkerList.add( conversionWorker );
			conversionWorker.start();
		}
	}

	/**
	 * test if a xml can be used
	 * @param name
	 * @return
	 */
	public static boolean xmlValidation(JAXBContext context, String xmlFile){
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

		//load xml file
		InputStream xmlStream = AncorToDemocrat.class.getClassLoader().getResourceAsStream(xmlFile);
		Schema schema = null;
		try {
			schema = schemaFactory.newSchema(new File(".", "schema1.xsd"));


		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = context.createUnmarshaller();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		unmarshaller.setSchema(schema);
		try {
			unmarshaller.unmarshal(xmlStream);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
