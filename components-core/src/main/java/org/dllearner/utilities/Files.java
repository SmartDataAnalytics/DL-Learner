/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.google.common.io.FileWriteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

/**
 * @author Jens Lehmann
 * 
 */
public class Files {

	private static final Logger logger = LoggerFactory.getLogger(Files.class);
	
	public static boolean debug = false;

	/**
	 * Reads input from a URL and stores it in a string (only recommend for small files).
	 * @param file URL of a file.
	 * @return Contents of the file.
	 * @throws IOException URL not accessible or content cannot be read for some reason.
	 */
	public static String readFile(URL file) throws IOException {
		 BufferedReader in = new BufferedReader(new InputStreamReader(file.openStream()));

		StringBuilder input = new StringBuilder();
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
		    input.append(inputLine).append("\n");
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
	public static String readFile(File file) throws IOException {

		StringBuilder content = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			while ((line = br.readLine()) != null) {
				content.append(line);
				content.append(System.getProperty("line.separator"));
			}
		}
		return content.toString();
		
	}
	
	/**
	 * Reads in a file as Array
	 * 
	 * @param file
	 *            The file to read.
	 * @return StringArray with lines
	 */
	public static String[] readFileAsArray(File file) throws IOException {
		String content = readFile(file);
		StringTokenizer st = new StringTokenizer(content, System.getProperty("line.separator"));
		List<String> l = new ArrayList<>();
		while (st.hasMoreTokens()) {
			l.add(st.nextToken());
			
		}
		
		return l.toArray(new String[l.size()]);
		
	}
	
	/**
	 * writes a serializable Object to a File.
	 * @param obj the object
	 * @param file the file
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
		File parentFile = file.getParentFile();
		if (parentFile != null) { parentFile.mkdirs(); }
		try {
			com.google.common.io.Files.asCharSink(file, Charsets.UTF_8).write(content);
		} catch (IOException e) {
			logger.error("Failed to write content to file " + file, e);
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
			com.google.common.io.Files.asCharSink(file, Charsets.UTF_8, FileWriteMode.APPEND).write(content);
		} catch (IOException e) {
			logger.error("Failed to append content to file " + file, e);
		}
	}

	/**
	 * Write content to a file.
	 *
	 * @param file
	 *            The file to create.
	 * @param content
	 *            Content of the file.
	 */
	public static void writeToFile(String content, File file) throws IOException {
		java.nio.file.Files.write(file.toPath(), content.getBytes());
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
	

}
