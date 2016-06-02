/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;

import org.apache.jena.query.ParameterizedSparqlString;

@ComponentAnn(name="functional data property axiom learner", shortName="dplfunc", version=0.1, description="A learning algorithm for functional data property axioms.")
public class FunctionalDataPropertyAxiomLearner extends DataPropertyAxiomLearner<OWLFunctionalDataPropertyAxiom> {
	
	private final ParameterizedSparqlString GET_SAMPLE_QUERY = new ParameterizedSparqlString(
			"CONSTRUCT {?s ?p ?o.} WHERE {?s ?p ?o}");
	
	private final ParameterizedSparqlString POS_FREQUENCY_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?s ?p ?o1. FILTER NOT EXISTS {?s ?p ?o2. FILTER(?o1 != ?o2)} }");
	
	private boolean declaredAsFunctional;

	public FunctionalDataPropertyAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		
		posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?s ?p ?o1. FILTER NOT EXISTS {?s ?p ?o2. FILTER(?o1 != ?o2)} }");
		negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?s ?p ?o1. ?s ?p ?o2. FILTER(?o1 != ?o2)}");
		
		COUNT_QUERY = DISTINCT_SUBJECTS_COUNT_QUERY;
		
		axiomType = AxiomType.FUNCTIONAL_DATA_PROPERTY;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		declaredAsFunctional = reasoner.isFunctional(entityToDescribe);
		if(declaredAsFunctional) {
			existingAxioms.add(df.getOWLFunctionalDataPropertyAxiom(entityToDescribe));
			logger.warn("Data property " + entityToDescribe + " is already declared as functional in knowledge base.");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.PropertyAxiomLearner#setEntityToDescribe(org.semanticweb.owlapi.model.OWLProperty)
	 */
	@Override
	public void setEntityToDescribe(OWLDataProperty entityToDescribe) {
		super.setEntityToDescribe(entityToDescribe);
		
		POS_FREQUENCY_QUERY.setIri("p", entityToDescribe.toStringID());
		GET_SAMPLE_QUERY.setIri("p", entityToDescribe.toStringID());
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.DataPropertyAxiomLearner#run()
	 */
	@Override
	protected void run() {
		boolean declared = !existingAxioms.isEmpty();
		
		int frequency = getCountValue(POS_FREQUENCY_QUERY.toString());

		currentlyBestAxioms.add(new EvaluatedAxiom<>(
				df.getOWLFunctionalDataPropertyAxiom(entityToDescribe),
				computeScore(popularity, frequency, useSampling),
				declared));
	}
}
