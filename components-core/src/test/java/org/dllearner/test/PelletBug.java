/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.test;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class PelletBug {

	public static void main(String[] args) throws OWLOntologyCreationException,
			 UnknownOWLOntologyException, OWLOntologyStorageException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File f = new File("examples/family/father_oe.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
		OWLDataFactory factory = manager.getOWLDataFactory();

		// create a view class expressions and an axiom
		String ontologyURI = "http://example.com/father#";
		OWLClass male = factory.getOWLClass(IRI.create(ontologyURI + "male"));
		OWLClass female = factory.getOWLClass(IRI.create(ontologyURI + "female"));
		OWLClass father = factory.getOWLClass(IRI.create(ontologyURI + "father"));
		OWLClassExpression insat = factory.getOWLObjectIntersectionOf(male, female);
		OWLClassExpression test = factory.getOWLObjectComplementOf(male);
		OWLAxiom axiom = factory.getOWLEquivalentClassesAxiom(father, test);

		// load ontology
		OWLReasoner reasoner = new PelletReasonerFactory().createReasoner(ontology);

		// first subsumption check => everything runs smoothly
		boolean result = reasoner.isEntailed(factory.getOWLSubClassOfAxiom(female, insat));
		System.out.println("subsumption before: " + result);

		// add axiom causing the ontology to be inconsistent
		try {
			manager.applyChange(new AddAxiom(ontology, axiom));
		} catch (OWLOntologyChangeException e1) {
			e1.printStackTrace();
		}

		// Pellet correctly detects the inconsistency
		System.out.println("consistent: " + reasoner.isConsistent());

		// remove axiom
		try {
			manager.applyChange(new RemoveAxiom(ontology, axiom));
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}

		// save file to verify that it remained unchanged (it is unchanged)
		manager.saveOntology(ontology, IRI.create(new File("test.owl")));

		// perform subsumption check => Pellet now fails due to an
		// inconsistency, although the ontology is unchanged from the 
		// point of view of the OWL API
		result = reasoner.isEntailed(factory.getOWLSubClassOfAxiom(female, insat));
		System.out.println("subsumption after: " + result);

	}

}
