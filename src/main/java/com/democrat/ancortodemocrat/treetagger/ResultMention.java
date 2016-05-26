package com.democrat.ancortodemocrat.treetagger;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the results of one sequence parsed by TreeTagger
 */
public class ResultMention {
	
	private List<ResultToken> tokenList;
	
	
	public ResultMention(){
		tokenList = new ArrayList<ResultToken>();
	}
	
	public void newToken( ResultToken token ){
		this.tokenList.add( token );
	}

	public List<ResultToken> getTokenList() {
		return tokenList;
	}
	
	public boolean containsNoun(){
		for(int t = 0; t < tokenList.size(); t++){
			if( tokenList.get( t ).isNoun() ){
				return true;
			}
		}
		return false;
	}

	/**
	 * Return a list containing the noun of the sequence
	 * @return
	 */
	public List<ResultToken> getNounList(){
		List<ResultToken> nounList = new ArrayList<ResultToken>();
		for(int t = 0; t < tokenList.size(); t++){
			if( tokenList.get( t ).isNoun() ){
				nounList.add( tokenList.get( t ) );
			}
		}
		return nounList;
	}

	@Override
	public String toString() {
		String str = "";
		for(int t = 0; t < tokenList.size(); t++){
			str += tokenList.get( t ).getToken() + "\t" + tokenList.get( t ).getPos() + "\t" + tokenList.get( t ).getLemma() + System.lineSeparator();
		}
		return str;
	}
	
	
	

}
