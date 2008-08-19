/**
 * Copyright (C) 2007, Jens Lehmann
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
	
	public SortedSet<String> getNegativeExamples(int neglimit ) {
		return getNegativeExamples(neglimit, false);
	}

	/**
	 * aggregates all collected neg examples
	 * @param neglimit size of negative Example set
	 * @param stable decides whether neg Examples are randomly picked, default false, faster for developing, since the cache can be used
	 * @return
	 */
	public SortedSet<String> getNegativeExamples(int neglimit, boolean stable ) {

		SortedSet<String> negatives = new TreeSet<String>();
		negatives.addAll(fromParallelClasses);
		negatives.addAll(fromRelated);
		negatives.addAll(fromSuperclasses);
		logger.debug("neg Example size before shrinking: " + negatives.size());
		if (stable) {
			negatives = SetManipulation.stableShrink(negatives,neglimit);
		}
		else {
			negatives = SetManipulation.fuzzyShrink(negatives,neglimit);
		}
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

		fromRelated.addAll(sparqltasks.queryAsSet(SPARQLquery, "object"));
		fromRelated.removeAll(fullPositiveSet);

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
	 * makes negEx from classes, the posEx belong to.
	 * Gets all Classes from PosEx, gets Instances from these Classes, returns all
	 * @param positiveSet
	 * @param sparqlResultLimit
	 */
	public void makeNegativeExamplesFromParallelClasses(SortedSet<String> positiveSet, int sparqlResultLimit){
		makeNegativeExamplesFromClassesOfInstances(positiveSet, sparqlResultLimit);
	}
	
	private void makeNegativeExamplesFromClassesOfInstances(SortedSet<String> positiveSet,
			int sparqlResultLimit) {
		logger.debug("making neg Examples from parallel classes");
		SortedSet<String> classes = new TreeSet<String>();
		// superClasses.add(concept.replace("\"", ""));
		// logger.debug("before"+superClasses);
		// superClasses = dbpediaGetSuperClasses( superClasses, 4);
		// logger.debug("getting negExamples from "+superClasses.size()+"
		// superclasses");

		for (String instance : positiveSet) {
			try{
			classes.addAll(sparqltasks.getClassesForInstance(instance, sparqlResultLimit));
			}catch (Exception e) {
				logger.warn("ignoring SPARQLQuery failure, see log/sparql.txt");
			}
		}
		logger.debug("getting negExamples from " + classes.size() + " parallel classes");
		for (String oneClass : classes) {
			logger.debug(oneClass);
			// rsc = new
			// JenaResultSetConvenience(queryConcept("\""+oneClass+"\"",limit));
			try{
			this.fromParallelClasses.addAll(sparqltasks.retrieveInstancesForClassDescription("\"" + oneClass
					+ "\"", sparqlResultLimit));
			}catch (Exception e) {
				logger.warn("ignoring SPARQLQuery failure, see log/sparql.txt");
			}
		}
		
		fromParallelClasses.removeAll(fullPositiveSet);
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
			fromSuperclasses.addAll(sparqltasks.retrieveInstancesForClassDescription("\""
					+ oneSuperClass + "\"", resultLimit));

		}
		this.fromSuperclasses.removeAll(fullPositiveSet);
		logger.debug("|-neg Example from superclass: " + fromSuperclasses.size());
	}
	
	@SuppressWarnings("unused")
	private void makeNegativeExamplesFromDomain(String role, int resultLimit){
		logger.debug("making Negative Examples from Domain of : "+role);
		fromDomain.addAll(sparqltasks.getDomainInstances(role, resultLimit));
		fromDomain.removeAll(fullPositiveSet);
		logger.debug("|-neg Example size from Domain: "+this.fromDomain.size());
	}
	
	@SuppressWarnings("unused")
	private void makeNegativeExamplesFromRange(String role, int resultLimit){
		logger.debug("making Negative Examples from Range of : "+role);
		fromRange.addAll(sparqltasks.getRangeInstances(role, resultLimit));
		fromRange.removeAll(fullPositiveSet);
		logger.debug("|-neg Example size from Range: "+fromRange.size());
	}
}
