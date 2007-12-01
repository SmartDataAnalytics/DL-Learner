package org.dllearner.kb.extraction.datastructures;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.dllearner.kb.extraction.Manipulator;
import org.dllearner.kb.extraction.sparql.TypedSparqlQuery;

public class ClassNode extends Node{
	Set<PropertyNode> properties=new  HashSet<PropertyNode>();
	
	public ClassNode(URI u) {
		super(u);
		this.type="class";
	}
	
	@Override
	public Vector<Node> expand(TypedSparqlQuery tsq,Manipulator m){
		Set<Tupel> s =tsq.query(this.URI);
		s=m.check(s, this);
		Vector<Node> Nodes=new Vector<Node>();
		// Manipulation
		
		Iterator<Tupel> it=s.iterator();
		while(it.hasNext()){
			Tupel t=(Tupel)it.next();
			try{
			if(t.a.equals(m.type) || t.a.equals(m.subclass)){
				ClassNode tmp=new ClassNode(new URI(t.b));
				properties.add(new PropertyNode(new URI(m.subclass),this,tmp));
				Nodes.add(tmp);
			}
			}catch (Exception e) {System.out.println(t);e.printStackTrace();}
			
		}
		return Nodes;
	}
	
	@Override
	public boolean isClass(){
		return true;
	}
	
	@Override
	public  Set<String> toNTriple(){
		Set<String> s= new HashSet<String>();
		s.add("<"+this.URI+"><"+"http://www.w3.org/1999/02/22-rdf-syntax-ns#type"+"><"+"http://www.w3.org/2002/07/owl#Class"+">.");
		
		for (PropertyNode one:properties){
			s.add("<"+this.URI+"><"+one.getURI()+"><"+one.getB().getURI()+">.");
			s.addAll(one.getB().toNTriple());
		}
		
		return s;
	}
	
}
