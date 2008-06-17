package org.dllearner.kb.sparql;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.core.ResultBinding;

public class SPARQLTasks {
	
	//@SuppressWarnings("unused")
	//LOGGER: SPARQLTasks
	private static Logger logger = Logger
		.getLogger(SPARQLTasks.class);
	private Cache c;
	private SparqlEndpoint se;
	
	public SPARQLTasks(Cache c, SparqlEndpoint se) {
		super();
		this.c = c;
		this.se = se;
	}
	
	public SPARQLTasks( SparqlEndpoint se) {
		super();
		this.c = null;
		this.se = se;
	}
	
	
	
	
	/**
	 * QUALITY: doesn't seem optimal, check!
	 * get all superclasses up to a certain depth
	 * 1 means direct superclasses
	 * depth 
	 * @param superClasses
	 * @param depth
	 * @return
	 */
	public SortedSet<String> getSuperClasses(String oneClass, int depth) {
		SortedSet<String> superClasses = new TreeSet<String>();
		superClasses.add(oneClass);
		SortedSet<String> ret = new TreeSet<String>();
		SortedSet<String> tmpset = new TreeSet<String>();
		//ret.addAll(superClasses);
		//logger.debug(superClasses);
		
		
		String SPARQLquery = "";
		for (; depth != 0 ; depth--) {
			for (String oneSuperClass : superClasses) {
				
				//tmp = oneSuperClass.replace("\"", "");
				SPARQLquery = "SELECT * WHERE { \n" + "<" + oneSuperClass  + "> " 
					+ "<http://www.w3.org/2000/01/rdf-schema#subClassOf>  ?superclass. \n"
					+ "}";
				
				tmpset.addAll(queryAsSet(SPARQLquery, "superclass"));
				
			}
			ret.addAll(tmpset);
			//logger.debug(ret);
			superClasses.clear();
			superClasses.addAll(tmpset);
			tmpset.clear();
		}
		//logger.debug(concept);
		//logger.debug(query);
		return 	ret;
	}
	
	
	/**
	 * gets a SortedSet of all subclasses QUALITY: maybe it is better to have a
	 * parameter int depth, to choose a depth of subclass interference
	 * 
	 * @see conceptRewrite(String descriptionKBSyntax, SparqlEndpoint se, Cache
	 *      c, boolean simple )
	 * @param description
	 * @param se
	 * @param c
	 * @param simple
	 * @return
	 */
	public SortedSet<String> getSubClasses(String description, boolean simple) {

		// ResultSet rs = null;
		// System.out.println(description);
		SortedSet<String> alreadyQueried = new TreeSet<String>();
		try {

			// initialisation get direct Subclasses
			LinkedList<String> remainingClasses = new LinkedList<String>();

			// collect remaining classes
			remainingClasses.addAll(getDirectSubClasses(description.replaceAll("\"", "")));

			// remainingClasses.addAll(alreadyQueried);

			// alreadyQueried = new TreeSet<String>();
			alreadyQueried.add(description.replaceAll("\"", ""));

			if (simple) {
				alreadyQueried.addAll(remainingClasses);
				return alreadyQueried;
			} else {

				logger.warn("Retrieval auf all subclasses via SPARQL is cost intensive and might take a while");
				while (remainingClasses.size() != 0) {
					SortedSet<String> tmpSet = new TreeSet<String>();
					String tmp = remainingClasses.removeFirst();
					alreadyQueried.add(tmp);

					tmpSet = getDirectSubClasses(tmp);
					for (String string : tmpSet) {
						if (!(alreadyQueried.contains(string))) {
							remainingClasses.add(string);
						}// if
					}// for
				}// while
			}// else

		} catch (Exception e) {

		}

		return alreadyQueried;
	}

