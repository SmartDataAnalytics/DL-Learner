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
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class OWLAPIBugDemo {

    public static void main(String[] args) {
        try {

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            IRI ontologyURI = IRI.create("http://www.examples.com/test");
//            File f = new File("test.owl");
            
            File f = new File("src/dl-learner/org/dllearner/tools/ore/inconsistent.owl");
            IRI physicalURI = IRI.create(f.toURI());
            SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyURI, physicalURI);
            manager.addIRIMapper(mapper);

            OWLOntology ontology = manager.createOntology(ontologyURI);
            OWLDataFactory factory = manager.getOWLDataFactory();
            
            // create a set of two individuals
            OWLIndividual a = factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#a"));
            OWLIndividual b = factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#b"));
            Set<OWLIndividual> inds = new HashSet<OWLIndividual>();
            inds.add(a);
            inds.add(b);
            
            // create a set of two classes
            OWLClass c = factory.getOWLClass(IRI.create(ontologyURI + "#c"));
            OWLClass d = factory.getOWLClass(IRI.create(ontologyURI + "#d"));
            Set<OWLClass> classes = new HashSet<OWLClass>();
            classes.add(c);
            classes.add(d);            
            
            // state that a and b are different
            OWLAxiom axiom = factory.getOWLDifferentIndividualsAxiom(inds);
            AddAxiom addAxiom = new AddAxiom(ontology, axiom);
            manager.applyChange(addAxiom);
            
            // state that c and d are disjoint
            OWLAxiom axiom2 = factory.getOWLDisjointClassesAxiom(classes);
            AddAxiom addAxiom2 = new AddAxiom(ontology, axiom2);
            manager.applyChange(addAxiom2);
            
            // add property p with domain c 
    		OWLObjectProperty p = factory.getOWLObjectProperty(IRI.create(ontologyURI + "#p"));
    		OWLAxiom axiom3 = factory.getOWLObjectPropertyDomainAxiom(p, c);
            AddAxiom addAxiom3 = new AddAxiom(ontology, axiom3);
            manager.applyChange(addAxiom3);
            
            Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
            ontologies.add(ontology);
            
            OWLReasoner reasoner = new PelletReasonerFactory().createReasoner(ontology);
            
            // class cast exception
            NodeSet<OWLClass> test = reasoner.getObjectPropertyDomains(p, false);
//            OWLClass oc = (OWLClass) test.iterator().next();
//            System.out.println(oc);
            for(Node<OWLClass> test2 : test) {
            	System.out.println(test2);
            }
            
            // save ontology
            manager.saveOntology(ontology);
        }
        catch (OWLException e) {
            e.printStackTrace();
        }
    }
}
