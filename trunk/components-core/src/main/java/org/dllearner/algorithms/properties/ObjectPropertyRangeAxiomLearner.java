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

package org.dllearner.algorithms.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.ObjectPropertyEditor;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyRangeAxiom;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

@ComponentAnn(name="objectproperty range learner", shortName="oplrange", version=0.1)
public class ObjectPropertyRangeAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(ObjectPropertyRangeAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	
	public ObjectPropertyRangeAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
	}
	
	public ObjectProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(ObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
	}

	@Override
	public void start() {
		logger.info("Start learning...");
		startTime = System.currentTimeMillis();
		fetchedRows = 0;
		currentlyBestAxioms = new ArrayList<EvaluatedAxiom>();
		
		if(reasoner.isPrepared()){
			//get existing ranges
			Description existingDomain = reasoner.getRange(propertyToDescribe);
			if(existingDomain != null){
				existingAxioms.add(new ObjectPropertyRangeAxiom(propertyToDescribe, existingDomain));
				if(reasoner.isPrepared()){
					if(reasoner.getClassHierarchy().contains(existingDomain)){
						for(Description sup : reasoner.getClassHierarchy().getSuperClasses(existingDomain)){
							existingAxioms.add(new ObjectPropertyRangeAxiom(propertyToDescribe, existingDomain));
							logger.info("Existing range(inferred): " + sup);
						}
					}
					
				}
			}
		}
		
		//get objects with types
		Map<Individual, SortedSet<Description>> individual2Types = new HashMap<Individual, SortedSet<Description>>();
		boolean repeat = true;
		int limit = 1000;
		while(!terminationCriteriaSatisfied() && repeat){
			int ret = addIndividualsWithTypes(individual2Types, limit, fetchedRows);
			currentlyBestAxioms = buildEvaluatedAxioms(individual2Types);
			fetchedRows += 1000;
			repeat = (ret == limit);
		}
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}
	
	private List<EvaluatedAxiom> buildEvaluatedAxioms(Map<Individual, SortedSet<Description>> individual2Types){
		List<EvaluatedAxiom> axioms = new ArrayList<EvaluatedAxiom>();
		Map<Description, Integer> result = new HashMap<Description, Integer>();
		for(Entry<Individual, SortedSet<Description>> entry : individual2Types.entrySet()){
			for(Description nc : entry.getValue()){
				Integer cnt = result.get(nc);
				if(cnt == null){
					cnt = Integer.valueOf(1);
				} else {
					cnt = Integer.valueOf(cnt + 1);
				}
				result.put(nc, cnt);
			}
		}
		
		//omit owl:Thing
		result.remove(new NamedClass(Thing.instance.getURI()));
		
		EvaluatedAxiom evalAxiom;
		int total = individual2Types.keySet().size();
		for(Entry<Description, Integer> entry : sortByValues(result)){
			evalAxiom = new EvaluatedAxiom(new ObjectPropertyRangeAxiom(propertyToDescribe, entry.getKey()),
					computeScore(total, entry.getValue()));
			if(existingAxioms.contains(evalAxiom.getAxiom())){
				evalAxiom.setAsserted(true);
			}
			axioms.add(evalAxiom);
		}
		
		return axioms;
	}
	
	private int addIndividualsWithTypes(Map<Individual, SortedSet<Description>> ind2Types, int limit, int offset){
		String query = String.format("SELECT DISTINCT ?ind ?type WHERE {?s <%s> ?ind. ?ind a ?type.} LIMIT %d OFFSET %d", propertyToDescribe.getName(), limit, offset);
		
//		String query = String.format("SELECT DISTINCT ?ind ?type WHERE {?ind a ?type. {SELECT ?ind {?ind <%s> ?o.} LIMIT %d OFFSET %d}}", propertyToDescribe.getName(), limit, offset);
		
		ResultSet rs = executeSelectQuery(query);
		Individual ind;
		Description newType;
		QuerySolution qs;
		SortedSet<Description> types;
		int cnt = 0;
		while(rs.hasNext()){
			cnt++;
			qs = rs.next();
			ind = new Individual(qs.getResource("ind").getURI());
			newType = new NamedClass(qs.getResource("type").getURI());
			types = ind2Types.get(ind);
			if(types == null){
				types = new TreeSet<Description>();
				ind2Types.put(ind, types);
			}
			types.add(newType);
			Set<Description> superClasses;
			if(reasoner.isPrepared()){
				if(reasoner.getClassHierarchy().contains(newType)){
					superClasses = reasoner.getClassHierarchy().getSuperClasses(newType);
					types.addAll(superClasses);
				}
				
			}
		}
		return cnt;
	}
	
	public static void main(String[] args) throws Exception{
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		
		SPARQLReasoner reasoner = new SPARQLReasoner(ks);
		reasoner.prepareSubsumptionHierarchy();
		
		ObjectPropertyRangeAxiomLearner l = new ObjectPropertyRangeAxiomLearner(ks);
		l.setReasoner(reasoner);
		l.setPropertyToDescribe(new ObjectProperty("http://dbpedia.org/ontology/author"));
		l.setMaxExecutionTimeInSeconds(10);
		l.setReturnOnlyNewAxioms(true);
		l.init();
		l.start();
		
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5));
	}

}