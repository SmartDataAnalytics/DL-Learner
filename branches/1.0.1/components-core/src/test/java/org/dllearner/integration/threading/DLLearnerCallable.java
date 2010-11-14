package org.dllearner.integration.threading;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.*;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.KBFile;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.OWLAPIAxiomConvertVisitor;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 13, 2010
 * Time: 11:00:24 AM
 * <p/>
 * Callable class that returns an EvaluatedDescription
 */
public class DLLearnerCallable implements Callable<EvaluatedDescription> {


    protected static Logger logger = Logger.getLogger(DLLearnerCallable.class);

    @Override
    public EvaluatedDescription call() throws Exception {

        /** Create the Knowledge Sources*/
        Set<KnowledgeSource> sources = createKnowledgeSources();

        /** Configure the Reasoner */
        ReasonerComponent reasonerComponent = createReasoner(sources);

        /** Configure the Learning Problem */
        LearningProblem learningProblem = createLearningProblem(reasonerComponent);

        /** Configure the Learning Algorithm */
        LearningAlgorithm learningAlgorithm = createLearningAlgorithm(reasonerComponent, learningProblem);

        learningAlgorithm.start();
        printConclusions(reasonerComponent, 1000);

        EvaluatedDescriptionPosNeg evaluatedDescription = (EvaluatedDescriptionPosNeg) learningAlgorithm.getCurrentlyBestEvaluatedDescription();
        return evaluatedDescription;
    }


