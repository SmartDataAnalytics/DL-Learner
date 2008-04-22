package org.dllearner.utilities;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentManager;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.kb.sparql.SparqlQueryDescriptionConvertVisitor;
import org.dllearner.kb.sparql.configuration.SparqlEndpoint;

import com.hp.hpl.jena.query.ResultSet;

public class AutomaticExampleFinderSPARQL {

	private static Logger logger = Logger
			.getLogger(ComponentManager.class);
	
	private Cache c;
	private SparqlEndpoint se;
	private SortedSet<String> posExamples;
	private SortedSet<String> negExamples;
	
	
	public AutomaticExampleFinderSPARQL(SparqlEndpoint se){
		this.c=new Cache();
		this.se=se;
		posExamples = new TreeSet<String>();
		negExamples = new TreeSet<String>();
	}
	
	public void init(String concept, boolean useRelated, boolean useSuperclasses, int poslimit, int neglimit) { 
		makePositiveExamplesFromConcept( concept);
		SortedSet<String> keepForClean = new TreeSet<String>();
		keepForClean.addAll(this.posExamples);
		this.posExamples = SetManipulation.fuzzyShrink(this.posExamples, poslimit);
		
		if(useRelated) {
			dbpediaMakeNegativeExamplesFromRelatedInstances(this.posExamples);
		}
		if(useSuperclasses) {
			 dbpediaMakeNegativeExamplesFromSuperClasses(concept);
		}
		//clean
		negExamples.removeAll(keepForClean);
		this.negExamples = SetManipulation.fuzzyShrink(negExamples, neglimit);
		logger.debug("Finished examples for concept :"+concept);
	}
	
	public SortedSet<String> getPosOnly(String concept, int limit){
		makePositiveExamplesFromConcept( concept);
		return SetManipulation.fuzzyShrink(this.posExamples, limit);
	}
	
	private void makePositiveExamplesFromConcept(String concept){
		this.posExamples = new JenaResultSetConvenience(queryConcept(concept,0))
			.getStringListForVariable("subject");
		
	}
	
	
	
	
	
	private void dbpediaMakeNegativeExamplesFromRelatedInstances(SortedSet<String> subject) {
		for (String string : subject) {
			dbpediaMakeNegativeExamplesFromRelatedInstances(string);
		}
	}
	
	
	/**
	 * 
	 * @param subject
	 * @return
	 */
	private void dbpediaMakeNegativeExamplesFromRelatedInstances(String subject) {
		// SortedSet<String> result = new TreeSet<String>();

		String query = "SELECT * WHERE { \n" + "<" + subject + "> " + "?p ?o. \n"
				+ "FILTER (REGEX(str(?o), 'http://dbpedia.org/resource/')).\n"
				+ "FILTER (!REGEX(str(?p), 'http://www.w3.org/2004/02/skos'))\n"
				+ "}";
		
		String JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
		ResultSet rs =SparqlQuery.JSONtoResultSet(JSON);
		JenaResultSetConvenience rsc = new JenaResultSetConvenience(rs);
		this.negExamples.addAll(rsc.getStringListForVariable("o"));
		
	}
	
	
	private void dbpediaMakeNegativeExamplesFromSuperClasses(String concept) {
		
		SortedSet<String> superClasses = new TreeSet<String>();
		superClasses.add(concept.replace("\"", ""));
		//logger.debug("before"+superClasses);
		superClasses = dbpediaGetSuperClasses( superClasses, 4);
		logger.debug("getting negExamples from "+superClasses.size()+" superclasses");
		JenaResultSetConvenience rsc;
		for (String oneSuperClass : superClasses) {
			
			rsc = new JenaResultSetConvenience(queryConcept("\""+oneSuperClass+"\"", 0));
			this.negExamples.addAll(rsc.getStringListForVariable("subject"));
		}
	}

	private SortedSet<String> dbpediaGetSuperClasses(SortedSet<String> superClasses, int depth) {
		SortedSet<String> ret = new TreeSet<String>();
		SortedSet<String> tmpset = new TreeSet<String>();
		ret.addAll(superClasses);
		//logger.debug(superClasses);
		JenaResultSetConvenience rsc;
		
		String query = "";
		for (; depth != 0 ; depth--) {
			for (String oneSuperClass : superClasses) {
				//logger.debug("one"+oneSuperClass);
				//tmp = oneSuperClass.replace("\"", "");
				query = "SELECT * WHERE { \n" + "<" + oneSuperClass  + "> " 
					+ "<http://www.w3.org/2000/01/rdf-schema#subClassOf>  ?superclass. \n"
					+ "}";
				String JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
				ResultSet rs =SparqlQuery.JSONtoResultSet(JSON);
				rsc = new JenaResultSetConvenience(rs);
				tmpset.addAll(rsc.getStringListForVariable("superclass"));
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
	
	public  ResultSet queryConcept(String concept,int limit) {
		ResultSet rs = null;
		try {
			String query = SparqlQueryDescriptionConvertVisitor
					.getSparqlQuery(concept,limit);
			
			SparqlQuery sq = new SparqlQuery(query, se);
			String JSON = c.executeSparqlQuery(sq);
			//System.out.println(JSON);
			rs = SparqlQuery.JSONtoResultSet(JSON);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return rs;
	}

	public SortedSet<String> getPosExamples() {
		return posExamples;
	}

	public SortedSet<String> getNegExamples() {
		return negExamples;
	}
	
	
	
}
