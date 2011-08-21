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
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;

public class FaCTBugDemo {

    public static void main(String[] args) {

        try {
        	IRI uri = IRI.create(new File("examples/father.owl").toURI());
        	
            // Create our ontology manager in the usual way.
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

            // Load a copy of the pizza ontology.  We'll load the ontology from the web.
            OWLOntology ont = manager.loadOntologyFromOntologyDocument(uri);

            OWLReasoner reasoner = new FaCTPlusPlusReasonerFactory().createReasoner(ont);
            // OWLReasoner reasoner = new PelletReasonerFactory().createReasoner(ont);:

           reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS);
            
            OWLDataFactory factory = manager.getOWLDataFactory();
            
            OWLClass male = factory.getOWLClass(IRI.create("http://example.com/father#male"));
            OWLObjectProperty hasChild = factory.getOWLObjectProperty(IRI.create("http://example.com/father#hasChild"));
            OWLObjectSomeValuesFrom hasSomeChild = factory.getOWLObjectSomeValuesFrom(hasChild, factory.getOWLThing());
            Set<OWLClassExpression> set = new HashSet<OWLClassExpression>();
            set.add(male);
            set.add(hasSomeChild);
            OWLClassExpression father = factory.getOWLObjectIntersectionOf(set);
            OWLNamedIndividual martin = factory.getOWLNamedIndividual(IRI.create("http://example.com/father#martin"));
            
            if(reasoner.isEntailed(factory.getOWLClassAssertionAxiom(father, martin))) 
            	System.out.println("positive result"); // Pellet 1.5.1 (correct) 
            else
            	System.out.println("negative result"); // FaCT++ 1.10
         
        }
        catch(UnsupportedOperationException exception) {
            System.out.println("Unsupported reasoner operation.");
        }
        catch (OWLOntologyCreationException e) {
            System.out.println("Could not load the pizza ontology: " + e.getMessage());
        } catch (Exception e) {
			e.printStackTrace();
		}
    }
}
