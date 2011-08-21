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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.ibm.icu.util.StringTokenizer;

/**
 * @author Jens Lehmann
 * 
 */
public class CSVFileToArray {
	public String filename;
	BufferedReader br; 
	String separator = "	";
	
	public CSVFileToArray(String filename, String separator)throws FileNotFoundException, IOException{
		this(filename);	
		this.separator = separator;
			
		
	}
	
	public CSVFileToArray(String filename)throws FileNotFoundException, IOException{
		File file = new File(filename);
		this.br = new BufferedReader(new FileReader(file));
		
	}
	
	
	public ArrayList<String> next()throws IOException{
		String line = this.br.readLine();
		if(line == null) {
			br.close();
			return null;}
		ArrayList<String> a = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(line,this.separator);
		while(st.hasMoreElements()){
			a.add(st.nextToken());
			
		}
		return a;
		
	}
	

	
	

}
