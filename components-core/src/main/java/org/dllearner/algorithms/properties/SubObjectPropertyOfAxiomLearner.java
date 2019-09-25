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
import org.dllearner.core.ConsoleAxiomLearningProgressMonitor;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

@ComponentAnn(name = "object subproperty axiom learner", shortName = "oplsubprop", version = 0.1, description="A learning algorithm object subproperty axioms.")
public class SubObjectPropertyOfAxiomLearner extends ObjectPropertyHierarchyAxiomLearner<OWLSubObjectPropertyOfAxiom>{

	private final double BETA = 3.0;

	public SubObjectPropertyOfAxiomLearner(SparqlEndpointKS ks) {
		super(ks);
		
		setBeta(BETA);
		
		axiomType = AxiomType.SUB_OBJECT_PROPERTY;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		SortedSet<OWLObjectProperty> existingSuperProperties = reasoner.getSuperProperties(entityToDescribe);
		if (existingSuperProperties != null && !existingSuperProperties.isEmpty()) {
			for (OWLObjectProperty supProp : existingSuperProperties) {
				existingAxioms.add(df.getOWLSubObjectPropertyOfAxiom(entityToDescribe, supProp));
			}
			logger.info("Existing axioms:" + existingAxioms);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyHierarchyAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLObjectProperty, org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	public OWLSubObjectPropertyOfAxiom getAxiom(OWLObjectProperty property, OWLObjectProperty otherProperty) {
		return df.getOWLSubObjectPropertyOfAxiom(property, otherProperty);
	}

	public static void main(String[] args) throws Exception {
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia());
		ks.init();

		SubObjectPropertyOfAxiomLearner la = new SubObjectPropertyOfAxiomLearner(ks);
		la.setEntityToDescribe(new OWLObjectPropertyImpl(IRI.create("http://dbpedia.org/ontology/author")));
		la.setUseSampling(false);
		la.setBatchMode(true);
		la.setProgressMonitor(new ConsoleAxiomLearningProgressMonitor());
		la.init();

		la.start();

		la.getCurrentlyBestEvaluatedAxioms().forEach(ax -> {
			System.out.println("---------------\n" + ax);
			la.getPositiveExamples(ax).stream().limit(5).forEach(System.out::println);
		});
	}
}
