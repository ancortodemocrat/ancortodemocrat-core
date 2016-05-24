package com.democrat.ancortodemocrat.treetagger;

/**
 * contains the token, and theses results:
 * pos
 * lemma
 */
public class ResultToken {

	private String token;
	private String pos;
	private String lemma;

	public ResultToken(String token, String pos, String lemma) {
		this.token = token;
		this.pos = pos;
		this.lemma = lemma;
	}

}
