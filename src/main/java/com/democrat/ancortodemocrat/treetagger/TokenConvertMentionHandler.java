package com.democrat.ancortodemocrat.treetagger;

import org.annolab.tt4j.TokenHandler;

public class TokenConvertMentionHandler implements TokenHandler<String>{


	private ResultMention resultMention;
	private boolean done;
	private String[] mentionSplitted;


	/**
	 * 
	 * @param relationHandler
	 * @param mention the token is done when all the result is equals to the mention
	 */
	public TokenConvertMentionHandler( String mention ){
		this.resultMention = new ResultMention();
		done = false;
		this.mentionSplitted = this.splitMention( mention );
	}

	
	/**
	 * Used by TreeTagger
	 * when the task is done to find the pos and lemma of the token
	 * this method is called
	 */
	@Override
	public void token(String token, String pos, String lemma) {
		this.resultMention.newToken( new ResultToken( token, pos, lemma ) );		
		if( this.resultMention.getTokenList().size() == mentionSplitted.length ){
			this.done = true;
		}
	}
	
	public boolean isDone() {
		return done;
	}
	

	public ResultMention getResultMention() {
		return resultMention;
	}


	private String[] splitMention( String sentence ){
		sentence = sentence.replace(", ", " , ");
		sentence = sentence.replace(".", " .");
		return sentence.split(" ");
	}


	public String[] getMentionSplitted() {
		return this.mentionSplitted;
	}
	
	
}
