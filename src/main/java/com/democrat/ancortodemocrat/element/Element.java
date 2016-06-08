package com.democrat.ancortodemocrat.element;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.Text;
import com.democrat.ancortodemocrat.feature.Feature;

@XmlTransient
public class Element {

	private static Logger logger = Logger.getLogger(Element.class);

	protected Characterisation characterisation  = new Characterisation();
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
		if(this.characterisation == null){
			this.characterisation = new Characterisation();
		}
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
				if( features.get( f ).getValue() == null){
					break;
				}
				if( features.get( f ).getValue().equalsIgnoreCase("default") ){
					if(featureName.equalsIgnoreCase( "DEF" )){
						return new String( "DEF_SPLE" );
					}
				}
				return features.get( f ).getValue();
			}
		}
		if(featureName.equalsIgnoreCase( "DEF" )){
			return new String( "UNK" );
		}else if(featureName.equalsIgnoreCase("id_def") ||
				featureName.equalsIgnoreCase("id_type") ||
				featureName.equalsIgnoreCase("id_en") ||
				featureName.equalsIgnoreCase("id_genre") ||
				featureName.equalsIgnoreCase("id_nombre") ||
				featureName.equalsIgnoreCase("id_spk") ||
				featureName.equalsIgnoreCase("id_form") ||
				featureName.equalsIgnoreCase("id_subform")
				){
			return new String("NA");
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

	public List<Feature> getFeatureList(){
		return this.getCharacterisation().getFeatureSet().getFeature();
	}
}
