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
import java.util.List;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.IntegerEditor;
import org.dllearner.core.config.ObjectPropertyEditor;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.TransitiveObjectPropertyAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.vocabulary.OWL;

@ComponentAnn(name="transitive objectproperty axiom learner", shortName="opltrans", version=0.1)
public class TransitiveObjectPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(TransitiveObjectPropertyAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	@ConfigOption(name="maxFetchedRows", description="The maximum number of rows fetched from the endpoint to approximate the result.", propertyEditorClass=IntegerEditor.class)
	private int maxFetchedRows = 0;
	
	private List<EvaluatedAxiom> currentlyBestAxioms;
	private long startTime;
	private int fetchedRows;
	

	public TransitiveObjectPropertyAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
	}

	public ObjectProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(ObjectProperty propertyToDescribe) {
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
		
		//check if property is already declared as transitive in knowledge base
		String query = String.format("ASK {<%s> a <%s>}", propertyToDescribe, OWL.TransitiveProperty.getURI());
		boolean declaredAsTransitive = executeAskQuery(query);
		if(declaredAsTransitive) {
			logger.info("Property is already declared as transitive in knowledge base.");
		}
		
		//get fraction of instances s where for a chain <s p o> <o p o1> exists also <s p o1> 
		query = "SELECT (COUNT(?o)) AS ?all ,(COUNT(?o2)) AS ?transitive WHERE {?s <%s> ?o. ?o <%s> ?o1. OPTIONAL{?s <%s> ?o1. ?s <%s> ?o2}}";
		query = query.replace("%s", propertyToDescribe.getURI().toString());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			int all = qs.getLiteral("all").getInt();
			int transitive = qs.getLiteral("transitive").getInt();
			if(all > 0){
				double frac = transitive / (double)all;
				currentlyBestAxioms.add(new EvaluatedAxiom(new TransitiveObjectPropertyAxiom(propertyToDescribe), new AxiomScore(frac)));
			}
			
		}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}

	@Override
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms() {
		return currentlyBestAxioms;
	}
	
	public static void main(String[] args) throws Exception{
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia());
		TransitiveObjectPropertyAxiomLearner l = new TransitiveObjectPropertyAxiomLearner(ks);
		l.setPropertyToDescribe(new ObjectProperty("http://dbpedia.org/ontology/influencedBy"));
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(1));
	}


}
