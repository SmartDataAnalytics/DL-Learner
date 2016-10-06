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
package org.dllearner.algorithms.schema;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * 
 * @author Lorenz Buehmann
 * @since Oct 21, 2014
 */
public class EntityDependencyMatrix<T> {
	
	
	private EntityDependencyMatrix() {
		
	}
	
	public static void getEntityDependencyMatrix(OWLOntology ontology) {
		OWLReasoner reasoner = new StructuralReasonerFactory().createNonBufferingReasoner(ontology);
		
		// how often are individuals of class A related to individuals of class B
		Set<OWLClass> classes = ontology.getClassesInSignature(Imports.INCLUDED);
		for (OWLClass clsA : classes) {
			for (OWLClass clsB : classes) {
				if(!clsA.equals(clsB)) {
					Set<OWLNamedIndividual> instancesA = reasoner.getInstances(clsA, false).getFlattened();
					Set<OWLNamedIndividual> instancesB = reasoner.getInstances(clsB, false).getFlattened();

					// S_1 = { o_i | A(a_i) and there is an p_i(a_i, o_i) in O }
					Set<OWLIndividual> objectsOfInstancesFromA = new HashSet<>();
					for (OWLNamedIndividual ind : instancesA) {
						Set<OWLObjectPropertyAssertionAxiom> axioms = ontology.getObjectPropertyAssertionAxioms(ind);
						for (OWLObjectPropertyAssertionAxiom axiom : axioms) {
							objectsOfInstancesFromA.add(axiom.getObject());
						}
					}

					// S_2 = { o_i | B(b_i) and there is an p_i(b_i, o_i) in O }
					Set<OWLIndividual> objectsOfInstancesFromB = new HashSet<>();
					for (OWLNamedIndividual ind : instancesB) {
						Set<OWLObjectPropertyAssertionAxiom> axioms = ontology.getObjectPropertyAssertionAxioms(ind);
						for (OWLObjectPropertyAssertionAxiom axiom : axioms) {
							objectsOfInstancesFromB.add(axiom.getObject());
						}
					}
					
					// A -> B
					SetView<OWLIndividual> aToB = Sets.intersection(objectsOfInstancesFromA, instancesB);
					// B -> A
					SetView<OWLIndividual> bToA = Sets.intersection(objectsOfInstancesFromB, instancesA);
					
				}
			}
		}
	}
	
	/**
	 * Returns the degree by which entity1 depends on entity2.
	 * Note that this value is not necessarily symmetric, i.e. the degree
	 * entity1 depends on entity2 might be different from what
	 * entity2 depends on entity1.
	 * 
	 * @param entity1 the first entity
	 * @param entity2 the second entity
	 * @return the dependency value
	 */
	public double getDependency(OWLEntity entity1, OWLEntity entity2){
		return 0;
	}

}
