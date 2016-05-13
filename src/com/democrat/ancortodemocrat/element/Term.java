package com.democrat.ancortodemocrat.element;


public class Term extends Element{

    public Term(String id){
    	super.setId( id );
    }

    
    public Term(){
    	
    }
    
    
    public Element getElement(Annotation annotation){
    	return annotation.getElementById( this.getId() );
    }

}