package com.democrat.ancortodemocrat.treetagger;

import org.annolab.tt4j.TokenHandler;

import com.democrat.ancortodemocrat.AncorToDemocrat;
import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Element;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Unit;

/**
 * start the request to tag each element of the relation
 * once we have the results, compare the nouns of each
 * and if the lemmas are the same, so the relation should be "DIRECT"
 * else "INDIRECT
 */
public class TokenConvertRelationHandler  implements TokenHandler<String>, Runnable{
	
	private Relation relation;
	private ResultToken[] result;
	private String firstMention;
	private String secondMention;
	private boolean done;

	
	/**
	 * 
	 * @param relation to convert
	 * @param firstMention mention of the element of the relation
	 * @param secondMention mention of the PREelement of the relation
	 * @throws TokenConvertRelationHandlerException
	 */
	public TokenConvertRelationHandler( Relation relation,
			String firstMention,
			String secondMention) throws TokenConvertRelationHandlerException{
		this.relation = relation;
		result = new ResultToken[ 2 ];
		this.firstMention = firstMention;
		this.secondMention = secondMention;
		
		this.done = false;
		
	}

	/**
	 * Used by TreeTagger
	 * when the task is done to find the pos and lemma of the token
	 * this method is called
	 */
	@Override
	public void token(String token, String pos, String lemma) {
		if(result[ 0 ] == null && pos.equalsIgnoreCase( "NOM" )){
			result[ 0 ] = new ResultToken(token, pos, lemma);
		}else if(result[ 0 ] != null && pos.equalsIgnoreCase( "NOM" )){
			result[ 1 ] = new ResultToken(token, pos, lemma);
			this.done = true;
		}
		
	}

	@Override
	public void run() {
		//start the wrapper
		//for each element of the relation
		AncorToDemocrat.treeTagger.work( this, splitMention ( firstMention ));
		AncorToDemocrat.treeTagger.work( this, splitMention ( secondMention ));
		//wait the treeTaggers makes their tasks
		while ( ! this.done ){
			try {
				Thread.sleep( 10 );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//work
		//compare the nouns
	}
	
	
	private String[] splitMention( String sentence ){
		sentence = sentence.replace(", ", " , ");
		sentence = sentence.replace(".", " .");
		return sentence.split(" ");
	}
}