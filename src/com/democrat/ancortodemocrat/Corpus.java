package com.democrat.ancortodemocrat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.element.Annotation;

public class Corpus {
	
	private static Logger logger = Logger.getLogger(Corpus.class);
	
	public String name;
	public String path;
	private List<Annotation> annotation;
	
	
	public Corpus(String path) {
		this(path, new ArrayList<Annotation>());
	}
	
	public Corpus(String path, List<Annotation> list){
		this.path = path;
		if(path.contains( "\\" )){
			this.path = this.path.replace("\\", "/");
			String[] pathSplited = this.path.split( "/" );
			this.name = pathSplited[ pathSplited.length - 1 ];
		}else if(path.contains( "/" )){
			String[] pathSplited = path.split( "/" );
			this.name = pathSplited[ pathSplited.length - 1 ];
		}else{
			logger.error("Corpus path not valid: "+path);
		}
		this.annotation = list;
	}
	
	public void export(){
		AncorToDemocrat.fileManager.mkdir("generated/" + this.getName());
		AncorToDemocrat.fileManager.mkdir("generated/" + this.getName() + "/aa_fichiers/");
		for(Annotation a : this.annotation){
			XmlWriter.writeXml(a, "generated/" + this.getName() + "/aa_fichiers/" + a.getFileName());
		}
	}
	
	public void loadAnnotation(){
		List<String> annotationFile = AncorToDemocrat.fileManager.loadAaFile( this );
		for(String str : annotationFile){
			File file = new File(this.getPath() + "/aa_fichiers/" + str);

			Annotation annotation = XmlLoader.loadAnnotationFromFile( file.getAbsolutePath() );
			annotation.setFileName( str );
			this.annotation.add( annotation );

		}
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}

	public List<Annotation> getAnnotation() {
		return annotation;
	}

	public void setAnnotation(List<Annotation> annotation) {
		this.annotation = annotation;
	}
	
	
	
	
	
	

}
