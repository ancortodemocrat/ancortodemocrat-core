package com.democrat.classification;

public class Mention {

	
	private int id;
	private boolean coref;
	private boolean corefSet = false;
	
	
	public Mention( int id ){
		this.id = id;
		this.coref = false;
	}
	
	public Mention(int id, boolean coref ){
		this.id = id;
		this.coref = coref;
	}
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public boolean isCoref() {
		if( ! corefSet ){
			return true;
		}
		return coref;
	}

	/**
	 * Si il a pas défini on rempli simplement
	 * Sinon on fait ou logique
	 * @param coref
	 */
	public void setCoref(boolean coref) {
		if( ! corefSet ){
			//coref non indiqué
			this.coref = coref;
			this.corefSet = true;
		}else{
			this.coref = this.coref | coref;
		}
	}

	
	
}
