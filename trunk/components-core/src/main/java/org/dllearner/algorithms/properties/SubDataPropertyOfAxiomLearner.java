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

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.DataPropertyEditor;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.SubDatatypePropertyAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

@ComponentAnn(name="data subPropertyOf axiom learner", shortName="dplsubprop", version=0.1)
public class SubDataPropertyOfAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(ObjectPropertyDomainAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=DataPropertyEditor.class)
	private DatatypeProperty propertyToDescribe;
	
	public SubDataPropertyOfAxiomLearner(SparqlEndpointKS ks){
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
		//get existing super properties
		SortedSet<DatatypeProperty> existingSuperProperties = reasoner.getSuperProperties(propertyToDescribe);
		logger.debug("Existing super properties: " + existingSuperProperties);
		
		//get properties and how often they occur
		int limit = 1000;
		int offset = 0;
		String queryTemplate = "SELECT ?p COUNT(?s) AS ?count WHERE {?p a <http://www.w3.org/2002/07/owl#DatatypeProperty>. ?s ?p ?o. " +
		"{SELECT ?s ?o WHERE {?s <%s> ?o.} LIMIT %d OFFSET %d}" +
		"}";
		String query;
		Map<DatatypeProperty, Integer> result = new HashMap<DatatypeProperty, Integer>();
		DatatypeProperty prop;
		Integer oldCnt;
		boolean repeat = true;
		
		while(!terminationCriteriaSatisfied() && repeat){
			query = String.format(queryTemplate, propertyToDescribe, limit, offset);
			ResultSet rs = executeSelectQuery(query);
			QuerySolution qs;
			repeat = false;
			while(rs.hasNext()){
				qs = rs.next();
				prop = new DatatypeProperty(qs.getResource("p").getURI());
				int newCnt = qs.getLiteral("count").getInt();
				oldCnt = result.get(prop);
				if(oldCnt == null){
					oldCnt = Integer.valueOf(newCnt);
				}
				result.put(prop, oldCnt);
				qs.getLiteral("count").getInt();
				repeat = true;
			}
			
			if(!result.isEmpty()){
				currentlyBestAxioms = buildAxioms(result);
				offset += 1000;
			}
		}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}
	
	private List<EvaluatedAxiom> buildAxioms(Map<DatatypeProperty, Integer> property2Count){
		List<EvaluatedAxiom> axioms = new ArrayList<EvaluatedAxiom>();
		Integer total = property2Count.get(propertyToDescribe);
		property2Count.remove(propertyToDescribe);
		
		EvaluatedAxiom evalAxiom;
		for(Entry<DatatypeProperty, Integer> entry : sortByValues(property2Count)){
			evalAxiom = new EvaluatedAxiom(new SubDatatypePropertyAxiom(propertyToDescribe, entry.getKey()),
					computeScore(total, entry.getValue()));
			axioms.add(evalAxiom);
		}
		
		property2Count.put(propertyToDescribe, total);
		return axioms;
	}
	
	public static void main(String[] args) throws Exception{
		SubDataPropertyOfAxiomLearner l = new SubDataPropertyOfAxiomLearner(new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia()));
		l.setPropertyToDescribe(new DatatypeProperty("http://dbpedia.org/ontology/purpose"));
		l.setMaxExecutionTimeInSeconds(10);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5));
	}
	
}
