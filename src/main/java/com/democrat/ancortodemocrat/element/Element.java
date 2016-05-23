package com.democrat.ancortodemocrat.element;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import com.democrat.ancortodemocrat.feature.Feature;

@XmlTransient
public class Element {


	private Characterisation characterisation;
	private String id;


	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Element [id=" + id + "]";
	}
	

	@XmlAttribute(name="id")
	public String getId() {
		return this.id;
	}
	
	/**
	 * Gets the value of the characterisation property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Characterisation }
	 *     
	 */
	@XmlElement(name="characterisation")
	public Characterisation getCharacterisation() {
		return characterisation;
	}

	/**
	 * Sets the value of the characterisation property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link Characterisation }
	 *     
	 */
	public void setCharacterisation(Characterisation value) {
		this.characterisation = value;
	}
	
	public String getFeature(String featureName){
		Characterisation charact = this.getCharacterisation();
		List<Feature> features = charact.getFeatureSet().getFeature();
		for(int f = 0; f < features.size(); f++){
			if(features.get( f ).getName().equalsIgnoreCase( featureName )){
				return features.get( f ).getValue();
			}
		}
		return new String("NULL");
	}
	
	public void setFeature(String featureName, String value){
		//check if feature already exist

		Characterisation charact = this.getCharacterisation();
		List<Feature> features = charact.getFeatureSet().getFeature();
		for(int f = 0; f < features.size(); f++){
			if(features.get( f ).getName().equalsIgnoreCase( featureName )){
				features.get( f ).setValue( value );
				return;
			}
		}
		//feature doesnt exists, create this one
		features.add( new Feature( featureName, value ) );
	}
}
