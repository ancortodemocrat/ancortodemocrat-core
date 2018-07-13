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
 * @deprecated Not used in project
 */
public class ComparaisonRelation implements Runnable {

	private String outputPath;
	private String inputPath;
	private static Logger logger = Logger.getLogger(MultiClassifier.class);
	private List<String> fileOuput = new ArrayList<String>();
	
	String indice;
	String value_de_base;
	String value_de_classif;
	String instance_de_base;
	String instance_de_classif;

	int compteur_comparaison=0;
	
	int vrai_relation;
	int faux_relation;
	int vrai_not_relation;
	int faux_not_relation;
	
	double total_relation;
	double total_not_relation;
	
	double precision_relation;
	double rappel_relation;
	double fmesure_relation;

	double precision_not_relation;
	double rappel_not_relation;
	double fmesure_not_relation;
	
	double precision_avg;
	double rappel_avg;
	double fmesure_avg;
	
	public ComparaisonRelation(String inputPath, String outputPath) {
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

		String[] tab_result = null;
		

		try {
			writer = new PrintWriter(this.outputPath + fileName + ".arff", "UTF-8");
			this.fileOuput.add(this.outputPath + fileName + ".arff");

			InputStream ips;
			ips = new FileInputStream(inputPath);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;

			while ((line = br.readLine()) != null) {
				
						tab_result = line.split(",");
						indice = tab_result[0];
						value_de_base = tab_result[1];
						value_de_classif = tab_result[2];
						value_base = value_de_base;
						value_classif = value_de_classif;
//						values_base.add(value_base);
//						values_classif.add(value_classif);
						
						logger.info("Value de base : " + value_base + " value de classif : " + value_classif) ;

							if (!value_base.equals(value_classif)) {
								compteur_comparaison++;
								writer.println("Une différence à la ligne " + indice  + " BASE : " + value_base + " CLASSIF : " + value_classif);
								
								if(value_base.contains("1") && value_classif.contains("2")){
									faux_not_relation++;
								}
								else if(value_base.contains("2") && value_classif.contains("1")){
									faux_relation++;
								}
							}
							else{
								if(value_base.contains("1") && value_classif.contains("1")){
									vrai_relation++;
								}
								else if(value_base.contains("2") && value_classif.contains("2")){
									vrai_not_relation++;
								}
							}

						
						precision_relation = (double) (vrai_relation)/(vrai_relation + faux_relation);
						rappel_relation = (double) (vrai_relation)/(vrai_relation + faux_not_relation);
						fmesure_relation = (2 * precision_relation * rappel_relation)/(precision_relation + rappel_relation);
						
						precision_not_relation = (double) (vrai_not_relation)/(vrai_not_relation + faux_not_relation);
						rappel_not_relation = (double) (vrai_not_relation)/(vrai_not_relation + faux_relation);
						fmesure_not_relation = (2 * precision_not_relation * rappel_not_relation)/(precision_not_relation + rappel_not_relation);
						
						total_relation = vrai_relation + faux_not_relation;
						total_not_relation = vrai_not_relation + faux_relation;
						
						precision_avg = ((precision_relation * total_relation) + (precision_not_relation * total_not_relation)) / (total_relation + total_not_relation);
						rappel_avg = ((rappel_relation * total_relation) + (rappel_not_relation * total_not_relation)) / (total_relation + total_not_relation);
						fmesure_avg = ((fmesure_relation * total_relation) + (fmesure_not_relation * total_not_relation)) / (total_relation + total_not_relation);

			}
			br.close();
			
			logger.info("Nombre de vrai relation :" + vrai_relation + " et faux relation : " + faux_relation);
			logger.info("Nombre de vrai not_relation :" + vrai_not_relation + " et faux not relation : " + faux_not_relation);

			writer.print("      YES");
			writer.println("      NO");
			writer.print("YES");
			writer.print("    " +  vrai_relation);
			writer.println("    " +  faux_not_relation);
			writer.print("NO");
			writer.print("    " +  faux_relation);
			writer.println("    " +  vrai_not_relation);
			

			writer.println("Rappel relation :" + rappel_relation);
			writer.println("Précision relation :" + precision_relation);
			writer.println("F-mesure relation :" + fmesure_relation);
			
			writer.println("Rappel not_relation :" + rappel_not_relation);
			writer.println("Précision not_relation :" + precision_not_relation);
			writer.println("F-mesure not_relation :" + fmesure_not_relation);
			
			writer.println("Rappel avg elation :" + rappel_avg);
			writer.println("Précision avg relation :" + precision_avg);
			writer.println("F-mesure avg relation :" + fmesure_avg);

			logger.info("La précision relation est de " + precision_relation + " , le rappel de " + rappel_relation + " et la fmesure de " +fmesure_relation);
			logger.info("La précision not_relation est de " + precision_not_relation + " , le rappel de " + rappel_not_relation + " et la fmesure de " +fmesure_not_relation);
			logger.info("La précision avg est de : " + precision_avg + ", le rappel de " + rappel_avg + " et la fmesure de " + fmesure_avg);
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
