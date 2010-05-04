package org.dllearner.test;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class OWLAPIConsistency {
    public static void main(String[] args) {

        try {
            File f = new File("examples/ore/inconsistent.owl");
        	
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

            OWLOntology ont = manager.loadOntologyFromOntologyDocument(f);
            System.out.println("Loaded " + ont.getOntologyID());

            OWLReasoner reasoner = new PelletReasonerFactory().createReasoner(ont);

            boolean consistent = reasoner.isConsistent();
            System.out.println("Consistent: " + consistent);
            System.out.println("\n");

        }
        catch(UnsupportedOperationException exception) {
            System.out.println("Unsupported reasoner operation.");
        }
        catch (OWLOntologyCreationException e) {
            System.out.println("Could not load the pizza ontology: " + e.getMessage());
        }
    }
}
