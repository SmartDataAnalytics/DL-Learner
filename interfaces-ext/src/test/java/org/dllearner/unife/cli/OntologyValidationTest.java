/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.unife.cli;

import java.util.Set;
import java.util.TreeSet;
import org.dllearner.core.ComponentInitException;
import org.dllearner.unife.core.probabilistic.BUNDLE;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLP;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 *
 * @author giuseppe
 */
@RunWith(MockitoJUnitRunner.class)
public class OntologyValidationTest {

    private OntologyValidation instance;

    @Mock
    private PosNegLP lpMock;

    public OntologyValidationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
//        MockitoAnnotations.initMocks(this);
        String[] posExamplesStr = {
            "http://dl-learner.org/mammographic#Patient285",
            "http://dl-learner.org/mammographic#Patient288",
            "http://dl-learner.org/mammographic#Patient290",
            "http://dl-learner.org/mammographic#Patient292",
            "http://dl-learner.org/mammographic#Patient293",
            "http://dl-learner.org/mammographic#Patient295",
            "http://dl-learner.org/mammographic#Patient297",
            "http://dl-learner.org/mammographic#Patient299"
        };

        String[] negExamplesStr = {
            "http://dl-learner.org/mammographic#Patient255",
            "http://dl-learner.org/mammographic#Patient26",
            "http://dl-learner.org/mammographic#Patient261",
            "http://dl-learner.org/mammographic#Patient262",
            "http://dl-learner.org/mammographic#Patient263",
            "http://dl-learner.org/mammographic#Patient265",
            "http://dl-learner.org/mammographic#Patient267",
            "http://dl-learner.org/mammographic#Patient27"
        };

        OWLDataFactory df = OWLManager.getOWLDataFactory();
        Set<OWLIndividual> posExamples = new TreeSet<>();
        Set<OWLIndividual> negExamples = new TreeSet<>();

        for (String posExampleStr : posExamplesStr) {
            posExamples.add(df.getOWLNamedIndividual(IRI.create(posExampleStr)));
        }

        for (String negExampleStr : negExamplesStr) {
            negExamples.add(df.getOWLNamedIndividual(IRI.create(negExampleStr)));
        }

        when(lpMock.getPositiveExamples()).thenReturn(posExamples);
        when(lpMock.getNegativeExamples()).thenReturn(negExamples);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of run method, of class OntologyValidation.
     */
    @Test
    public void testRun() throws ComponentInitException {
        System.out.println("run");
        instance = new OntologyValidation();
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        String classExpressionStr = "https://sites.google.com/a/unife.it/ml/disponte/learnedClass";
        OWLClass classExpression = df.getOWLClass(IRI.create(classExpressionStr));
        instance.setClassExpression(classExpression);

        OWLFile ks = new OWLFile("../examples/probabilistic/mammographic/"
                + "mammographic.owl");
        ks.init();
        BUNDLE bundle = new BUNDLE();
        bundle.setSources(ks);
        bundle.init();
        instance.setReasoner(bundle);
        instance.setLp(lpMock);
        instance.run();
    }
    
}
