/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 */

package org.dllearner.utilities.examples;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.owl.OWLVocabulary;

public class AutomaticNegativeExampleFinderSPARQL {

	// LOGGER: ComponentManager
	private static Logger logger = Logger.getLogger(AutomaticNegativeExampleFinderSPARQL.class);

	private SPARQLTasks sparqltasks;

	private SortedSet<String> filterClasses;
	
	private SortedSet<String> fullPositiveSet;
	
	private SortedSet<String> fromRelated  = new TreeSet<String>();
	private SortedSet<String> fromNearbyClasses  = new TreeSet<String>();
	private SortedSet<String> fromSuperclasses = new TreeSet<String>();;
	private SortedSet<String> fromParallelClasses = new TreeSet<String>();;
	private SortedSet<String> fromRandom = new TreeSet<String>();;
	private SortedSet<String> fromDomain = new TreeSet<String>();;
	private SortedSet<String> fromRange = new TreeSet<String>();;
	
	static int poslimit = 10;
	static int neglimit = 20;

	
	/**
	 * takes as input a full positive set to make sure no negatives are added as positives
	 *  
	 * @param fullPositiveSet
	 * @param st
	 */
	public AutomaticNegativeExampleFinderSPARQL(
			SortedSet<String> fullPositiveSet,
			SPARQLTasks st, SortedSet<String> filterClasses) {
		super();
		this.fullPositiveSet = new TreeSet<String>(); 
		this.fullPositiveSet.addAll(fullPositiveSet);
		this.sparqltasks = st;
		this.filterClasses=filterClasses;
	}
	
	
	
	/**
	 * see <code>  getNegativeExamples(int neglimit, boolean stable )</code>
	 * @param neglimit
	 */
	public SortedSet<String> getNegativeExamples(int neglimit ) {
		return getNegativeExamples(neglimit, false);
	}

