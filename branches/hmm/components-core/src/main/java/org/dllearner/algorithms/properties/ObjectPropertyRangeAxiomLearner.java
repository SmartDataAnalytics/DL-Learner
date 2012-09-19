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

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

@ComponentAnn(name="objectproperty range learner", shortName="oplrange", version=0.1)
public class ObjectPropertyRangeAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(ObjectPropertyRangeAxiomLearner.class);
	private Map<Individual, SortedSet<Description>> individual2Types;
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	
	public ObjectPropertyRangeAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		super.iterativeQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?ind ?type WHERE {?s ?p ?ind. ?ind a ?type.}");
	}
	
	public ObjectProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(ObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
	}

	@Override
	public void start() {
		iterativeQueryTemplate.setIri("p", propertyToDescribe.getName());
		logger.info("Start learning...");
		startTime = System.currentTimeMillis();
		fetchedRows = 0;
		currentlyBestAxioms = new ArrayList<EvaluatedAxiom>();
		
		if(returnOnlyNewAxioms){
			//get existing ranges
			Description existingRange = reasoner.getRange(propertyToDescribe);
			if(existingRange != null){
				existingAxioms.add(new ObjectPropertyRangeAxiom(propertyToDescribe, existingRange));
				if(reasoner.isPrepared()){
					if(reasoner.getClassHierarchy().contains(existingRange)){
						for(Description sup : reasoner.getClassHierarchy().getSuperClasses(existingRange)){
							existingAxioms.add(new ObjectPropertyRangeAxiom(propertyToDescribe, existingRange));
							logger.info("Existing range(inferred): " + sup);
						}
					}
					
				}
			}
		}
		
		runIterativeQueryMode();
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}
	
	private void runSingleQueryMode(){
		
	}
	
	private void runIterativeQueryMode(){
		individual2Types = new HashMap<Individual, SortedSet<Description>>();
		while(!terminationCriteriaSatisfied() && !fullDataLoaded){
			ResultSet rs = fetchData();
			processData(rs);
			buildEvaluatedAxioms();
		}
	}
	
	private void processData(ResultSet rs){
		QuerySolution qs;
		Individual ind;
		Description type;
		SortedSet<Description> types;
		int cnt = 0;
		while(rs.hasNext()){
			cnt++;
			qs = rs.next();
			if(qs.get("type").isURIResource()){
				types = new TreeSet<Description>();
				ind = new Individual(qs.getResource("ind").getURI());
				type = new NamedClass(qs.getResource("type").getURI());
				types.add(type);
				if(reasoner.isPrepared()){
					if(reasoner.getClassHierarchy().contains(type)){
						types.addAll(reasoner.getClassHierarchy().getSuperClasses(type));
					}
				}
				addToMap(individual2Types, ind, types);
			}
		}
		lastRowCount = cnt;
	}

	private void buildEvaluatedAxioms(){
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
		
		currentlyBestAxioms = axioms;
	}
	
	public static void main(String[] args) throws Exception{
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia());
		
		SPARQLReasoner reasoner = new SPARQLReasoner(ks);
		reasoner.prepareSubsumptionHierarchy();
		
		ObjectPropertyRangeAxiomLearner l = new ObjectPropertyRangeAxiomLearner(ks);
		l.setReasoner(reasoner);
		l.setPropertyToDescribe(new ObjectProperty("http://dbpedia.org/ontology/ideology"));
		l.setMaxExecutionTimeInSeconds(10);
//		l.setReturnOnlyNewAxioms(true);
		l.init();
		l.start();
		
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5));
	}

}
