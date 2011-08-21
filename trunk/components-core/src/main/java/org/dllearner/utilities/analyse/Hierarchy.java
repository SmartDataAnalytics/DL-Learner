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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class Hierarchy {
	public static String subclassof = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
	
	public static void main(String[] args) {
		printMap(new Hierarchy().getHierarchyDown("dbpedia_3.4.owl", "RDF/XML", subclassof, false));
		printMap(new Hierarchy().getHierarchyUp("dbpedia_3.4.owl", "RDF/XML", subclassof, false));
	}
	
	
	
	public static void printMap(Map<String, SortedSet<String>>  hierarchy){
		for(String key:hierarchy.keySet()){
			SortedSet<String> current = hierarchy.get(key);
			System.out.print(key+"\t");
			System.out.println(current);
		}
	}
	
	public Map<String, SortedSet<String>>  getHierarchyDown (String filename, String format, String relationUri, boolean noExpand){
		long n = System.currentTimeMillis();
		Map<String, SortedSet<String>> m =  getHierarchy(filename, format, relationUri, true, noExpand);
		System.out.println("hierarchy down needed "+(System.currentTimeMillis()-n));
		return m;
	}
	public Map<String, SortedSet<String>>  getHierarchyUp (String filename, String format, String relationUri, boolean noExpand){
		long n = System.currentTimeMillis();
		Map<String, SortedSet<String>> m =  getHierarchy(filename, format, relationUri, false, noExpand);
		System.out.println("hierarchy up needed "+(System.currentTimeMillis()-n));
		return m;
	}
		
	private Map<String, SortedSet<String>>  getHierarchy (String filename, String format, String relationUri, boolean invert, boolean noExpand){
		
		
		Model m = read(filename, format);
		Map<String, SortedSet<String>>  hierarchy  = new HashMap<String, SortedSet<String>>();
		for(Object o : m.listStatements(null, m.getProperty(relationUri), (RDFNode)null).toList()){
			Statement s = (Statement) o;
			Resource sub = s.getSubject();
			Resource obj = (Resource) s.getObject();
			if(sub.isAnon()||obj.isAnon()){
				continue;
			}
			if(invert){
				put(hierarchy, obj.getURI(), sub.getURI() );
			}else{
				put(hierarchy, sub.getURI(), obj.getURI() );
			}
		}
		
		return (noExpand)?hierarchy:expandHierarchy(hierarchy);
	}
	
	public Map<String, SortedSet<String>> expandHierarchy(Map<String, SortedSet<String>>  hierarchy){
		Map<String, SortedSet<String>>  expandedHierarchy  = new HashMap<String, SortedSet<String>>();
		for(String key : hierarchy.keySet()){
			boolean expanded = true;
			SortedSet<String> current = hierarchy.get(key); 
			if(current == null){
				continue;
			}
			while(expanded){
				expanded = false;
				SortedSet<String> tmp = new TreeSet<String>(current);
				for(String currentObject: current){
					SortedSet<String> toAddSet = hierarchy.get(currentObject);
					if(toAddSet==null){
						continue;
					}
					for(String toAdd :  toAddSet){
						if(tmp.add(toAdd)){
							expanded = true;
						}
					}//for2
				}//for1
				current = tmp;
			}//while
//			System.out.println("finished "+current.size()+"  "+key);
			expandedHierarchy.put(key, current);
		}//for
		return expandedHierarchy;
	}
	
	private void put(Map<String, SortedSet<String>>  hierarchy, String key, String value){
		if(hierarchy.get(key)==null){
			hierarchy.put(key, new TreeSet<String>());
		}
		hierarchy.get(key).add(value);
	}
	
	public Model read(String filename, String format){
		Model m = ModelFactory.createDefaultModel();
		long n = System.currentTimeMillis();
		m.read(new File(filename).toURI().toString(), format );
		System.out.println("reading "+filename+" needed "+(System.currentTimeMillis()-n));
		return m;
	}
	
//	@SuppressWarnings("unchecked")
//	public Map<String, SortedSet<String>>  getHierarchyUp (String filename, String format, String relationUri){
//		Model m = read(filename, format);
//		Map<String, SortedSet<String>>  hierarchy  = new HashMap<String, SortedSet<String>>();
//
//		for(Object o : m.listSubjects().toList()){
//			Resource c = (Resource) o;
//			if(c.isAnon()){
//				continue;
//			}
//			SortedSet<String> set = new TreeSet<String>();
//			
//			Resource next = c;
//			set.add(next.getURI());
//			while(next != null ){
//				List<Statement> l = next.listProperties().toList();
//				next = null;
//				for(Statement s: l){
//					if(s.getPredicate().getURI().contains(relationUri)){
//						
//						Resource r = (Resource) s.getObject();
//						set.add(r.getURI());
//						next = r; 
//					}
//				}
//				if(next != null && hierarchy.get(next.getURI())!=null){
//					set.addAll(hierarchy.get(next.getURI()));
//					next = null;
//				}
//			}
//			hierarchy.put(c.getURI(), set );
//		}
//		return hierarchy;
//	}
	
	
	
	

}
