package com.democrat.classification;

import java.util.Random;

import weka.classifiers.functions.SMO;

public class ClassifierSMOParameter extends ClassifierParameter {
	
	private int numberOfSplit;
	private float randomSeed;

	public ClassifierSMOParameter( int numberOfSplit, float randomSeed) {
		super( new SMO( ) );
		this.numberOfSplit = numberOfSplit;
		this.randomSeed = randomSeed;
	}
	
	public ClassifierSMOParameter( ){
		super( new SMO( ) );
		this.numberOfSplit = 5;
		this.randomSeed = new Random( ).nextInt();
	}

}
