package org.dllearner.utilities.examples;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.kb.sparql.SparqlQueryDescriptionConvertVisitor;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.core.ResultBinding;

public class SPARQLTasks {
	
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
				"} LIMIT "+resultLimit;
		 
		 return queryAsSet(SPARQLquery, "subject");			
	}
	
	public  SortedSet<String> retrieveObjectsForSubjectAndRole(String subject, String role,int resultLimit) {
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
		return retrieveDISTINCTSubjectsForRoleAndObject("http://www.w3.org/2004/02/skos/core#subject", 
				SKOSconcept, resultLimit);
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
	

	 /**
	  * little higher level, executes query ,returns all resources for a variable
	 * @param SPARQLquery
	 * @param var
	 * @return
	 */
	public SortedSet<String> queryAsSet(String SPARQLquery, String var){
		 ResultSet rs = null;
			try {
			rs = SparqlQuery.JSONtoResultSet(query(SPARQLquery));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return getStringListForVariable(rs,"subject");
	 }
	
	
	/**
	 * lowlevel, executes query returns JSON 
	 * @param SPARQLquery
	 * @return
	 */
	public String query(String SPARQLquery){
		if(c==null){
			SparqlQuery sq = new SparqlQuery(SPARQLquery,se);
			sq.send();
			return sq.getResult();
		}else{
			return c.executeSparqlQuery(new SparqlQuery(SPARQLquery,se));
		}
		
	}
	
	private String limit(int resultLimit){
		if(resultLimit>0)return " LIMIT "+resultLimit;
		return "";
	}
	
	@SuppressWarnings("unchecked")
	public static SortedSet<String> getStringListForVariable(ResultSet rs, String var){
		SortedSet<String> result = new TreeSet<String>();
		
		//String s=ResultSetFormatter.asXMLString(this.rs);
		List<ResultBinding> l =  ResultSetFormatter.toList(rs);
		
		for (ResultBinding resultBinding : l) {
				
			result.add(resultBinding.get(var).toString());
		
		}
		
		return result;
		
	}

}
