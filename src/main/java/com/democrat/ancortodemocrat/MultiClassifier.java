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

public class MultiClassifier implements Runnable {

	private String outputPath;
	private String inputPath;
	private static Logger logger = Logger.getLogger(MultiClassifier.class);
	private List<String> fileOuput = new ArrayList<String>();
	String instance;
	String prediction;
	String expected;

	public MultiClassifier(String inputPath, String outputPath) {
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
		ArrayList<String> values_directe = new ArrayList<String>();
		ArrayList<String> values_indirecte = new ArrayList<String>();
		ArrayList<String> values_anaphore = new ArrayList<String>();
		ArrayList<String> values_assoc = new ArrayList<String>();

		String value_directe = "";
		String value_indirecte = "";
		String value_anaphore = "";
		String value_assoc = "";

		String[] tab = null;
		try {
			writer = new PrintWriter(this.outputPath + fileName + ".arff", "UTF-8");
			this.fileOuput.add(this.outputPath + fileName + ".arff");

			InputStream ips;
			ips = new FileInputStream(inputPath);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;
			int compteur = 0;

			while ((line = br.readLine()) != null) {
				// logger.info("affichage de la ligne : " +line);
				// logger.info("compteur : " + compteur);

				if (line.equals("")) {
					compteur++;
				} else {
					if (compteur == 0) {
						tab = line.split(",");
						instance = tab[0];
						prediction = tab[2];
						value_directe = instance + "," + prediction;
						values_directe.add(value_directe);
					}

					if (compteur == 1) {
						tab = line.split(",");
						instance = tab[0];
						prediction = tab[2];
						value_indirecte = instance + "," + prediction;
						values_indirecte.add(value_indirecte);
					}
					if (compteur == 2) {
						tab = line.split(",");
						instance = tab[0];
						prediction = tab[2];
						value_anaphore = instance + "," + prediction;
						values_anaphore.add(value_anaphore);
					}
					
//					if (compteur == 3) {
//						tab = line.split(",");
//						instance = tab[0];
//						prediction = tab[2];
//						value_assoc = instance + "," + prediction;
//						values_assoc.add(value_assoc);
//					}
				}
				
			}
			br.close();

			for (int i = 0; i < values_directe.size(); i++) {
				// logger.info("tableau directe" +values_directe.get(i));
				int compteur_directe = 0;
				int compteur_indirecte = 0;
				int compteur_anaphore = 0;
//				int compteur_assoc = 0;
				int compteur_total = 0;
				
				String[] splitd = values_directe.get(i).split(",");
				String[] spliti = values_indirecte.get(i).split(",");
				String[] splita = values_anaphore.get(i).split(",");
//				String[] splitas = values_assoc.get(i).split(",");

//				
//				logger.info("d:" +splitd[1]);
//				logger.info("i:" +spliti[1]);
//				logger.info("a:" +splita[1]);
//				logger.info(splitas[1]);
				
				logger.info("instance : " +splitd[0]);
//				logger.info("instance as : " +splitas[0]);

				if (splitd[0].equals(spliti[0]) && spliti[0].equals(splita[0]) && splitd[0].equals(splita[0]))  {
					
					writer.print(splitd[0]);
					
					if (splitd[1].equals("1:DIRECTE")) {
						compteur_directe++;
					}
					if (spliti[1].equals("1:INDIRECTE")) {
						compteur_indirecte++;
					}
					if (splita[1].equals("1:ANAPHORE")) {
						compteur_anaphore++;
					}
//					if (splitas[1].equals("1:ASSOC")) {
//						compteur_assoc++;
//					}
					
					compteur_total = compteur_directe + compteur_indirecte + compteur_anaphore;
					logger.info("compteur total : " +compteur_total);
//					logger.info("compteur assoc : " +compteur_assoc);


					if (compteur_total > 0) {
//						writer.println(" ; " + compteur_directe + ", " + compteur_indirecte + ", " + compteur_anaphore + ", COREF");
						writer.println(", COREF");

					} else if (compteur_total < 1) {
//						writer.println(" ; " + compteur_directe + ", " + compteur_indirecte + ", " + compteur_anaphore + ", NOT_COREF");
						writer.println(", NOT_COREF");
					}
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
