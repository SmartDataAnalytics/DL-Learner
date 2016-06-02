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
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import org.apache.jena.query.ParameterizedSparqlString;

@ComponentAnn(name = "irreflexive object property axiom learner", shortName = "oplirrefl", version = 0.1, description="A learning algorithm for irreflexive object property axioms.")
public class IrreflexiveObjectPropertyAxiomLearner extends ObjectPropertyCharacteristicsAxiomLearner<OWLIrreflexiveObjectPropertyAxiom> {

	public IrreflexiveObjectPropertyAxiomLearner(SparqlEndpointKS ks) {
		super(ks);
		
		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s WHERE {?s ?p ?o . FILTER NOT EXISTS{?s ?p ?s .}}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s WHERE {?s ?p ?s .}");
		
		super.POS_FREQUENCY_QUERY = new ParameterizedSparqlString(
					"SELECT (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?s ?p ?o . FILTER NOT EXISTS {?s ?p ?s .} }");
		
		COUNT_QUERY = DISTINCT_SUBJECTS_COUNT_QUERY;
		
		axiomType = AxiomType.IRREFLEXIVE_OBJECT_PROPERTY;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyCharacteristicsAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	protected OWLIrreflexiveObjectPropertyAxiom getAxiom(OWLObjectProperty property) {
		return df.getOWLIrreflexiveObjectPropertyAxiom(property);
	}
}
