/**
 * Copyright (C) 2007, Sebastian Hellmann
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
package org.dllearner.modules.sparql;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * mainly converts datatypes
 * 
 * TODO: move to org.dllearner.utilities
 * 
 * @author Sebastian Hellmann
 *
 */
public class Util {

	
	/**
	 * easy conversion
	 * 
	 * @param s
	 * @return
	 */
	public  static String[] setToArray(Set<String> s){
		if(s==null)return null;
		String[] ret=new String[s.size()];
		int i=0;
		for (Iterator<String> iter = s.iterator(); iter.hasNext();) {
			ret[i] = iter.next();
			i++;
			
		}
		return ret;
		
	}
	
	/**
	 * Warning use only for visualization
	 * @param s
	 * @return String without certain namespaces
	 */
	public static String replaceNamespace(String s){
		s=s.replace("http://dbpedia.org/class/yago/", "yago:");
		s=s.replace("http://dbpedia.org/class/", "yago2:");
		s=s.replace("http://dbpedia.org/resource/Category:", "cat:");
		s=s.replace("http://dbpedia.org/resource/Template:", "temp:");
		s=s.replace("http://www.w3.org/2004/02/skos/core#", "skos:");
		
		s=s.replace("http://dbpedia.org/property/", "prop:");
		//s=s.replace("http://dbpedia.org/resource/", "base:");
		return s;
	}
	
	/**
	 * prints a hashset to stdout
	 * @param h
	 */
	public static void printHashSet(HashSet<String> h){
		Iterator<String> it=h.iterator();
		String current="";
		while (it.hasNext()){
			current=it.next();
			
			if(current.contains("http://dbpedia.org/resource/"))System.out.println("test(\""+current+"\").");
			}
	}
}