	/**
	 * aggregates all collected neg examples
	 * CAVE: it is necessary to call one of the make functions before calling this
	 * OTHERWISE it will choose random examples
	 * 
	 * @param neglimit size of negative Example set, 0 means all, which can be quite large several thousands
	 * @param stable decides whether neg Examples are randomly picked, default false, faster for developing, since the cache can be used
	 */
	public SortedSet<String> getNegativeExamples(int neglimit, boolean stable ) {
		SortedSet<String> negatives = new TreeSet<String>();
		negatives.addAll(fromNearbyClasses);
		negatives.addAll(fromParallelClasses);
		negatives.addAll(fromRelated);
		negatives.addAll(fromSuperclasses);
		if(negatives.isEmpty()) {
			negatives.addAll(fromRandom);
		}
		if(neglimit<=0){
			logger.debug("neg Example size NO shrinking: " + negatives.size());
			return negatives;
		}
		
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

	
	public void makeNegativeExamplesFromRandomInstances() {
		logger.debug("making random examples ");
		String variable = "subject";
		String sparqlQueryString="SELECT ?"+variable+" WHERE {" +
				"?"+variable+" <" +OWLVocabulary.RDF_TYPE+">" + " ?o" + 
				"}";
		
		fromRandom = sparqltasks.queryAsSet(sparqlQueryString, variable);
		fromRandom.removeAll(fullPositiveSet);
		logger.debug("|-negExample size from random: " + fromRandom.size());
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

	// keep a while may still be needed
	/*public void dbpediaMakeNegativeExamplesFromRelatedInstances(String subject) {
		// SortedSet<String> result = new TreeSet<String>();

		String SPARQLquery = "SELECT * WHERE { \n" + "<" + subject + "> " + "?p ?o. \n"
				+ "FILTER (REGEX(str(?o), 'http://dbpedia.org/resource/')).\n"
				+ "FILTER (!REGEX(str(?p), 'http://www.w3.org/2004/02/skos'))\n" + "}";

		this.fromRelated.addAll(sparqltasks.queryAsSet(SPARQLquery, "o"));

	}*/
	
	public void makeNegativeExamplesFromNearbyClasses(SortedSet<String> positiveSet, int sparqlResultLimit){
		SortedSet<String> classes = new TreeSet<String>();
		Iterator<String> instanceIter = positiveSet.iterator();
		while(classes.isEmpty() && instanceIter.hasNext()) {
			classes.addAll(sparqltasks.getClassesForInstance(instanceIter.next(), 100));
		}
		String concept=classes.first();
		if (filterClasses!=null&&filterClasses.size()>0){
			boolean br=false;
			for (String oneClass : classes){
				Iterator<String> iter=filterClasses.iterator();
				while (iter.hasNext()){
					if (oneClass.startsWith(iter.next())){
						break;
					}
					else{
						concept=oneClass;
						br=true;
						break;
					}
				}
				if (br) break;
			}
		}
		concept = concept.replaceAll("\"", "");
		SortedSet<String> superClasses = sparqltasks.getSuperClasses(concept, 1);
		
		classes = new TreeSet<String>();
		for (String oneSuperClass : superClasses) {
			classes.addAll(sparqltasks.getSubClasses(oneSuperClass, 1));
		}
		classes.remove(concept);
		for (String oneClass : classes) {
			try{
				fromNearbyClasses.addAll(sparqltasks.retrieveInstancesForClassDescription("\""
						+ oneClass + "\"", sparqlResultLimit));
			} catch (Exception e){}
		}
		
		this.fromNearbyClasses.removeAll(fullPositiveSet);
	}
	
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
				e.printStackTrace();
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
	 * it gets the first class of an arbitrary  instance and queries the superclasses of it,
	 * could be more elaborate.
	 * It is better to use makeNegativeExamplesFromSuperClasses
	 * @param positiveSet
	 * @param sparqlResultSetLimit
	 */
	public void makeNegativeExamplesFromSuperClassesOfInstances(SortedSet<String> positiveSet, 
			int sparqlResultSetLimit) {
		SortedSet<String> classes = new TreeSet<String>();
		Iterator<String> instanceIter = positiveSet.iterator();
		while(classes.isEmpty() && instanceIter.hasNext()) {
			classes.addAll(sparqltasks.getClassesForInstance(instanceIter.next(), sparqlResultSetLimit));
		
		}
		makeNegativeExamplesFromSuperClasses(classes.first(), sparqlResultSetLimit);
	}
	

	public void makeNegativeExamplesFromSuperClasses(String concept, int sparqlResultSetLimit) {
		makeNegativeExamplesFromSuperClasses( concept,  sparqlResultSetLimit, 2);
	}
	
	/**
	 * if pos ex derive from one class, then neg ex are taken from a superclass
	 * @param concept
	 * @param sparqlResultSetLimit
	 */
	public void makeNegativeExamplesFromSuperClasses(String concept, int sparqlResultSetLimit, int depth) {

		concept = concept.replaceAll("\"", "");
		// superClasses.add(concept.replace("\"", ""));
		// logger.debug("before"+superClasses);
		SortedSet<String> superClasses = sparqltasks.getSuperClasses(concept, depth);
		logger.debug("making neg Examples from " + superClasses.size() + " superclasses");

		for (String oneSuperClass : superClasses) {
			logger.debug(oneSuperClass);
			fromSuperclasses.addAll(sparqltasks.retrieveInstancesForClassDescription("\""
					+ oneSuperClass + "\"", sparqlResultSetLimit));

		}
		this.fromSuperclasses.removeAll(fullPositiveSet);
		logger.debug("|-neg Example from superclass: " + fromSuperclasses.size());
	}
	
	@SuppressWarnings("unused")
	private void makeNegativeExamplesFromDomain(String role, int sparqlResultSetLimit){
		logger.debug("making Negative Examples from Domain of : "+role);
		fromDomain.addAll(sparqltasks.getDomainInstances(role, sparqlResultSetLimit));
		fromDomain.removeAll(fullPositiveSet);
		logger.debug("|-neg Example size from Domain: "+this.fromDomain.size());
	}
	
	@SuppressWarnings("unused")
	private void makeNegativeExamplesFromRange(String role, int sparqlResultSetLimit){
		logger.debug("making Negative Examples from Range of : "+role);
		fromRange.addAll(sparqltasks.getRangeInstances(role, sparqlResultSetLimit));
		fromRange.removeAll(fullPositiveSet);
		logger.debug("|-neg Example size from Range: "+fromRange.size());
	}
}
