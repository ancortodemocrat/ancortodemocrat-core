package com.democrat.ancortodemocrat;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Relation;

public class Corpus {
	
	private static Logger logger = Logger.getLogger(Corpus.class);
	
	
	/**
	 * name of the corpus
	 */
	public String name;
	/**
	 * absolute path of the corpus without his name
	 */
	public String path;
	/**
	 * List of annotation file
	 */
	private List<Annotation> annotation;
	/**
	 * List of text file
	 */
	private List<Text> text = new ArrayList<Text>();

	private boolean done;
	
	/**
	 * 
	 * @param path dossier où se trouve le corpus, doit contenir les dossiers aa_fichiers, et ac_fichiers
	 * 			contenant respectivement des fichiers .aa et des fichiers .ac
	 */
	public Corpus(String path) {
		this(path, new ArrayList<Annotation>());
		
	}
	
	/**
	 * 
	 * @param path dossier où se trouve le corpus, doit contenir les dossiers aa_fichiers, et ac_fichiers
	 * 			contenant respectivement des fichiers .aa et des fichiers .ac
	 * @param list Liste des annotations du corpus précisés
	 */
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
			logger.error("Chemin du corpus invalide: "+path);
		}
		this.annotation = list;
	}
	
	/**
	 * Exporte les fichiers aa du corpus
	 * sous le chemin spécifié
	 * @param path dossier où sera exporté le dossier aa_fichiers du corpus avec les .aa dedans
	 */
	public void export( String path ){
		AncorToDemocrat.fileManager.mkdir( path );
		AncorToDemocrat.fileManager.mkdir( path + "/aa_fichiers/");
		for(Annotation a : this.annotation){
			XmlWriter.writeXml(a, path + "/aa_fichiers/" + a.getFileName() + ".aa");
		}
		
	}
	
	/**
	 * Exporte les fichiers aa du corpus
	 * dans le dossier par défaut: generated/corpus/nomDuCorpus
	 */
	public void export(){
		this.export( "generated/corpus/" + this.getName() );
	}
	
	
	/**
	 * Charge les annotations du corpus,;
	 * les annotations se résument aux fichiers .aa
	 */
	public void loadAnnotation(){
		List<String> annotationFile = AncorToDemocrat.fileManager.loadAaFile( this );
		for(String str : annotationFile){
			File file = new File(this.getPath() + "/aa_fichiers/" + str);

			Annotation annotation = XmlLoader.loadAnnotationFromFile( file.getAbsolutePath() );
			annotation.setFileName( str.substring(0, str.length() - 3) );
			this.annotation.add( annotation );

		}
	}
	
	/**
	 * Charges les fichiers textes bruts du corpus,
	 * les fichiers textes se résument aux fichiers .ac
	 */
	public void loadText(){
		List<String> textFile = AncorToDemocrat.fileManager.loadAcFile( this );
		for(String fileName : textFile){
			File file = new File(this.getPath() + "/ac_fichiers/" + fileName);
			
			Scanner sc = null;
			try {
				sc = new Scanner( file );
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String str = new String();
			while(sc.hasNextLine()){
			    str += sc.nextLine();                     
			}
			Text text = new Text( fileName.substring(0, fileName.length() - 3 ), str );
			this.text.add( text );
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

	public boolean isDone() {
		return this.done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public Text getText(String fileName) {
		for(int t = 0; t < this.text.size(); t++){
			if( text.get( t ).getFileName().equals( fileName ) ){
				return text.get( t );
			}
		}
		return null;
		
	}

}
