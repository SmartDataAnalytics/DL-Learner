/**
 * Copyright (C) 2007, Jens Lehmann
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
 *
 */
package org.dllearner.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Jens Lehmann
 * 
 */
public class Files {
	public static boolean debug = true;

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
	public static void appendFile(File file, String content) {
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
