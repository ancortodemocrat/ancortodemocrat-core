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

	public String getToken() {
		return token;
	}

	public String getPos() {
		return pos;
	}

	public String getLemma() {
		return lemma;
	}

	/**
	 * return if this token is a noun or not
	 * @return
	 */
	public boolean isNoun() {
		if( this.pos.equalsIgnoreCase( "NOM" )){
			return true;
		}
		return false;
	}
	
	
	

}
