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
	
	public void init(String concept, String namespace, boolean useRelated, boolean useSuperclasses,boolean useParallelClasses, int poslimit, int neglimit) { 
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
		logger.trace("neg Example size after cleaning: "+negExamples.size());
		this.negExamples = SetManipulation.fuzzyShrink(negExamples, neglimit);
		logger.debug("pos Example size after shrinking: "+posExamples.size());
		logger.debug("neg Example size after shrinking: "+negExamples.size());
		logger.debug("Finished examples for concept: "+concept);
	}
	
	
	
	public SortedSet<String> dbpediaGetPosOnly(String concept, int limit){
		dbpediaMakePositiveExamplesFromConcept( concept);
		return SetManipulation.fuzzyShrink(this.posExamples, limit);
	}
	
	public SortedSet<String> getPosOnly(String concept, int limit){
		makePositiveExamplesFromConcept( concept);
		return SetManipulation.fuzzyShrink(this.posExamples, limit);
	}
	
	private void dbpediaMakePositiveExamplesFromConcept(String concept){
		logger.debug("making Positive Examples from Concept: "+concept);
		if(concept.contains("http://dbpedia.org/resource/Category:")) {
			this.posExamples = new JenaResultSetConvenience(dbpediaQuerySKOSConcept(concept,0))
				.getStringListForVariable("subject");
		}else {
			this.posExamples = new JenaResultSetConvenience(queryConcept(concept,0))
				.getStringListForVariable("subject");
		}
		logger.debug("   pos Example size: "+posExamples.size());
	}
	
	private void makePositiveExamplesFromConcept(String concept){
		logger.debug("making Positive Examples from Concept: "+concept);	
		this.posExamples = new JenaResultSetConvenience(queryConcept(concept,0))
				.getStringListForVariable("subject");
		logger.debug("   pos Example size: "+posExamples.size());
	}
	
	
	
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
	
	
	
	
	
	private void dbpediaMakeNegativeExamplesFromRelatedInstances(SortedSet<String> subject) {
		logger.debug("making examples from related instances");
		for (String string : subject) {
			dbpediaMakeNegativeExamplesFromRelatedInstances(string);
		}
		logger.debug("  negExample size: "+negExamples.size());
	}
	
	private void makeNegativeExamplesFromRelatedInstances(SortedSet<String> subject, String namespace) {
		logger.debug("making examples from related instances");
		for (String string : subject) {
			makeNegativeExamplesFromRelatedInstances(string,namespace);
		}
		logger.debug("  negExample size: "+negExamples.size());
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
	
	private void makeNegativeExamplesFromRelatedInstances(String subject, String namespace) {
		// SortedSet<String> result = new TreeSet<String>();

		String query = "SELECT * WHERE { \n" + "<" + subject + "> " + "?p ?o. \n"
				+ "FILTER (REGEX(str(?o), '"+namespace+"')).\n"
				+ "}";
		
		String JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
		ResultSet rs =SparqlQuery.JSONtoResultSet(JSON);
		JenaResultSetConvenience rsc = new JenaResultSetConvenience(rs);
		this.negExamples.addAll(rsc.getStringListForVariable("o"));
		
		
	}
	
	
	private void makeNegativeExamplesFromSuperClasses(String concept) {
		
		SortedSet<String> superClasses = new TreeSet<String>();
		superClasses.add(concept.replace("\"", ""));
		//logger.debug("before"+superClasses);
		superClasses = getSuperClasses( superClasses, 4);
		logger.debug("making neg Examples from "+superClasses.size()+" superclasses");
		JenaResultSetConvenience rsc;
		for (String oneSuperClass : superClasses) {
			
			rsc = new JenaResultSetConvenience(queryConcept("\""+oneSuperClass+"\"", 0));
			this.negExamples.addAll(rsc.getStringListForVariable("subject"));
		}
		logger.debug("   neg Example size: "+negExamples.size());
	}
	
	
	
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
			
			rsc = new JenaResultSetConvenience(queryConcept("\""+oneClass+"\"",0));
			this.negExamples.addAll(rsc.getStringListForVariable("subject"));
		}
		logger.debug("neg Example size: "+negExamples.size());
		
	}

	private SortedSet<String> getSuperClasses(SortedSet<String> superClasses, int depth) {
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
			//System.out.println("JSON:\n"+JSON);
			rs = SparqlQuery.JSONtoResultSet(JSON);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return rs;
	}
	
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
	}
	
	public  ResultSet getClassesForInstance(String instance) {
		ResultSet rs = null;
		try {
			
			String query = "SELECT ?subject WHERE { \n " + 
			"<" + instance  + ">"+
			" a " + 
			"?subject " +
			"\n" +
			"}";
			SparqlQuery sq = new SparqlQuery(query, se);
			//System.out.println(query);
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
	
	
	/**
	 * NOT WORKING
	 * @param description
	 */
	public void getSubClasses(String description) {
		ResultSet rs = null;
		try {
			String query = SparqlQueryDescriptionConvertVisitor
					.getSparqlSubclassQuery(description.replace("\"", ""));
			
			rs = new SparqlQuery(query, se).send();
			System.out.println(query);
			//System.out.println(SparqlQuery.getAsXMLString(rs));
			System.out.println(rs.getResultVars());
			SortedSet<String> remainingClasses = new JenaResultSetConvenience(rs).getStringListForVariable("subject");
			SortedSet<String> alreadyQueried = new TreeSet<String>();
			alreadyQueried.add(description);
			while (remainingClasses.size()!=0){
				String tmp = remainingClasses.first();
				remainingClasses.remove(tmp);
				query = SparqlQueryDescriptionConvertVisitor
					.getSparqlSubclassQuery(tmp);
				alreadyQueried.add(tmp);
				rs = new SparqlQuery(query, se).send();
				remainingClasses.addAll(new JenaResultSetConvenience(rs).getStringListForVariable("subject"));
			}
			//System.out.println(JSON);
			System.out.println(alreadyQueried);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
}
