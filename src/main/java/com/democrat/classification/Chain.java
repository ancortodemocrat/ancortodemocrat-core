package com.democrat.classification;

import java.util.ArrayList;
import java.util.List;

public class Chain {
	
	private int ref;
	private List<Mention> mentionList;
	
	public Chain( int ref ){
		this.ref = ref;
		this.mentionList = new ArrayList<Mention>();
	}
	
	
	
	public int getRef() {
		return ref;
	}



	public List<Mention> getMentionList() {
		return mentionList;
	}



	/**
	 * Ajoute une mention à la chaîne si cette dernière 
	 * n'est pas présente
	 * @param mention
	 */
	public void addMention( Mention mention ){
		if( ! this.containsMention( mention ) ){
			this.mentionList.add( mention );
		}
	}
	
	/**
	 * Test si une mention est contenu dans cette chaîne
	 * seulement en testant l'id de la mention donnée
	 * @param mention
	 * @return
	 */
	public boolean containsMention( Mention mention ){
		//vérifier les ids seulement
		for( Mention m : mentionList ){
			if( m.getId() == mention.getId() ){
				return true;
			}
		}
		return false;
	}



	public boolean containsMention(int idMentionElement) {
		return containsMention( new Mention( idMentionElement ) );
	}



	public Mention getMention(int idMention) {
		for( Mention mention : this.mentionList ){
			if( mention.getId() == idMention ){
				return mention;
			}
		}
		return null;
	}



	/**
	 * Retourne le nombre de mentions comprises dans la chaîne
	 * @return
	 */
	public int size() {
		return this.mentionList.size();
	}

}
