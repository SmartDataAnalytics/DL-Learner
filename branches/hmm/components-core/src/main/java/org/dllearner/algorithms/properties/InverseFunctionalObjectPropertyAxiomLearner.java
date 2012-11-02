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

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.ObjectPropertyEditor;
import org.dllearner.core.owl.InverseFunctionalObjectPropertyAxiom;
import org.dllearner.core.owl.ObjectProperty;
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

@ComponentAnn(name="inversefunctional objectproperty axiom learner", shortName="oplinvfunc", version=0.1)
public class InverseFunctionalObjectPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(InverseFunctionalObjectPropertyAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	
	private boolean declaredAsInverseFunctional;

	public InverseFunctionalObjectPropertyAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		
		posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?o1 ?p ?s. FILTER NOT EXISTS {?o2 ?p ?s. FILTER(?o1 != ?o2)} }");
		negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?o1 ?p ?s. ?o2 ?p ?s. FILTER(?o1 != ?o2)}");
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
		
		//check if property is already declared as symmetric in knowledge base
		String query = String.format("ASK {<%s> a <%s>}", propertyToDescribe, OWL.InverseFunctionalProperty.getURI());
		declaredAsInverseFunctional = executeAskQuery(query);
		if(declaredAsInverseFunctional) {
			existingAxioms.add(new InverseFunctionalObjectPropertyAxiom(propertyToDescribe));
			logger.info("Property is already declared as functional in knowledge base.");
		}
		
		if(!forceSPARQL_1_0_Mode && ks.supportsSPARQL_1_1()){
			runSPARQL1_1_Mode();
		} else {
			runSPARQL1_0_Mode();
		}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}
	
	private void runSPARQL1_0_Mode() {
		workingModel = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery  = "CONSTRUCT {?s <%s> ?o.} WHERE {?s <%s> ?o} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, propertyToDescribe.getName(), propertyToDescribe.getName(), limit, offset);
		Model newModel = executeConstructQuery(query);
		while(!terminationCriteriaSatisfied() && newModel.size() != 0){
			workingModel.add(newModel);
			// get number of instances of s with <s p o>
			query = String.format(
					"SELECT (COUNT(DISTINCT ?o) AS ?all) WHERE {?s <%s> ?o.}",
					propertyToDescribe.getName());
			ResultSet rs = executeSelectQuery(query, workingModel);
			QuerySolution qs;
			int all = 1;
			while (rs.hasNext()) {
				qs = rs.next();
				all = qs.getLiteral("all").getInt();
			}
			// get number of instances of s with <s p o> <s p o1> where o != o1
			query = "SELECT (COUNT(DISTINCT ?o) AS ?inversefunctional) WHERE {?s1 <%s> ?o. FILTER NOT EXISTS {?s2 <%s> ?o. FILTER(?s1 != ?s2)}}";
			query = query.replace("%s", propertyToDescribe.getURI().toString());
			rs = executeSelectQuery(query, workingModel);
			int inverseFunctional = 1;
			while (rs.hasNext()) {
				qs = rs.next();
				inverseFunctional = qs.getLiteral("inversefunctional")
						.getInt();
			}
			if (all > 0) {
				currentlyBestAxioms.clear();
				currentlyBestAxioms
						.add(new EvaluatedAxiom(
								new InverseFunctionalObjectPropertyAxiom(
										propertyToDescribe), computeScore(all, inverseFunctional),
								declaredAsInverseFunctional));
			}
			
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.getName(), propertyToDescribe.getName(), limit, offset);
			newModel = executeConstructQuery(query);
		}
	}
	
	private void runSPARQL1_1_Mode() {
		// get number of instances of s with <s p o>
		int numberOfObjects = reasoner.getObjectCountForProperty(propertyToDescribe, getRemainingRuntimeInMilliSeconds());
		if(numberOfObjects == -1){
			logger.warn("Early termination: Got timeout while counting number of distinct objects for given property.");
			return;
		}
		
		if (numberOfObjects > 0) {
			// get number of instances of s with <s p o> <s p o1> where o != o1
			String query = "SELECT (COUNT(DISTINCT ?o) AS ?inversefunctional) WHERE {?s1 <%s> ?o. FILTER NOT EXISTS {?s2 <%s> ?o. FILTER(?s1 != ?s2)}}";
			query = query.replace("%s", propertyToDescribe.getURI().toString());
			ResultSet rs = executeSelectQuery(query);
			int inverseFunctional = 1;
			QuerySolution qs;
			while (rs.hasNext()) {
				qs = rs.next();
				inverseFunctional = qs.getLiteral("inversefunctional")
						.getInt();
			}
			
			currentlyBestAxioms
					.add(new EvaluatedAxiom(
							new InverseFunctionalObjectPropertyAxiom(
									propertyToDescribe), computeScore(numberOfObjects, inverseFunctional),
							declaredAsInverseFunctional));
		}
	}
	
	public static void main(String[] args) throws Exception{
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		InverseFunctionalObjectPropertyAxiomLearner l = new InverseFunctionalObjectPropertyAxiomLearner(ks);
		l.setPropertyToDescribe(new ObjectProperty("http://dbpedia.org/ontology/profession"));
		l.setMaxExecutionTimeInSeconds(10);
		l.init();
		l.setForceSPARQL_1_0_Mode(true);
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(1));
	}
	
}
