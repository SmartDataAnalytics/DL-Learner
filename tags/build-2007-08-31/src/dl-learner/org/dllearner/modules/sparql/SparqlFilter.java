package org.dllearner.modules.sparql;

public class SparqlFilter {
	public int mode=0;
	//  0 yago, 1 only cat, 2 skos+cat
	String[] PredFilter=null;
	String[] ObjFilter=null;
	boolean useLiterals=false;
	
	
	String[] yagoPredFilterDefault={
			"http://www.w3.org/2004/02/skos/core",
			"http://xmlns.com/foaf/0.1/",	
			"http://dbpedia.org/property/wikipage-",
			"http://www.w3.org/2002/07/owl#sameAs",
			"http://dbpedia.org/property/reference"	};
	String[] yagoObjFilterDefault={
			"http://dbpedia.org/resource/Category:Articles_",
			"http://dbpedia.org/resource/Category:Wikipedia_",
			"http://xmlns.com/foaf/0.1/",
			"http://dbpedia.org/resource/Category",
			"http://dbpedia.org/resource/Template",
			"http://upload.wikimedia.org/wikipedia/commons"};
	
	String[] onlyCatPredFilterDefault={
			"http://www.w3.org/2004/02/skos/core",
			"http://xmlns.com/foaf/0.1/",	
			"http://dbpedia.org/property/wikipage-",
			"http://www.w3.org/2002/07/owl#sameAs",
			"http://dbpedia.org/property/reference"	};
	String[] onlyCatObjFilterDefault={
			"http://dbpedia.org/resource/Category:Articles_",
			"http://dbpedia.org/resource/Category:Wikipedia_",
			"http://xmlns.com/foaf/0.1/",
			"http://dbpedia.org/class/yago",
			"http://dbpedia.org/resource/Template",
			"http://upload.wikimedia.org/wikipedia/commons"};
	
	String[] skosPredFilterDefault={
			"http://www.w3.org/2004/02/skos/core#narrower",
			"http://xmlns.com/foaf/0.1/",	
			"http://dbpedia.org/property/wikipage-",
			"http://www.w3.org/2002/07/owl#sameAs",
			"http://dbpedia.org/property/reference"	};
	String[] skosObjFilterDefault={
			"http://dbpedia.org/resource/Category:Articles_",
			"http://dbpedia.org/resource/Category:Wikipedia_",
			"http://xmlns.com/foaf/0.1/",
			"http://dbpedia.org/class/yago",
			"http://dbpedia.org/resource/Template",
			"http://upload.wikimedia.org/wikipedia/commons"};
	
	public SparqlFilter(int mode, String[] pred, String[] obj) {
		if (mode==-1 && (pred==null || pred.length==0 || obj==null||obj.length==0))
			{mode=0;}
		
		switch (mode){
		case 0: //yago
			ObjFilter=yagoObjFilterDefault;
			PredFilter=yagoPredFilterDefault;
			break;
		case 1: // only Categories
			ObjFilter=onlyCatObjFilterDefault;
			PredFilter=onlyCatPredFilterDefault;			
			break;
		case 2:
			ObjFilter=skosObjFilterDefault;
			PredFilter=skosPredFilterDefault;			
			break;
		default:
			ObjFilter=obj;
			PredFilter=pred;
			break;
		
	}}
	public SparqlFilter(int mode, String[] pred, String[] obj,boolean uselits) throws Exception{
		this(mode,  pred,obj);
		this.useLiterals=uselits;
	}
	
	public String[] getObjFilter(){
		return this.ObjFilter;
		}
	public String[] getPredFilter(){
		return this.PredFilter;
		}
	
	
		
	}

