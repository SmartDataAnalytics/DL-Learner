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

package org.dllearner.utilities.analyse;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.utilities.Files;
import org.dllearner.utilities.analyse.CountInstances.Count;

public class ScriptDoAll {
	
	public static String subclassof = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
	public static String broader = "http://www.w3.org/2004/02/skos/core#broader";
	
	public static String subject = "http://www.w3.org/2004/02/skos/core#subject";
	public static String rdftype = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	
	
	public static String catns = "http://dbpedia.org/resource/Category:";
	public static String dbns = "http://dbpedia.org/ontology/";
	public static String yagons = "http://dbpedia.org/class/yago/";
	
	static CountInstances c = new CountInstances("http://db0.aksw.org:8893/sparql", Arrays.asList(new String[]{"http://dbpedia.org/ontology"}));
	
	public static void main(String[] args) {
		
		String dbpediaFile = "dbpedia_3.5.1.owl";
		@SuppressWarnings("unused")
		String yagoFile = "yagoclasses_links.nt";
		@SuppressWarnings("unused")
		String categoryFile = "skoscategories_en.nt";
		
		doIt(dbpediaFile, "RDF/XML", subclassof, rdftype, dbns,false);
//		doIt(yagoFile, "N-TRIPLES", subclassof, rdftype, yagons,false);
//		doIt(categoryFile, "N-TRIPLES", broader, subject, catns, true);
		
	}
	
	public static void doIt(String file, String format, String relation, String type, String nsFilter, boolean noExpand){
		
		Map<String, SortedSet<String>>  dbdown = new Hierarchy().getHierarchyDown(file, format, relation, noExpand);
		Files.writeObjectToFile(dbdown, new File(file+".sub.ser"));
		Map<String, SortedSet<String>>  dbup = new Hierarchy().getHierarchyUp(file, format,  relation, noExpand);
		Files.writeObjectToFile(dbup, new File(file+".super.ser"));
		
		dbup = null;

		List<Count> countdb = c.countInstances(type, nsFilter);
		
		toFile(countdb, file+".count");
		
		toFile(expand(countdb, dbdown), file+".expanded.count");
		
		Files.writeObjectToFile(purge(countdb, dbdown), new File( file+".purged.ser"));
		
		
	}
	
	public static Map<String, SortedSet<String>>  purge(List<Count> count, Map<String, SortedSet<String>> hierarchy){
		Map<String, Integer> map = toMap(count);
//		System.out.println(hierarchy.size());
		Map<String, SortedSet<String>> ret = new HashMap<String, SortedSet<String>>();
		for(String key: hierarchy.keySet()){
			SortedSet<String> tmp = new TreeSet<String>();
			for(String s : hierarchy.get(key)){
				if(map.get(s)!=null){
					tmp.add(s);
				}else{
//					System.out.println("purged: "+s);
				}
			}
			ret.put(key, tmp);
			
		}
//		System.out.println(ret.size());
		return ret;
	}
	
	public static List<Count> expand(List<Count> count, Map<String, SortedSet<String>> hierarchy){
		Map<String, Integer> classNrOfInstances = toMap(count);
		SortedSet<Count> ret = new TreeSet<Count>();
		SortedSet<String> allClasses = new TreeSet<String>();
		allClasses.addAll(classNrOfInstances.keySet());
		allClasses.addAll(hierarchy.keySet());
		
		for(String key : allClasses){
			
			SortedSet<String> expanded = hierarchy.get(key);
			int now = 0;
			if(classNrOfInstances.get(key) != null){
				now = classNrOfInstances.get(key).intValue();
			}
			
			if(expanded == null){
				//just add this one, i.e. no subclasses
				ret.add(c.new Count(key, now));
			}else{
				Integer add = null;
				for(String rel:expanded){
					if(!rel.equals(key) && (add = classNrOfInstances.get(rel))!=null ){
						now += add;
					}
				}
				ret.add(c.new Count(key, now));
			}
			
		}
		return new ArrayList<Count>(ret);
	}
	
	public static Map<String, Integer> toMap(List<Count> c){
		Map<String, Integer> ret = new HashMap<String, Integer>();
		for(Count count: c){
			ret.put(count.uri, new Integer(count.count));
		}
		return ret;
	}
	
	public static void toFile(List<Count> c, String filename){
		StringBuffer buf = new StringBuffer();
		for (Count count : c) {
			buf.append(count.toString()+"\n");
		}
		
		Files.createFile(new File(filename), buf.toString());
	}
	
	
	
	

}
