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

import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class HermitTest {
	
	public static void main(String[] args) throws OWLOntologyCreationException{
		IRI ontologyIRI = IRI.create("http://www.mindswap.org/ontologies/SC.owl");
//		IRI ontologyIRI = IRI.create("http://acl.icnet.uk/%7Emw/MDM0.73.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(ontologyIRI);
		
		ConsoleProgressMonitor mon = new ConsoleProgressMonitor();
		OWLReasonerConfiguration conf = new SimpleConfiguration(mon);
		
		System.out.println("Using Pellet reasoner");
		OWLReasoner pellet = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontology, conf);
		pellet.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS);
		
		for(OWLIndividual ind : ontology.getIndividualsInSignature(true)){
			System.out.println("Individual: " + ind);
			for(OWLObjectProperty prop : ontology.getObjectPropertiesInSignature(true)){
				System.out.println("Property: " + prop);
				pellet.getObjectPropertyValues(ind.asOWLNamedIndividual(), prop);
			}
		}
		
		System.out.println("Using HermiT reasoner");
		OWLReasoner hermit = new ReasonerFactory().createNonBufferingReasoner(ontology, conf);
		hermit.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS);
		
		for(OWLIndividual ind : ontology.getIndividualsInSignature(true)){
			System.out.println("Individual: " + ind);
			for(OWLObjectProperty prop : ontology.getObjectPropertiesInSignature(true)){
				System.out.println("Property: " + prop);
				hermit.getObjectPropertyValues(ind.asOWLNamedIndividual(), prop);
			}
		}
		
		
	}

}
