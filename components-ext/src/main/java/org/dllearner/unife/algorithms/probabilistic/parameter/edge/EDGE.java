/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.unife.algorithms.probabilistic.parameter.edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.unife.core.probabilistic.ParameterLearningException;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.semanticweb.owlapi.model.OWLAxiom;
import unife.math.ApproxDouble;

/**
 * This class is a wrapper for EDGE algorithm. This algorithm uses BUNDLE for
 * probabilistic reasoning. This class stores an instantiation of EDGE and
 * invokes its methods in order to compute the parameters.
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
@ComponentAnn(name = "EDGE", shortName = "edge", version = 1.0)
public class EDGE extends AbstractEDGE {

    private static Logger logger
            = Logger.getLogger(EDGE.class.getName());

    private boolean fullyInitialized = false;

    public EDGE() {
        edge = new unife.edge.EDGE();
    }

    public EDGE(ClassLearningProblem lp, Set<OWLAxiom> targetAxioms) {
        super(lp, targetAxioms);
        edge = new unife.edge.EDGE();
    }

    @Override
    public ApproxDouble getParameter(OWLAxiom ax) {
        ApproxDouble parameter = super.getParameter(ax);
        if (parameter == null) {
            String msg = "the given axiom: " + ax.getAxiomWithoutAnnotations() + " is not probabilistic or does not exist";
            logger.warn(msg);
        }
        return parameter;
    }

    @Override
    public void init() throws ComponentInitException {
        if (edge == null) {
            String msg = "Underlying EDGE class not instantiated";
            logger.error(msg);
            throw new ComponentInitException(msg);
        }
        logger.debug("Initializing EDGE");
        fullyInitialized = false;
        super.init();

    }

    @Override
    public void start() {
        isRunning = true;
        stop = false;

        try {
            if (!fullyInitialized) {
                List<OWLAxiom> positiveExamplesList = new ArrayList<>(positiveExampleAxioms);
                if (maxPositiveExamples > 0) {
                    logger.debug("max number of positive examples to set: " + maxPositiveExamples);
                    //List positiveIndividualsList = new ArrayList(positiveIndividuals);
                    Collections.shuffle(positiveExamplesList);
                    if (maxPositiveExamples < positiveExamplesList.size()) {
                        positiveExamplesList = positiveExamplesList.subList(0, maxPositiveExamples);
                    }
                }
                edge.setPositiveExamples(positiveExamplesList);
                
                List<OWLAxiom> negativeExamplesList = new ArrayList<>(negativeExampleAxioms);
                if (maxNegativeExamples > 0) {
                    logger.debug("max number of negative examples to set: " + maxNegativeExamples);
                    Collections.shuffle(negativeExamplesList);
                    if (maxNegativeExamples < negativeExamplesList.size()) {
                        negativeExamplesList = negativeExamplesList.subList(0, maxNegativeExamples);
                    }
                }
                edge.setNegativeExamples(negativeExamplesList);
                edge.init();
                fullyInitialized = true;
            }
            results = edge.computeLearning();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new ParameterLearningException(ex);
        }
        isRunning = false;
    }
    
}
