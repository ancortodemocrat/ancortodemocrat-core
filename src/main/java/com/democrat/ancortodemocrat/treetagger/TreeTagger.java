package com.democrat.ancortodemocrat.treetagger;

import java.io.IOException;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

public class TreeTagger {

	private final String pathTreeTagger = "TreeTagger";

	public TreeTagger(){
		System.setProperty("treetagger.home", pathTreeTagger);
	}
	
	public void work( TokenConvertMentionHandler handler ){
		
		TreeTaggerWrapper<String> treeTaggerWrapper = new TreeTaggerWrapper<String>();

		try {
			treeTaggerWrapper.setModel(pathTreeTagger + "/model/french.par:UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		treeTaggerWrapper.setHandler( handler );
		try {
			treeTaggerWrapper.process( handler.getMentionSplitted() );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TreeTaggerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			treeTaggerWrapper.destroy();
		}
	}
	
	

}
