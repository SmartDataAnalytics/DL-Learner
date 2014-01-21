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

import static org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2.Strategy.RANDOM;
import static org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2.Strategy.SIBLING;
import static org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2.Strategy.SUPERCLASS;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;
/**
 * 
 * Utility class for automatically retrieving negative examples from a 
 * SPARQL endpoint given a set of positive examples.
 * 
 * @author Jens Lehmann
 * @author Sebastian Hellmann
 *
 */
public class AutomaticNegativeExampleFinderSPARQL2 {
	
	private static final Logger logger = LoggerFactory.getLogger(AutomaticNegativeExampleFinderSPARQL2.class.getSimpleName());
	
	public enum Strategy{
		SUPERCLASS, SIBLING, RANDOM;
	}

	private SparqlEndpoint se;
	
	// for re-using existing queries
	private SPARQLReasoner sr;
	private SPARQLTasks st;

	private String namespace;
	
	public AutomaticNegativeExampleFinderSPARQL2(SparqlEndpoint se) {
		this.se = se;
		SparqlEndpointKS ks = new SparqlEndpointKS(se);
		sr = new SPARQLReasoner(ks);
		st = new SPARQLTasks(se);
	}
	
	public AutomaticNegativeExampleFinderSPARQL2(SPARQLReasoner reasoner, String namespace) {
		this.sr = reasoner;
		this.namespace = namespace;
	}
	
	public AutomaticNegativeExampleFinderSPARQL2(SPARQLReasoner reasoner) {
		this.sr = reasoner;
	}
	
	/**
	 * Get negative examples when learning the description of a class, i.e.
	 * all positives are from some known class.
	 * 
	 * Currently, the method implementation is preliminary and does not allow
	 * to configure internals.
	 * 
	 * @param classURI The known class of all positive examples.
	 * @param positiveExamples The existing positive examples.
	 */
	public SortedSet<String> getNegativeExamples(String classURI, SortedSet<String> positiveExamples) {
		// get some individuals from parallel classes (we perform one query per class to avoid
		// only getting individuals from a single class)
		Set<String> parallelClasses = st.getParallelClasses(classURI, 5); // TODO: limit could be configurable
		SortedSet<String> negEx = new TreeSet<String>();
		for(String parallelClass : parallelClasses) {
			Set<String> inds = Datastructures.individualSetToStringSet(sr.getIndividuals(new NamedClass(parallelClass), 10));
			negEx.addAll(inds);
			if(negEx.size()>100) {
				return negEx;
			}
		}
		// add some random instances
		String query = "SELECT ?inst { ?inst <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?x } LIMIT 20";
		negEx.addAll(st.queryAsSet(query, "?inst"));
        return negEx;
	}
	
	public SortedSet<Individual> getNegativeExamples(NamedClass classToDescribe, Set<Individual> positiveExamples, int limit) {
		return getNegativeExamples(classToDescribe, positiveExamples, Arrays.asList(SUPERCLASS, SIBLING, RANDOM), limit);
	}
	
	public SortedSet<Individual> getNegativeExamples(NamedClass classToDescribe, Set<Individual> positiveExamples, Collection<Strategy> strategies, int limit) {
		Map<Strategy, Double> strategiesWithWeight = new HashMap<Strategy, Double>();
		double weight = 1d/strategies.size();
		for (Strategy strategy : strategies) {
			strategiesWithWeight.put(strategy, weight);
		}
		return getNegativeExamples(classToDescribe, positiveExamples, strategiesWithWeight, limit);
	}
	
	public SortedSet<Individual> getNegativeExamples(NamedClass classToDescribe, Set<Individual> positiveExamples, Map<Strategy, Double> strategiesWithWeight, int maxNrOfReturnedInstances) {
		//set class to describe as the type for each instance
		Multiset<NamedClass> types = HashMultiset.create();
		types.add(classToDescribe);
		
		return computeNegativeExamples(types, strategiesWithWeight, maxNrOfReturnedInstances);
	}
	
	public SortedSet<Individual> getNegativeExamples(Set<Individual> positiveExamples, int limit) {
		return getNegativeExamples(positiveExamples, Arrays.asList(SUPERCLASS, SIBLING, RANDOM), limit);
	}
	
	public SortedSet<Individual> getNegativeExamples(Set<Individual> positiveExamples, Collection<Strategy> strategies, int limit) {
		Map<Strategy, Double> strategiesWithWeight = new HashMap<Strategy, Double>();
		double weight = 1d/strategies.size();
		for (Strategy strategy : strategies) {
			strategiesWithWeight.put(strategy, weight);
		}
		return getNegativeExamples(positiveExamples, strategiesWithWeight, limit);
	}
	
	public SortedSet<Individual> getNegativeExamples(Set<Individual> positiveExamples, Map<Strategy, Double> strategiesWithWeight, int maxNrOfReturnedInstances) {
		//get the types for each instance
		Multiset<NamedClass> types = HashMultiset.create();
		for (Individual ex : positiveExamples) {
			types.addAll(sr.getTypes(ex));
		}
		
		//remove types that do not have the given namespace
		types = filterByNamespace(types);
		
		//keep the most specific types
		keepMostSpecificClasses(types);
		return computeNegativeExamples(types, strategiesWithWeight, maxNrOfReturnedInstances);
	}
	
