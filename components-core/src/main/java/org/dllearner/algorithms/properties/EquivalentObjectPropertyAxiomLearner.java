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
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;

@ComponentAnn(name="equivalent object properties axiom learner", shortName="oplequiv", version=0.1, description="A learning algorithm for equivalent object properties axioms.")
public class EquivalentObjectPropertyAxiomLearner extends ObjectPropertyHierarchyAxiomLearner<OWLEquivalentObjectPropertiesAxiom> {
	
	private final double BETA = 1.0;
	
	public EquivalentObjectPropertyAxiomLearner(SparqlEndpointKS ks){
		super(ks);
		
		setBeta(BETA);
		
		axiomType = AxiomType.EQUIVALENT_OBJECT_PROPERTIES;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		SortedSet<OWLObjectProperty> existingEquivalentProperties = reasoner.getEquivalentProperties(entityToDescribe);
		if (existingEquivalentProperties != null && !existingEquivalentProperties.isEmpty()) {
			for (OWLObjectProperty eqProp : existingEquivalentProperties) {
				existingAxioms.add(df.getOWLEquivalentObjectPropertiesAxiom(entityToDescribe, eqProp));
			}
			logger.info("Existing axioms:" + existingAxioms);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyHierarchyAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLObjectProperty, org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	public OWLEquivalentObjectPropertiesAxiom getAxiom(OWLObjectProperty property, OWLObjectProperty otherProperty) {
		return df.getOWLEquivalentObjectPropertiesAxiom(property, otherProperty);
	}
}
