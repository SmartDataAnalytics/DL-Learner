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
import org.dllearner.core.owl.IrreflexiveObjectPropertyAxiom;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.learningproblems.AxiomScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.vocabulary.OWL2;

@ComponentAnn(name="irreflexive objectproperty axiom learner", shortName="oplirrefl", version=0.1)
public class IrreflexiveObjectPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(IrreflexiveObjectPropertyAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	@ConfigOption(name="maxFetchedRows", description="The maximum number of rows fetched from the endpoint to approximate the result.", propertyEditorClass=IntegerEditor.class)
	private int maxFetchedRows = 0;
	
	private List<EvaluatedAxiom> currentlyBestAxioms;
	private long startTime;
	private int fetchedRows;
	

	public IrreflexiveObjectPropertyAxiomLearner(SparqlEndpointKS ks){
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
		
		//check if property is already declared as reflexive in knowledge base
		String query = String.format("ASK {<%s> a <%s>}", propertyToDescribe, OWL2.IrreflexiveProperty.getURI());
		boolean declaredAsReflexive = executeAskQuery(query);
		if(declaredAsReflexive) {
			logger.info("Property is already declared as irreflexive in knowledge base.");
		}

		//get all instance s with <s p o>
		query = String.format("SELECT (COUNT(DISTINCT ?s) AS ?all) WHERE {?s <%s> ?o.}", propertyToDescribe);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		int all = 0;
		while(rs.hasNext()){
			qs = rs.next();
			all = qs.getLiteral("all").getInt();
			
		}
		
		//get number of instances s where not exists  <s p s> 
		query = "SELECT (COUNT(DISTINCT ?s) AS ?irreflexive) WHERE {?s <%s> ?o. OPTIONAL{?s <%s> ?o1.FILTER(?s = ?o1)} FILTER(!BOUND(?o1))}";
		query = query.replace("%s", propertyToDescribe.getURI().toString());
		rs = executeSelectQuery(query);
		int irreflexive = 0;
		while(rs.hasNext()){
			qs = rs.next();
			irreflexive = qs.getLiteral("irreflexive").getInt();
		}
		
		if(all > 0){
			double frac = irreflexive / (double)all;
			currentlyBestAxioms.add(new EvaluatedAxiom(new IrreflexiveObjectPropertyAxiom(propertyToDescribe), new AxiomScore(frac)));
		}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}

	@Override
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms() {
		return currentlyBestAxioms;
	}
	
}
