package org.dllearner.utilities.examples;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentManager;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.datastructures.SetManipulation;

public class AutomaticExampleFinderSKOSSPARQL {

	private static Logger logger = Logger
			.getLogger(ComponentManager.class);
	
	private Cache c;
	private SparqlEndpoint se;
	private SortedSet<String> posExamples= new TreeSet<String>();;
	private SortedSet<String> negExamples= new TreeSet<String>();;
	public  SortedSet<String> totalSKOSset= new TreeSet<String>();;
	public  SortedSet<String> rest= new TreeSet<String>();;
	private int limit=2000;
	
	
	public AutomaticExampleFinderSKOSSPARQL(SparqlEndpoint se){
		this.c=new Cache("cacheExamplesValidation");
		this.se=se;
		posExamples = new TreeSet<String>();
		negExamples = new TreeSet<String>();
	}
	
	@Deprecated
	public void initDBpediaSKOS(String concept, double percent, boolean useRelated,boolean useParallelClasses) { 
		//dbpediaMakePositiveExamplesFromConcept( concept);
		SortedSet<String> keepForClean = new TreeSet<String>();
		keepForClean.addAll(this.posExamples);
		totalSKOSset.addAll(this.posExamples);
		rest.addAll(totalSKOSset);
		int poslimit=(int)Math.round(percent*totalSKOSset.size());
		int neglimit=(int)Math.round(poslimit);
		/*while (this.posExamples.size()>poslimit) {
			this.posExamples.remove(posExamples.last());
		}*/
		this.posExamples = SetManipulation.fuzzyShrink(this.posExamples, poslimit);
		
		rest.removeAll(this.posExamples);
		
		logger.debug("pos Example size: "+posExamples.size());
		logger.debug("totalSKOSset: "+totalSKOSset.size());
		logger.debug("rest: "+rest.size());
		
		if(useRelated) {
			//dbpediaMakeNegativeExamplesFromRelatedInstances(this.posExamples);
		}
		
		if(useParallelClasses) {
			int limit = this.posExamples.size();
			 //makeNegativeExamplesFromClassesOfInstances(limit);
		}
		//clean
		negExamples.removeAll(keepForClean);
		logger.debug("neg Example size after cleaning: "+negExamples.size());
		this.negExamples = SetManipulation.fuzzyShrink(negExamples, neglimit);
		logger.debug("pos Example size after shrinking: "+posExamples.size());
		logger.debug("neg Example size after shrinking: "+negExamples.size());
		logger.debug("Finished examples for concept: "+concept);
	}
	
/*	public void init(String concept, String namespace, boolean useRelated, boolean useSuperclasses,boolean useParallelClasses, int poslimit, int neglimit) { 
		makePositiveExamplesFromConcept( concept);
		SortedSet<String> keepForClean = new TreeSet<String>();
		keepForClean.addAll(this.posExamples);
		this.posExamples = SetManipulation.fuzzyShrink(this.posExamples, poslimit);
		logger.trace("shrinking: pos Example size: "+posExamples.size());
		
		if(useRelated) {
			makeNegativeExamplesFromRelatedInstances(this.posExamples,namespace);
		}
		if(useSuperclasses) {
			 makeNegativeExamplesFromSuperClasses(concept);
		}
		if(useParallelClasses) {
			makeNegativeExamplesFromClassesOfInstances();
		}
		//clean
		negExamples.removeAll(keepForClean);
		logger.debug("neg Example size after cleaning: "+negExamples.size());
		this.negExamples = SetManipulation.fuzzyShrink(negExamples, neglimit);
		logger.debug("pos Example size after shrinking: "+posExamples.size());
		logger.debug("neg Example size after shrinking: "+negExamples.size());
		logger.debug("Finished examples for concept: "+concept);
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
		logger.debug("pos Example size: "+posExamples.size());
	}*/
	
	/*
	private void makePositiveExamplesFromConcept(String concept){
		logger.debug("making Positive Examples from Concept: "+concept);	
		this.posExamples = new JenaResultSetConvenience(queryConcept(concept,0))
				.getStringListForVariable("subject");
		logger.debug("   pos Example size: "+posExamples.size());
	}*/
	
	
	
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
	
	
	
	
	
	
	
	/**
	 * 
	 * @param subject
	 * @return
	 */

	

	
	/*
	private void makeNegativeExamplesFromSuperClasses(String concept) {
		
		SortedSet<String> superClasses = new TreeSet<String>();
		superClasses.add(concept.replace("\"", ""));
		//logger.debug("before"+superClasses);
		superClasses = getSuperClasses( superClasses, 4);
		logger.debug("making neg Examples from "+superClasses.size()+" superclasses");
		JenaResultSetConvenience rsc;
		for (String oneSuperClass : superClasses) {
			logger.debug(oneSuperClass);
			rsc = new JenaResultSetConvenience(queryConcept("\""+oneSuperClass+"\"", limit));
			this.negExamples.addAll(rsc.getStringListForVariable("subject"));
		}
		logger.debug("   neg Example size: "+negExamples.size());
	}*/
	
	
	
	

	
	/*
	public  ResultSet dbpediaQuerySKOSConcept(String SKOSconcept,int limit) {
		if(limit==0)limit=99999;
		//
		ResultSet rs = null;
		try {
			
			String query = "SELECT * WHERE { \n " + 
			"?subject " +
			"<http://www.w3.org/2004/02/skos/core#subject> " + 
			"<" + SKOSconcept  + "> \n" +
			"} LIMIT "+limit;
			SparqlQuery sq = new SparqlQuery(query, se);
			String JSON = c.executeSparqlQuery(sq);
			//System.out.println(JSON);
			rs = SparqlQuery.JSONtoResultSet(JSON);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return rs;
	}*/
	
	

	public SortedSet<String> getPosExamples() {
		return posExamples;
	}

	public SortedSet<String> getNegExamples() {
		return negExamples;
	}
	
	
	
	
	
	
	
}
