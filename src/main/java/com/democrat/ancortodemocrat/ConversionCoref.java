package com.democrat.ancortodemocrat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Maëlle Brassier
 * @deprecated Not Used in project
 */
public class ConversionCoref implements Runnable {

	private String outputPath;
	private String inputPath;
	private static Logger logger = Logger.getLogger(MultiClassifier.class);
	private List<String> fileOuput = new ArrayList<String>();
	String instance;
	String prediction;
	String expected;

	public ConversionCoref(String inputPath, String outputPath) {
		this.outputPath = outputPath;
		this.inputPath = inputPath;
	}

	public void writeInstance() throws IOException {
		PrintWriter writer = null;

		String fileName;
		DateFormat shortDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		fileName = shortDateFormat.format(new Date());
		logger.info(fileName);
		fileName = fileName.replace(" ", "_");
		fileName = fileName.replace("/", "_");
		fileName = fileName.replace(":", "H");
		ArrayList<String> values_coref = new ArrayList<String>();
	

		String value_coref = "";
		String[] tab = null;
		
		try {
			writer = new PrintWriter(this.outputPath + fileName + ".arff", "UTF-8");
			this.fileOuput.add(this.outputPath + fileName + ".arff");

			InputStream ips;
			ips = new FileInputStream(inputPath);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;

			while ((line = br.readLine()) != null) {
				// logger.info("affichage de la ligne : " +line);
				// logger.info("compteur : " + compteur);

						tab = line.split(",");
						instance = tab[0];
						prediction = tab[2];
						value_coref = instance + "," + prediction;
						values_coref.add(value_coref);
					
				
			}
			br.close();

			for (int i = 0; i < values_coref.size(); i++) {
				
				
				String[] splitc = values_coref.get(i).split(",");
		

			
					writer.print(splitc[0]);
					
					if (splitc[1].equals("1:COREF")) {
						writer.println(", COREF");
					}
					else{
						writer.println(", NOT_COREF");
					}

					
			}

			logger.info("Fichier " + fileName + " enregistré dans : " + outputPath);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private void work() throws IOException {
		this.writeInstance();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			this.work();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
