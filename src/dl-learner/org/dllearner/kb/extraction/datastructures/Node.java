package org.dllearner.kb.extraction.datastructures;

import java.net.URI;
import java.util.Set;
import java.util.Vector;

import org.dllearner.kb.extraction.Manipulator;
import org.dllearner.kb.extraction.sparql.TypedSparqlQuery;

public abstract class Node {
	URI URI;
	protected String type;
	protected boolean expanded=false;
	
	//Hashtable<String,Node> classes=new Hashtable<String,Node>();
	//Hashtable<String,Node> instances=new Hashtable<String,Node>();;
	//Hashtable<String,Node> datatype=new Hashtable<String,Node>();;
	
	public Node (URI u){
		this.URI=u;
		
	}
	
	/*public void checkConsistency(){
		if (type.equals("class") && ( instances.size()>0 || datatype.size()>0)){
			System.out.println("Warning, inconsistent:"+this.toString());
		}
		
	}*/
	
	public abstract Vector<Node> expand(TypedSparqlQuery tsq,Manipulator m);
	public abstract  Set<String> toNTriple();
	
	@Override
	public String toString(){
		return "Node: "+URI+":"+type;
		
	}
	
	
	public boolean isClass(){
		return false;
	}
	public boolean isInstance(){
		return false;
	}
	public boolean isProperty(){
		return false;
	}
	public URI getURI() {
		return URI;
	}
	
}
