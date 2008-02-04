package org.dllearner.test;

import org.semanticweb.owl.model.*;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.io.File;

public class FaCTBugDemo {

    public static void main(String[] args) {

        try {
        	URI uri = new File("examples/father.owl").toURI();
        	
            // Create our ontology manager in the usual way.
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

            // Load a copy of the pizza ontology.  We'll load the ontology from the web.
            OWLOntology ont = manager.loadOntologyFromPhysicalURI(uri);

            OWLReasoner reasoner = new uk.ac.manchester.cs.factplusplus.owlapi.Reasoner(manager);
            // OWLReasoner reasoner = new org.mindswap.pellet.owlapi.Reasoner(manager);

            // seems to be needed for some reason although no ontology is imported
            Set<OWLOntology> importsClosure = manager.getImportsClosure(ont);
            reasoner.loadOntologies(importsClosure);

            reasoner.classify();
            reasoner.realise();
            
            OWLDataFactory factory = manager.getOWLDataFactory();
            
            OWLClass male = factory.getOWLClass(URI.create("http://example.com/father#male"));
            OWLObjectProperty hasChild = factory.getOWLObjectProperty(URI.create("http://example.com/father#hasChild"));
            OWLObjectSomeRestriction hasSomeChild = factory.getOWLObjectSomeRestriction(hasChild, factory.getOWLThing());
            Set<OWLDescription> set = new HashSet<OWLDescription>();
            set.add(male);
            set.add(hasSomeChild);
            OWLDescription father = factory.getOWLObjectIntersectionOf(set);
            OWLIndividual martin = factory.getOWLIndividual(URI.create("http://example.com/father#martin"));
            
            if(reasoner.hasType(martin, father, false))
            	System.out.println("positive result"); // Pellet 1.5.1 (correct) 
            else
            	System.out.println("negative result"); // FaCT++ 1.10
         
        }
        catch(UnsupportedOperationException exception) {
            System.out.println("Unsupported reasoner operation.");
        }
        catch(OWLReasonerException ex) {
            System.out.println("Reasoner error: " + ex.getMessage());
        }
        catch (OWLOntologyCreationException e) {
            System.out.println("Could not load the pizza ontology: " + e.getMessage());
        } catch (Exception e) {
			e.printStackTrace();
		}
    }
}
