package org.dllearner.integration;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.*;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.KBFile;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.Helper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 13, 2010
 * Time: 8:41:12 AM
 *
 * This test shows how to run the DL-Learner Code programmtically in a basic way.  We'll
 * also use it to test a series of basic functions - to ensure we don't break anything as we're developing.
 *
 */
public class SingleThreadedDLLearnerTest {

    private static Logger logger = Logger.getLogger(SingleThreadedDLLearnerTest.class);

    /**
     * Default Constructor
     */
    public SingleThreadedDLLearnerTest() {

    }


    /**
     * Run the test all the way through.
     */
    @Test
    public void runTest() throws Exception {

        SingleThreadedDLLearnerTest example = new SingleThreadedDLLearnerTest();
        example.learn();

        EvaluatedDescriptionPosNeg eDPN = example.learn();

        Set<Individual> coveredPositives = eDPN.getCoveredPositives();
        Set<Individual> notCoveredPositives = eDPN.getNotCoveredPositives();

        Set<Individual> coveredNegatives = eDPN.getCoveredNegatives();
        Set<Individual> notCoveredNegatives = eDPN.getNotCoveredNegatives();

        assert coveredPositives.size() == 3;
        assert notCoveredPositives.isEmpty();

        assert coveredNegatives.isEmpty();
        assert notCoveredNegatives.size() == 4;


    }


    /**
     * Do the learning
     * @throws Exception
     */
    public EvaluatedDescriptionPosNeg learn() throws Exception {

        /** Component Manager */
        ComponentManager cm = ComponentManager.getInstance();

        /** Create the Knowledge Sources*/
        Set<KnowledgeSource> sources = createKnowledgeSources(cm);

        /** Configure the Reasoner */
        ReasonerComponent reasonerComponent = createReasoner(cm, sources);

        /** Configure the Learning Problem */
        LearningProblem learningProblem = createLearningProblem(cm, reasonerComponent);

        /** Configure the Learning Algorithm */
        LearningAlgorithm learningAlgorithm = createLearningAlgorithm(cm, reasonerComponent, learningProblem);

        learningAlgorithm.start();


        printConclusions(reasonerComponent, 1000);

        EvaluatedDescriptionPosNeg evaluatedDescription = (EvaluatedDescriptionPosNeg)learningAlgorithm.getCurrentlyBestEvaluatedDescription();
        return evaluatedDescription;

    }

    /**
     * Create the Knowledge Sources
     *
     * These are just things like KBFile, OWLFile, etc.
     *
     * @param cm
     * @return
     * @throws java.net.MalformedURLException
     * @throws ComponentInitException
     */
    private Set<KnowledgeSource> createKnowledgeSources(ComponentManager cm) throws MalformedURLException, ComponentInitException {
//        String baseDir = "examples/family";
        Resource kbResource = new ClassPathResource("/org/dllearner/integration/father.kb");
        /** Knowledge Base File Name*/
//        String knowledgeBaseFileName = baseDir + "/father.kb";
        File knowledgeBaseFile = null;
        try {
            knowledgeBaseFile = kbResource.getFile();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Set<KnowledgeSource> sources = new HashSet<KnowledgeSource>();

        /** This gets the imported files (if any) that are listed in the configuration file */
        Map<URL, Class<? extends KnowledgeSource>> importedFiles = new HashMap<URL, Class<? extends KnowledgeSource>>();

        /** Import the Knowledge Base File */
        importedFiles.put(knowledgeBaseFile.toURI().toURL(), KBFile.class);

        for (Map.Entry<URL, Class<? extends KnowledgeSource>> entry : importedFiles.entrySet()) {
            KnowledgeSource ks = cm.knowledgeSource(entry.getValue());

            cm.applyConfigEntry(ks, "url", entry.getKey());
            sources.add(ks);

            /** Not providing any configuration options */
            /** Initialize component */
            ks.init();
        }
        return sources;
    }

    private LearningAlgorithm createLearningAlgorithm(ComponentManager cm, ReasonerComponent reasonerComponent, LearningProblem learningProblem) throws LearningProblemUnsupportedException, ComponentInitException {
        Class<? extends LearningAlgorithm> laClass = OCEL.class;
        LearningAlgorithm learningAlgorithm = cm.learningAlgorithm(laClass, learningProblem, reasonerComponent);
        learningAlgorithm.init();
        return learningAlgorithm;
    }

    private LearningProblem createLearningProblem(ComponentManager cm, ReasonerComponent reasonerComponent) throws ComponentInitException {
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
     * @param cm
     * @param sources
     * @return
     * @throws ComponentInitException
     */
    private ReasonerComponent createReasoner(ComponentManager cm, Set<KnowledgeSource> sources) throws ComponentInitException {
        OWLAPIReasoner coreReasoner = new OWLAPIReasoner(sources);
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
    private SortedSet<Individual> createPositiveExamples() {
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
    private SortedSet<Individual> createNegativeExamples() {
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
    private static void printConclusions(ReasonerComponent rs, long algorithmDuration) {
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
