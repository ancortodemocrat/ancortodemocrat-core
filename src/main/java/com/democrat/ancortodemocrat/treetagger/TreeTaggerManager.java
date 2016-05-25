package com.democrat.ancortodemocrat.treetagger;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.element.Relation;

public class TreeTaggerManager {
	
	private static Logger logger = Logger.getLogger(TokenConvertRelationHandler.class);
	/**
	 * represent a list of id
	 * this list contains the id of relation
	 * who are waiting to be converting by {@link TokenConvertRelationHandler}
	 */
	private List<String> idRelationList;
	
	public TreeTaggerManager(){
		idRelationList = new ArrayList<String>();
	}
	
	
	public synchronized void relationInProgress( Relation relation ){
		if( idRelationList.contains( relation.getId() ) ){
			logger.debug("The manager already contains this relation ! " + relation);
			return;
		}
		this.idRelationList.add( relation.getId() );
	}
	
	public synchronized void relationDone( Relation relation ){
		if( ! idRelationList.contains( relation.getId() ) ){
			logger.debug("The manager not contains this relation ! " + relation);
			return;
		}
		this.idRelationList.remove( relation.getId() );
	}
	
	public synchronized boolean relationIsDone( Relation relation ){
		if( ! idRelationList.contains( relation.getId() ) ){
			return true;
		}
		return false;
		
	}

}
