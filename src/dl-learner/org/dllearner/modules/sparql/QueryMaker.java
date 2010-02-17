package org.dllearner.modules.sparql;

public class QueryMaker {
	//Good
	/*public static String  owl ="http://www.w3.org/2002/07/owl#";
	public static String  xsd="http://www.w3.org/2001/XMLSchema#";
	public static String  rdfs="http://www.w3.org/2000/01/rdf-schema#";
	public static String  rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static String  base="http://dbpedia.org/resource/";
	public static String  dbpedia2="http://dbpedia.org/property/";
	public static String  dbpedia="http://dbpedia.org/";
	
	
	//BAD
	public static String  skos="http://www.w3.org/2004/02/skos/core#";
	public static String  foaf="http://xmlns.com/foaf/0.1/";
	public static String  dc="http://purl.org/dc/elements/1.1/";
	public static String  foreign="http://dbpedia.org/property/wikipage-";
	public static String  sameAs="http://www.w3.org/2002/07/owl#sameAs";
	public static String  reference="http://dbpedia.org/property/reference";*/
	
	
	
	int tempyago=0;

	
	
	
	public String makeQueryFilter(String subject, SparqlFilter sf){
		
		
		String Filter="";
		if(!sf.useLiterals)Filter+="!isLiteral(?object))";
		for (String  p : sf.getPredFilter()) {
			Filter+="\n" + filterPredicate(p);
		}
		for (String  o : sf.getObjFilter()) {
			Filter+="\n" + filterObject(o);
		}
		
		
		String ret=		
		"SELECT * WHERE { \n" +
		"<"+
		subject+
		
		"> ?predicate ?object.\n" +
		"FILTER( \n" +
		"(" +Filter+").}";
		//System.out.println(ret);
		return ret;
	}
	
	
	/*public String makeQueryDefault(String subject){
	String ret=		
	"SELECT * WHERE { \n" +
	"<"+
	subject+
	
	"> ?predicate ?object.\n" +
	"FILTER( \n" +
	"(!isLiteral(?object))" +
	"\n" + filterPredicate(skos)+
	//"\n" + filterObject(skos)+
	"\n" + filterPredicate(foaf)+
	"\n" + filterObject(foaf)+
	"\n" + filterPredicate(foreign)+
	"\n" + filterPredicate(sameAs)+
	"\n" + filterPredicate(reference)+
	")." +
	" }";
	
	//System.out.println(ret);
	return ret;
}*/
	
	public String filterObject(String ns){
		 return "&&( !regex((?object), '"+ns+"') )";
	}
	public String filterPredicate(String ns){
		 return "&&( !regex(str(?predicate), '"+ns+"') )";
	}
}
