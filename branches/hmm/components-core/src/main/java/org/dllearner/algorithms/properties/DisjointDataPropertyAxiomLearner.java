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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.DataPropertyEditor;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DisjointDatatypePropertyAxiom;
import org.dllearner.core.owl.GenericDatatypePropertyAssertion;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KBElement;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

@ComponentAnn(name="disjoint dataproperty axiom learner", shortName="dpldisjoint", version=0.1)
public class DisjointDataPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(ObjectPropertyDomainAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=DataPropertyEditor.class)
	private DatatypeProperty propertyToDescribe;
	
	private Set<DatatypeProperty> allDataProperties;
	
	private boolean usePropertyPopularity = true;
	
	private int popularity;
	
	public DisjointDataPropertyAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s ?o WHERE {?s ?p1 ?o. FILTER NOT EXISTS{?s ?p ?o}}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s ?o WHERE {?s ?p ?o. }");
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
		
		// we return here if the class contains no instances
		popularity = reasoner.getPopularity(propertyToDescribe);
		if (popularity == 0) {
			return;
		}
		
		//TODO detect existing axioms
		
		//at first get all existing dataproperties in knowledgebase
		allDataProperties = new SPARQLTasks(ks.getEndpoint()).getAllDataProperties();
		allDataProperties.remove(propertyToDescribe);
		
		if(!forceSPARQL_1_0_Mode && ks.supportsSPARQL_1_1()){
//			runSPARQL1_1_Mode();
			runSingleQueryMode();
		} else {
			runSPARQL1_0_Mode();
		}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}
	
	private void runSingleQueryMode(){
		//compute the overlap if exist
		Map<ObjectProperty, Integer> property2Overlap = new HashMap<ObjectProperty, Integer>(); 
		String query = String.format("SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o. ?s ?p ?o.} GROUP BY ?p", propertyToDescribe.getName());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			ObjectProperty prop = new ObjectProperty(qs.getResource("p").getURI());
			int cnt = qs.getLiteral("cnt").getInt();
			property2Overlap.put(prop, cnt);
		}
		//for each property in knowledge base
		for(DatatypeProperty p : allDataProperties){
			//get the popularity
			int otherPopularity = reasoner.getPopularity(p);
			if(otherPopularity == 0){//skip empty properties
				continue;
			}
			//get the overlap
			int overlap = property2Overlap.containsKey(p) ? property2Overlap.get(p) : 0;
			//compute the estimated precision
			double precision = accuracy(otherPopularity, overlap);
			//compute the estimated recall
			double recall = accuracy(popularity, overlap);
			//compute the final score
			double score = 1 - fMEasure(precision, recall);
			
			currentlyBestAxioms.add(new EvaluatedAxiom(new DisjointDatatypePropertyAxiom(propertyToDescribe, p), new AxiomScore(score)));
		}
	}
	
	private void runSPARQL1_0_Mode() {
		workingModel = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery  = "CONSTRUCT {?s ?p ?o.} WHERE {?s <%s> ?o. ?s ?p ?o.} LIMIT %d OFFSET %d";
		String countQuery = "SELECT ?p (COUNT(?s) AS ?count) WHERE {?s ?p ?o.} GROUP BY ?p";
		String query = String.format(baseQuery, propertyToDescribe.getName(), limit, offset);
		Model newModel = executeConstructQuery(query);
		Map<DatatypeProperty, Integer> result = new HashMap<DatatypeProperty, Integer>();
		while(!terminationCriteriaSatisfied() && newModel.size() != 0){
			workingModel.add(newModel);
			DatatypeProperty prop;
			Integer oldCnt;
			ResultSet rs = executeSelectQuery(countQuery, workingModel);
			QuerySolution qs;
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
			}
			if(!result.isEmpty()){
				currentlyBestAxioms = buildAxioms(result, allDataProperties);
			}
			
			
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.getName(), limit, offset);
			newModel = executeConstructQuery(query);
		}
		
	}
	
	private void runSPARQL1_1_Mode() {
		//get properties and how often they occur
		int limit = 1000;
		int offset = 0;
		String queryTemplate = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p (COUNT(?s) as ?count) WHERE {?p a owl:DatatypeProperty. ?s ?p ?o." +
		"{SELECT ?s ?o WHERE {?s <%s> ?o.} LIMIT %d OFFSET %d}" +
		"}";
		String query;
		Map<DatatypeProperty, Integer> result = new HashMap<DatatypeProperty, Integer>();
		DatatypeProperty prop;
		Integer oldCnt;
		boolean repeat = true;
		
		ResultSet rs = null;
		while(!terminationCriteriaSatisfied() && repeat){
			query = String.format(queryTemplate, propertyToDescribe, limit, offset);
			rs = executeSelectQuery(query);
			QuerySolution qs;
			repeat = false;
			while(rs.hasNext()){
				qs = rs.next();
				prop = new DatatypeProperty(qs.getResource("p").getURI());
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
				currentlyBestAxioms = buildAxioms(result, allDataProperties);
				offset += 1000;
			}
		}
		
	}

	@Override
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms() {
		return currentlyBestAxioms;
	}

	private List<EvaluatedAxiom> buildAxioms(Map<DatatypeProperty, Integer> property2Count, Set<DatatypeProperty> allProperties){
		List<EvaluatedAxiom> axioms = new ArrayList<EvaluatedAxiom>();
		Integer all = property2Count.get(propertyToDescribe);
		property2Count.remove(propertyToDescribe);
		
		//get complete disjoint properties
		Set<DatatypeProperty> completeDisjointProperties = new TreeSet<DatatypeProperty>(allProperties);
		completeDisjointProperties.removeAll(property2Count.keySet());
		
		EvaluatedAxiom evalAxiom;
		//first create disjoint axioms with properties which not occur and give score of 1
		for(DatatypeProperty p : completeDisjointProperties){
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
				
				evalAxiom = new EvaluatedAxiom(new DisjointDatatypePropertyAxiom(propertyToDescribe, p), new AxiomScore(score));
			} else {
				evalAxiom = new EvaluatedAxiom(new DisjointDatatypePropertyAxiom(propertyToDescribe, p), new AxiomScore(1));
			}
			axioms.add(evalAxiom);
		}
		
		//second create disjoint axioms with other properties and score 1 - (#occurence/#all)
		DatatypeProperty p;
		for(Entry<DatatypeProperty, Integer> entry : sortByValues(property2Count)){
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
			
			evalAxiom = new EvaluatedAxiom(new DisjointDatatypePropertyAxiom(propertyToDescribe, p), new AxiomScore(score));
		}
		
		property2Count.put(propertyToDescribe, all);
		return axioms;
	}
	
	@Override
	public Set<KBElement> getPositiveExamples(EvaluatedAxiom evAxiom) {
		DisjointDatatypePropertyAxiom axiom = (DisjointDatatypePropertyAxiom) evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("p", axiom.getDisjointRole().getName());
		if(workingModel != null){
			Set<KBElement> posExamples = new HashSet<KBElement>();
			
			ResultSet rs = executeSelectQuery(posExamplesQueryTemplate.toString(), workingModel);
			Individual subject;
			Literal object;
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				subject = new Individual(qs.getResource("s").getURI());
				object = qs.getLiteral("o");
				posExamples.add(new GenericDatatypePropertyAssertion(
						propertyToDescribe, subject, object.getLexicalForm(), new Datatype(object.getDatatypeURI())));
			}
			
			return posExamples;
		} else {
			throw new UnsupportedOperationException("Getting positive examples is not possible.");
		}
	}
	
	@Override
	public Set<KBElement> getNegativeExamples(EvaluatedAxiom evAxiom) {
		DisjointDatatypePropertyAxiom axiom = (DisjointDatatypePropertyAxiom) evAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("p", axiom.getDisjointRole().getName());
		if(workingModel != null){
			Set<KBElement> negExamples = new TreeSet<KBElement>();
			
			ResultSet rs = executeSelectQuery(negExamplesQueryTemplate.toString(), workingModel);
			Individual subject;
			Literal object;
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				subject = new Individual(qs.getResource("s").getURI());
				object = qs.getLiteral("o");
				negExamples.add(new GenericDatatypePropertyAssertion(
						propertyToDescribe, subject, object.getLexicalForm(), new Datatype(object.getDatatypeURI())));
			}
			
			return negExamples;
		} else {
			throw new UnsupportedOperationException("Getting positive examples is not possible.");
		}
	}
	
	public static void main(String[] args) throws Exception{
		DisjointDataPropertyAxiomLearner l = new DisjointDataPropertyAxiomLearner(new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW()));
		l.setPropertyToDescribe(new DatatypeProperty("http://dbpedia.org/ontology/accessDate"));
		l.setMaxExecutionTimeInSeconds(10);
		l.init();
		l.getReasoner().precomputeDataPropertyPopularity();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5));
	}

}
