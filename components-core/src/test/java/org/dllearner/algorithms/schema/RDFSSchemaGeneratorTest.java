package org.dllearner.algorithms.schema;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Lorenz Buehmann
 */
public class RDFSSchemaGeneratorTest {

    OWLOntology ontology;

    @Before
    public void setUp() throws Exception {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = man.getOWLDataFactory();
        ontology = man.createOntology();

        PrefixManager pm = new DefaultPrefixManager();
        pm.setDefaultPrefix("http://dl-learner.org/test/");

        OWLClass clsA = df.getOWLClass("A", pm);
        OWLClass clsB = df.getOWLClass("B", pm);
        OWLClass clsC = df.getOWLClass("C", pm);

        OWLObjectProperty op1 = df.getOWLObjectProperty("p1", pm);
        OWLObjectProperty op2 = df.getOWLObjectProperty("p2", pm);


        // A(a_i), 1<=i<=10
        for(int i = 0; i < 10; i++) {
            man.addAxiom(ontology, df.getOWLClassAssertionAxiom(clsA, df.getOWLNamedIndividual("a" + i, pm)));
        }

        // B(a_i), 1<=i<=8
        for(int i = 0; i < 7; i++) {
            man.addAxiom(ontology, df.getOWLClassAssertionAxiom(clsB, df.getOWLNamedIndividual("a" + i, pm)));
        }



    }

    @Test
    public void testGenerateSchema() throws Exception {
        SchemaGenerator gen = new RDFSSchemaGenerator(ontology);
        Set<OWLAxiom> schema = gen.generateSchema();

        System.out.println("generated schema:");
        schema.forEach(axiom -> System.out.println(axiom));

    }
}