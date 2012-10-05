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
import java.util.SortedSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.ObjectPropertyEditor;
import org.dllearner.core.owl.InverseObjectPropertyAxiom;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@ComponentAnn(name="inverse objectproperty domain axiom learner", shortName="oplinv", version=0.1)
public class InverseObjectPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(InverseObjectPropertyAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	
	public InverseObjectPropertyAxiomLearner(SparqlEndpointKS ks){
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
		
		if(reasoner.isPrepared()){
			//get existing inverse object property axioms
			SortedSet<ObjectProperty> existingInverseObjectProperties = reasoner.getInverseObjectProperties(propertyToDescribe);
			for(ObjectProperty invProp : existingInverseObjectProperties){
				existingAxioms.add(new InverseObjectPropertyAxiom(invProp, propertyToDescribe));
			}
		}
		
		if(!forceSPARQL_1_0_Mode && ks.supportsSPARQL_1_1()){
			runSingleQueryMode();
		} else {
			runSPARQL1_0_Mode();
		}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}
	
	private void runSingleQueryMode(){
		int total = reasoner.getPopularity(propertyToDescribe);
		
		String query = String.format("PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o. ?o ?p ?s.} GROUP BY ?p", propertyToDescribe.getName());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			currentlyBestAxioms.add(new EvaluatedAxiom(
					new InverseObjectPropertyAxiom(new ObjectProperty(qs.getResource("p").getURI()), propertyToDescribe),
					computeScore(total, qs.getLiteral("cnt").getInt())));
		}
	}
	
	private void runSPARQL1_0_Mode(){
		Model model = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery  = "CONSTRUCT {?s <%s> ?o. ?o ?p ?s} WHERE {?s <%s> ?o. OPTIONAL{?o ?p ?s. ?p a <http://www.w3.org/2002/07/owl#ObjectProperty>}} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, propertyToDescribe.getName(), propertyToDescribe.getName(), limit, offset);
		Model newModel = executeConstructQuery(query);
		while(!terminationCriteriaSatisfied() && newModel.size() != 0){
			model.add(newModel);
			// get number of instances of s with <s p o>
			query = "SELECT (COUNT(*) AS ?total) WHERE {?s <%s> ?o.}";
			query = query.replace("%s", propertyToDescribe.getURI().toString());
			ResultSet rs = executeSelectQuery(query, model);
			QuerySolution qs;
			int total = 0;
			while(rs.hasNext()){
				qs = rs.next();
				total = qs.getLiteral("total").getInt();
			}
			
			query = String.format("SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o. ?o ?p ?s.} GROUP BY ?p", propertyToDescribe.getName());
			rs = executeSelectQuery(query, model);
			while(rs.hasNext()){
				qs = rs.next();
				currentlyBestAxioms.add(new EvaluatedAxiom(
						new InverseObjectPropertyAxiom(new ObjectProperty(qs.getResource("p").getURI()), propertyToDescribe),
						computeScore(total, qs.getLiteral("cnt").getInt())));
			}
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.getName(), propertyToDescribe.getName(), limit, offset);
			newModel = executeConstructQuery(query);
		}
	}
	
	private void runSPARQL1_1_Mode(){
		String query = "SELECT (COUNT(*) AS ?total) WHERE {?s <%s> ?o.}";
		query = query.replace("%s", propertyToDescribe.getURI().toString());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		int total = 0;
		while(rs.hasNext()){
			qs = rs.next();
			total = qs.getLiteral("total").getInt();
		}
		
		query = String.format("PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o. ?o ?p ?s. ?p a <http://www.w3.org/2002/07/owl#ObjectProperty>} GROUP BY ?p", propertyToDescribe.getName());
		rs = executeSelectQuery(query);
		while(rs.hasNext()){
			qs = rs.next();
			currentlyBestAxioms.add(new EvaluatedAxiom(
					new InverseObjectPropertyAxiom(new ObjectProperty(qs.getResource("p").getURI()), propertyToDescribe),
					computeScore(total, qs.getLiteral("cnt").getInt())));
		}
		
	}
	
	public static void main(String[] args) throws Exception{
		SparqlEndpointKS ks = new SparqlEndpointKS(new SparqlEndpoint(new URL("http://[2001:638:902:2010:0:168:35:138]/sparql")));
		
		InverseObjectPropertyAxiomLearner l = new InverseObjectPropertyAxiomLearner(ks);
		l.setPropertyToDescribe(new ObjectProperty("http://dbpedia.org/ontology/animal"));
		l.setMaxExecutionTimeInSeconds(10);
		l.setForceSPARQL_1_0_Mode(true);
//		l.setReturnOnlyNewAxioms(true);
		l.init();
		l.start();
		
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(10, 0.2));
	}
	
}
