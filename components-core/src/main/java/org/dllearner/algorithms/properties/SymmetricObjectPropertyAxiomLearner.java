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

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@ComponentAnn(name="symmetric objectproperty axiom learner", shortName="oplsymm", version=0.1)
public class SymmetricObjectPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm<OWLSymmetricObjectPropertyAxiom, OWLIndividual> {
	
	private static final Logger logger = LoggerFactory.getLogger(SymmetricObjectPropertyAxiomLearner.class);
	
	private OWLObjectProperty propertyToDescribe;
	
	private boolean declaredAsSymmetric;

	public SymmetricObjectPropertyAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		
		posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?s ?p ?o. ?o ?p ?s}");
		negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?s ?p ?o. FILTER NOT EXISTS {?o ?p ?s}}");
	}

	public OWLObjectProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(OWLObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		declaredAsSymmetric = reasoner.isSymmetric(propertyToDescribe);
		if(declaredAsSymmetric) {
			existingAxioms.add(df.getOWLSymmetricObjectPropertyAxiom(propertyToDescribe));
			logger.info("Property is already declared as symmetric in knowledge base.");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		if(!forceSPARQL_1_0_Mode && ks.supportsSPARQL_1_1()){
			runSPARQL1_1_Mode();
		} else {
			runSPARQL1_0_Mode();
		}
	}
	
	private void runSPARQL1_0_Mode(){
		workingModel = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery  = "CONSTRUCT {?s <%s> ?o.} WHERE {?s <%s> ?o} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, propertyToDescribe.toStringID(), propertyToDescribe.toStringID(), limit, offset);
		Model newModel = executeConstructQuery(query);
		while(!terminationCriteriaSatisfied() && newModel.size() != 0){
			workingModel.add(newModel);
			// get number of instances of s with <s p o>
			query = "SELECT (COUNT(*) AS ?total) WHERE {?s <%s> ?o.}";
			query = query.replace("%s", propertyToDescribe.toStringID());
			ResultSet rs = executeSelectQuery(query, workingModel);
			QuerySolution qs;
			int total = 0;
			while(rs.hasNext()){
				qs = rs.next();
				total = qs.getLiteral("total").getInt();
			}
			query = "SELECT (COUNT(*) AS ?symmetric) WHERE {?s <%s> ?o. ?o <%s> ?s}";
			query = query.replace("%s", propertyToDescribe.toStringID());
			rs = executeSelectQuery(query, workingModel);
			int symmetric = 0;
			while(rs.hasNext()){
				qs = rs.next();
				symmetric = qs.getLiteral("symmetric").getInt();
			}
			
			
			if(total > 0){
				currentlyBestAxioms.clear();
				currentlyBestAxioms.add(new EvaluatedAxiom<OWLSymmetricObjectPropertyAxiom>(
						df.getOWLSymmetricObjectPropertyAxiom(propertyToDescribe),
						computeScore(total, symmetric), declaredAsSymmetric));
			}
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.toStringID(), propertyToDescribe.toStringID(), limit, offset);
			newModel = executeConstructQuery(query);
		}
	}
	
	private void runSPARQL1_1_Mode(){
		int total = reasoner.getPopularity(propertyToDescribe);
		
		if(total > 0){
			int symmetric = 0;
			String query = "SELECT (COUNT(*) AS ?symmetric) WHERE {?s <%s> ?o. ?o <%s> ?s}";
			query = query.replace("%s", propertyToDescribe.toStringID());
			ResultSet rs = executeSelectQuery(query);
			if(rs.hasNext()){
				symmetric = rs.next().getLiteral("symmetric").getInt();
			}
			
			currentlyBestAxioms.add(new EvaluatedAxiom<OWLSymmetricObjectPropertyAxiom>(
					df.getOWLSymmetricObjectPropertyAxiom(propertyToDescribe),
					computeScore(total, symmetric), declaredAsSymmetric));
		}
		
	}
}
