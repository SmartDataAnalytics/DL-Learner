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
import org.dllearner.core.config.DataPropertyEditor;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyRangeAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

@ComponentAnn(name="dataproperty range learner", shortName="dblrange", version=0.1)
public class DataPropertyRangeAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(DataPropertyRangeAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=DataPropertyEditor.class)
	private DatatypeProperty propertyToDescribe;
	
	public DataPropertyRangeAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
	}
	
	public DatatypeProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(DatatypeProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
	}
	
	@Override
	public void start() {
		logger.info("Start learning...");
		startTime = System.currentTimeMillis();
		fetchedRows = 0;
		currentlyBestAxioms = new ArrayList<EvaluatedAxiom>();
		//get existing range
		DataRange existingRange = reasoner.getRange(propertyToDescribe);
		if(existingRange != null){
			existingAxioms.add(new DatatypePropertyRangeAxiom(propertyToDescribe, existingRange));
			logger.debug("Existing range: " + existingRange);
		}
		
		//get objects with datatypes
		Map<Individual, SortedSet<Datatype>> individual2Datatypes = new HashMap<Individual, SortedSet<Datatype>>();
		boolean repeat = true;
		int limit = 1000;
		while(!terminationCriteriaSatisfied() && repeat){
			int ret = addIndividualsWithTypes(individual2Datatypes, limit, fetchedRows);
			currentlyBestAxioms = buildEvaluatedAxioms(individual2Datatypes);
			fetchedRows += 1000;
			repeat = (ret == limit);
		}
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}

	private List<EvaluatedAxiom> buildEvaluatedAxioms(Map<Individual, SortedSet<Datatype>> individual2Types){
		List<EvaluatedAxiom> axioms = new ArrayList<EvaluatedAxiom>();
		Map<Datatype, Integer> result = new HashMap<Datatype, Integer>();
		for(Entry<Individual, SortedSet<Datatype>> entry : individual2Types.entrySet()){
			for(Datatype nc : entry.getValue()){
				Integer cnt = result.get(nc);
				if(cnt == null){
					cnt = Integer.valueOf(1);
				} else {
					cnt = Integer.valueOf(cnt + 1);
				}
				result.put(nc, cnt);
			}
		}
		
		EvaluatedAxiom evalAxiom;
		int total = individual2Types.keySet().size();
		for(Entry<Datatype, Integer> entry : sortByValues(result)){
			evalAxiom = new EvaluatedAxiom(new DatatypePropertyRangeAxiom(propertyToDescribe, entry.getKey()),
					computeScore(total, entry.getValue()));
			if(existingAxioms.contains(evalAxiom.getAxiom())){
				evalAxiom.setAsserted(true);
			}
			axioms.add(evalAxiom);
		}
		
		return axioms;
	}
	
	
	private int addIndividualsWithTypes(Map<Individual, SortedSet<Datatype>> ind2Datatypes, int limit, int offset){
		String query = String.format("SELECT ?ind (DATATYPE(?val) AS ?datatype) WHERE {?ind <%s> ?val.} LIMIT %d OFFSET %d", propertyToDescribe.getName(), limit, offset);
		
		ResultSet rs = executeSelectQuery(query);
		Individual ind;
		Datatype newType;
		QuerySolution qs;
		SortedSet<Datatype> types;
		int cnt = 0;
		while(rs.hasNext()){
			cnt++;
			newType = null;
			qs = rs.next();
			ind = new Individual(qs.getResource("ind").getURI());
			if(qs.getResource("datatype") != null){
				newType = new Datatype(qs.getResource("datatype").getURI());
				types = ind2Datatypes.get(ind);
				if(types == null){
					types = new TreeSet<Datatype>();
					ind2Datatypes.put(ind, types);
				}
				types.add(newType);
			}
			
		}
		return cnt;
	}
	
	public static void main(String[] args) throws Exception{
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		
		SPARQLReasoner reasoner = new SPARQLReasoner(ks);
		reasoner.prepareSubsumptionHierarchy();
		
		DataPropertyRangeAxiomLearner l = new DataPropertyRangeAxiomLearner(ks);
		l.setReasoner(reasoner);
		l.setPropertyToDescribe(new DatatypeProperty("http://dbpedia.org/ontology/topSpeed"));
		l.setMaxExecutionTimeInSeconds(10);
		l.setReturnOnlyNewAxioms(true);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(1));
	}
}
