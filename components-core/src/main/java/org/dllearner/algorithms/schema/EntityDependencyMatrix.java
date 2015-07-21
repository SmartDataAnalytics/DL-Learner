/**
 * 
 */
package org.dllearner.algorithms.schema;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.NodeSet;
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
	
	public static void EntityDependencyMatrix(OWLOntology ontology) {
		OWLReasoner reasoner = new StructuralReasonerFactory().createNonBufferingReasoner(ontology);
		
		// how often are individuals of class A related to individuals of class B
		Set<OWLClass> classes = ontology.getClassesInSignature(true);
		for (OWLClass clsA : classes) {
			for (OWLClass clsB : classes) {
				if(!clsA.equals(clsB)) {
					Set<OWLNamedIndividual> instancesA = reasoner.getInstances(clsA, false).getFlattened();
					Set<OWLNamedIndividual> instancesB = reasoner.getInstances(clsB, false).getFlattened();
					
					Set<OWLIndividual> objectsOfInstancesFromA = new HashSet<OWLIndividual>();
					for (OWLNamedIndividual ind : instancesA) {
						Set<OWLObjectPropertyAssertionAxiom> axioms = ontology.getObjectPropertyAssertionAxioms(ind);
						for (OWLObjectPropertyAssertionAxiom axiom : axioms) {
							objectsOfInstancesFromA.add(axiom.getObject());
						}
					}
					
					Set<OWLIndividual> objectsOfInstancesFromB = new HashSet<OWLIndividual>();
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
	 * @param entity1
	 * @param entity2
	 */
	public void getDependency(OWLEntity entity1, OWLEntity entity2){
		
	}

}
