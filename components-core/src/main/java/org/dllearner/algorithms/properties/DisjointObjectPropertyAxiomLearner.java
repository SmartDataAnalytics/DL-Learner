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

import java.util.SortedSet;

import org.dllearner.core.ComponentAnn;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.learningproblems.AxiomScore;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import org.apache.jena.query.ParameterizedSparqlString;

@ComponentAnn(name = "disjoint object properties axiom learner", shortName = "opldisjoint", version = 0.1, description="A learning algorithm for disjoint object properties axioms.")
public class DisjointObjectPropertyAxiomLearner extends ObjectPropertyHierarchyAxiomLearner<OWLDisjointObjectPropertiesAxiom> {

	public DisjointObjectPropertyAxiomLearner(SparqlEndpointKS ks) {
		super(ks);

		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s ?o WHERE {?s ?p ?o. FILTER NOT EXISTS{?s ?p_other ?o}}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s ?o WHERE {?s ?p ?o; ?p_other ?o.}");
		
		axiomType = AxiomType.DISJOINT_OBJECT_PROPERTIES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		SortedSet<OWLObjectProperty> existingDisjointProperties = reasoner.getDisjointProperties(entityToDescribe);
		if (existingDisjointProperties != null && !existingDisjointProperties.isEmpty()) {
			for (OWLObjectProperty disProp : existingDisjointProperties) {
				existingAxioms.add(df.getOWLDisjointObjectPropertiesAxiom(entityToDescribe, disProp));
			}
			logger.info("Existing axioms:" + existingAxioms);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyHierarchyAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLObjectProperty, org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	public OWLDisjointObjectPropertiesAxiom getAxiom(OWLObjectProperty property, OWLObjectProperty otherProperty) {
		return df.getOWLDisjointObjectPropertiesAxiom(property, otherProperty);
	}
	
	@Override
	public AxiomScore computeScore(int candidatePopularity, int popularity, int overlap) {
		AxiomScore score = super.computeScore(candidatePopularity, popularity, overlap);

		// we need to invert the value
		AxiomScore invertedScore = new AxiomScore(
				1 - score.getAccuracy(),
				1 - score.getConfidence(),
				score.getNrOfPositiveExamples(), score.getNrOfNegativeExamples(),
				score.isSampleBased());

		return invertedScore;
	}
}
