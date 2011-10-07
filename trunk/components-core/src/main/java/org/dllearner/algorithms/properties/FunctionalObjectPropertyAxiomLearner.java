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
import org.dllearner.core.owl.FunctionalObjectPropertyAxiom;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.vocabulary.OWL;

@ComponentAnn(name="functional objectproperty axiom learner", shortName="oplfunc", version=0.1)
public class FunctionalObjectPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(FunctionalObjectPropertyAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;

	public FunctionalObjectPropertyAxiomLearner(SparqlEndpointKS ks){
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
		
		//check if property is already declared as symmetric in knowledge base
		String query = String.format("ASK {<%s> a <%s>}", propertyToDescribe, OWL.FunctionalProperty.getURI());
		boolean declaredAsFunctional = executeAskQuery(query);
		if(declaredAsFunctional) {
			existingAxioms.add(new FunctionalObjectPropertyAxiom(propertyToDescribe));
			logger.info("Property is already declared as functional in knowledge base.");
		}
		
		//get number of instances of s with <s p o> 
		query = String.format("SELECT (COUNT(DISTINCT ?s) AS ?all) WHERE {?s <%s> ?o.}", propertyToDescribe.getName());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		int all = 1;
		while(rs.hasNext()){
			qs = rs.next();
			all = qs.getLiteral("all").getInt();
		}
		//get number of instances of s with <s p o> <s p o1> where o != o1
		query = "SELECT (COUNT(DISTINCT ?s) AS ?notfunctional) WHERE {?s <%s> ?o. ?s <%s> ?o1. FILTER(?o != ?o1) }";
		query = query.replace("%s", propertyToDescribe.getURI().toString());
		rs = executeSelectQuery(query);
		int notFunctional = 1;
		while(rs.hasNext()){
			qs = rs.next();
			notFunctional = qs.getLiteral("notfunctional").getInt();
		}
		if(all > 0){
			currentlyBestAxioms.add(new EvaluatedAxiom(new FunctionalObjectPropertyAxiom(propertyToDescribe),
					computeScore(all, all - notFunctional), declaredAsFunctional));
		}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}
	
	public static void main(String[] args) throws Exception{
		FunctionalObjectPropertyAxiomLearner l = new FunctionalObjectPropertyAxiomLearner(new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW()));
		l.setPropertyToDescribe(new ObjectProperty("http://dbpedia.org/ontology/league"));
		l.setMaxExecutionTimeInSeconds(10);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5));
	}
	
}
