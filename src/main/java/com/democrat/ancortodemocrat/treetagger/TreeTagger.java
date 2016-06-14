package com.democrat.ancortodemocrat.treetagger;

import java.io.IOException;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.feature.CalculateFeature;

public class TreeTagger {
	private static Logger logger = Logger.getLogger(TreeTagger.class);

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
			logger.debug("Dossier de TreeTagger non trouvé !");
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