	private SortedSet<Individual> computeNegativeExamples(Multiset<NamedClass> positiveExamplesTypes, Map<Strategy, Double> strategiesWithWeight, int maxNrOfReturnedInstances) {
		SortedSet<Individual> negativeExamples = new TreeSet<Individual>();
		
		for (Entry<Strategy, Double> entry : strategiesWithWeight.entrySet()) {
			Strategy strategy = entry.getKey();
			Double weight = entry.getValue();
			//the max number of instances returned by the current strategy
			int strategyLimit = (int)(weight * maxNrOfReturnedInstances);
			//the highest frequency value
			int maxFrequency = positiveExamplesTypes.entrySet().iterator().next().getCount();
			
			if(strategy == SIBLING){//get sibling class based examples
				logger.info("Applying sibling classes strategy...");
				SortedSet<Individual> siblingNegativeExamples = new TreeSet<Individual>();
				//for each type of the positive examples
				for (NamedClass nc : positiveExamplesTypes.elementSet()) {
					int frequency = positiveExamplesTypes.count(nc);
					//get sibling classes
					Set<NamedClass> siblingClasses = sr.getSiblingClasses(nc);
					siblingClasses = filterByNamespace(siblingClasses);
					logger.info("Sibling classes: " + siblingClasses);
					
					int limit = (int)Math.ceil(((double)frequency / positiveExamplesTypes.size()) / siblingClasses.size() * strategyLimit);
					//get instances for each sibling class
					for (NamedClass siblingClass : siblingClasses) {
						SortedSet<Individual> individuals = sr.getIndividualsExcluding(siblingClass, nc, maxNrOfReturnedInstances);
						individuals.removeAll(siblingNegativeExamples);
						SetManipulation.stableShrink(individuals, limit);
						siblingNegativeExamples.addAll(individuals);
					}
				}
				siblingNegativeExamples = SetManipulation.stableShrink(siblingNegativeExamples, strategyLimit);
				logger.info("Negative examples(" + siblingNegativeExamples.size() + "): " + siblingNegativeExamples);
				negativeExamples.addAll(siblingNegativeExamples);
			} else if(strategy == SUPERCLASS){//get super class based examples
				logger.info("Applying super class strategy...");
				SortedSet<Individual> superClassNegativeExamples = new TreeSet<Individual>();
				//for each type of the positive examples
				for (NamedClass nc : positiveExamplesTypes.elementSet()) {
					int frequency = positiveExamplesTypes.count(nc);
					//get super classes
					Set<Description> superClasses = sr.getSuperClasses(nc);
					superClasses.remove(new NamedClass(Thing.instance.getURI()));
					superClasses.remove(Thing.instance);
					superClasses = filterByNamespace(superClasses);
					logger.info("Super classes: " + superClasses);
					
					int limit = (int)Math.ceil(((double)frequency / positiveExamplesTypes.size()) / superClasses.size() * strategyLimit);
					//get instances for each super class
					for (Description superClass : superClasses) {
						SortedSet<Individual> individuals = sr.getIndividualsExcluding(superClass, nc, maxNrOfReturnedInstances);
						individuals.removeAll(negativeExamples);
						individuals.removeAll(superClassNegativeExamples);
						SetManipulation.stableShrink(individuals, limit);
						superClassNegativeExamples.addAll(individuals);
					}
				}
				superClassNegativeExamples = SetManipulation.stableShrink(superClassNegativeExamples, strategyLimit);
				logger.info("Negative examples(" + superClassNegativeExamples.size() + "): " + superClassNegativeExamples);
				negativeExamples.addAll(superClassNegativeExamples);
			} else if(strategy == RANDOM){//get some random examples
				
			}
		}
        return negativeExamples;
	}
	
	private <T extends Description> Set<T> filterByNamespace(Set<T> classes){
		if(namespace != null){
			return Sets.filter(classes, new Predicate<T>() {
				public boolean apply(T input){
					return input.toString().startsWith(namespace);
				}
			});
		}
		return classes;
	}
	
	private Multiset<NamedClass> filterByNamespace(Multiset<NamedClass> classes){
		if(namespace != null){
			return Multisets.filter(classes, new Predicate<NamedClass>() {
				public boolean apply(NamedClass input){
					return input.getName().startsWith(namespace);
				}
			});
		}
		return classes;
	}
	
	private void keepMostSpecificClasses(Multiset<NamedClass> classes){
		HashMultiset<NamedClass> copy = HashMultiset.create(classes);
		final ClassHierarchy hierarchy = sr.getClassHierarchy();
		for (NamedClass nc1 : copy.elementSet()) {
			for (NamedClass nc2 : copy.elementSet()) {
				if(!nc1.equals(nc2)){
					//remove class nc1 if it is superclass of another class nc2
					if(hierarchy.isSubclassOf(nc2, nc1)){
						classes.remove(nc1, classes.count(nc1));
						break;
					}
				}
			}
		}
	}
}