	/**
	 * QUALITY: workaround for a sparql glitch {?a owl:subclassOf ?b} returns an
	 * empty set on some entpoints. returns all direct subclasses of String
	 * concept
	 * 
	 * @param concept
	 * @return SortedSet of direct subclasses as String
	 */
	private SortedSet<String> getDirectSubClasses(String concept) {
		String SPARQLquery = "SELECT * \n";
		SPARQLquery += "WHERE {\n";
		SPARQLquery += " ?subject ?predicate  <" + concept + "> \n";
		SPARQLquery += "}\n";

		ResultSet rs = queryAsResultSet(SPARQLquery);
		
		SortedSet<String> subClasses = new TreeSet<String>();
		@SuppressWarnings("unchecked")
		List<ResultBinding> l = ResultSetFormatter.toList(rs);
		String p = "", s = "";
		for (ResultBinding resultBinding : l) {

			s = ((resultBinding.get("subject").toString()));
			p = ((resultBinding.get("predicate").toString()));
			if (p.equalsIgnoreCase("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
				subClasses.add(s);
			}
		}
		return subClasses;
	}

	/**
	 * QUALITY: buggy because role doesn't work sometimes
	 * get subject with fixed role and object
	 * @param role
	 * @param object
	 * @param resultLimit
	 * @return
	 */
	public  SortedSet<String> retrieveDISTINCTSubjectsForRoleAndObject(String role, String object,int resultLimit) {
		 String SPARQLquery = 
				"SELECT DISTINCT * WHERE { \n " + 
				"?subject " +
				"<"+role+"> " + 
				"<" + object  + "> \n" +
				"} "+limit(resultLimit);
		 
		 return queryAsSet(SPARQLquery, "subject");			
	}
	
	public  SortedSet<String> retrieveObjectsForSubjectAndRole(String subject, String role, int resultLimit) {
		 String SPARQLquery = 
				"SELECT DISTINCT * WHERE { \n " + 
				"<" +subject+ "> "+
				"<"+role+"> " + 
				" ?object \n" +
				"} LIMIT "+resultLimit;
		 
		 return queryAsSet(SPARQLquery, "object");			
	}
	
	/**
	 * all instances for a SKOS concept
	 * @param SKOSconcept
	 * @param resultLimit
	 * @return
	 */
	public  SortedSet<String> retrieveInstancesForSKOSConcept(String SKOSconcept,int resultLimit) {
		return queryPatternAsSet("?subject", "?predicate", "<"+SKOSconcept+">", "subject", resultLimit);
		//return retrieveDISTINCTSubjectsForRoleAndObject("http://www.w3.org/2004/02/skos/core#subject", 
	}
	
	
	
	/**
	 * get all instances for a concept
	 * @param conceptKBSyntax
	 * @param sparqlResultLimit
	 * @return
	 */
	public  SortedSet<String> retrieveInstancesForConcept (String conceptKBSyntax,int sparqlResultLimit) {
			
			String SPARQLquery = ""; 
			try{
			SPARQLquery = SparqlQueryDescriptionConvertVisitor
					.getSparqlQuery(conceptKBSyntax,sparqlResultLimit);
			}catch (Exception e) {e.printStackTrace();}
			return queryAsSet(SPARQLquery, "subject");
	}
	
	/**
	 * get all instances for a concept including RDFS Reasoning
	 * @param conceptKBSyntax
	 * @param sparqlResultLimit
	 * @return
	 */
	public  SortedSet<String> retrieveInstancesForConceptIncludingSubclasses (String conceptKBSyntax,int sparqlResultLimit) {
			
			String SPARQLquery = ""; 
			try{
			SPARQLquery = SparqlQueryDescriptionConvertVisitor
					.getSparqlQueryIncludingSubclasses(conceptKBSyntax,sparqlResultLimit,this,true);
		
			}catch (Exception e) {e.printStackTrace();}
			return queryAsSet(SPARQLquery, "subject");
	}
	
		
	
	
	/**
	 * get all direct Classes of an instance
	 * @param instance
	 * @param resultLimit
	 * @return
	 */
	public  SortedSet<String> getClassesForInstance(String instance, int resultLimit) {
				
			String SPARQLquery = "SELECT ?subject WHERE { \n " + 
			"<" + instance  + ">"+
			" a " + 
			"?subject " +
			"\n" +
			"} "+limit(resultLimit);
			
			return queryAsSet(SPARQLquery, "subject");
	}
	

	
	public SortedSet<String> getDomain(String role,int resultLimit){
		
		String SPARQLquery = "" +
		"SELECT DISTINCT ?domain " +
		"WHERE { \n" + 
		"?domain <" + role + "> " + " ?o. \n" +
		"?domain a []\n." +
		"FILTER (!isLiteral(?domain))." +
		"}\n" + limit(resultLimit);
	
		return queryAsSet(SPARQLquery, "domain");
		
	
	}
	
	
	public SortedSet<String> getRange(String role,int resultLimit){
		
		String SPARQLquery = "" +
		"SELECT DISTINCT ?range " +
		"WHERE { \n" + 
		"?s <" + role + "> " + " ?range. \n" +
		"?range a [].\n" +
		"FILTER (!isLiteral(?range))." +
		"}\n" + limit(resultLimit);

		return queryAsSet(SPARQLquery, "range");
	
	}
	
	
	
	/**
	 * query a pattern with a standard SPARQL query
	 * usage (?subject, ?predicate, <http:something> , subject )
	 * @param subject
	 * @param predicate
	 * @param object
	 * @param var
	 * @return
	 */
	public SortedSet<String> queryPatternAsSet(String subject, String predicate, String object, String var, int resultLimit){
		String SPARQLquery = "SELECT ?subject WHERE { \n " + 
		" " + subject +
		" " + predicate +
		" " + object +
		" \n" +
		"} "+limit(resultLimit);
		return queryAsSet( SPARQLquery,  var);
	 }
	
	
	 /**
	  * little higher level, executes query ,returns all resources for a variable
	 * @param SPARQLquery
	 * @param var
	 * @return
	 */
	public SortedSet<String> queryAsSet(String SPARQLquery, String var){
		 ResultSet rs = null;
			try {
			String JSON = query(SPARQLquery);
			rs = SparqlQuery.JSONtoResultSet(JSON);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return getStringListForVariable(rs,var);
	 }
	
	
	/**
	 * low level, executes query returns ResultSet
	 * @param SPARQLquery
	 * @return jena ResultSet
	 */
	public ResultSet queryAsResultSet(String SPARQLquery){
		return SparqlQuery.JSONtoResultSet(query(SPARQLquery));
		
	}
	
	/**
	 * low level, executes query returns JSON 
	 * @param SPARQLquery
	 * @return
	 */
	public String query(String SPARQLquery){
		if(c==null){
			SparqlQuery sq = new SparqlQuery(SPARQLquery,se);
//			sq.extraDebugInfo+=se.getURL();
			ResultSet rs=sq.send();
			String JSON = SparqlQuery.getAsJSON(rs); 
			return JSON;
		}else{
			return c.executeSparqlQuery(new SparqlQuery(SPARQLquery,se));
		}
		
	}
	
	private String limit(int resultLimit){
		if(resultLimit>0)return " LIMIT "+resultLimit;
		return "";
	}
	
	
	public static SortedSet<String> getStringListForVariable(ResultSet rs, String var){
		SortedSet<String> result = new TreeSet<String>();
		
		//String s=ResultSetFormatter.asXMLString(this.rs);
		@SuppressWarnings("unchecked")
		List<ResultBinding> l =  ResultSetFormatter.toList(rs);
		
		for (ResultBinding resultBinding : l) {
			result.add(resultBinding.get(var).toString());
		}
		
		return result;
		
	}

}
