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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A map of prefixes based on http://prefix.cc/ service. The prefixes are cached locally in
 * src/main/resources/prefixes.csv. An update of this file has to be forced manually by running the main method
 * of this class.
 *
 * @author Lorenz Buehmann
 */
public class PrefixCCMap extends HashMap<String, String>{
	
	private static final String LOCAL_FILE = "prefixes.csv";
	
	private static PrefixCCMap instance;
	
	private PrefixCCMap(){
		fillMap();
	}
	
	private void fillMap() {
		try(InputStream is = this.getClass().getClassLoader().getResourceAsStream(LOCAL_FILE)) {
			try(BufferedReader bufRdr = new BufferedReader(new InputStreamReader(is))) {
				String line = null;
				String key = null;
				String value = null;
				while ((line = bufRdr.readLine()) != null) {
					String[] entry = line.split(",");
					if(entry.length == 2){
						key = entry[0].trim();
						value = entry[1].trim();

						put(key, value);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static PrefixCCMap getInstance(){
		if(instance == null){
			instance = new PrefixCCMap();
		}
		return instance;
	}
	
	/**
	 * This main methods updates the local prefix file by loading latest prefix list from prefix.cc.
	 * @param args the arguments
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		//load latest file from prefix.cc
		String target = "src/main/resources/prefixes.csv";
		URL google = new URL("http://prefix.cc/popular/all.file.csv");
	    ReadableByteChannel rbc = Channels.newChannel(google.openStream());
	    File file = new File(target);
	    if(!file.exists()){
	    	file.createNewFile();
	    }
	    FileOutputStream fos = new FileOutputStream(file);
	    fos.getChannel().transferFrom(rbc, 0, 1 << 24);
	    fos.close();
	    
	    //Reload file and filter entries where second argument is empty
	    File tmpFile = new File(target + ".tmp");
	    File inFile = new File(target);
	    PrintWriter pw = new PrintWriter(new FileWriter(tmpFile));
	    BufferedReader br = new BufferedReader(new FileReader(inFile));
		String line = null;
		Set<String> values = new HashSet<>();
		while ((line = br.readLine()) != null) {
			String[] entry = line.split(",");
			if(entry.length == 2){
				String key = entry[0];
				String value = entry[1];
				value = value.substring(1);
				value = value.substring(0, value.length()-1);
				if(!value.trim().isEmpty() && !values.contains(value)){
					values.add(value);
					pw.println(entry[0] + "," + value);
					pw.flush();
				}
			}
		}
		pw.close();
	    br.close();
	    
	    inFile.delete();
	    tmpFile.renameTo(inFile);
	}

}
