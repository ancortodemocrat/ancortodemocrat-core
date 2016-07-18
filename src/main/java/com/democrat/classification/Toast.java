package com.democrat.classification;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.democrat.ancortodemocrat.FileManager;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;

public class Toast {

	public static FileManager fileManager;

	public static void main(String[] args) {
		
		Model model = Model.loadModel("generated/models/toast.model");
		Evaluation eval = model.crossValidate( loadInstance("generated/2016_07_05_10000_no_assoc_40_60_1-4_C.arff"),
				5);
		System.out.println(eval.toSummaryString("\nResults\n========\n", true));
		System.out.println("PRECISION " + eval.precision(0)+" RAPPEL "+eval.recall(0)+" F-MESURE "+eval.fMeasure(0));
		System.out.println("PRECISION " + eval.precision(1)+" RAPPEL "+eval.recall(1)+" F-MESURE "+eval.fMeasure(1));

	}
	
	public static Instances loadInstance(String arffFile){
		BufferedReader reader = null;
		Instances instances = null;
		try {
			reader = new BufferedReader( new FileReader( arffFile ) );
			instances = new Instances( reader );

			instances.setClassIndex( instances.numAttributes() - 1 );
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return instances;
	}

}
