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
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;

import org.apache.jena.query.ParameterizedSparqlString;

@ComponentAnn(name="symmetric object property axiom learner", shortName="oplsymm", version=0.1, description="A learning algorithm for symmetric object property axioms.")
public class SymmetricObjectPropertyAxiomLearner extends ObjectPropertyCharacteristicsAxiomLearner<OWLSymmetricObjectPropertyAxiom> {
	
	public SymmetricObjectPropertyAxiomLearner(SparqlEndpointKS ks){
		super(ks);
		
		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s ?o WHERE {?s ?p ?o . ?o ?p ?s .}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s ?o WHERE {?s ?p ?o . FILTER NOT EXISTS{?o ?p ?s .}");
		
		super.POS_FREQUENCY_QUERY = new ParameterizedSparqlString(
					"SELECT (COUNT(*) AS ?cnt) WHERE {?s ?p ?o . ?o ?p ?s .}");
		
		axiomType = AxiomType.SYMMETRIC_OBJECT_PROPERTY;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyCharacteristicsAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	protected OWLSymmetricObjectPropertyAxiom getAxiom(OWLObjectProperty property) {
		return df.getOWLSymmetricObjectPropertyAxiom(property);
	}
}
