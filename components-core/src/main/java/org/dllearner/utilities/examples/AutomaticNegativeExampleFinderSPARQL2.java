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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.cache.h2.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
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

	
	// for re-using existing queries
	private SPARQLReasoner sr;

	private String namespace;
	private String cacheDirectory = "cache";
	private QueryExecutionFactory qef;
	
	public AutomaticNegativeExampleFinderSPARQL2(SparqlEndpoint se, SPARQLReasoner reasoner) {
		this(se, reasoner, null);
	}
	
	public AutomaticNegativeExampleFinderSPARQL2(SparqlEndpoint se, SPARQLReasoner reasoner, String namespace) {
		this.sr = reasoner;
		this.namespace = namespace;
		
		qef = new QueryExecutionFactoryHttp(se.getURL().toString(), se.getDefaultGraphURIs());
		if(cacheDirectory != null){
				long timeToLive = TimeUnit.DAYS.toMillis(30);
				CacheFrontend cacheFrontend = CacheUtilsH2.createCacheFrontend(cacheDirectory, true, timeToLive);
				qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
		}
	}
	
	public AutomaticNegativeExampleFinderSPARQL2(SparqlEndpoint se) {
		this(se, (String) null);
	}
	
	public AutomaticNegativeExampleFinderSPARQL2(SparqlEndpoint se, String namespace) {
		this(se, new SPARQLReasoner(new SparqlEndpointKS(se)), namespace);
	}
	
	public AutomaticNegativeExampleFinderSPARQL2(SPARQLReasoner reasoner, String namespace) {
		this.sr = reasoner;
		this.namespace = namespace;
	}
	
	public AutomaticNegativeExampleFinderSPARQL2(SPARQLReasoner reasoner) {
		this.sr = reasoner;
	}
	
	public SortedSet<Individual> getNegativeExamples(NamedClass classToDescribe, Set<Individual> positiveExamples, int limit) {
		return getNegativeExamples(classToDescribe, positiveExamples, Arrays.asList(SUPERCLASS, SIBLING, RANDOM), limit);
	}
	
	public SortedSet<Individual> getNegativeExamples(NamedClass classToDescribe, Set<Individual> positiveExamples, Collection<Strategy> strategies, int limit) {
		Map<Strategy, Double> strategiesWithWeight = Maps.newLinkedHashMap();
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
		
		return computeNegativeExamples(classToDescribe, types, strategiesWithWeight, maxNrOfReturnedInstances);
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
		return computeNegativeExamples(null, types, strategiesWithWeight, maxNrOfReturnedInstances);
	}
	
	private SortedSet<Individual> computeNegativeExamples(NamedClass classToDescribe, Multiset<NamedClass> positiveExamplesTypes, Map<Strategy, Double> strategiesWithWeight, int maxNrOfReturnedInstances) {
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
//					superClasses.remove(Thing.instance);
					superClasses.remove(new NamedClass("http://www.w3.org/2000/01/rdf-schema#Resource"));
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
				logger.info("Applying random strategy...");
				SortedSet<Individual> randomNegativeExamples = new TreeSet<Individual>();
				String query = "SELECT DISTINCT ?s WHERE {?s a ?type.";
				if(classToDescribe != null){
					query += "FILTER NOT EXISTS{?s a <" + classToDescribe + "> }";
				} else {
					for (NamedClass nc : positiveExamplesTypes.elementSet()) {
						
					}
					throw new UnsupportedOperationException("Currently it's not possible to get random examples for unknown class to describe.");
				}
				
				query += "} ORDER BY RAND() LIMIT " + maxNrOfReturnedInstances;
				QueryExecution qe = qef.createQueryExecution(query);
				ResultSet rs = qe.execSelect();
				QuerySolution qs;
				while(rs.hasNext()){
					qs = rs.next();
					randomNegativeExamples.add(new Individual(qs.getResource("s").getURI()));
				}
				randomNegativeExamples.removeAll(negativeExamples);
				negativeExamples.addAll(new ArrayList<>(randomNegativeExamples).subList(0, Math.min(randomNegativeExamples.size(), maxNrOfReturnedInstances - negativeExamples.size())));
				logger.info("Negative examples(" + randomNegativeExamples.size() + "): " + randomNegativeExamples);
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
		for (NamedClass nc1 : copy.elementSet()) {
			for (NamedClass nc2 : copy.elementSet()) {
				if(!nc1.equals(nc2)){
					//remove class nc1 if it is superclass of another class nc2
					boolean isSubClassOf = false;
					if(sr.getClassHierarchy() != null){
						isSubClassOf = sr.getClassHierarchy().isSubclassOf(nc2, nc1);
					} else {
						isSubClassOf = sr.isSuperClassOf(nc1, nc2);
					}
					if(isSubClassOf){
						classes.remove(nc1, classes.count(nc1));
						break;
					}
				}
			}
		}
		
		
	}
}