    /**
     * Create the Knowledge Sources
     * <p/>
     * These are just things like KBFile, OWLFile, etc.
     *
     * @return
     * @throws java.net.MalformedURLException
     * @throws ComponentInitException
     */
    protected Set<KnowledgeSource> createKnowledgeSources() throws MalformedURLException, ComponentInitException {
//        String baseDir = "examples/family";
        Resource kbResource = new ClassPathResource("/org/dllearner/integration/father.kb");
        /** Knowledge Base File Name*/
//        String knowledgeBaseFileName = baseDir + "/father.kb";
        File knowledgeBaseFile = null;
        try {
            knowledgeBaseFile = kbResource.getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Set<KnowledgeSource> sources = new HashSet<KnowledgeSource>();

        KBFile kbFile = new KBFile();
        kbFile.setURL((knowledgeBaseFile.toURI().toURL()));

        kbFile.init();
        sources.add(kbFile);
        return sources;
    }

    protected LearningAlgorithm createLearningAlgorithm(ReasonerComponent reasonerComponent, LearningProblem learningProblem) throws LearningProblemUnsupportedException, ComponentInitException {
        OCEL learningAlgorithm = new OCEL((PosNegLP) learningProblem, reasonerComponent);
        learningAlgorithm.setLogLevel("DEBUG");
        learningAlgorithm.init();
        return learningAlgorithm;
    }

    protected LearningProblem createLearningProblem(ReasonerComponent reasonerComponent) throws ComponentInitException {
        PosNegLPStandard learningProblem = new PosNegLPStandard(reasonerComponent, createPositiveExamples(), createNegativeExamples());
        learningProblem.setUseApproximations(false);
        learningProblem.setAccuracyMethod("predacc");
        learningProblem.setApproxAccuracy(.05);
        learningProblem.init();
        return learningProblem;
    }

    /**
     * Create the Reasoner that will be used.
     *
     * @param sources
     * @return
     * @throws ComponentInitException
     */
    protected ReasonerComponent createReasoner(Set<KnowledgeSource> sources) throws ComponentInitException {

        /** Create the OWL Data Factory */
        OWLDataFactory dataFactory = new OWLDataFactoryImpl();
        /** Create the OWL Ontology Manager */
        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager(dataFactory);

        OWLAPIDescriptionConvertVisitor descriptionConvertVisitor = new OWLAPIDescriptionConvertVisitor();
        descriptionConvertVisitor.setFactory(dataFactory);

        OWLAPIAxiomConvertVisitor axiomConvertVisitor = new OWLAPIAxiomConvertVisitor();
        axiomConvertVisitor.setOwlAPIDescriptionConvertVisitor(descriptionConvertVisitor);

        OWLAPIConverter converter = new OWLAPIConverter();
        converter.setDataFactory(dataFactory);
        converter.setOwlAPIAxiomConvertVisitor(axiomConvertVisitor);
        converter.setOwlAPIDescriptionConvertVisitor(descriptionConvertVisitor);

        OWLAPIReasoner coreReasoner = new OWLAPIReasoner(sources);
        coreReasoner.setManager(ontologyManager);
        coreReasoner.setOwlAPIAxiomConvertVisitor(axiomConvertVisitor);
        coreReasoner.setOwlAPIDescriptionConvertVisitor(descriptionConvertVisitor);
        coreReasoner.setOWLAPIConverter(converter);

        coreReasoner.setConfigReasonerType("pellet");
//        coreReasoner.setOntologyResources(getOntologyResources());
        coreReasoner.init();

        FastInstanceChecker reasonerComponent = new FastInstanceChecker(sources);
        /** This is the underlying reasoner */
        reasonerComponent.setReasonerComponent(coreReasoner);
        reasonerComponent.init();

        return reasonerComponent;
    }

    /**
     * Create the Positive Examples - these must be fully qualified URIs.
     *
     * @return The Positive Examples.
     */
    protected SortedSet<Individual> createPositiveExamples() {
        SortedSet<Individual> result = new TreeSet<Individual>();
        result.add(new Individual("http://localhost/foo#stefan"));
        result.add(new Individual("http://localhost/foo#markus"));
        result.add(new Individual("http://localhost/foo#bernd"));
        return result;
    }

    /**
     * Create the Negative Examples - these must be fully qualified URIs.
     *
     * @return The Negative Examples.
     */
    protected SortedSet<Individual> createNegativeExamples() {
        SortedSet<Individual> result = new TreeSet<Individual>();
        result.add(new Individual("http://localhost/foo#heinz"));
        result.add(new Individual("http://localhost/foo#anna"));
        result.add(new Individual("http://localhost/foo#gabi"));
        result.add(new Individual("http://localhost/foo#michelle"));
        return result;
    }

    /**
     * Copied from org.dllearner.cli.Start
     *
     * @param rs
     * @param algorithmDuration
     */
    protected static void printConclusions(ReasonerComponent rs, long algorithmDuration) {
        if (rs.getNrOfRetrievals() > 0) {
            logger.info("number of retrievals: " + rs.getNrOfRetrievals());
            logger.info("retrieval reasoning time: "
                    + Helper.prettyPrintNanoSeconds(rs.getRetrievalReasoningTimeNs())
                    + " ( " + Helper.prettyPrintNanoSeconds(rs.getTimePerRetrievalNs())
                    + " per retrieval)");
        }
        if (rs.getNrOfInstanceChecks() > 0) {
            logger.info("number of instance checks: " + rs.getNrOfInstanceChecks() + " ("
                    + rs.getNrOfMultiInstanceChecks() + " multiple)");
            logger.info("instance check reasoning time: "
                    + Helper.prettyPrintNanoSeconds(rs.getInstanceCheckReasoningTimeNs()) + " ( "
                    + Helper.prettyPrintNanoSeconds(rs.getTimePerInstanceCheckNs())
                    + " per instance check)");
        }
        if (rs.getNrOfSubsumptionHierarchyQueries() > 0) {
            logger.info("subsumption hierarchy queries: "
                    + rs.getNrOfSubsumptionHierarchyQueries());
            /*
                * System.out.println("subsumption hierarchy reasoning time: " +
                * Helper.prettyPrintNanoSeconds(rs
                * .getSubsumptionHierarchyTimeNs()) + " ( " +
                * Helper.prettyPrintNanoSeconds(rs
                * .getTimePerSubsumptionHierarchyQueryNs()) + " per subsumption
                * hierachy query)");
                */
        }
        if (rs.getNrOfSubsumptionChecks() > 0) {
            logger.info("(complex) subsumption checks: " + rs.getNrOfSubsumptionChecks()
                    + " (" + rs.getNrOfMultiSubsumptionChecks() + " multiple)");
            logger.info("subsumption reasoning time: "
                    + Helper.prettyPrintNanoSeconds(rs.getSubsumptionReasoningTimeNs()) + " ( "
                    + Helper.prettyPrintNanoSeconds(rs.getTimePerSubsumptionCheckNs())
                    + " per subsumption check)");
        }
        DecimalFormat df = new DecimalFormat();
        double reasoningPercentage = 100 * rs.getOverallReasoningTimeNs()
                / (double) algorithmDuration;
        logger.info("overall reasoning time: "
                + Helper.prettyPrintNanoSeconds(rs.getOverallReasoningTimeNs()) + " ("
                + df.format(reasoningPercentage) + "% of overall runtime)");
        logger.info("overall algorithm runtime: "
                + Helper.prettyPrintNanoSeconds(algorithmDuration));
    }
}
