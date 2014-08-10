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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.SimpleLayout;
import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.ObjectPropertyEditor;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyDomainAxiom;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

@ComponentAnn(name="objectproperty domain axiom learner", shortName="opldomain", version=0.1)
public class ObjectPropertyDomainAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(ObjectPropertyDomainAxiomLearner.class);
	
	private Map<Individual, SortedSet<OWLClassExpression>> individual2Types;
	
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private OWLObjectProperty propertyToDescribe;
	
	public ObjectPropertyDomainAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		super.iterativeQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?ind ?type WHERE {?ind ?p ?o. ?ind a ?type.}");
	}
	
	public OWLObjectProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(ObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
	}
	
	@Override
	public void start() {
		iterativeQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		logger.info("Start learning...");
		startTime = System.currentTimeMillis();
		fetchedRows = 0;
		currentlyBestAxioms = new ArrayList<EvaluatedAxiom>();
		
		if(returnOnlyNewAxioms){
			//get existing domains
			Description existingDomain = reasoner.getDomain(propertyToDescribe);
			if(existingDomain != null){
				existingAxioms.add(new ObjectPropertyDomainAxiom(propertyToDescribe, existingDomain));
				if(reasoner.isPrepared()){
					if(reasoner.getClassHierarchy().contains(existingDomain)){
						for(OWLClassExpression sup : reasoner.getClassHierarchy().getSuperClasses(existingDomain)){
							existingAxioms.add(new ObjectPropertyDomainAxiom(propertyToDescribe, existingDomain));
							logger.info("Existing domain(inferred): " + sup);
						}
					}
					
				}
			}
		}
		
		if(!forceSPARQL_1_0_Mode && ks.supportsSPARQL_1_1()){
			runSingleQueryMode();
		} else {
			runIterativeQueryMode();
		}
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}
	
	private void runSingleQueryMode(){
		String query = String.format("SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s <%s> ?o.}", propertyToDescribe.toStringID());
		ResultSet rs = executeSelectQuery(query);
		int nrOfSubjects = rs.next().getLiteral("cnt").getInt();
		
		query = String.format("SELECT ?type (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s <%s> ?o. ?s a ?type.} GROUP BY ?type", propertyToDescribe.toStringID());
		rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			NamedClass domain = df.getOWLClass(IRI.create(qs.getResource("type").getURI());
			int cnt = qs.getLiteral("cnt").getInt();
			if(!domain.getURI().equals(Thing.uri)){
				currentlyBestAxioms.add(new EvaluatedAxiom(new ObjectPropertyDomainAxiom(propertyToDescribe, domain), computeScore(nrOfSubjects, cnt)));
				
			}
		}
	}
	
	private void runIterativeQueryMode(){
		individual2Types = new HashMap<Individual, SortedSet<OWLClassExpression>>();
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
		SortedSet<OWLClassExpression> types;
		int cnt = 0;
		while(rs.hasNext()){
			cnt++;
			qs = rs.next();
			if(qs.get("type").isURIResource()){
				types = new TreeSet<OWLClassExpression>();
				ind = df.getOWLNamedIndividual(IRI.create(qs.getResource("ind").getURI());
				type = df.getOWLClass(IRI.create(qs.getResource("type").getURI());
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
		Map<OWLClassExpression, Integer> result = new HashMap<OWLClassExpression, Integer>();
		for(Entry<Individual, SortedSet<OWLClassExpression>> entry : individual2Types.entrySet()){
			for(OWLClassExpression nc : entry.getValue()){
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
		result.remove(df.getOWLClass(IRI.create(Thing.instance.getURI()));
		
		EvaluatedAxiom evalAxiom;
		int total = individual2Types.keySet().size();
		for(Entry<OWLClassExpression, Integer> entry : sortByValues(result)){
			evalAxiom = new EvaluatedAxiom(new ObjectPropertyDomainAxiom(propertyToDescribe, entry.getKey()),
					computeScore(total, entry.getValue()));
			if(existingAxioms.contains(evalAxiom.getAxiom())){
				evalAxiom.setAsserted(true);
			}
			axioms.add(evalAxiom);
		}
		
		currentlyBestAxioms = axioms;
	}
	
	public static void main(String[] args) throws Exception{
		org.apache.log4j.Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout()));
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
		org.apache.log4j.Logger.getLogger(DataPropertyDomainAxiomLearner.class).setLevel(Level.INFO);		
		
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia());
		
		
		SPARQLReasoner reasoner = new SPARQLReasoner(ks);
		reasoner.prepareSubsumptionHierarchy();
		
		
		ObjectPropertyDomainAxiomLearner l = new ObjectPropertyDomainAxiomLearner(ks);
		l.setReasoner(reasoner);
		l.setPropertyToDescribe(df.getOWLObjectProperty(IRI.create("http://dbpedia.org/ontology/birthPlace"));
		l.setMaxExecutionTimeInSeconds(10);
//		l.addFilterNamespace("http://dbpedia.org/ontology/");
//		l.setReturnOnlyNewAxioms(true);
		l.setForceSPARQL_1_0_Mode(true);
		l.init();
		l.start();
		
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(0.7));
	}
	
}
