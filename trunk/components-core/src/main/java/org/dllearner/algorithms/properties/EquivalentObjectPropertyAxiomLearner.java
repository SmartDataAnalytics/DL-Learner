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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.Score;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.ObjectPropertyEditor;
import org.dllearner.core.owl.EquivalentObjectPropertiesAxiom;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@ComponentAnn(name="equivalent objectproperty axiom learner", shortName="oplequiv", version=0.1)
public class EquivalentObjectPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(EquivalentObjectPropertyAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	
	public EquivalentObjectPropertyAxiomLearner(SparqlEndpointKS ks){
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
		//get existing super properties
		SortedSet<ObjectProperty> existingSuperProperties = reasoner.getSuperProperties(propertyToDescribe);
		logger.debug("Existing super properties: " + existingSuperProperties);
		
		if(!forceSPARQL_1_0_Mode && ks.supportsSPARQL_1_1()){
			runSPARQL1_1_Mode();
		} else {
			runSPARQL1_0_Mode();
		}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}
	
	private void runSPARQL1_0_Mode() {
		Model model = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery  = "CONSTRUCT {?s ?p ?o.} WHERE {?s <%s> ?o. ?s ?p ?o.} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, propertyToDescribe.getName(), limit, offset);
		Model newModel = executeConstructQuery(query);
		Map<ObjectProperty, Integer> result = new HashMap<ObjectProperty, Integer>();
		while(!terminationCriteriaSatisfied() && newModel.size() != 0){
			model.add(newModel);
			query = "SELECT ?p (COUNT(?s) AS ?count) WHERE {?s ?p ?o.} GROUP BY ?p";
			
			ObjectProperty prop;
			Integer oldCnt;
			ResultSet rs = executeSelectQuery(query, model);
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				prop = new ObjectProperty(qs.getResource("p").getURI());
				int newCnt = qs.getLiteral("count").getInt();
				oldCnt = result.get(prop);
				if(oldCnt == null){
					oldCnt = Integer.valueOf(newCnt);
				}
				result.put(prop, oldCnt);
				qs.getLiteral("count").getInt();
			}
			if(!result.isEmpty()){
				currentlyBestAxioms = buildAxioms(result);
			}
			
			
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.getName(), limit, offset);
			newModel = executeConstructQuery(query);
		}
		
	}
	
	private void runSPARQL1_1_Mode() {
		//get subjects with types
				int offset = 0;
				String queryTemplate = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p (COUNT(?s) AS ?count) WHERE {?s ?p ?o.?p a owl:ObjectProperty." +
				"{SELECT ?s ?o WHERE {?s <%s> ?o.} LIMIT %d OFFSET %d}" +
				"}";
				String query;
				Map<ObjectProperty, Integer> result = new HashMap<ObjectProperty, Integer>();
				ObjectProperty prop;
				Integer oldCnt;
				boolean repeat = true;
				
				while(!terminationCriteriaSatisfied() && repeat){
					query = String.format(queryTemplate, propertyToDescribe, limit, offset);
					ResultSet rs = executeSelectQuery(query);
					QuerySolution qs;
					repeat = false;
					while(rs.hasNext()){
						qs = rs.next();
						prop = new ObjectProperty(qs.getResource("p").getURI());
						int newCnt = qs.getLiteral("count").getInt();
						oldCnt = result.get(prop);
						if(oldCnt == null){
							oldCnt = Integer.valueOf(newCnt);
						} else {
							oldCnt += newCnt;
						}
						result.put(prop, oldCnt);
						repeat = true;
					}
					if(!result.isEmpty()){
						currentlyBestAxioms = buildAxioms(result);
						offset += limit;
					}
					
				}
		
	}
	
	private List<EvaluatedAxiom> buildAxioms(Map<ObjectProperty, Integer> property2Count){
		List<EvaluatedAxiom> axioms = new ArrayList<EvaluatedAxiom>();
		Integer all = property2Count.get(propertyToDescribe);
		property2Count.remove(propertyToDescribe);
		
		EvaluatedAxiom evalAxiom;
		Set<ObjectProperty> properties;
		for(Entry<ObjectProperty, Integer> entry : sortByValues(property2Count)){
			properties = new HashSet<ObjectProperty>();
			properties.add(propertyToDescribe);
			properties.add(entry.getKey());
			int popularity = reasoner.getPropertyCount(entry.getKey());
//			int total = popularity;
			int total = Math.max(popularity, all);
			int success = entry.getValue();//System.out.println(entry.getKey());System.out.println(entry.getKey());System.out.println(total);System.out.println(success);
			Score score = computeScore(total, success);//System.out.println(entry + ": " + score.getAccuracy());
			evalAxiom = new EvaluatedAxiom(new EquivalentObjectPropertiesAxiom(properties),score);
			axioms.add(evalAxiom);
		}
		
		property2Count.put(propertyToDescribe, all);
		return axioms;
	}
	
	public static void main(String[] args) throws Exception{
		EquivalentObjectPropertyAxiomLearner l = new EquivalentObjectPropertyAxiomLearner(new SparqlEndpointKS(new SparqlEndpoint(
				new URL("http://dbpedia.aksw.org:8902/sparql"), Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList())));//.getEndpointDBpediaLiveAKSW()));
		l.setPropertyToDescribe(new ObjectProperty("http://dbpedia.org/ontology/nationality"));
		l.setMaxExecutionTimeInSeconds(10);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5, 0.75));
	}
	

}
