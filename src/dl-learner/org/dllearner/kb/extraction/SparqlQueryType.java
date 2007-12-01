package org.dllearner.kb.extraction;

import java.util.Set;

public class SparqlQueryType {

	private String mode="forbid";
	private String[] objectfilterlist={
			"http://dbpedia.org/resource/Category:Articles_",
			"http://dbpedia.org/resource/Category:Wikipedia_",
			"http://xmlns.com/foaf/0.1/",
			"http://dbpedia.org/resource/Category",
			"http://dbpedia.org/resource/Template",
			"http://upload.wikimedia.org/wikipedia/commons"};
	private String[] predicatefilterlist={
			"http://www.w3.org/2004/02/skos/core",
			"http://xmlns.com/foaf/0.1/",	
			"http://dbpedia.org/property/wikipage-",
			"http://www.w3.org/2002/07/owl#sameAs",
			"http://dbpedia.org/property/reference"	};
	private boolean literals=false;
	
	public SparqlQueryType(String mode, String[] obectfilterlist, String[] predicatefilterlist, boolean literals) {
		super();
		this.mode = mode;
		this.objectfilterlist = obectfilterlist;
		this.predicatefilterlist = predicatefilterlist;
		this.literals = literals;
	}
	
	public SparqlQueryType(String mode, Set<String> objectfilterlist, Set<String>  predicatefilterlist, String literals) {
		super();
		this.mode = mode;
		this.literals = (literals.equals("true"))?true:false;
		
		Object[] arr=objectfilterlist.toArray();
		Object[] arr2=predicatefilterlist.toArray();
		this.objectfilterlist = new String[arr.length];
		this.predicatefilterlist = new String[arr2.length];
		for (int i = 0; i < arr.length; i++) {
			this.objectfilterlist[i]=(String)arr[i];
		}
		for (int i = 0; i < arr2.length; i++) {
			this.predicatefilterlist[i]=(String)arr2[i];
		}
		
		
	}

	public boolean isLiterals() {
		return literals;
	}

	public String getMode() {
		return mode;
	}

	public String[] getObjectfilterlist() {
		return objectfilterlist;
	}

	public String[] getPredicatefilterlist() {
		return predicatefilterlist;
	}
	
	
	
}
