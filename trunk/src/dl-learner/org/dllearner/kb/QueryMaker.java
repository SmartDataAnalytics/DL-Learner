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
package org.dllearner.kb;

/**
 * 
 * This class produces sparql queries
 * 
 * @author Sebastian Hellmann
 *
 */
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

	/**
	 * reads all the options and makes the sparql query accordingly
	 * @param subject
	 * @param sf special object encapsulating all options
	 * @return sparql query
	 */
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
	
	/**
	 * add a new object filter
	 * (objects are filtered out of sparql result)
	 * @param ns namespace
	 * @return
	 */
	public String filterObject(String ns){
		 return "&&( !regex((?object), '"+ns+"') )";
	}
	/**
	* add a new object filter
	 * (objects are filtered out of sparql result)
	 * @param ns namespace
	 * * @return
	 */
	public String filterPredicate(String ns){
		 return "&&( !regex(str(?predicate), '"+ns+"') )";
	}
}
