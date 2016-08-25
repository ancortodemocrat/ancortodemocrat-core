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
	private int idMention;
	/**
	 * Indique dans quel id
	 * de la chaîne il a été mis
	 * dans le fichier gold
	 */
	private int refGoldChain;



	public int getRefGoldChain() {
		return refGoldChain;
	}

	public void setRefGoldChain(int refGoldChain) {
		this.refGoldChain = refGoldChain;
	}

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

	public int getIdMention() {
		return idMention;
	}

	public void setIdMention(int idMention) {
		this.idMention = idMention;
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

	
	/**
	 * Retourne la valeur de la feature voulue
	 * @param featureName nom de la feature
	 * @return la valeur de la feature passée
	 */
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
		if(featureName.equalsIgnoreCase( "DEF" ) ||
				featureName.equalsIgnoreCase( "GENRE" ) ||
				featureName.equalsIgnoreCase( "nombre" ) ){
			return new String( "UNK" );
		}else if(featureName.equalsIgnoreCase("id_def") ||
				featureName.equalsIgnoreCase("id_type") ||
				featureName.equalsIgnoreCase("id_en") ||
				featureName.equalsIgnoreCase("id_genre") ||
				featureName.equalsIgnoreCase("id_nombre") ||
				featureName.equalsIgnoreCase("id_spk") ||
				featureName.equalsIgnoreCase("id_form") ||
				featureName.equalsIgnoreCase("id_subform") ||
				featureName.equalsIgnoreCase("id_new") ||
				featureName.equalsIgnoreCase("id_previous") ||
				featureName.equalsIgnoreCase("id_next") ||
				featureName.equalsIgnoreCase("EMBEDDED")
				){
			return new String("NA");
		}else if(featureName.equalsIgnoreCase("com_rate") ||
			featureName.equalsIgnoreCase( "incl_rate" ) ||
			featureName.toLowerCase().contains("distance") ){
				return new String("0");
		}
		return new String("NULL");
	}

	
	/**
	 * Change la valeur de la feature si elle existe déjà
	 * Sinon ajoute la feature avec sa valeurs
	 * @param featureName nom de la feature
	 * @param value valeur de la feature
	 */
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
