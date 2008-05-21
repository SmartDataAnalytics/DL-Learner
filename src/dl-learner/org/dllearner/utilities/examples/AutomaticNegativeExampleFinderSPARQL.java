package org.dllearner.utilities.examples;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentManager;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.utilities.datastructures.SetManipulation;

import com.hp.hpl.jena.query.ResultSet;

public class AutomaticNegativeExampleFinderSPARQL {

	// CHECK
	private static Logger logger = Logger.getLogger(ComponentManager.class);

	private SPARQLTasks sparqltasks;

	private SortedSet<String> fullPositiveSet;
	
	//static boolean useRelated = false;
	private SortedSet<String> fromRelated;
	//static boolean useSuperClasses = false;
	private SortedSet<String> fromSuperclasses;
	//static boolean useParallelClasses = true;
	private SortedSet<String> fromParallelClasses;
	
	static int poslimit = 10;
	static int neglimit = 20;

	// CHECK all vars needed
	public AutomaticNegativeExampleFinderSPARQL(
			SortedSet<String> fullPositiveSet,
			SPARQLTasks st) {
		super();
		this.fromParallelClasses = new TreeSet<String>();
		this.fromRelated = new TreeSet<String>();
		this.fromSuperclasses = new TreeSet<String>();
		
		this.fullPositiveSet = fullPositiveSet;
		this.sparqltasks = st;

	}
	
	

	public SortedSet<String> getNegativeExamples(int neglimit ) {

		SortedSet<String> negatives = new TreeSet<String>();
		negatives.addAll(fromParallelClasses);
		negatives.addAll(fromRelated);
		negatives.addAll(fromSuperclasses);
		logger.debug("neg Example size before shrinking: " + negatives.size());
		negatives = SetManipulation.fuzzyShrink(negatives,neglimit);
		logger.debug("neg Example size after shrinking: " + negatives.size());
		return negatives;
	}

	// CHECK namespace
	public void makeNegativeExamplesFromRelatedInstances(SortedSet<String> instances,
			String namespace) {
		logger.debug("making examples from related instances");
		for (String oneInstance : instances) {
			makeNegativeExamplesFromRelatedInstances(oneInstance, namespace);
		}
		logger.debug("|-negExample size from related: " + fromRelated.size());
	}

	private void makeNegativeExamplesFromRelatedInstances(String oneInstance, String objectnamespace) {
		// SortedSet<String> result = new TreeSet<String>();

		String SPARQLquery = "SELECT * WHERE { \n" + "<" + oneInstance + "> " + "?p ?object. \n"
				+ "FILTER (REGEX(str(?object), '" + objectnamespace + "')).\n" + "}";

		this.fromRelated.addAll(sparqltasks.queryAsSet(SPARQLquery, "object"));
		this.fromRelated.removeAll(this.fullPositiveSet);

	}

	// QUALITY: weird function, best removed
	public void dbpediaMakeNegativeExamplesFromRelatedInstances(String subject) {
		// SortedSet<String> result = new TreeSet<String>();

		String SPARQLquery = "SELECT * WHERE { \n" + "<" + subject + "> " + "?p ?o. \n"
				+ "FILTER (REGEX(str(?o), 'http://dbpedia.org/resource/')).\n"
				+ "FILTER (!REGEX(str(?p), 'http://www.w3.org/2004/02/skos'))\n" + "}";

		this.fromRelated.addAll(sparqltasks.queryAsSet(SPARQLquery, "o"));

	}

	public void makeNegativeExamplesFromParallelClasses(SortedSet<String> positiveSet, int resultLimit){
		makeNegativeExamplesFromClassesOfInstances(positiveSet, resultLimit);
	}
	
	public void makeNegativeExamplesFromClassesOfInstances(SortedSet<String> positiveSet,
			int resultLimit) {
		logger.debug("making neg Examples from parallel classes");
		SortedSet<String> classes = new TreeSet<String>();
		// superClasses.add(concept.replace("\"", ""));
		// logger.debug("before"+superClasses);
		// superClasses = dbpediaGetSuperClasses( superClasses, 4);
		// logger.debug("getting negExamples from "+superClasses.size()+"
		// superclasses");

		for (String instance : positiveSet) {
			classes.addAll(sparqltasks.getClassesForInstance(instance, resultLimit));
		}
		logger.debug("getting negExamples from " + classes.size() + " parallel classes");
		for (String oneClass : classes) {
			logger.debug(oneClass);
			// rsc = new
			// JenaResultSetConvenience(queryConcept("\""+oneClass+"\"",limit));
			this.fromParallelClasses.addAll(sparqltasks.retrieveInstancesForConcept("\"" + oneClass
					+ "\"", resultLimit));

		}
		
		this.fromParallelClasses.removeAll(this.fullPositiveSet);
		logger.debug("|-neg Example size from parallelclass: " + fromParallelClasses.size());

	}

	public void makeNegativeExamplesFromSuperClasses(String concept, int resultLimit) {

		concept = concept.replaceAll("\"", "");
		// superClasses.add(concept.replace("\"", ""));
		// logger.debug("before"+superClasses);
		SortedSet<String> superClasses = sparqltasks.getSuperClasses(concept, 4);
		logger.debug("making neg Examples from " + superClasses.size() + " superclasses");

		for (String oneSuperClass : superClasses) {
			logger.debug(oneSuperClass);
			this.fromSuperclasses.addAll(sparqltasks.retrieveInstancesForConcept("\""
					+ oneSuperClass + "\"", resultLimit));

		}
		this.fromSuperclasses.removeAll(this.fullPositiveSet);
		logger.debug("|-neg Example from superclass: " + fromSuperclasses.size());
	}

}
