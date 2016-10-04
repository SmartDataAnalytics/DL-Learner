/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.algorithms.probabilistic.parameter.distributed.unife.edge;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.probabilistic.unife.ParameterLearningException;
import org.dllearner.core.probabilistic.distributed.unife.AbstractEDGEDistributed;
import org.semanticweb.owlapi.model.OWLAxiom;
import unife.bundle.logging.BundleLoggerFactory;
import unife.edge.EDGEMPIDynamic;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
@ComponentAnn(name = "EDGEDistributedDynamic", shortName = "edgedynamic", version = 1.0)
public class EDGEDistributedDynamic extends AbstractEDGEDistributed {

    private static Logger logger
            = Logger.getLogger(EDGEDistributedDynamic.class.getName(), new BundleLoggerFactory());

    @ConfigOption(defaultValue = "1", description = "number of example for chunk")
    private int chunkDim = 1;

    @ConfigOption(description = " max number of concurrent threads which send examples to the slaves", defaultValue = "#processors - 1")
    private int maxSenderThreads;

    private boolean fullyInitialized = false;

    public EDGEDistributedDynamic() {
        edge = new EDGEMPIDynamic();
    }

    @Override
    public void init() throws ComponentInitException {
        fullyInitialized = false;
        super.init();
        if (maxSenderThreads == 0) {
            maxSenderThreads = Runtime.getRuntime().availableProcessors() - 1;
            if (maxSenderThreads == 0) {
                maxSenderThreads = 1;
            }
        }
    }

    @Override
    public void start() {
        isRunning = true;
        stop = false;

        try {
            EDGEMPIDynamic edgeDyn = (EDGEMPIDynamic) edge;
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
                edge.setPositiveExamples(positiveExamplesList);
                edge.setNegativeExamples(negativeExamplesList);
                edgeDyn.setChunkDim(chunkDim);
                edgeDyn.setMaxSenderThreads(getMaxSenderThreads());
                edgeDyn.setMaxSenderThreads(maxSenderThreads);
                edgeDyn.setComm(comm);
                fullyInitialized = true;
                logger.debug("qui");
            }
            logger.debug("qui");
            //logger.debug(PelletOptions.USE_TRACING);
            results = edgeDyn.computeLearning();
            //logger.debug(PelletOptions.USE_TRACING);
//            logger.info("Log-Likelihood " + results.getLL());
//            Map<String, Long> timers = results.getTimers();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new ParameterLearningException(ex);
        }
        isRunning = false;
    }

    /**
     * @return the chunkDim
     */
    public int getChunkDim() {
        return chunkDim;
    }

    /**
     * @param chunkDim the chunkDim to set
     */
    public void setChunkDim(int chunkDim) {
        this.chunkDim = chunkDim;
    }

    /**
     * @return the maxSenderThreads
     */
    public int getMaxSenderThreads() {
        return maxSenderThreads;
    }

    /**
     * @param maxSenderThreads the maxSenderThreads to set
     */
    public void setMaxSenderThreads(int maxSenderThreads) {
        this.maxSenderThreads = maxSenderThreads;
    }

//    @Override
//    public void reset() {
//        super.reset();
//        isRunning = false;
//        stop = false;
//    }

    @Override
    public BigDecimal getParameter(OWLAxiom ax) {
        BigDecimal parameter = super.getParameter(ax);
        if (parameter == null) {
            String msg = "the given axiom: " + ax.getAxiomWithoutAnnotations() + " is not probabilistic or does not exist";
            logger.warn(msg);
        }
        return parameter;
    }
}
