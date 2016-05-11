package com.democrat.ancortodemocrat;

import java.io.File;
import java.util.ArrayList;

public class FileManager {
	
	/**
	 * return a list of file name from a folder path
	 * @param path of the folder
	 * @param ext only the extension without the dot
	 * @return
	 */
	public static ArrayList<File> fileFromFolder(String path, String ext){
		
		File folder = new File(path);
		
		if( ! folder.isDirectory() ){
			throw new IllegalArgumentException("Bad path, folder path expected");
		}
		
		File[] files = folder.listFiles();
		ArrayList<File> list = new ArrayList<File>();
		for(int f = 0; f < files.length; f++){
			if(files[f].getName().endsWith("."+ext)){
				list.add( files[f] );
				System.out.println(files[f].getName());
			}
		}
		return list;
	}

}
