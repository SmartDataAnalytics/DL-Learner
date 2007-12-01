package org.dllearner.kb.extraction;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dllearner.kb.extraction.datastructures.Node;
import org.dllearner.kb.extraction.datastructures.Tupel;

public class Manipulator {
	public String subclass="http://www.w3.org/2000/01/rdf-schema#subClassOf";
	public String type="http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	
	String objectProperty="http://www.w3.org/2002/07/owl#ObjectProperty";
	String classns="http://www.w3.org/2002/07/owl#Class";
	String thing="http://www.w3.org/2002/07/owl#Thing";
	
	Set<String> classproperties;
	
	
	String[] defaultClasses={
			"http://dbpedia.org/class/yago",
			"http://dbpedia.org/resource/Category:",
			"http://dbpedia.org/resource/Template:",
			"http://www.w3.org/2004/02/skos/core",
			"http://dbpedia.org/class/"}; //TODO FEHLER hier fehlt yago
	
	public Manipulator(){
		Set<String> classproperties=new HashSet<String>();
		classproperties.add(subclass);
		
	}
	
	public Set<Tupel> check(Set<Tupel> s,Node node){
		Set<Tupel> toRemove=new HashSet<Tupel>();
		Iterator<Tupel> it=s.iterator();
		while(it.hasNext()){
			Tupel t=(Tupel)it.next();
			//all classes with owl:type class
			if(t.a.equals(this.type) && t.b.equals(this.classns)&& node.isClass() )
				{toRemove.add(t);};
			// all with type class  
			if( t.b.equals(this.classns) && node.isClass() )
				{toRemove.add(t);};
			// all instances with owl:type thing
			if(t.a.equals(this.type) && t.b.equals(this.thing)&& node.isInstance() )
				{toRemove.add(t);};
			
		}
		s.removeAll(toRemove);
		
		return s;
	}
	
}
