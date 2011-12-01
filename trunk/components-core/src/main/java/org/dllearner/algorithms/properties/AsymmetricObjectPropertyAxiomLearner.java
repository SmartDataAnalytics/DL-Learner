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

import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.ObjectPropertyEditor;
import org.dllearner.core.owl.AsymmetricObjectPropertyAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.SymmetricObjectPropertyAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.vocabulary.OWL2;

@ComponentAnn(name="asymmetric objectproperty axiom learner", shortName="oplasymm", version=0.1)
public class AsymmetricObjectPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(AsymmetricObjectPropertyAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;

	public AsymmetricObjectPropertyAxiomLearner(SparqlEndpointKS ks){
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
		
		//check if property is already declared as asymmetric in knowledge base
		String query = String.format("ASK {<%s> a <%s>}", propertyToDescribe, OWL2.AsymmetricProperty.getURI());
		boolean declaredAsymmetric = executeAskQuery(query);
		if(declaredAsymmetric) {
			existingAxioms.add(new AsymmetricObjectPropertyAxiom(propertyToDescribe));
			logger.info("Property is already declared as symmetric in knowledge base.");
		}
		
		if(ks.supportsSPARQL_1_1()){
			runSPARQL1_1_Mode();
		} else {
			runSPARQL1_0_Mode();
		}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}
	
	private void runSPARQL1_0_Mode(){
		BiHashMultimap<Individual, Individual> individualsMap = new BiHashMultimap<Individual, Individual>();
		boolean repeat = true;
		int limit = 1000;
		while(!terminationCriteriaSatisfied() && repeat){
			String query = String.format("SELECT DISTINCT ?s ?o WHERE {?s <%s> ?o.} LIMIT %d OFFSET %d", propertyToDescribe.getURI().toString(), limit, fetchedRows);
			ResultSet rs = executeSelectQuery(query);
			QuerySolution qs;
			Individual s;
			Individual o;
			int cnt = 0;
			while(rs.hasNext()){
				qs = rs.next();
				s = new Individual(qs.getResource("s").getURI());
				o = new Individual(qs.getResource("o").getURI());
				individualsMap.put(s, o);
				cnt++;
			}
			int total = individualsMap.size();
			int asymmetric = 0;
			
			for(java.util.Map.Entry<Individual, Individual> e : individualsMap.entries()){
				if(!individualsMap.getInverse().containsEntry(e.getKey(), e.getValue())){
					asymmetric++;
				}
			}
			
			currentlyBestAxioms = Collections.singletonList(new EvaluatedAxiom(new AsymmetricObjectPropertyAxiom(propertyToDescribe),
			computeScore(total, asymmetric)));
			fetchedRows += limit;
			repeat = (cnt == limit);
		}
	}
	
	private void runSPARQL1_1_Mode(){
		String query = "SELECT (COUNT(?s) AS ?total) WHERE {?s <%s> ?o.}";
		query = query.replace("%s", propertyToDescribe.getURI().toString());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		int total = 0;
		while(rs.hasNext()){
			qs = rs.next();
			total = qs.getLiteral("total").getInt();
		}
		query = "SELECT (COUNT(?s) AS ?symmetric) WHERE {?s <%s> ?o. ?o <%s> ?s.}";
		query = query.replace("%s", propertyToDescribe.getURI().toString());
		rs = executeSelectQuery(query);
		int symmetric = 0;
		while(rs.hasNext()){
			qs = rs.next();
			symmetric = qs.getLiteral("symmetric").getInt();
		}
		int asymmetric = total - symmetric;
		
		if(total > 0){
			currentlyBestAxioms.add(new EvaluatedAxiom(new AsymmetricObjectPropertyAxiom(propertyToDescribe),
					computeScore(total, asymmetric)));
		}
		
	}
	
	public static void main(String[] args) throws Exception{
		AsymmetricObjectPropertyAxiomLearner l = new AsymmetricObjectPropertyAxiomLearner(new SparqlEndpointKS(new SparqlEndpoint(new URL("http://factforge.net/sparql"))));//.getEndpointDBpediaLiveAKSW()));
		l.setPropertyToDescribe(new ObjectProperty("http://dbpedia.org/ontology/country"));
		l.setMaxExecutionTimeInSeconds(10);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5));
	}

}
