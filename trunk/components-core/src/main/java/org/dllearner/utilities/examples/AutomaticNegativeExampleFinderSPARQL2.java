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
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.datastructures.Datastructures;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
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
		SortedSet<Individual> negEx = new TreeSet<Individual>();
		
		//get the types for each instance
		Multiset<NamedClass> types = HashMultiset.create();
		for (Individual ex : positiveExamples) {
			types.addAll(sr.getTypes(ex));
		}
		
		//remove types that do not have the given namespace
		if(namespace != null){
			types = Multisets.filter(types, new Predicate<NamedClass>() {
				public boolean apply(NamedClass input){
					return input.getName().startsWith(namespace);
				}
			});
		}
		
		//keep the most specific types
		keepMostSpecificClasses(types);
		
		for (Entry<Strategy, Double> entry : strategiesWithWeight.entrySet()) {
			Strategy strategy = entry.getKey();
			Double weight = entry.getValue();
			//the max number of instances returned by the current strategy
			int limit = (int)(weight * maxNrOfReturnedInstances);
			//the highest frequency value
			int maxFrequency = types.entrySet().iterator().next().getCount();
			if(strategy == SIBLING){
				System.out.println("Sibling Classes Strategy");
				for (NamedClass nc : types.elementSet()) {
					int frequency = types.count(nc);
					//get sibling classes
					Set<NamedClass> siblingClasses = sr.getSiblingClasses(nc);
					int nrOfSiblings = siblingClasses.size();
					int v = (int)Math.ceil(((double)frequency / types.size()) / nrOfSiblings * limit); System.out.println(nc + ": " + v);
					for (NamedClass siblingClass : siblingClasses) {
						negEx.addAll(sr.getIndividualsExcluding(siblingClass, nc, v));
					}
					
				}
			} else if(strategy == SUPERCLASS){
				System.out.println("Super Classes Strategy");
				for (NamedClass nc : types.elementSet()) {
					int frequency = types.count(nc);
					//get sibling classes
					Set<Description> superClasses = sr.getSuperClasses(nc);System.out.println(superClasses);
					int nrOfSuperClasses = superClasses.size();
					int v = (int)Math.ceil(((double)frequency / types.size()) / nrOfSuperClasses * limit); System.out.println(nc + ": " + v);
					for (Description superClass : superClasses) {
						negEx.addAll(sr.getIndividualsExcluding(superClass, nc, v));
					}
				}
			} else if(strategy == RANDOM){
				
			}
		}
        return negEx;
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
