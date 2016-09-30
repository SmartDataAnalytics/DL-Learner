/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.algorithms.probabilistic.structure.unife.leap;

import static junit.framework.TestCase.assertTrue;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.probabilistic.parameter.unife.edge.AbstractEDGE;
import org.dllearner.algorithms.probabilistic.parameter.unife.edge.DummyParameterLearner;
import org.dllearner.algorithms.probabilistic.parameter.unife.edge.EDGE;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

/**
 *
 * @author Giuseppe Cota
 */
public class LEAPTest {

    static AbstractKnowledgeSource ks;
    static AbstractReasonerComponent rc;
    static ClassLearningProblem lp;
    static AbstractEDGE lpr;
    static CELOE cela;

    private static final Logger logger = LoggerFactory.getLogger(LEAPTest.class);

    @BeforeClass
    public static void setUpClass() throws ComponentInitException {
        System.out.println("Current dir: " + System.getProperty("user.dir"));
//        ks = new OWLFile("../examples/probabilistic/family/father_oe.owl");
        ks = new OWLFile("../examples/probabilistic/family/father_oe.owl");
        ks.init();

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCase1() throws Exception {
        System.out.println("Test case 1 - Equivalent axioms");

        rc = new ClosedWorldReasoner(ks);
        rc.init();

        lp = new ClassLearningProblem(rc);
        lp.setClassToDescribe(new OWLClassImpl(IRI.create("http://example.com/father#father")));
        lp.setCheckConsistency(false);
        lp.init();

        lpr = new EDGE(lp, null);
        lpr.setRandomize(true);
        lpr.setProbabilizeAll(true);
        lpr.init();

        cela = new CELOE(lp, rc);
        cela.setMaxExecutionTimeInSeconds(10);
        cela.setMaxNrOfResults(10);
        cela.init();

        System.out.println("Debug logger: " + logger.isDebugEnabled());
        LEAP leap = new LEAP(cela, lpr);
        leap.setClassExpressionLearningAlgorithm(cela);
        leap.setEdge((AbstractEDGE) lpr);
        leap.init();
        leap.setClassAxiomType("equivalentClasses");
        leap.start();
        assertTrue(true);
    }

    @Test
    public void testCase2() throws Exception {
        System.out.println("Test case 2 - SubClassOf axioms");

        rc = new ClosedWorldReasoner(ks);
        rc.init();

        lp = new ClassLearningProblem(rc);
        lp.setClassToDescribe(new OWLClassImpl(IRI.create("http://example.com/father#father")));
        lp.setCheckConsistency(false);
        lp.init();

        lpr = new EDGE(lp, null);
        lpr.setRandomize(true);
        lpr.setProbabilizeAll(true);
        lpr.init();

        cela = new CELOE(lp, rc);
        cela.setMaxExecutionTimeInSeconds(10);
        cela.setMaxNrOfResults(10);
        cela.init();

        System.out.println("Debug logger: " + logger.isDebugEnabled());
        LEAP leap = new LEAP(cela, lpr);
        leap.setClassExpressionLearningAlgorithm(cela);
        leap.setEdge((AbstractEDGE) lpr);
        leap.init();
        leap.setClassAxiomType("subClassOf");
        leap.start();
        assertTrue(true);
    }

    @Test
    public void testCase3() throws Exception {
        System.out.println("Test case 3 - Dummy parameter learner");

        rc = new ClosedWorldReasoner(ks);
        rc.init();

        lp = new ClassLearningProblem(rc);
        lp.setClassToDescribe(new OWLClassImpl(IRI.create("http://example.com/father#father")));
        lp.setCheckConsistency(false);
        lp.init();

        lpr = new DummyParameterLearner(lp, null);
        lpr.setProbabilizeAll(true);
        lpr.init();

        cela = new CELOE(lp, rc);
        cela.setMaxExecutionTimeInSeconds(10);
        cela.setMaxNrOfResults(10);
        cela.init();

        System.out.println("Debug logger: " + logger.isDebugEnabled());
        LEAP leap = new LEAP(cela, lpr);
        leap.setClassExpressionLearningAlgorithm(cela);
        leap.setEdge((AbstractEDGE) lpr);
        leap.init();
        leap.setClassAxiomType("subClassOf");
        leap.start();
        assertTrue(true);
    }

}
