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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.DataPropertyEditor;
import org.dllearner.core.config.IntegerEditor;
import org.dllearner.core.configurators.Configurator;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DisjointDatatypePropertyAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtendedQueryEngineHTTP;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

@ComponentAnn(name="disjoint dataproperty axiom learner", shortName="dpldisjoint", version=0.1)
public class DisjointDataPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(ObjectPropertyDomainAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=DataPropertyEditor.class)
	private DatatypeProperty propertyToDescribe;
	@ConfigOption(name="maxExecutionTimeInSeconds", description="", propertyEditorClass=IntegerEditor.class)
	private int maxExecutionTimeInSeconds = 10;
	@ConfigOption(name="maxFetchedRows", description="The maximum number of rows fetched from the endpoint to approximate the result.", propertyEditorClass=IntegerEditor.class)
	private int maxFetchedRows = 0;
	
	private SPARQLReasoner reasoner;
	private SparqlEndpointKS ks;
	
	private List<EvaluatedAxiom> currentlyBestAxioms;
	private long startTime;
	private int fetchedRows;
	
	public DisjointDataPropertyAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
	}
	
	public int getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}

	public DatatypeProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(DatatypeProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
	}
	
	public int getMaxFetchedRows() {
		return maxFetchedRows;
	}

	public void setMaxFetchedRows(int maxFetchedRows) {
		this.maxFetchedRows = maxFetchedRows;
	}

	@Override
	public void start() {
		logger.info("Start learning...");
		startTime = System.currentTimeMillis();
		fetchedRows = 0;
		currentlyBestAxioms = new ArrayList<EvaluatedAxiom>();
		
		//TODO
		
		//at first get all existing dataproperties in knowledgebase
		Set<DatatypeProperty> dataProperties = getAllDataProperties();
		
		//get properties and how often they occur
				int limit = 1000;
				int offset = 0;
				String queryTemplate = "SELECT ?p COUNT(?s) AS ?count WHERE {?s ?p ?o." +
				"{SELECT ?s ?o WHERE {?s <%s> ?o.} LIMIT %d OFFSET %d}" +
				"}";
				String query;
				Map<DatatypeProperty, Integer> result = new HashMap<DatatypeProperty, Integer>();
				DatatypeProperty prop;
				Integer oldCnt;
				boolean repeat = true;
				
				while(!terminationCriteriaSatisfied() && repeat){
					query = String.format(queryTemplate, propertyToDescribe, limit, offset);
					ResultSet rs = executeQuery(query);
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
						currentlyBestAxioms = buildAxioms(result, dataProperties);
						offset += 1000;
					}
				}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}

	@Override
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms() {
		return currentlyBestAxioms;
	}

	@Override
	public Configurator getConfigurator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() throws ComponentInitException {
		reasoner = new SPARQLReasoner(ks);
		
	}
	
	private List<EvaluatedAxiom> buildAxioms(Map<DatatypeProperty, Integer> property2Count, Set<DatatypeProperty> allProperties){
		List<EvaluatedAxiom> axioms = new ArrayList<EvaluatedAxiom>();
		Integer all = property2Count.get(propertyToDescribe);
		property2Count.remove(propertyToDescribe);
		
		EvaluatedAxiom evalAxiom;
		//first create disjoint axioms with properties which not occur and give score of 1
		for(DatatypeProperty p : allProperties){
			evalAxiom = new EvaluatedAxiom(new DisjointDatatypePropertyAxiom(propertyToDescribe, p),
					new AxiomScore(1));
			axioms.add(evalAxiom);
		}
		
		//second create disjoint axioms with other properties and score 1 - (#occurence/#all)
		for(Entry<DatatypeProperty, Integer> entry : sortByValues(property2Count)){
			evalAxiom = new EvaluatedAxiom(new DisjointDatatypePropertyAxiom(propertyToDescribe, entry.getKey()),
					new AxiomScore(1 - (entry.getValue() / (double)all)));
			axioms.add(evalAxiom);
		}
		
		property2Count.put(propertyToDescribe, all);
		return axioms;
	}
	
	/*
	 * Returns the entries of the map sorted by value.
	 */
	private SortedSet<Entry<DatatypeProperty, Integer>> sortByValues(Map<DatatypeProperty, Integer> map){
		SortedSet<Entry<DatatypeProperty, Integer>> sortedSet = new TreeSet<Map.Entry<DatatypeProperty,Integer>>(new Comparator<Entry<DatatypeProperty, Integer>>() {

			@Override
			public int compare(Entry<DatatypeProperty, Integer> value1, Entry<DatatypeProperty, Integer> value2) {
				if(value1.getValue() > value2.getValue()){
					return 1;
				} else if(value2.getValue() > value1.getValue()){
					return -1;
				} else {
					return value1.getKey().compareTo(value2.getKey());
				}
			}
		});
		sortedSet.addAll(map.entrySet());
		return sortedSet;
	}
	
	private boolean terminationCriteriaSatisfied(){
		boolean timeLimitExceeded = maxExecutionTimeInSeconds == 0 ? false : (System.currentTimeMillis() - startTime) >= maxExecutionTimeInSeconds * 1000;
		boolean resultLimitExceeded = maxFetchedRows == 0 ? false : fetchedRows >= maxFetchedRows;
		return  timeLimitExceeded || resultLimitExceeded; 
	}
	
	private Set<DatatypeProperty> getAllDataProperties() {
		Set<DatatypeProperty> properties = new TreeSet<DatatypeProperty>();
		String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p WHERE {?p a owl:DatatypeProperty}";
		
		ResultSet q = executeQuery(query);
		while (q.hasNext()) {
			QuerySolution qs = q.next();
			properties.add(new DatatypeProperty(qs.getResource("p").getURI()));
		}
		//remove property to describe
		properties.remove(propertyToDescribe);
		
		return properties;
	}
	
	
	/*
	 * Executes a SELECT query and returns the result.
	 */
	private ResultSet executeQuery(String query){
		logger.info("Sending query \n {}", query);
		
		ExtendedQueryEngineHTTP queryExecution = new ExtendedQueryEngineHTTP(ks.getEndpoint().getURL().toString(), query);
		queryExecution.setTimeout(maxExecutionTimeInSeconds * 1000);
		for (String dgu : ks.getEndpoint().getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : ks.getEndpoint().getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}			
		ResultSet resultSet = queryExecution.execSelect();
		return resultSet;
	}
	
	public static void main(String[] args) throws Exception{
		DisjointDataPropertyAxiomLearner l = new DisjointDataPropertyAxiomLearner(new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia()));
		l.setPropertyToDescribe(new DatatypeProperty("http://dbpedia.org/ontology/maximumBoatLength"));
		l.setMaxExecutionTimeInSeconds(0);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5));
	}

}
