package com.democrat.classification;

public class Mention {

	
	public int id;
	public boolean bool;
	
	
	public Mention( int id ){
		this.id = id;
		this.bool = true;
	}
	
	public Mention(int id, boolean bool ){
		this.id = id;
		this.bool = bool;
	}
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isBool() {
		return bool;
	}
	public void setBool(boolean bool) {
		this.bool = bool;
	}
	
}
