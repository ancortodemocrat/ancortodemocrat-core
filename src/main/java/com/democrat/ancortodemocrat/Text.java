package com.democrat.ancortodemocrat;

import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.bind.UnmarshalException;

import org.apache.log4j.Logger;

import com.democrat.ancor.speech.Trans;
import com.democrat.ancor.speech.Turn;
import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Schema;
import com.democrat.ancortodemocrat.element.Unit;

public class Text {

	private static Logger logger = Logger.getLogger(Text.class);

	private String content;
	private String fileName;
	private Trans trans;
	//private int patchStart = 0;

	public Text(String fileName, String content){
		this.fileName = fileName;
		this.content = content;
		String tmpContent = content;
		
		//some conversion, if the file is corrupt 
		//trans starter found
		//section start OK
		String[] countSectionStarter = this.content.split("<Section");
		String[] countSectionEnd = this.content.split("</Section");
		int index = this.content.indexOf("<Trans");

		if( index == -1){
			if( this.content.indexOf("<Section ") >= 0 ){
				this.content = this.content.substring( this.content.indexOf("<Section "), this.content.length() );
			}else if(this.content.indexOf("</Turn") >= 0 ){
				this.content = this.content.substring( this.content.indexOf("<Turn "), this.content.length() );
			}
		}

		if( countSectionStarter.length > 1 || countSectionEnd.length > 1){
			//remove the sections
			this.content = this.content.replace("</Section>", "");
			this.content = this.content.replaceAll("<Section ([^<>]+)>", "");

		}

		if( index == -1 ){
			//missing trans tag
			//adding the start and en tag section


			this.content = "<Trans><Episode><Section>" + this.content;
			//patchStart += "<Trans><Episode>".length();
			index = this.content.indexOf("<Trans");
		}else{
			int indexOfTurn = this.content.indexOf("<Turn");
			if( indexOfTurn != -1 ){
				this.content = this.content.substring(indexOfTurn, this.content.length() );
				this.content = "<Trans><Episode><Section>" + this.content;
			}

		}
		if( this.content.indexOf("</Section") == -1 ){
			if(this.content.indexOf("</Trans") != -1 ){
				this.content = this.content.replace("</Trans>", "");
				this.content = this.content.replace("</Episode>", "");
			}
			int countTurnStart = this.content.split("<Turn ").length;
			int countTurnEnd = this.content.split("</Turn").length;
			if(countTurnStart > countTurnEnd){
				this.content = this.content + "</Turn>";
			}
			this.content = this.content + "</Section>";
		}
		if( this.content.indexOf("</Trans") == -1){
			//missing end of tag
			this.content = this.content + "</Episode></Trans>";
		}
		try{
			this.trans =  JAXB.unmarshal(new StringReader( this.content ), Trans.class);
		}catch(javax.xml.bind.DataBindingException e){
			e.printStackTrace();
			logger.debug(this.content.substring(index, this.content.length() ));
			logger.debug("==> ["+this.fileName+"]"+this.content );
		}
		this.content = tmpContent;

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
			try{
				return this.getContent().substring( unit.getStart( annotation ) , unit.getEnd( annotation ) );
			}catch(StringIndexOutOfBoundsException e){
				logger.debug(this.fileName);
				logger.debug(this.getContent());
				e.printStackTrace();
				return "";
			}
		}
	}

	/**
	 * Get the content contained before unit
	 * @param annotation
	 * @param unit
	 * @param context_len Number of characters to get from the context
	 * @return
	 */
	public String getPreUnit(Annotation annotation, Unit unit, Integer context_len){
		if( unit instanceof Schema){
			return getPreUnit( annotation, ((Schema) unit).getUnitWhereFeatureNotNull( annotation ) , context_len);
		}else{
			String pre_content = this.getContent().substring(0,unit.getStart(annotation))
					.replaceAll("<[^>]+>"," ");
			int i = Math.max(pre_content.length() - context_len, 0);
			String prespace = "";
			if (i==0)
				prespace = new String(new char[-(pre_content.length() - context_len - i)]).replace('\0', ' ');
			return prespace + pre_content.substring(i);
		}
	}

	/**
	 * Get the content contained after unit
	 * @param annotation
	 * @param unit
	 * @param context_len Number of characters to get from the context
	 * @return
	 */
	public String getSufUnit(Annotation annotation, Unit unit, Integer context_len){
		if( unit instanceof Schema){
			return getSufUnit( annotation, ((Schema) unit).getUnitWhereFeatureNotNull( annotation ) , context_len);
		}else{
			String suf_content = this.getContent().substring(unit.getEnd(annotation))
					.replaceAll("<[^>]+>"," ");
			return suf_content.substring(0 , context_len);
		}
	}
	
	public Turn getPreviousTurn( Turn turn ){
		List<Turn> turnList = trans.getEpisode().getSection().getTurn();
		for(int t = 0; t < turnList.size(); t++){
			if( turnList.get( t ).equals( turn ) ){
				if( t == 0){
					return null;
				}else{
					return turnList.get( t - 1 );
				}
			}
		}
		return null;
	}
	
	public Turn getNextTurn( Turn turn ){
		List<Turn> turnList = trans.getEpisode().getSection().getTurn();
		for(int t = 0; t < turnList.size(); t++){
			if( turnList.get( t ).equals( turn ) ){
				if( t == turnList.size() - 1 ){
					return null;
				}else{
					return turnList.get( t + 1 );
				}
			}
		}
		return null;		
	}


	/**
	 * get the converted text with xml attribute to speech 
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
		if(turn.getText().size() > 1){
			return this.content.indexOf( turn.getText().get( 0 ) );
		}else{
			return this.content.indexOf( turn.getContent() );
		}
	}

	/**
	 * Return the content of the text without the xml tag
	 * @return
	 */
	public String getContentWithoutTag(){
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
			int endOfUnit = unit.getEnd( annotation );
			int indexOfTurn = this.indexOf( turn );
			int endOfTurn = indexOfTurn + turn.getContent().length();
			if(  indexOfTurn <= startOfUnit &&
					endOfTurn >= endOfUnit ){
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
