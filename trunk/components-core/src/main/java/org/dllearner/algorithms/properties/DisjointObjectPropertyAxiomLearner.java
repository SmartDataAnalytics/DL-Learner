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
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

@ComponentAnn(name="disjoint objectproperty axiom learner", shortName="opldisjoint", version=0.1)
public class DisjointObjectPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
private static final Logger logger = LoggerFactory.getLogger(ObjectPropertyDomainAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	
	private Set<ObjectProperty> allObjectProperties;
	
	private boolean usePropertyPopularity = true;
	
	private int popularity;
	
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
		
		//we return here if the class contains no instances
		popularity = reasoner.getPopularity(propertyToDescribe);
		if(popularity == 0){
			return;
		}
		
		//TODO detect existing axioms
		
		
		//at first get all existing objectproperties in knowledge base
		allObjectProperties = new SPARQLTasks(ks.getEndpoint()).getAllObjectProperties();
		allObjectProperties.remove(propertyToDescribe);
		
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
				currentlyBestAxioms = buildAxioms(result, allObjectProperties);
			}
			
			
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.getName(), limit, offset);
			newModel = executeConstructQuery(query);
		}
		
	}
	
	private void runSPARQL1_1_Mode() {
		//get properties and how often they occur
		int offset = 0;
		String queryTemplate = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p COUNT(?s) AS ?count WHERE {?p a owl:ObjectProperty. ?s ?p ?o." +
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
				currentlyBestAxioms = buildAxioms(result, allObjectProperties);
				offset += limit;
			}
		}
		
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
			if(usePropertyPopularity){
				int overlap = 0;
				int pop;
				if(ks.isRemote()){
					pop = reasoner.getPopularity(p);
				} else {
					Model model = ((LocalModelBasedSparqlEndpointKS)ks).getModel();
					pop = model.listStatements(null, model.getProperty(p.getName()), (RDFNode)null).toSet().size();
				}
				//we skip classes with no instances
				if(pop == 0) continue;
				
				//we compute the estimated precision
				double precision = accuracy(pop, overlap);
				//we compute the estimated recall
				double recall = accuracy(popularity, overlap);
				//compute the overall score
				double score = 1 - fMEasure(precision, recall);
				
				evalAxiom = new EvaluatedAxiom(new DisjointObjectPropertyAxiom(propertyToDescribe, p), new AxiomScore(score));
			} else {
				evalAxiom = new EvaluatedAxiom(new DisjointObjectPropertyAxiom(propertyToDescribe, p), new AxiomScore(1));
			}
			axioms.add(evalAxiom);
		}
		
		//second create disjoint axioms with other properties and score 1 - (#occurence/#all)
		ObjectProperty p;
		for(Entry<ObjectProperty, Integer> entry : sortByValues(property2Count)){
			p = entry.getKey();
			int overlap = entry.getValue();
			int pop;
			if(ks.isRemote()){
				pop = reasoner.getPopularity(p);
			} else {
				Model model = ((LocalModelBasedSparqlEndpointKS)ks).getModel();
				pop = model.listStatements(null, model.getProperty(p.getName()), (RDFNode)null).toSet().size();
			}
			//we skip classes with no instances
			if(pop == 0) continue;
			
			//we compute the estimated precision
			double precision = accuracy(pop, overlap);
			//we compute the estimated recall
			double recall = accuracy(popularity, overlap);
			//compute the overall score
			double score = 1 - fMEasure(precision, recall);
			
			evalAxiom = new EvaluatedAxiom(new DisjointObjectPropertyAxiom(propertyToDescribe, p), new AxiomScore(score));
		}
		
		property2Count.put(propertyToDescribe, all);
		return axioms;
	}
	
	public static void main(String[] args) throws Exception{
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
//		endpoint = new SparqlEndpoint(new URL("http://dbpedia.aksw.org:8902/sparql"), Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList()));
		DisjointObjectPropertyAxiomLearner l = new DisjointObjectPropertyAxiomLearner(new SparqlEndpointKS(endpoint));//.getEndpointDBpediaLiveAKSW()));
		l.setPropertyToDescribe(new ObjectProperty("http://dbpedia.org/ontology/aircraftTransport"));
		l.setMaxExecutionTimeInSeconds(10);
		l.init();
		l.getReasoner().precomputeObjectPropertyPopularity();
		l.start();
		for(EvaluatedAxiom ax : l.getCurrentlyBestEvaluatedAxioms(Integer.MAX_VALUE)){
			System.out.println(ax);
		}
	}
}
