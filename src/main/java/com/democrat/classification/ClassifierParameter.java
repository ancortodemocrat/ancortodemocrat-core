package com.democrat.classification;

import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

public class ClassifierParameter {
	
	private AbstractClassifier classifier;
	
	public ClassifierParameter( AbstractClassifier classifier ){
		this.classifier = classifier;
	}
	
	public void buildClassifier( Instances instances ){
		try {
			this.classifier.buildClassifier( instances );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public AbstractClassifier getClassifier(){
		return classifier;
	}

}
