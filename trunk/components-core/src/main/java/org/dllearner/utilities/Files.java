/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.ibm.icu.util.StringTokenizer;

/**
 * @author Jens Lehmann
 * 
 */
public class Files {
	public static boolean debug = false;

	/**
	 * Reads input from a URL and stores it in a string (only recommend for small files).
	 * @param file URL of a file.
	 * @return Contents of the file.
	 * @throws IOException URL not accessible or content cannot be read for some reason.
	 */
	public static String readFile(URL file) throws IOException {
		 BufferedReader in = new BufferedReader(new InputStreamReader(file.openStream()));

		StringBuffer input = new StringBuffer();
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
		    input.append(inputLine + "\n");
		}		    
		in.close();
			    
		return input.toString();
	}
	
	/**
	 * Reads in a file.
	 * 
	 * @param file
	 *            The file to read.
	 * @return Content of the file.
	 */
	public static String readFile(File file) throws FileNotFoundException, IOException {
			
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuffer content = new StringBuffer();
		try{
		String line;
		
		while ((line = br.readLine()) != null) {
			content.append(line);
			content.append(System.getProperty("line.separator"));
		}
		}finally{br.close();}
		return content.toString();
		
	}
	
	/**
	 * Reads in a file as Array
	 * 
	 * @param file
	 *            The file to read.
	 * @return StringArray with lines
	 */
	public static String[] readFileAsArray(File file) throws FileNotFoundException, IOException {
		String content = readFile(file);
		StringTokenizer st = new StringTokenizer(content, System.getProperty("line.separator"));
		List<String> l = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			l.add((String) st.nextToken());
			
		}
		
		return l.toArray(new String[l.size()]);
		
	}
	
	/**
	 * writes a serializable Object to a File.
	 * @param obj
	 * @param file
	 */
	public static void writeObjectToFile(Object obj, File file){
		
		ObjectOutputStream oos = null;
		try{
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			oos = new ObjectOutputStream(bos);
			
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			}catch (Exception e) {
				 e.printStackTrace();
			}finally{
				try{
					oos.close();
				}catch (Exception e) {
					 e.printStackTrace();
				}
			}
	}
	
	public static Object readObjectfromFile( File file){
		ObjectInputStream ois = null;
		try{
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ois = new ObjectInputStream(bis);
			return ois.readObject();
		}catch (Exception e) {
			 e.printStackTrace();
		}finally{
			try {
				ois.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		return null;
	}
		

	/**
	 * Creates a new file with the given content or replaces the content of a
	 * file.
	 * 
	 * @param file
	 *            The file to use.
	 * @param content
	 *            Content of the file.
	 */
	public static void createFile(File file, String content) {
		
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(content.getBytes());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			if(debug){System.exit(0);}
		} catch (IOException e) {
			e.printStackTrace();
			if(debug){System.exit(0);}
		}
	}

	/**
	 * Appends content to a file.
	 * 
	 * @param file
	 *            The file to create.
	 * @param content
	 *            Content of the file.
	 */
	public static void appendToFile(File file, String content) {
		try {
			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write(content.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			if(debug){System.exit(0);}
		} catch (IOException e) {
			e.printStackTrace();
			if(debug){System.exit(0);}
		}
	}
	
	public static void clearFile(File file) {
		try{
		createFile(file, "");
		}catch (Exception e) {
			e.printStackTrace();
			if(debug){System.exit(0);}
		}
	}
	
	
	public static void deleteFile(String file) {
		deleteFile(new File(file));
	}
	
	public static void deleteFile(File file) {
		
		try{
			file.delete();
		}catch (Exception e) {
			e.printStackTrace();
			if(debug){System.exit(0);}
		}
	}
	
	public static void mkdir(String dir){
		if (!new File(dir).exists()) {
			try{
			new File(dir).mkdir();
			}catch (Exception e) {
				e.printStackTrace();
				if(debug){System.exit(0);}
				// this should not be a show stopper
			}		
		}
	}
	
	/**
	 * deletes all Files in the dir, does not delete the dir itself
	 * no warning is issued, use with care, cannot undelete files
	 *
	 * @param dir without a separator e.g. tmp/dirtodelete
	 */
	public static void deleteDir(String dir) {
		
			File f = new File(dir);
			
			if(debug){
				System.out.println(dir);
				System.exit(0);
			}
			
		    String[] files = f.list();
		   
		    for (int i = 0; i < files.length; i++) {
		    	
		    	Files.deleteFile(new File(dir+File.separator+files[i]));
		    }     
	}
	
	/**
	 * lists all files in a directory
	 * 
	 *
	 * @param dir without a separator e.g. tmp/dir
	 * @return a string array with filenames
	 */
	public static String[] listDir(String dir) {
		
			File f = new File(dir);
			
			if(debug){
				System.out.println(dir);
				System.exit(0);
			}
			
		    return f.list();
		   
		   
	}
	
	/**
	 * copies all files in dir to "tmp/"+System.currentTimeMillis()
	 * @param dir the dir to be backupped
	 */
	public static void backupDirectory(String dir){
		File f = new File(dir);
		String backupDir = "../tmp/"+System.currentTimeMillis();
		mkdir("../tmp");
		mkdir(backupDir);
		
		if(debug){
			System.out.println(dir);
			System.exit(0);
		}
		
	    String[] files = f.list();
	   try{
	    for (int i = 0; i < files.length; i++) {
	    	File target = new File(dir+File.separator+files[i]);
	    	if(!target.isDirectory()){
	    		String s = readFile(target);
	    		createFile(new File(backupDir+File.separator+files[i]), s);
	    	}
	    }   
	   }catch (Exception e) {
		e.printStackTrace();
	}
	}
	
	

}
