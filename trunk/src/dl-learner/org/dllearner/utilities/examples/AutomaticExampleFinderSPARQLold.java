package org.dllearner.utilities.examples;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentManager;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class AutomaticExampleFinderSPARQLold {

	private static Logger logger = Logger
			.getLogger(ComponentManager.class);
	
	private Cache c;
	private SparqlEndpoint se;
	private SortedSet<String> posExamples;
	private SortedSet<String> negExamples;
	public  SortedSet<String> totalSKOSset;
	private int limit=1000;
	
	
	public AutomaticExampleFinderSPARQLold(SparqlEndpoint se){
		this.c=new Cache("cachetemp");
		this.se=se;
		posExamples = new TreeSet<String>();
		negExamples = new TreeSet<String>();
	}
	/*
	public void initDBpedia(String concept, boolean useRelated, boolean useSuperclasses,boolean useParallelClasses, int poslimit, int neglimit) { 
		dbpediaMakePositiveExamplesFromConcept( concept);
		SortedSet<String> keepForClean = new TreeSet<String>();
		keepForClean.addAll(this.posExamples);
		
		this.posExamples = SetManipulation.fuzzyShrink(this.posExamples, poslimit);
		
		
		logger.trace("shrinking: pos Example size: "+posExamples.size());
		
		if(useRelated) {
			dbpediaMakeNegativeExamplesFromRelatedInstances(this.posExamples);
		}
		if(useSuperclasses) {
			 makeNegativeExamplesFromSuperClasses(concept);
		}
		if(useParallelClasses) {
			 makeNegativeExamplesFromClassesOfInstances();
		}
		//clean
		negExamples.removeAll(keepForClean);
		logger.trace("neg Example size after cleaning: "+negExamples.size());
		this.negExamples = SetManipulation.fuzzyShrink(negExamples, neglimit);
		logger.debug("pos Example size after shrinking: "+posExamples.size());
		logger.debug("neg Example size after shrinking: "+negExamples.size());
		logger.debug("Finished examples for concept: "+concept);
	}
	
	*/
	
	
	/*
	public SortedSet<String> dbpediaGetPosOnly(String concept, int limit){
		dbpediaMakePositiveExamplesFromConcept( concept);
		return SetManipulation.fuzzyShrink(this.posExamples, limit);
	}*/
	
	/*public SortedSet<String> getPosOnly(String concept, int limit){
		makePositiveExamplesFromConcept( concept);
		return SetManipulation.fuzzyShrink(this.posExamples, limit);
	}*/
	
	/*
	private void dbpediaMakePositiveExamplesFromConcept(String concept){
		logger.debug("making Positive Examples from Concept: "+concept);
		if(concept.contains("http://dbpedia.org/resource/Category:")) {
			this.posExamples = new JenaResultSetConvenience(dbpediaQuerySKOSConcept(concept,limit))
				.getStringListForVariable("subject");
		}else {
			this.posExamples = new JenaResultSetConvenience(queryConcept(concept,limit))
				.getStringListForVariable("subject");
		}
		logger.debug("   pos Example size: "+posExamples.size());
	}
	*/
	
	
	
	
	
	/*private void makePositiveExamplesFromConcept(String concept){
		logger.debug("making Positive Examples from Concept: "+concept);
		if(concept.contains("http://dbpedia.org/resource/Category:")) {
			this.posExamples = new JenaResultSetConvenience(querySKOSConcept(concept,0))
				.getStringListForVariable("subject");
		}else {
			this.posExamples = new JenaResultSetConvenience(queryConcept(concept,0))
				.getStringListForVariable("subject");
		}
		logger.debug("   pos Example size: "+posExamples.size());
	}*/
	
	
	
	
	
	
	
	
	
	/*private void dbpediaMakeNegativeExamplesFromRelatedInstances(String subject) {
		// SortedSet<String> result = new TreeSet<String>();

		String query = "SELECT * WHERE { \n" + "<" + subject + "> " + "?p ?o. \n"
				+ "FILTER (REGEX(str(?o), 'http://dbpedia.org/resource/')).\n"
				+ "FILTER (!REGEX(str(?p), 'http://www.w3.org/2004/02/skos'))\n"
				+ "}";
		
		String JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
		ResultSet rs =SparqlQuery.JSONtoResultSet(JSON);
		JenaResultSetConvenience rsc = new JenaResultSetConvenience(rs);
		this.negExamples.addAll(rsc.getStringListForVariable("o"));
		
		
	}*/
	/*
	private void makeNegativeExamplesFromRelatedInstances(String subject, String namespace) {
		// SortedSet<String> result = new TreeSet<String>();

		String query = "SELECT * WHERE { \n" + "<" + subject + "> " + "?p ?o. \n"
				+ "FILTER (REGEX(str(?o), '"+namespace+"')).\n"
				+ "}";
		
		String JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
		ResultSet rs =SparqlQuery.JSONtoResultSet(JSON);
		JenaResultSetConvenience rsc = new JenaResultSetConvenience(rs);
		this.negExamples.addAll(rsc.getStringListForVariable("o"));
		
		
	}*/
	
	
	
	
	
	/*
	private void makeNegativeExamplesFromClassesOfInstances() {
		logger.debug("making neg Examples from parallel classes");
		SortedSet<String> classes = new TreeSet<String>();
		//superClasses.add(concept.replace("\"", ""));
		//logger.debug("before"+superClasses);
		//superClasses = dbpediaGetSuperClasses( superClasses, 4);
		//logger.debug("getting negExamples from "+superClasses.size()+" superclasses");
		JenaResultSetConvenience rsc;
		ResultSet rs=null;
		for (String instance : posExamples) {
			//System.out.println(instance);
			rs = getClassesForInstance(instance);
			//System.out.println(ResultSetFormatter.asXMLString(rs));
			rsc = new JenaResultSetConvenience(rs);
			classes.addAll(rsc.getStringListForVariable("subject"));
			//System.out.println(classes);
		}
		logger.debug("getting negExamples from "+classes.size()+" parallel classes");
		for (String oneClass : classes) {
			logger.debug(oneClass);
			rsc = new JenaResultSetConvenience(queryConcept("\""+oneClass+"\"",limit));
			this.negExamples.addAll(rsc.getStringListForVariable("subject"));
		}
		logger.debug("neg Example size: "+negExamples.size());
		
	}*/

	
	
	
	

	public SortedSet<String> getPosExamples() {
		return posExamples;
	}

	public SortedSet<String> getNegExamples() {
		return negExamples;
	}
	
	
	
	
	
	
}
