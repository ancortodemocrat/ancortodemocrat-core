package com.democrat.ancortodemocrat.treetagger;

import java.util.List;

import org.annolab.tt4j.TokenHandler;
import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.AncorToDemocrat;
import com.democrat.ancortodemocrat.element.Relation;

/**
 * start the request to tag each element of the relation
 * once we have the results, compare the nouns of each
 * and if the lemmas are the same, so the relation should be "DIRECT"
 * else "INDIRECT
 */
public class TokenConvertRelationHandler  implements Runnable{
	
	
	private static Logger logger = Logger.getLogger(TokenConvertRelationHandler.class);
	
	private Relation relation;
	private String firstMention;
	private String secondMention;

	private TreeTaggerManager treeTaggerManager;

	
	/**
	 * 
	 * @param relation to convert
	 * @param firstMention mention of the element of the relation
	 * @param secondMention mention of the PREelement of the relation
	 * @throws TokenConvertRelationHandlerException
	 */
	public TokenConvertRelationHandler( TreeTaggerManager treeTaggerManager, Relation relation,
			String firstMention,
			String secondMention) throws TokenConvertRelationHandlerException{
		this.treeTaggerManager = treeTaggerManager;
		this.treeTaggerManager.relationInProgress( relation );
		this.relation = relation;
		this.firstMention = firstMention;
		this.secondMention = secondMention;
		
		Thread th = new Thread( this );
		th.start();
	}

	

	@Override
	public void run() {
		//start the wrapper
		//for each element of the relation
		
		TokenConvertMentionHandler firstTokenHandler = new TokenConvertMentionHandler( firstMention );
		TokenConvertMentionHandler secondTokenHandler = new TokenConvertMentionHandler( secondMention );
		
		AncorToDemocrat.treeTagger.work( firstTokenHandler );
		AncorToDemocrat.treeTagger.work( secondTokenHandler );
		//wait the treeTaggers makes their tasks
		while ( ! firstTokenHandler.isDone() && ! secondTokenHandler.isDone() ){
			try {
				Thread.sleep( 1 );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//work
		//compare the nouns
		
		//get the results
		ResultMention firstResult = firstTokenHandler.getResultMention();
		ResultMention secondResult = secondTokenHandler.getResultMention();
		
		//logger.debug( firstResult );
		//logger.debug( secondResult );
		
		if( firstResult.containsNoun() && secondResult.containsNoun() ){
			List<ResultToken> firstNounList = firstResult.getNounList();
			List<ResultToken> secondNounList = secondResult.getNounList();
			if( firstNounList.size() == 1 && secondNounList.size() == 1){
				//just compare the lemma
				if( firstNounList.get( 0 ).getLemma().equalsIgnoreCase( secondNounList.get( 0 ).getLemma() ) ){
					relation.getCharacterisation().getType().setValue( "DIRECTE" );
				}
				//else stay INDIRECTE, not need to change
			}else{
				//> 1
				//need compare if one of the noun is containing in other list
				//using lemma
				for(int n = 0; n < firstNounList.size(); n++){
					
					for(int s = 0; s < secondNounList.size(); s++){
						if(firstNounList.get( n ).getLemma().equalsIgnoreCase( secondNounList.get( s ).getLemma() ) ){
							//DIRECTE, we have found the same noun in the two mentions
							relation.getCharacterisation().getType().setValue( "DIRECTE" );
							break;
						}
					}
					
				}
			}
		}
		this.treeTaggerManager.relationDone( relation );
	}
	
}