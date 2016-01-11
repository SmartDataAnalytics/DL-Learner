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
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;

@ComponentAnn(name="equivalent data properties axiom learner", shortName="dplequiv", version=0.1, description="A learning algorithm for equivalent data properties axioms.")
public class EquivalentDataPropertyAxiomLearner extends DataPropertyHierarchyAxiomLearner<OWLEquivalentDataPropertiesAxiom> {
	
	private final double BETA = 1.0;
	
	public EquivalentDataPropertyAxiomLearner(SparqlEndpointKS ks){
		super(ks);
		
		setBeta(BETA);
		
		axiomType = AxiomType.EQUIVALENT_DATA_PROPERTIES;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		SortedSet<OWLDataProperty> existingEquivalentProperties = reasoner.getEquivalentProperties(entityToDescribe);
		if (existingEquivalentProperties != null && !existingEquivalentProperties.isEmpty()) {
			for (OWLDataProperty eqProp : existingEquivalentProperties) {
				existingAxioms.add(df.getOWLEquivalentDataPropertiesAxiom(entityToDescribe, eqProp));
			}
			logger.info("Existing axioms:" + existingAxioms);
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.DataPropertyHierarchyAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLDataProperty, org.semanticweb.owlapi.model.OWLDataProperty)
	 */
	@Override
	public OWLEquivalentDataPropertiesAxiom getAxiom(OWLDataProperty property, OWLDataProperty otherProperty) {
		return df.getOWLEquivalentDataPropertiesAxiom(property, otherProperty);
	}
}
