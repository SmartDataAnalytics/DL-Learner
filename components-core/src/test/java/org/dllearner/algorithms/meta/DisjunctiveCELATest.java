package org.dllearner.algorithms.meta;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Lorenz Buehmann
 */
public class DisjunctiveCELATest {
    @Test
    public void start() throws Exception {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = man.getOWLDataFactory();
        OWLOntology o = man.createOntology();

        PrefixManager pm = new DefaultPrefixManager();
        pm.setDefaultPrefix("http://ex.org/");

        for(int j = 1; j <= 10; j++) {
            OWLClass cls = df.getOWLClass("A" + j, pm);
            for(int i = 1; i <= 10; i++) {
                int idx = (j-1) * 10 + i;
                man.addAxiom(o, df.getOWLClassAssertionAxiom(
                        cls,
                        df.getOWLNamedIndividual("pos" + idx, pm)));
            }
        }

        Set<OWLIndividual> posExamples = new TreeSet<>();
        for(int i = 1; i <= 100; i++) {
            posExamples.add(df.getOWLNamedIndividual("pos" + i, pm));
        }
        Set<OWLIndividual> negExamples = new TreeSet<>();
        for(int i = 1; i <= 100; i++) {
            OWLNamedIndividual ind = df.getOWLNamedIndividual("neg" + i, pm);
            man.addAxiom(o, df.getOWLClassAssertionAxiom(
                    df.getOWLClass("B", pm),
                    ind));
            negExamples.add(ind);
        }

        AbstractKnowledgeSource source = new OWLAPIOntology(o);
        source.init();

        // set up a closed-world reasoner
        AbstractReasonerComponent reasoner = new ClosedWorldReasoner(source);
        reasoner.init();

        // create a learning problem and set the class to describe
        PosNegLPStandard lp = new PosNegLPStandard(reasoner);
        lp.setPositiveExamples(posExamples);
        lp.setNegativeExamples(negExamples);
        lp.init();

        RhoDRDown.Builder operatorBuilder = new RhoDRDown.Builder()
        .setReasoner(reasoner)
        .setUseHasValueConstructor(true)
        .setUseAllConstructor(false)
        .setUseStringDatatypes(true)
        .setUseDataHasValueConstructor(true)
        .setFrequencyThreshold(1)
        .setUseNegation(false);

        // create the learning algorithm
        CELOE la = new CELOE(lp, reasoner);
        la.setNoisePercentage(100.0);
        la.setOperatorBuilder(operatorBuilder);
        la.init();

        DisjunctiveCELA la2 = new DisjunctiveCELA(la);
        la2.setMaxExecutionTimeInSeconds(60);
        la2.init();
        la2.start();
    }

}