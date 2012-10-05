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

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.ObjectPropertyEditor;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.TransitiveObjectPropertyAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.OWL;

@ComponentAnn(name="transitive objectproperty axiom learner", shortName="opltrans", version=0.1)
public class TransitiveObjectPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(TransitiveObjectPropertyAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	
	private boolean declaredAsTransitive;

	public TransitiveObjectPropertyAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		
		posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s WHERE {?s ?p ?o1. ?o1 ?p ?o2. ?s ?p ?o2}");
		negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s WHERE {?s ?p ?o1. ?o1 ?p ?o2. FILTER NOT EXISTS {?s ?p ?o2 }}");
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
		
		//check if property is already declared as transitive in knowledge base
		String query = String.format("ASK {<%s> a <%s>}", propertyToDescribe, OWL.TransitiveProperty.getURI());
		declaredAsTransitive = executeAskQuery(query);
		if(declaredAsTransitive) {
			existingAxioms.add(new TransitiveObjectPropertyAxiom(propertyToDescribe));
			logger.info("Property is already declared as transitive in knowledge base.");
		}
		
		if(!forceSPARQL_1_0_Mode && ks.supportsSPARQL_1_1()){
			runSPARQL1_1_Mode();
		} else {
			runSPARQL1_0_Mode();
		}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}
	

	private void runSPARQL1_0_Mode(){
		workingModel = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery  = "CONSTRUCT {?s <%s> ?o.} WHERE {?s <%s> ?o} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, propertyToDescribe.getName(), propertyToDescribe.getName(), limit, offset);
		Model newModel = executeConstructQuery(query);
		while(!terminationCriteriaSatisfied() && newModel.size() != 0){
			workingModel.add(newModel);
			// get number of instances of s with <s p o>
			query = "SELECT (COUNT(*) AS ?total) WHERE {?s <%s> ?o. ?o <%s> ?o1.}";
			query = query.replace("%s", propertyToDescribe.getURI().toString());
			ResultSet rs = executeSelectQuery(query, workingModel);
			QuerySolution qs;
			int total = 0;
			while(rs.hasNext()){
				qs = rs.next();
				total = qs.getLiteral("total").getInt();
			}
			query = "SELECT (COUNT(*) AS ?transitive) WHERE {?s <%s> ?o. ?o <%s> ?o1. ?s <%s> ?o1.}";
			query = query.replace("%s", propertyToDescribe.getURI().toString());
			rs = executeSelectQuery(query, workingModel);
			int transitive = 0;
			while(rs.hasNext()){
				qs = rs.next();
				transitive = qs.getLiteral("transitive").getInt();
			}
			
			if(total > 0){
				currentlyBestAxioms.clear();
				currentlyBestAxioms.add(new EvaluatedAxiom(new TransitiveObjectPropertyAxiom(propertyToDescribe),
						computeScore(total, transitive), declaredAsTransitive));
			}
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.getName(), propertyToDescribe.getName(), limit, offset);
			newModel = executeConstructQuery(query);
		}
	}
	
	private void runSPARQL1_1_Mode(){
		String query = "SELECT (COUNT(*) AS ?total) WHERE {?s <%s> ?o. ?o <%s> ?o1. FILTER(?s != ?o && ?o != ?o1)}";
		query = query.replace("%s", propertyToDescribe.getURI().toString());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		int total = 0;
		while(rs.hasNext()){
			qs = rs.next();
			total = qs.getLiteral("total").getInt();
		}
		
		if(total > 0){
			query = "SELECT (COUNT(*) AS ?transitive) WHERE {?s <%s> ?o. ?o <%s> ?o1. ?s <%s> ?o1. FILTER(?s != ?o && ?o != ?o1)}";
			query = query.replace("%s", propertyToDescribe.getURI().toString());
			rs = executeSelectQuery(query);
			int transitive = 0;
			while(rs.hasNext()){
				qs = rs.next();
				transitive = qs.getLiteral("transitive").getInt();
			}
			
			currentlyBestAxioms.add(new EvaluatedAxiom(new TransitiveObjectPropertyAxiom(propertyToDescribe),
					computeScore(total, transitive), declaredAsTransitive));
		}
		
	}
	
	public static void main(String[] args) throws Exception{
		SparqlEndpointKS ks = new SparqlEndpointKS(new SparqlEndpoint(
				new URL("http://dbpedia.aksw.org:8902/sparql"), Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList()));
		TransitiveObjectPropertyAxiomLearner l = new TransitiveObjectPropertyAxiomLearner(ks);
		l.setPropertyToDescribe(new ObjectProperty("http://dbpedia.org/ontology/chairman"));
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(1));
	}


}
