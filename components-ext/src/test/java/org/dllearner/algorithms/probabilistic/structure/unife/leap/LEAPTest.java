/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.algorithms.probabilistic.structure.unife.leap;

import static junit.framework.TestCase.assertTrue;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.probabilistic.parameter.unife.edge.AbstractEDGE;
import org.dllearner.algorithms.probabilistic.parameter.unife.edge.EDGEWrapper;
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
    static AbstractCELA cela;

    private static final Logger logger = LoggerFactory.getLogger(LEAPTest.class);
    
    
    @BeforeClass
    public static void setUpClass() throws ComponentInitException {
        System.out.println("Current dir: " + System.getProperty("user.dir"));
        ks = new OWLFile("../examples/family/father_oe.owl");
        ks.init();

        rc = new ClosedWorldReasoner(ks);
        rc.init();

        lp = new ClassLearningProblem(rc);
        lp.setClassToDescribe(new OWLClassImpl(IRI.create("http://example.com/father#father")));
        lp.setCheckConsistency(false);
        lp.init();

        lpr = new EDGEWrapper(lp, null);
        lpr.setRandomizeAll(true);
        lpr.init();
        
        cela = new CELOE(lp, rc);
        cela.init();

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
        System.out.println("Test case 1");
        System.out.println("Debug logger: " + logger.isDebugEnabled());
        logger.debug("adfd");
        LEAP leap = new LEAP(cela, lpr);
        leap.setClassExpressionLearningAlgorithm(cela);
        leap.setEdge((AbstractEDGE) lpr);
        leap.init();
        leap.setClassAxiomType("equivalentClasses");
        //leap.setClassAxiomType("subClassOf");
        leap.start();
        assertTrue( true );
    }

}
