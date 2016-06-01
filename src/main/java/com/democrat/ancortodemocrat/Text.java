package com.democrat.ancortodemocrat;

import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXB;

import com.democrat.ancor.speech.Trans;
import com.democrat.ancor.speech.Turn;
import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Schema;
import com.democrat.ancortodemocrat.element.Unit;

public class Text {


	private String content;
	private String fileName;
	private Trans trans;

	public Text(String content){
		this.content = content;
		int index = this.content.indexOf("<Trans");

		this.trans =  JAXB.unmarshal(new StringReader( this.content.substring(index, this.content.length() ) ), Trans.class);


	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Get the content of one unit,
	 * check from the start and end positionning of the unit
	 * @param annotation useful to read the unit position
	 * @param unit the unit you want the text
	 * @return
	 */
	public String getContentFromUnit( Annotation annotation, Unit unit ){
		if( unit instanceof Schema){
			return getContentFromUnit( annotation, ((Schema) unit).getUnitWhereFeatureNotNull( annotation ) );
		}else{
			return this.getContent().substring( unit.getStart( annotation ), unit.getEnd( annotation ) );
		}
	}


	/**
	 * get the converted	 text with xml attribute to speech 
	 * @return
	 */
	public Trans toTrans(){
		return this.trans;
	}

	/**
	 * Returns the index within this string of the first occurrence of the specified character.
	 * @param turn to loc
	 * @return
	 */
	public int indexOf( Turn turn ){
		return this.content.indexOf( turn.getContent() );
	}

	public String contentWithoutTag(){
		String content = "";

		List<Turn> turnList = this.trans.getEpisode().getSection().getTurn();
		for(Turn turn : turnList){
			content += turn.getContent() + " ";
		}

		return content;
	}
	
	public boolean isCorresponding( Annotation annotation, Turn turn, Unit unit ){
		String contentOfUnit = this.getContentFromUnit(annotation, unit);
		if( turn.getContent().contains( contentOfUnit ) ){
			//found
			//just check the position is good
			int startOfUnit = unit.getStart( annotation );
			int endOfUnit = unit.getStart( annotation );
			int indexOfTurn = this.indexOf( turn );
			if(  indexOfTurn <= startOfUnit &&
					indexOfTurn + turn.getContent().length() >= endOfUnit ){
				return true;
			}
		}
		return false;
	}

	public Turn getTurnCorresponding( Annotation annotation, Unit unit ){
		List<Turn> turnList = this.trans.getEpisode().getSection().getTurn();
		for(int t = 0; t < turnList.size(); t++){
			if( this.isCorresponding(annotation, turnList.get( t ), unit) ){
				return turnList.get( t );
			}
		}
		return null;
	}


}
