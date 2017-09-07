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

public class ComparaisonResult implements Runnable {

	private String outputPath;
	private String inputPath;
	private static Logger logger = Logger.getLogger(MultiClassifier.class);
	private List<String> fileOuput = new ArrayList<String>();
	
	String value_de_base;
	String value_de_classif;
	String instance_de_base;
	String instance_de_classif;

	int compteur_comparaison=0;
	
	int vrai_coref;
	int faux_coref;
	int vrai_not_coref;
	int faux_not_coref;
	
	double total_coref;
	double total_not_coref;
	
	double precision_coref;
	double rappel_coref;
	double fmesure_coref;

	double precision_not_coref;
	double rappel_not_coref;
	double fmesure_not_coref;
	
	double precision_avg;
	double rappel_avg;
	double fmesure_avg;
	
	public ComparaisonResult(String inputPath, String outputPath) {
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
		ArrayList<String> values_base = new ArrayList<String>();
		ArrayList<String> values_classif = new ArrayList<String>();

		String value_base = "";
		String value_classif = "";

		String[] tab_base = null;
		String[] tab_classif = null;

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
				
				if (line.equals("")) {
					compteur++;
				} else {
					if (compteur == 0) {
						tab_base = line.split(",");
						instance_de_base = tab_base[0];
						value_de_base = tab_base[1];
						value_base = instance_de_base + "," + value_de_base;
						values_base.add(value_base);
					}
					if (compteur == 1) {
						tab_classif = line.split(",");
						instance_de_classif = tab_classif[0];
						value_de_classif = tab_classif[1];
						value_classif = instance_de_classif + "," + value_de_classif;
						values_classif.add(value_classif);
					}

				}
			}
			br.close();
			logger.info("value de classif :" +values_classif);
			
			
			for (int i = 0; i < values_base.size(); i++) {
				String[] splitb = values_base.get(i).split(",");
				String[] splitc = values_classif.get(i).split(",");


				if (splitb[0].equals(splitc[0])) {
					if (!splitb[1].equals(splitc[1])) {
						compteur_comparaison++;
						writer.println("Une différence à la ligne " + splitb[0] + " BASE : " + splitb[1] + " CLASSIF : " + splitc[1]);
						
						if(splitb[1].equals(" COREF") && splitc[1].equals(" NOT_COREF")){
							faux_not_coref++;
						}
						else if(splitb[1].equals(" NOT_COREF") && splitc[1].equals(" COREF")){
							faux_coref++;
						}
					}
					else{
						if(splitb[1].equals(" COREF") && splitc[1].equals(" COREF")){
							vrai_coref++;
						}
						else if(splitb[1].equals(" NOT_COREF") && splitc[1].equals(" NOT_COREF")){
							vrai_not_coref++;
						}
					}

				}
				
				
				precision_coref = (double) (vrai_coref)/(vrai_coref + faux_coref);
				rappel_coref = (double) (vrai_coref)/(vrai_coref + faux_not_coref);
				fmesure_coref = (2 * precision_coref * rappel_coref)/(precision_coref + rappel_coref);
				
				precision_not_coref = (double) (vrai_not_coref)/(vrai_not_coref + faux_not_coref);
				rappel_not_coref = (double) (vrai_not_coref)/(vrai_not_coref + faux_coref);
				fmesure_not_coref = (2 * precision_not_coref * rappel_not_coref)/(precision_not_coref + rappel_not_coref);
				
				total_coref = vrai_coref + faux_not_coref;
				total_not_coref = vrai_not_coref + faux_coref;
				
				precision_avg = ((precision_coref * total_coref) + (precision_not_coref * total_not_coref)) / (total_coref + total_not_coref);
				rappel_avg = ((rappel_coref * total_coref) + (rappel_not_coref * total_not_coref)) / (total_coref + total_not_coref);
				fmesure_avg = ((fmesure_coref * total_coref) + (fmesure_not_coref * total_not_coref)) / (total_coref + total_not_coref);

			}
			
			logger.info("total coref : " +total_coref + " total not coref : " +total_not_coref);
			logger.info("Nombre de vrai coref :" + vrai_coref + " et faux coref : " + faux_coref);
			logger.info("Nombre de vrai not_coref :" + vrai_not_coref + " et faux not coref : " + faux_not_coref);

			writer.print("      COREF");
			writer.println("      NOT_COREF");
			writer.print("COREF");
			writer.print("    " +  vrai_coref);
			writer.println("    " +  faux_not_coref);
			writer.print("NOT_COREF");
			writer.print("    " +  faux_coref);
			writer.println("    " +  vrai_not_coref);
			

			writer.println("Rappel coref :" + rappel_coref);
			writer.println("Précision coref :" + precision_coref);
			writer.println("F-mesure coref :" + fmesure_coref);
			
			writer.println("Rappel not_coref :" + rappel_not_coref);
			writer.println("Précision not_coref :" + precision_not_coref);
			writer.println("F-mesure not_coref :" + fmesure_not_coref);
			
			writer.println("Rappel avg :" + rappel_avg);
			writer.println("Précision avg :" + precision_avg);
			writer.println("F-mesure avg :" + fmesure_avg);

			logger.info("La précision coref est de " + precision_coref + " , le rappel de " + rappel_coref + " et la fmesure de " +fmesure_coref);
			logger.info("La précision not_coref est de " + precision_not_coref + " , le rappel de " + rappel_not_coref + " et la fmesure de " +fmesure_not_coref);
			logger.info("La précision moyenne est de " + precision_avg + " , le rappel de " + rappel_avg + " et la fmesure de " +fmesure_avg);
			logger.info("Fichier " + fileName + " enregistré dans : " + outputPath);
			logger.info("Le nombre de différences est de : " +compteur_comparaison);

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
