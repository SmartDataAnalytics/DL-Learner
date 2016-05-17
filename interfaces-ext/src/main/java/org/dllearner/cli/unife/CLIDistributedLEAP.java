/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.cli.unife;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mpi.MPI;
import mpi.MPIException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.cli.CLI;
import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.spring.ApplicationContextBuilder;
import org.dllearner.configuration.spring.DefaultApplicationContextBuilder;
import org.dllearner.confparser.ConfParserConfiguration;
import org.dllearner.confparser.ParseException;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.probabilistic.unife.AbstractPSLA;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import unife.bundle.logging.BundleLoggerFactory;
import unife.edge.mpi.MPIUtilities;
import org.dllearner.core.probabilistic.unife.ProbabilisticStructureLearningAlgorithm;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
public class CLIDistributedLEAP extends CLI {

    private IConfiguration configuration;
    private ProbabilisticStructureLearningAlgorithm algorithm;
    private AbstractPSLA psla;
    // TO DO: creare e gestire i gruppi MPI ed il ranking

    @Override
    public void init() throws IOException {
        super.init();
        psla = getContext().getBean(AbstractPSLA.class);
    }

    @Override
    public void run(){
        System.out.println("CLI Distributed LEAP");
        Logger logger = Logger.getLogger(CLIDistributedLEAP.class.getName(), new BundleLoggerFactory());
        logger.info("CLI Distributed LEAP");
        try {
            org.apache.log4j.Logger.getLogger("org.dllearner").setLevel(Level.toLevel(getLogLevel().toUpperCase()));
        } catch (Exception e) {
            logger.warn("Error setting log level to " + getLogLevel());
        }

//        init();
        boolean master = MPIUtilities.isMaster(MPI.COMM_WORLD);
//        try {
//            master = MPIUtilities.isMaster(MPI.COMM_WORLD);
//        } catch (MPIException mpiEx) {
//            String msg = "Unable to estabilish if the current process is master: " 
//                    + mpiEx.getMessage();
//            logger.error(msg);
//            throw new RuntimeException(msg);
//        }


        if (isPerformCrossValidation()) {
//            PosNegLP lp = context.getBean(PosNegLP.class);
//            if (la instanceof QTL2) {
//                new SPARQLCrossValidation((QTL2) la, lp, rs, nrOfFolds, false);
//            } else {
            try {
                new LEAPCrossValidation(psla, getNrOfFolds(), false, true);
            } catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
                logger.error(e.getMessage(), e);
                e.printStackTrace();
            }
//            }
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            for (Map.Entry<String, ProbabilisticStructureLearningAlgorithm> entry : getContext().getBeansOfType(ProbabilisticStructureLearningAlgorithm.class).entrySet()) {
                algorithm = entry.getValue();
                logger.info("Running algorithm instance \"" + entry.getKey() + "\" (" + algorithm.getClass().getSimpleName() + ")");
                algorithm.start();
            }
        }

    }

    public static void main(String[] args) throws ParseException, IOException,
            ReasoningMethodUnsupportedException {

        System.out.println("DL-Learner command line interface");

        // currently, CLI has exactly one parameter - the conf file
        if (args.length == 0) {
            System.out.println("You need to give a conf file as argument.");
            System.exit(0);
        }

        // read file and print and print a message if it does not exist
        File file = new File(args[args.length - 1]);
        if (!file.exists()) {
            System.out.println("File \"" + file + "\" does not exist.");
            System.exit(0);
        }

        // MPI Initialization
        try {
            args = MPI.Init(args);
        } catch (MPIException mpiEx) {
            String msg = "Impossible to initialize MPI: " + mpiEx.getMessage();
            System.err.print(msg);
        }
        // Setting output file for logging
        String slaveIdLog = System.getProperty("slaveId");
        if (slaveIdLog != null) {
            throw new RuntimeException("slaveId property already defined somewhere else.");
        }

        // set the index for the log file: log/leap${slaveId}.log
        MPIUtilities.createLogFile("slaveId");

        Logger logger = Logger.getLogger(CLIDistributedLEAP.class.getName(), new BundleLoggerFactory());

        Resource confFile = new FileSystemResource(file);

        List<Resource> springConfigResources = new ArrayList<Resource>();
        logger.debug("Spring!");
        try {
            //DL-Learner Configuration Object
            IConfiguration configuration = new ConfParserConfiguration(confFile);

            ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();
            ApplicationContext context = builder.buildApplicationContext(configuration, springConfigResources);

            // TODO: later we could check which command line interface is specified in the conf file
            // for now we just use the default one
            CLI cli;
            if (context.containsBean("cli")) {
                cli = (CLI) context.getBean("cli");
            } else {
                cli = new CLI();
            }
            logger.debug("setting context");
            cli.setContext(context);
            logger.debug("context set");
            logger.debug("setting conf file");
            cli.setConfFile(file);
            logger.debug("conf file set");
            logger.debug("Start Running");
            cli.run();
        } catch (Exception e) {
            e.printStackTrace();
            String stacktraceFileName = "log/error.log";

//            e.printStackTrace();
            //Find the primary cause of the exception.
            Throwable primaryCause = findPrimaryCause(e);

            // Get the Root Error Message
            logger.error("An Error Has Occurred During Processing.");
//            logger.error(primaryCause.getMessage());
            logger.debug("Stack Trace: ", e);
            logger.error("Terminating DL-Learner...and writing stacktrace to: " + stacktraceFileName);
            FileOutputStream fos = new FileOutputStream(stacktraceFileName);
            PrintStream ps = new PrintStream(fos);
            e.printStackTrace(ps);
        } finally {
            try {
                MPI.Finalize();
            } catch (MPIException mpiEx) {
                String msg = "Cannot finalize MPI";
                System.err.print(msg);
                logger.error(msg);
            }
        }
    }

    /**
     * Find the primary cause of the specified exception.
     *
     * @param e The exception to analyze
     * @return The primary cause of the exception.
     */
    protected static Throwable findPrimaryCause(Exception e) {
        // The throwables from the stack of the exception
        Throwable[] throwables = ExceptionUtils.getThrowables(e);

        //Look For a Component Init Exception and use that as the primary cause of failure, if we find it
        int componentInitExceptionIndex = ExceptionUtils.indexOfThrowable(e, ComponentInitException.class);

        Throwable primaryCause;
        if (componentInitExceptionIndex > -1) {
            primaryCause = throwables[componentInitExceptionIndex];
        } else {
            //No Component Init Exception on the Stack Trace, so we'll use the root as the primary cause.
            primaryCause = ExceptionUtils.getRootCause(e);
        }
        return primaryCause;
    }

}
