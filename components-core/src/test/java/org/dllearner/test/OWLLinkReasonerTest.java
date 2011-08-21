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

import java.net.MalformedURLException;
import java.net.URL;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.owllink.OWLlinkHTTPXMLReasonerFactory;
import org.semanticweb.owlapi.owllink.OWLlinkReasoner;
import org.semanticweb.owlapi.owllink.OWLlinkReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.NodeSet;

public class OWLLinkReasonerTest {

	/**
	 * @param args
	 * @throws OWLOntologyCreationException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws OWLOntologyCreationException, MalformedURLException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		OWLOntology ontology = manager.createOntology(IRI.create("tutorial"));
		OWLClass A = manager.getOWLDataFactory().getOWLClass(IRI.create("http://tutorial#A"));
		OWLClass B = manager.getOWLDataFactory().getOWLClass(IRI.create("http://tutorial#B"));
		OWLAxiom a = manager.getOWLDataFactory().getOWLSubClassOfAxiom(A, B);
		manager.addAxiom(ontology, a);
		
		OWLlinkHTTPXMLReasonerFactory factory = new OWLlinkHTTPXMLReasonerFactory();
		URL url = new URL("http://localhost:8080");//Configure the server end-point
		OWLlinkReasonerConfiguration config = new OWLlinkReasonerConfiguration(url);
		OWLlinkReasoner reasoner = factory.createReasoner(ontology, config);
		
		NodeSet<OWLClass> classes = reasoner.getSubClasses(manager.getOWLDataFactory().getOWLThing(), true);
		System.out.println(classes.getFlattened());

	}

}
