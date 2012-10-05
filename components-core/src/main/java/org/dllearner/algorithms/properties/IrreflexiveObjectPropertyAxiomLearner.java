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

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.ObjectPropertyEditor;
import org.dllearner.core.owl.IrreflexiveObjectPropertyAxiom;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.OWL2;

@ComponentAnn(name="irreflexive objectproperty axiom learner", shortName="oplirrefl", version=0.1)
public class IrreflexiveObjectPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(IrreflexiveObjectPropertyAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	
	private boolean declaredAsIrreflexive;

	public IrreflexiveObjectPropertyAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		
		posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s WHERE {?s ?p ?o. FILTER NOT EXISTS {?s ?p ?s} }");
		negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s WHERE {?s ?p ?s. }");
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
		
		//check if property is already declared as irreflexive in knowledge base
		String query = String.format("ASK {<%s> a <%s>}", propertyToDescribe, OWL2.IrreflexiveProperty.getURI());
		declaredAsIrreflexive = executeAskQuery(query);
		if(declaredAsIrreflexive) {
			existingAxioms.add(new IrreflexiveObjectPropertyAxiom(propertyToDescribe));
			logger.info("Property is already declared as irreflexive in knowledge base.");
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
			// get all instance s with <s p o>
			query = String.format(
					"SELECT (COUNT(DISTINCT ?s) AS ?all) WHERE {?s <%s> ?o.}",
					propertyToDescribe);
			ResultSet rs = executeSelectQuery(query, workingModel);
			QuerySolution qs;
			int all = 0;
			while (rs.hasNext()) {
				qs = rs.next();
				all = qs.getLiteral("all").getInt();

			}

			// get number of instances s where not exists <s p s>
			query = "SELECT (COUNT(DISTINCT ?s) AS ?irreflexive) WHERE {?s <%s> ?o. FILTER(?s != ?o)}";
			query = query.replace("%s", propertyToDescribe.getURI().toString());
			rs = executeSelectQuery(query, workingModel);
			int irreflexive = 0;
			while (rs.hasNext()) {
				qs = rs.next();
				irreflexive = qs.getLiteral("irreflexive").getInt();
			}

			if (all > 0) {
				currentlyBestAxioms.clear();
				currentlyBestAxioms.add(new EvaluatedAxiom(
						new IrreflexiveObjectPropertyAxiom(propertyToDescribe),
						computeScore(all, irreflexive), declaredAsIrreflexive));
			}
			
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.getName(), propertyToDescribe.getName(), limit, offset);
			newModel = executeConstructQuery(query);
		}
	}
	
	private void runSPARQL1_1_Mode() {
		int total = reasoner.getPopularity(propertyToDescribe);

		if (total > 0) {
			int irreflexive = 0;
			String query = String.format("SELECT (COUNT(DISTINCT ?s) AS ?irreflexive) WHERE {?s <%s> ?o. FILTER NOT EXISTS{?s <%s> ?s}}", propertyToDescribe.getName(), propertyToDescribe.getName());
			ResultSet rs = executeSelectQuery(query);
			if (rs.hasNext()) {
				irreflexive = rs.next().getLiteral("irreflexive").getInt();
			}
			
			currentlyBestAxioms.add(new EvaluatedAxiom(
					new IrreflexiveObjectPropertyAxiom(propertyToDescribe),
					computeScore(total, irreflexive), declaredAsIrreflexive));
		}
	}
	
	public static void main(String[] args) throws Exception {
		SparqlEndpointKS ks = new SparqlEndpointKS(new SparqlEndpoint(new URL("http://[2001:638:902:2010:0:168:35:138]/sparql")));
		
		SPARQLReasoner reasoner = new SPARQLReasoner(ks);
		reasoner.prepareSubsumptionHierarchy();
		
		IrreflexiveObjectPropertyAxiomLearner l = new IrreflexiveObjectPropertyAxiomLearner(ks);
		l.setReasoner(reasoner);
		l.setPropertyToDescribe(new ObjectProperty("http://dbpedia.org/ontology/author"));
		l.init();
		l.start();
		
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(10, 0.75));
	}
	
}
