package org.dllearner.test;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.*;

import java.io.File;
import java.net.URI;
import java.util.Set;

public class OWLAPIConsistency {
    public static void main(String[] args) {

        try {
            File f = new File("src/dl-learner/org/dllearner/tools/ore/inconsistent.owl");
            URI physicalURI = f.toURI();
        	
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

            OWLOntology ont = manager.loadOntologyFromPhysicalURI(physicalURI);
            System.out.println("Loaded " + ont.getURI());

            OWLReasoner reasoner = new org.mindswap.pellet.owlapi.Reasoner(manager);

            Set<OWLOntology> importsClosure = manager.getImportsClosure(ont);
            reasoner.loadOntologies(importsClosure);
//            reasoner.classify();

            boolean consistent = reasoner.isConsistent(ont);
            System.out.println("Consistent: " + consistent);
            System.out.println("\n");

        }
        catch(UnsupportedOperationException exception) {
            System.out.println("Unsupported reasoner operation.");
        }
        catch(OWLReasonerException ex) {
            System.out.println("Reasoner error: " + ex.getMessage());
        }
        catch (OWLOntologyCreationException e) {
            System.out.println("Could not load the pizza ontology: " + e.getMessage());
        }
    }
}
