package org.dllearner.test;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.*;
import org.semanticweb.owl.util.SimpleURIMapper;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class OWLAPIBugDemo {

    public static void main(String[] args) {
        try {

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            URI ontologyURI = URI.create("http://www.examples.com/test");
            File f = new File("test.owl");
            URI physicalURI = f.toURI();
            SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
            manager.addURIMapper(mapper);

            OWLOntology ontology = manager.createOntology(ontologyURI);
            OWLDataFactory factory = manager.getOWLDataFactory();
            
            OWLIndividual a = factory.getOWLIndividual(URI.create(ontologyURI + "#a"));
            OWLIndividual b = factory.getOWLIndividual(URI.create(ontologyURI + "#b"));
            Set<OWLIndividual> inds = new HashSet<OWLIndividual>();
            inds.add(a);
            inds.add(b);
            
            OWLAxiom axiom = factory.getOWLDifferentIndividualsAxiom(inds);
            AddAxiom addAxiom = new AddAxiom(ontology, axiom);
            manager.applyChange(addAxiom);
            manager.saveOntology(ontology);
        }
        catch (OWLException e) {
            e.printStackTrace();
        }
    }
}
