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
import org.dllearner.core.owl.SymmetricObjectPropertyAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.vocabulary.OWL2;

@ComponentAnn(name="symmetric objectproperty axiom learner", shortName="oplsymm", version=0.1)
public class SymmetricObjectPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(SymmetricObjectPropertyAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	@ConfigOption(name="maxFetchedRows", description="The maximum number of rows fetched from the endpoint to approximate the result.", propertyEditorClass=IntegerEditor.class)
	private int maxFetchedRows = 0;
	
	private List<EvaluatedAxiom> currentlyBestAxioms;
	private long startTime;
	private int fetchedRows;
	

	public SymmetricObjectPropertyAxiomLearner(SparqlEndpointKS ks){
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
		
		//check if property is already declared as symmetric in knowledge base
		String query = String.format("ASK {<%s> a <%s>}", propertyToDescribe, OWL2.SymmetricProperty.getURI());
		boolean declaredAsSymmetric = executeAskQuery(query);
		if(declaredAsSymmetric) {
			logger.info("Property is already declared as symmetric in knowledge base.");
		}
		
		//get fraction of instances s with <s p o> also exists <o p s> 
		query = "SELECT (COUNT(?s)) AS ?all ,(COUNT(?o1)) AS ?symmetric WHERE {?s <%s> ?o. OPTIONAL{?o <%s> ?s. ?o <%s> ?o1}}";
		query = query.replace("%s", propertyToDescribe.getURI().toString());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			int all = qs.getLiteral("all").getInt();
			int symmetric = qs.getLiteral("symmetric").getInt();
			if(all > 0){
				double frac = symmetric / (double)all;
				currentlyBestAxioms.add(new EvaluatedAxiom(new SymmetricObjectPropertyAxiom(propertyToDescribe),
						computeScore(all, symmetric)));
			}
			
		}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}

	@Override
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms() {
		return currentlyBestAxioms;
	}

}
