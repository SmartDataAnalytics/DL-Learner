package org.dllearner.utilities.examples;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentManager;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.utilities.datastructures.SetManipulation;

public class AutomaticNegativeExampleFinderSPARQL {

	// LOGGER: ComponentManager
	private static Logger logger = Logger.getLogger(ComponentManager.class);

	private SPARQLTasks sparqltasks;

	private SortedSet<String> fullPositiveSet;
	
	private SortedSet<String> fromRelated  = new TreeSet<String>();
	private SortedSet<String> fromSuperclasses = new TreeSet<String>();;
	private SortedSet<String> fromParallelClasses = new TreeSet<String>();;
	private SortedSet<String> fromDomain = new TreeSet<String>();;
	private SortedSet<String> fromRange = new TreeSet<String>();;
	
	static int poslimit = 10;
	static int neglimit = 20;

	
	/**
	 * takes as input a full positive set to make sure no negatives are added as positives
	 *  
	 * @param fullPositiveSet
	 * @param SPARQLTasks st
	 */
	public AutomaticNegativeExampleFinderSPARQL(
			SortedSet<String> fullPositiveSet,
			SPARQLTasks st) {
		super();
		this.fullPositiveSet = fullPositiveSet;
		this.sparqltasks = st;

	}
	
	

	/**
	 * aggregates all collected neg examples
	 * @param neglimit
	 * @return
	 */
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

	
	/**
	 * makes neg ex from related instances, that take part in a role R(pos,neg)
	 * filters all objects, that don't use the given namespace 
	 * @param instances
	 * @param objectNamespace
	 */
	public void makeNegativeExamplesFromRelatedInstances(SortedSet<String> instances,
			String objectNamespace) {
		logger.debug("making examples from related instances");
		for (String oneInstance : instances) {
			makeNegativeExamplesFromRelatedInstances(oneInstance, objectNamespace);
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

	// QUALITY: keep a while may still be needed
	/*public void dbpediaMakeNegativeExamplesFromRelatedInstances(String subject) {
		// SortedSet<String> result = new TreeSet<String>();

		String SPARQLquery = "SELECT * WHERE { \n" + "<" + subject + "> " + "?p ?o. \n"
				+ "FILTER (REGEX(str(?o), 'http://dbpedia.org/resource/')).\n"
				+ "FILTER (!REGEX(str(?p), 'http://www.w3.org/2004/02/skos'))\n" + "}";

		this.fromRelated.addAll(sparqltasks.queryAsSet(SPARQLquery, "o"));

	}*/

	/**
	 * makes neg ex from classes, the pos ex belong to 
	 * @param positiveSet
	 * @param resultLimit
	 */
	public void makeNegativeExamplesFromParallelClasses(SortedSet<String> positiveSet, int resultLimit){
		makeNegativeExamplesFromClassesOfInstances(positiveSet, resultLimit);
	}
	
	private void makeNegativeExamplesFromClassesOfInstances(SortedSet<String> positiveSet,
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

	/**
	 * if pos ex derive from one class, then neg ex are taken from a superclass
	 * @param concept
	 * @param resultLimit
	 */
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
	
	@SuppressWarnings("unused")
	private void makeNegativeExamplesFromDomain(String role, int resultLimit){
		logger.debug("making Negative Examples from Domain of : "+role);
		this.fromDomain.addAll(sparqltasks.getDomain(role, resultLimit));
		this.fromDomain.removeAll(this.fullPositiveSet);
		logger.debug("|-neg Example size from Domain: "+this.fromDomain.size());
	}
	
	@SuppressWarnings("unused")
	private void makeNegativeExamplesFromRange(String role, int resultLimit){
		logger.debug("making Negative Examples from Range of : "+role);
		this.fromRange.addAll(sparqltasks.getRange(role, resultLimit));
		this.fromRange.removeAll(this.fullPositiveSet);
		logger.debug("|-neg Example size from Range: "+this.fromRange.size());
	}
	

}
