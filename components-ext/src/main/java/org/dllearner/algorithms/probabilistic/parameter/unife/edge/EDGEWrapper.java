/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.algorithms.probabilistic.parameter.unife.edge;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.probabilistic.unife.ParameterLearningException;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.semanticweb.owlapi.model.OWLAxiom;
import unife.edge.EDGE;

/**
 * This class is a wrapper for EDGE algorithm. This algorithm uses BUNDLE for
 * probabilistic reasoning. This class stores an instantiation of EDGE and
 * invokes its methods in order to compute the parameters.
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
@ComponentAnn(name = "EDGEWrapper", shortName = "edge", version = 1.0)
public class EDGEWrapper extends AbstractEDGE {

    private static Logger logger
            = Logger.getLogger(EDGEWrapper.class.getName());

    private boolean fullyInitialized = false;

    public EDGEWrapper() {
        edge = new EDGE();
    }

    public EDGEWrapper(ClassLearningProblem lp, Set<OWLAxiom> targetAxioms) {
        super(lp, targetAxioms);
        edge = new EDGE();
    }

    @Override
    public BigDecimal getParameter(OWLAxiom ax) {
        BigDecimal parameter = super.getParameter(ax);
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
                    logger.debug("max positive examples set: " + maxPositiveExamples);
                    //List positiveIndividualsList = new ArrayList(positiveIndividuals);
                    Collections.shuffle(positiveExamplesList);
                    if (maxPositiveExamples < positiveExamplesList.size()) {
                        positiveExamplesList = positiveExamplesList.subList(0, maxPositiveExamples);
                    }
                }
                List<OWLAxiom> negativeExamplesList = new ArrayList<>(negativeExampleAxioms);
                if (maxNegativeExamples > 0) {
                    logger.debug("max negative examples set: " + maxNegativeExamples);
                    Collections.shuffle(negativeExamplesList);
                    if (maxNegativeExamples < negativeExamplesList.size()) {
                        negativeExamplesList = negativeExamplesList.subList(0, maxNegativeExamples);
                    }
                }
                logger.debug("qui");
                edge.setPositiveExamples(positiveExamplesList);
                edge.setNegativeExamples(negativeExamplesList);
                fullyInitialized = true;
                logger.debug("qui");
            }
            logger.debug("qui");
            results = edge.computeLearning();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new ParameterLearningException(ex);
        }
        isRunning = false;
    }

}
