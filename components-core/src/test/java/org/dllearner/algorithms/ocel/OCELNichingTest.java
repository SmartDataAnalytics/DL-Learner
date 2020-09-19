package org.dllearner.algorithms.ocel;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.datastructures.SearchTreeNonWeakPartialSet;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Random;

import static org.junit.Assert.assertEquals;


class MockHeuristic extends MultiHeuristic {
    private int seed = 123;
    private Random rng = new Random(seed);

    @Override
    public double getNodeScore(ExampleBasedNode node) {
        return rng.nextDouble();
    }
}

class MockOCEL extends OCELNiching {
    protected AbstractReasonerComponent reasoner;

    private static String ns = "http://dl-learner.org/ont#";
    private static OWLDataFactory df =
            OWLManager.createOWLOntologyManager().getOWLDataFactory();

    static OWLClassExpression owlThing = df.getOWLThing();
    static ExampleBasedNode owlThingNode =
            new ExampleBasedNode(MockOCEL.owlThing, null);

    static OWLClassExpression clsA = df.getOWLClass(IRI.create(ns + "A"));
    static ExampleBasedNode clsANode =
            new ExampleBasedNode(MockOCEL.clsA, null);

    static OWLClassExpression clsB = df.getOWLClass(IRI.create(ns + "B"));
    static ExampleBasedNode clsBNode =
            new ExampleBasedNode(MockOCEL.clsB, null);

    static OWLClassExpression clsC = df.getOWLClass(IRI.create(ns + "C"));
    static ExampleBasedNode clsCNode =
            new ExampleBasedNode(MockOCEL.clsC, null);

    static OWLClassExpression clsD = df.getOWLClass(IRI.create(ns + "D"));
    static ExampleBasedNode clsDNode =
            new ExampleBasedNode(MockOCEL.clsD, null);

    static OWLClassExpression clsE = df.getOWLClass(IRI.create(ns + "E"));
    static ExampleBasedNode clsENode =
            new ExampleBasedNode(MockOCEL.clsE, null);

    static OWLClassExpression clsF = df.getOWLClass(IRI.create(ns + "F"));
    static ExampleBasedNode clsFNode =
            new ExampleBasedNode(MockOCEL.clsF, null);

    public MockOCEL() throws OWLOntologyCreationException, ComponentInitException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ont = man.createOntology();

        man.addAxiom(ont, df.getOWLSubClassOfAxiom(clsA, owlThing));
        man.addAxiom(ont, df.getOWLSubClassOfAxiom(clsB, clsA));
        man.addAxiom(ont, df.getOWLSubClassOfAxiom(clsC, clsB));
        man.addAxiom(ont, df.getOWLSubClassOfAxiom(clsD, clsA));
        man.addAxiom(ont, df.getOWLSubClassOfAxiom(clsE, owlThing));
        man.addAxiom(ont, df.getOWLSubClassOfAxiom(clsF, clsE));

        reasoner = new OWLAPIReasoner(new OWLAPIOntology(ont));
        super.setReasoner(reasoner);
        reasoner.init();

        ExampleBasedHeuristic heuristic = new MockHeuristic();
        SearchTreeNonWeakPartialSet<ExampleBasedNode> searchTree =
                new SearchTreeNonWeakPartialSet<>(heuristic);

        searchTree.addNode(null, owlThingNode);
        searchTree.addNode(owlThingNode, clsANode);
        searchTree.addNode(clsANode, clsBNode);
        searchTree.addNode(clsBNode, clsCNode);
        searchTree.addNode(clsANode, clsDNode);
        searchTree.addNode(owlThingNode, clsENode);
        searchTree.addNode(clsENode, clsFNode);
    }
}

public class OCELNichingTest {
    @Test
    public void testComputeSimilarityPenalty() throws OWLOntologyCreationException, ComponentInitException {
        /*
         *        T
         *       / \
         *      A   E
         *     / \  |
         *    B   D F
         *    |
         *    C
         */

        OCELNiching ocel = new MockOCEL();

        // A --> A: 0
        assertEquals(1.0, ocel.computeSimilarityPenalty(MockOCEL.clsANode, MockOCEL.clsANode), 0);

        // A --> T: 1/2
        assertEquals(1./2, ocel.computeSimilarityPenalty(MockOCEL.clsANode, MockOCEL.owlThingNode), 0);
        assertEquals(1./2, ocel.computeSimilarityPenalty(MockOCEL.owlThingNode, MockOCEL.clsANode), 0);

        // B --> T: 1/3
        assertEquals(1./3, ocel.computeSimilarityPenalty(MockOCEL.clsBNode, MockOCEL.owlThingNode), 0);
        assertEquals(1./3, ocel.computeSimilarityPenalty(MockOCEL.owlThingNode, MockOCEL.clsBNode), 0);

        // C --> T: 1/4
        assertEquals(1./4, ocel.computeSimilarityPenalty(MockOCEL.clsCNode, MockOCEL.owlThingNode), 0);
        assertEquals(1./4, ocel.computeSimilarityPenalty(MockOCEL.owlThingNode, MockOCEL.clsCNode), 0);

        // C --> B: 1/2
        assertEquals(1./2, ocel.computeSimilarityPenalty(MockOCEL.clsCNode, MockOCEL.clsBNode), 0);
        assertEquals(1./2, ocel.computeSimilarityPenalty(MockOCEL.clsBNode, MockOCEL.clsCNode), 0);

        // D --> T: 1/3
        assertEquals(1./3, ocel.computeSimilarityPenalty(MockOCEL.clsDNode, MockOCEL.owlThingNode), 0);
        assertEquals(1./3, ocel.computeSimilarityPenalty(MockOCEL.owlThingNode, MockOCEL.clsDNode), 0);

        // D --> B: 0
        assertEquals(0.0, ocel.computeSimilarityPenalty(MockOCEL.clsDNode, MockOCEL.clsBNode), 0);
        assertEquals(0.0, ocel.computeSimilarityPenalty(MockOCEL.clsBNode, MockOCEL.clsDNode), 0);

        // E --> T: 1/2
        assertEquals(1./2, ocel.computeSimilarityPenalty(MockOCEL.clsENode, MockOCEL.owlThingNode), 0);
        assertEquals(1./2, ocel.computeSimilarityPenalty(MockOCEL.owlThingNode, MockOCEL.clsENode), 0);

        // E --> C: 0
        assertEquals(0.0, ocel.computeSimilarityPenalty(MockOCEL.clsENode, MockOCEL.clsCNode), 0);
        assertEquals(0.0, ocel.computeSimilarityPenalty(MockOCEL.clsCNode, MockOCEL.clsENode), 0);

        // F --> T: 1/3
        assertEquals(1./3, ocel.computeSimilarityPenalty(MockOCEL.clsFNode, MockOCEL.owlThingNode), 0);
        assertEquals(1./3, ocel.computeSimilarityPenalty(MockOCEL.owlThingNode, MockOCEL.clsFNode), 0);
    }
}
