package com.democrat.ancortodemocrat.element;

import javax.xml.bind.annotation.XmlAttribute;

public class Term {
	

	private String id;


	public void setId(String id) {
		this.id = id;
	}


	@XmlAttribute(name="id")
	public String getId() {
		return this.id;
	}

    public Term(String id){
    	this.setId( id );
    }

    
    public Term(){
    	
    }
    
    
    public Element getElement(Annotation annotation){
    	return annotation.getElementById( this.getId() );
    }
    

	@Override
	public String toString() {
		return "Term [id=" + id + "]";
	}
	

}