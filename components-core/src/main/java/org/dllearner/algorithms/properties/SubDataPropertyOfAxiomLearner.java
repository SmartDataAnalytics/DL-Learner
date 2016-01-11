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
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;

@ComponentAnn(name="data subproperty axiom learner", shortName="dplsubprop", version=0.1, description="A learning algorithm data subproperty axioms.")
public class SubDataPropertyOfAxiomLearner extends DataPropertyHierarchyAxiomLearner<OWLSubDataPropertyOfAxiom> {
	
	private final double BETA = 3.0;
	
	public SubDataPropertyOfAxiomLearner(SparqlEndpointKS ks){
		super(ks);
		
		super.beta = BETA;
		
		axiomType = AxiomType.SUB_DATA_PROPERTY;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		SortedSet<OWLDataProperty> existingSuperProperties = reasoner.getSuperProperties(entityToDescribe);
		if (existingSuperProperties != null && !existingSuperProperties.isEmpty()) {
			for (OWLDataProperty supProp : existingSuperProperties) {
				existingAxioms.add(df.getOWLSubDataPropertyOfAxiom(entityToDescribe, supProp));
			}
			logger.info("Existing axioms:" + existingAxioms);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.DataPropertyHierarchyAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLDataProperty, org.semanticweb.owlapi.model.OWLDataProperty)
	 */
	@Override
	public OWLSubDataPropertyOfAxiom getAxiom(OWLDataProperty property, OWLDataProperty otherProperty) {
		return df.getOWLSubDataPropertyOfAxiom(property, otherProperty);
	}
}
