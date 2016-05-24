package com.democrat.ancortodemocrat.element;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

@XmlRootElement(name="schema")
public class Schema extends Unit{

	private static Logger logger = Logger.getLogger(Schema.class);


	public int getStart( Annotation annotation ){
		List<EmbeddedUnit> units = this.getPositioning().getEmbeddedUnit();
		int start = 0;
		if(units.size() > 0){
			Element element = ((EmbeddedUnit) units.get( 0 )).getElement( annotation );
			if(element instanceof Unit){
				start = ((Unit) element).getStart( annotation );
				for(int u = 1; u < units.size(); u++){
					if(start > ((Unit) units.get( u ).getElement( annotation )).getStart( annotation ) ){
						start = ((Unit) units.get( u ).getElement( annotation )).getStart( annotation );
					}
				}
			}
		}
		return start;
	}

	/**
	 * check in positionningschema for each EmbeddedUnit
	 * if one has the feature NEW to Yes return true
	 * @param annotation of current file
	 * @return
	 */
	public boolean isNew( Annotation annotation ){
		boolean isNew = false;
		List<EmbeddedUnit> units = this.getPositioning().getEmbeddedUnit();
		if(units == null){
			return false;
		}
		for(int u = 0; u < units.size(); u++){
			Element element = units.get( u ).getElement( annotation );
			if(element instanceof Unit){
				isNew = isNew || ((Unit) element).isNew( annotation );
			}
		}
		return isNew;
	}
	
	public List<Unit> getUnitList( Annotation annotation ){
		List<EmbeddedUnit> embeddedUnit = this.getPositioning().getEmbeddedUnit();
		ArrayList<Unit> list = new ArrayList<Unit>();
		
		for(int e = 0; e < embeddedUnit.size(); e++){
			list.add( (Unit) annotation.getElementById( embeddedUnit.get( e ).getId() ));
		}
		
		return list;
	}
	
	
	public String getFeature ( Annotation annotation, String featureName ){
		List<Unit> list = this.getUnitList( annotation );
		for(int u = 0; u < list.size(); u++){
			if( ! list.get( u ).getFeature( featureName ).equalsIgnoreCase( "NULL" ) ){
				return list.get( u ).getFeature( featureName );
			}
		}
		return new String( "NULL" );
	}

}
