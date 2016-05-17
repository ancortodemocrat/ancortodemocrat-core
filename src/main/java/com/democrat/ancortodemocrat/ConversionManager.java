package com.democrat.ancortodemocrat;

import java.util.ArrayList;
import java.util.List;

public class ConversionManager {
	
	private List<Corpus> corpus;

	public ConversionManager(){
		this.corpus = new ArrayList<Corpus>();
	}
	
	public ConversionManager(List<Corpus> corpus) {
		super();
		this.corpus = corpus;
	}

	public List<Corpus> getCorpus() {
		return corpus;
	}

	public void setCorpus(List<Corpus> corpus) {
		this.corpus = corpus;
	}
	
	

}
