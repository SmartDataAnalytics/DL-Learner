package org.dllearner.modules.sparql;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Util {

	
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
	
	public static void printHashSet(HashSet<String> h){
		Iterator<String> it=h.iterator();
		String current="";
		while (it.hasNext()){
			current=it.next();
			
			if(current.contains("http://dbpedia.org/resource/"))System.out.println("test(\""+current+"\").");
			}
	}
}
