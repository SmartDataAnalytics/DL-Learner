package org.dllearner.cli.unife;

import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.cli.CLI;
import org.dllearner.configuration.IConfiguration;
//import org.dllearner.configuration.util.SpringConfigurationXMLBeanConverter;
import org.dllearner.confparser.ParseException;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.probabilistic.unife.AbstractPSLA;
import org.dllearner.core.probabilistic.unife.ProbabilisticStructureLearningAlgorithm;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import unife.bundle.logging.BundleLoggerFactory;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
public class CLILEAP extends CLI {

    private IConfiguration configuration;
    private AbstractPSLA psla;

    @Override
    public void init() throws IOException {
        super.init();
        psla = getContext().getBean(AbstractPSLA.class);
    }

    @Override
    public void run(){
        System.out.println("CLI LEAP");
        Logger logger = Logger.getLogger(CLILEAP.class.getName(), new BundleLoggerFactory());
        logger.info("CLI LEAP");
        try {
            org.apache.log4j.Logger.getLogger("org.dllearner").setLevel(Level.toLevel(getLogLevel().toUpperCase()));
        } catch (Exception e) {
            logger.warn("Error setting log level to " + getLogLevel());
        }

//        init();

        if (isPerformCrossValidation()) {
//            PosNegLP lp = context.getBean(PosNegLP.class);
//            if (la instanceof QTL2) {
//                new SPARQLCrossValidation((QTL2) la, lp, rs, nrOfFolds, false);
//            } else {
            try {
                new LEAPCrossValidation(psla, getNrOfFolds(), false, false);
            } catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
                logger.error(e.getMessage(), e);
                e.printStackTrace();
            }
//            }
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            ProbabilisticStructureLearningAlgorithm algorithm;
            for (Map.Entry<String, ProbabilisticStructureLearningAlgorithm> entry : getContext().getBeansOfType(ProbabilisticStructureLearningAlgorithm.class).entrySet()) {
                algorithm = entry.getValue();
                logger.info("Running algorithm instance \"" + entry.getKey() + "\" (" + algorithm.getClass().getSimpleName() + ")");
                algorithm.start();
            }
        }

    }

    public static void main(String[] args) throws ParseException, IOException,
            ReasoningMethodUnsupportedException {
        CLI.main(args);
    }
}
