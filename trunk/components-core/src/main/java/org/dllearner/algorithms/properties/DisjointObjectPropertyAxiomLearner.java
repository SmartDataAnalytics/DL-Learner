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
import java.util.TreeSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.ObjectPropertyEditor;
import org.dllearner.core.owl.DisjointObjectPropertyAxiom;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.learningproblems.AxiomScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

@ComponentAnn(name="disjoint objectproperty axiom learner", shortName="opldisjoint", version=0.1)
public class DisjointObjectPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
private static final Logger logger = LoggerFactory.getLogger(ObjectPropertyDomainAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	
	public DisjointObjectPropertyAxiomLearner(SparqlEndpointKS ks){
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
		
		//TODO
		
		//at first get all existing objectproperties in knowledgebase
		Set<ObjectProperty> objectProperties = new SPARQLTasks(ks.getEndpoint()).getAllObjectProperties();
		objectProperties.remove(propertyToDescribe);
		
		//get properties and how often they occur
				int limit = 1000;
				int offset = 0;
				String queryTemplate = "SELECT ?p COUNT(?s) AS ?count WHERE {?s ?p ?o." +
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
						}
						result.put(prop, oldCnt);
						qs.getLiteral("count").getInt();
						repeat = true;
					}
					if(!result.isEmpty()){
						currentlyBestAxioms = buildAxioms(result, objectProperties);
						offset += 1000;
					}
				}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}

	private List<EvaluatedAxiom> buildAxioms(Map<ObjectProperty, Integer> property2Count, Set<ObjectProperty> allProperties){
		List<EvaluatedAxiom> axioms = new ArrayList<EvaluatedAxiom>();
		Integer all = property2Count.get(propertyToDescribe);
		property2Count.remove(propertyToDescribe);
		
		//get complete disjoint properties
		Set<ObjectProperty> completeDisjointProperties = new TreeSet<ObjectProperty>(allProperties);
		completeDisjointProperties.removeAll(property2Count.keySet());
		
		EvaluatedAxiom evalAxiom;
		//first create disjoint axioms with properties which not occur and give score of 1
		for(ObjectProperty p : completeDisjointProperties){
			evalAxiom = new EvaluatedAxiom(new DisjointObjectPropertyAxiom(propertyToDescribe, p),
					new AxiomScore(1));
			axioms.add(evalAxiom);
		}
		
		//second create disjoint axioms with other properties and score 1 - (#occurence/#all)
		for(Entry<ObjectProperty, Integer> entry : sortByValues(property2Count)){
			evalAxiom = new EvaluatedAxiom(new DisjointObjectPropertyAxiom(propertyToDescribe, entry.getKey()),
					new AxiomScore(1 - (entry.getValue() / (double)all)));
			axioms.add(evalAxiom);
		}
		
		property2Count.put(propertyToDescribe, all);
		return axioms;
	}
	
	
}
