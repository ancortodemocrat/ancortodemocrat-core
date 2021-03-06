package com.democrat.ancortodemocrat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * File Manager
 * @author Alexis Puret
 */
public class FileManager {

	private static Logger logger = Logger.getLogger(FileManager.class);

	public FileManager(){
		init();
	}

	/**
	 * Initialisation
	 */
	private void init(){
		//path.txt
		File filePath = new File("path.txt");
		//if path doesnt exist
		if( ! filePath.exists() ){
			try {
				List<String> lines = Arrays.asList("#Vous pouvez mettre des commentaires comme ceci",
						"#Ajouter le(s) chemin(s) absolu(s) que vous souhaitez des corpus que vous souhaitez.",
						"#chaque corpus doit comporter un dossier 'aa_fichiers' et 'ac_fichiers'");
				Path file = Paths.get("path.txt");

				Files.write(file, lines, Charset.forName( "UTF-8" ));
				//we create
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//generated folder
		mkdir( "generated" );
		
		//corpus folder
		mkdir( "generated/corpus" );
		
		mkdir( "generated/arff" );
	}

	/**
	 * returns a list of file name from a folder path
	 * @param folder of the folder
	 * @param ext only the extension without the dot
	 * @return list of file name from a folder path
	 */
	public ArrayList<String> getFileFromFolder(File folder, String ext){

		if( ! folder.isDirectory() ){
			throw new IllegalArgumentException("Bad path, folder path expected");
		}

		File[] files = folder.listFiles();
		ArrayList<String> list = new ArrayList<String>();
		for(int f = 0; f < files.length; f++){
			if(files[f].getName().endsWith("."+ext)){
				list.add( files[f].getName() );
			}
		}
		return list;
	}

	/**
	 * returns a list of folder names from a folder path
	 * @param folder of the folder
	 * @return list of folder names from a folder path
	 */
	public ArrayList<String> getFolderFromFolder(File folder ){
		if( ! folder.isDirectory() ){
			throw new IllegalArgumentException("Bad path, folder path expected");
		}

		File[] files = folder.listFiles();
		ArrayList<String> list = new ArrayList<String>();
		for(int f = 0; f < files.length; f++){
			if(files[f].isDirectory() ){
				list.add( files[f].getName() );
			}
		}
		return list;
	}

	/**
	 * Loading path.txt file
	 * @return list of path in the file
	 */
	public List<String> loadPathFile(){
		InputStream pathIn = null;
		try {
			pathIn = new FileInputStream( new File("path.txt") );
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader( pathIn ));

		ArrayList<String> list = new ArrayList<String>();
		String line;
		logger.info("Path read:");
		try {
			while ( ( line = reader.readLine() ) != null) {
				if( ! line.startsWith("#") && ! line.isEmpty() ){
					list.add( line );
					logger.info("> "+line);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		if( list.isEmpty() ){
			logger.info("Nothing..");
		}
		return list;
	}

	public void mkdir(String path){
		File folder = new File( path );
		if( ! folder.exists() || ! folder.isDirectory() ){
			folder.mkdir();
		}
	}


	/**
	 * return the name of every file in aa_fichier folder
	 * @param corpus
	 * @return
	 */
	public List<String> loadAaFile(Corpus corpus){

		File folder = new File( corpus.getPath() + "/aa_fichiers" );
		if( folder.exists() ){
			return this.getFileFromFolder(folder, "aa");
		}else{
			logger.error("Folder doesn't exists: "+corpus.getPath() +" aa folder missing");
		}
		return new ArrayList<String>();
	}

	/**
	 * return the name of every file in ac_fichier folder
	 * @param corpus
	 * @return
	 */
	public List<String> loadAcFile(Corpus corpus){
		File folder = new File( corpus.getPath() + "/ac_fichiers" );
		if( folder.exists() ){
			return this.getFileFromFolder(folder, "ac");
		}else{
			logger.error("Folder doesn't exists: "+corpus.getPath() +" ac folder missing");
		}
		return new ArrayList<String>();
	}

}
