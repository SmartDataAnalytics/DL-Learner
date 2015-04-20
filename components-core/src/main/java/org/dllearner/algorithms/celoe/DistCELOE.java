/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.algorithms.celoe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import mpi.MPI;
import mpi.Request;
import mpi.Status;

import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractHeuristic;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.refinementoperators.CustomHierarchyRefinementOperator;
import org.dllearner.refinementoperators.CustomStartRefinementOperator;
import org.dllearner.refinementoperators.LengthLimitedRefinementOperator;
import org.dllearner.refinementoperators.OperatorInverter;
import org.dllearner.refinementoperators.ReasoningBasedRefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.mpi.MPIConfig;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionMinimizer;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.dllearner.utilities.owl.PropertyContext;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNaryBooleanClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * The CELOE (Class Expression Learner for Ontology Engineering) algorithm.
 * It adapts and extends the standard supervised learning algorithm for the
 * ontology engineering use case.
 *
 * @author Jens Lehmann
 * @author Patrick Westphal
 *
 */
@ComponentAnn(name="Distributed CELOE", shortName="distceloe", version=0.1,
    description="CELOE is an adapted and extended version of the OCEL " +
            "algorithm applied for the ontology engineering use case. See " +
            "http://jens-lehmann.org/files/2011/celoe.pdf for reference.")
public class DistCELOE extends AbstractCELA implements Cloneable{

    private MPIConfig mpiConf;
    private int bufferSize = 50000;

    private static Logger logger = LoggerFactory.getLogger(DistCELOE.class);

    private boolean isRunning = false;
    private boolean stop = false;

    private LengthLimitedRefinementOperator operator;
    private OWLClassExpressionMinimizer minimizer;

    @ConfigOption(name="useMinimizer", defaultValue="true",
            description="Specifies whether returned expressions should be " +
                    "minimised by removing those parts, which are not " +
                    "needed. (Basically the minimiser tries to find the " +
                    "shortest expression which is equivalent to the learned " +
                    "expression). Turning this feature off may improve " +
                    "performance.")
    private boolean useMinimizer = true;

    /** all nodes in the search tree (used for selecting most promising node) */
    private TreeSet<OENode> nodes;
    private AbstractHeuristic heuristic;
    // root of search tree
    private OENode startNode;

    /** the class with which we start the refinement process */
    @ConfigOption(name="startClass", defaultValue="owl:Thing",
            description="You can specify a start class for the algorithm. " +
                    "To do this, you have to use Manchester OWL syntax " +
                    "without using prefixes.")
    private OWLClassExpression startClass;

    /** all descriptions in the search tree plus those which were too weak
     * (for fast redundancy check) */
    private TreeSet<OWLClassExpression> descriptions;

    @ConfigOption(name="singleSuggestionMode", defaultValue="false",
            description="Use this if you are interested in only one " +
                    "suggestion and your learning problem has many (more " +
                    "than 1000) examples.")
    private boolean singleSuggestionMode;

    private OWLClassExpression bestDescription;
    private double bestAccuracy = Double.MIN_VALUE;

    private OWLClass classToDescribe;

    /**
     * examples are either
     * 1.) instances of the class to describe
     * 2.) positive examples
     * 3.) union of pos.+neg. examples depending on the learning problem at hand
     */
    private Set<OWLIndividual> examples;

    /** CELOE was originally created for learning classes in ontologies, but
     * also works for other learning problem types */
    private boolean isClassLearningProblem;
    private boolean isEquivalenceProblem;

    // ----- important parameters (non-config options but internal) -----

    private double noise;
    private boolean filterFollowsFromKB = false;

    // -----less important parameters -----

    /** forces that one solution cannot be subexpression of another expression;
     * this option is useful to get diversity but it can also suppress quite
     * useful expressions */
    private boolean forceMutualDifference = false;

    // ----- utility variables -----

    // statistical variables
    private int expressionTests = 0;
    private int minHorizExp = 0;
    private int maxHorizExp = 0;
    private long totalRuntimeNs = 0;

    // TODO: turn those into config options
    // important: do not initialise those with empty sets
    // null = no settings for allowance / ignorance
    // empty set = allow / ignore nothing (it is often not desired to allow no class!)
    Set<OWLClass> allowedConcepts = null;
    Set<OWLClass> ignoredConcepts = null;

    @ConfigOption(name="writeSearchTree", defaultValue="false",
            description="specifies whether to write a search tree")
    private boolean writeSearchTree = false;

    @ConfigOption(name="searchTreeFile", defaultValue="log/searchTree.txt",
            description="file to use for the search tree")
    private String searchTreeFile = "log/searchTree.txt";

    @ConfigOption(name="replaceSearchTree", defaultValue="false",
            description="specifies whether to replace the search tree in the " +
                    "log file after each run or append the new search tree")
    private boolean replaceSearchTree = false;

    @ConfigOption(name="maxNrOfResults", defaultValue="10",
            description="Sets the maximum number of results one is " +
                    "interested in. (Setting this to a lower value may " +
                    "increase performance as the learning algorithm has to " +
                    "store/evaluate/beautify less descriptions).")
    private int maxNrOfResults = 10;

    @ConfigOption(name="noisePercentage", defaultValue="0.0",
            description="the (approximated) percentage of noise within the " +
                    "examples")
    private double noisePercentage = 0.0;

    @ConfigOption(name="filterDescriptionsFollowingFromKB",
            defaultValue="false", description="If true, then the results " +
                    "will not contain suggestions, which already follow " +
                    "logically from the knowledge base. Be careful, since " +
                    "this requires a potentially expensive consistency check " +
                    "for candidate solutions.")
    private boolean filterDescriptionsFollowingFromKB = false;

    @ConfigOption(name="reuseExistingDescription", defaultValue="false",
            description="If true, the algorithm tries to find a good " +
                    "starting point close to an existing definition/super " +
                    "class of the given class in the knowledge base.")
    private boolean reuseExistingDescription = false;

    @ConfigOption(name="maxClassExpressionTests", defaultValue="0",
            description="The maximum number of candidate hypothesis the " +
                    "algorithm is allowed to test (0 = no limit). The " +
                    "algorithm will stop afterwards. (The real number of " +
                    "tests can be slightly higher, because this criterion " +
                    "usually won't be checked after each single test.)")
    private int maxClassExpressionTests = 0;

// FIXME: currently not supported
//    @ConfigOption(name="maxClassExpressionTestsAfterImprovement",
//            defaultValue="0", description="The maximum number of candidate " +
//                    "hypothesis the algorithm is allowed after an " +
//                    "improvement in accuracy (0 = no limit). The algorithm " +
//                    "will stop afterwards. (The real number of tests can be " +
//                    "slightly higher, because this criterion usually won't " +
//                    "be checked after each single test.)")
//    private int maxClassExpressionTestsAfterImprovement = 0;

    @ConfigOption(defaultValue="10", name="maxExecutionTimeInSeconds",
            description="maximum execution of the algorithm in seconds")
    private int maxExecutionTimeInSeconds = 10;

    @ConfigOption(defaultValue="0",
            name="maxExecutionTimeInSecondsAfterImprovement",
            description="maximum execution of the algorithm in seconds")
    private int maxExecutionTimeInSecondsAfterImprovement = 0;

    @ConfigOption(name="terminateOnNoiseReached", defaultValue="false",
            description="specifies whether to terminate when noise " +
                    "criterion is met")
    private boolean terminateOnNoiseReached = false;

    @ConfigOption(name="maxDepth", defaultValue="7",
            description="maximum depth of description")
    private double maxDepth = 7;

    @ConfigOption(name="stopOnFirstDefinition", defaultValue="false",
            description="algorithm will terminate immediately when a correct " +
                    "definition is found")
    private boolean stopOnFirstDefinition = false;

    private int expressionTestCountLastImprovement;

    @SuppressWarnings("unused")
    private long timeLastImprovement = 0;
    private boolean expandAccuracy100Nodes = false;

    public DistCELOE() { }

    public DistCELOE(DistCELOE celoe){
        setReasoner(celoe.reasoner);
        setLearningProblem(celoe.learningProblem);
        setAllowedConcepts(celoe.getAllowedConcepts());
        setExpandAccuracy100Nodes(celoe.expandAccuracy100Nodes);
        setFilterDescriptionsFollowingFromKB(celoe.filterDescriptionsFollowingFromKB);
        setHeuristic(celoe.heuristic);
        setIgnoredConcepts(celoe.ignoredConcepts);
        setLearningProblem(celoe.learningProblem);
        setMaxClassExpressionTests(celoe.maxClassExpressionTests);
        // FIXME: currently not supported
//        setMaxClassExpressionTestsAfterImprovement(
//                celoe.maxClassExpressionTestsAfterImprovement);
        setMaxDepth(celoe.maxDepth);
        setMaxExecutionTimeInSeconds(celoe.maxExecutionTimeInSeconds);
        setMaxExecutionTimeInSecondsAfterImprovement(
                celoe.maxExecutionTimeInSecondsAfterImprovement);
        setMaxNrOfResults(celoe.maxNrOfResults);
        setNoisePercentage(celoe.noisePercentage);

        RhoDRDown op = new RhoDRDown((RhoDRDown)celoe.operator);
        try {
            op.init();
        } catch (ComponentInitException e) {
            e.printStackTrace();
        }
        setOperator(op);

        setReplaceSearchTree(celoe.replaceSearchTree);
        setReuseExistingDescription(celoe.reuseExistingDescription);
        setSingleSuggestionMode(celoe.singleSuggestionMode);
        setStartClass(celoe.startClass);
        setStopOnFirstDefinition(celoe.stopOnFirstDefinition);
        setTerminateOnNoiseReached(celoe.terminateOnNoiseReached);
        setUseMinimizer(celoe.useMinimizer);
        setWriteSearchTree(celoe.writeSearchTree);
    }

    public DistCELOE(AbstractLearningProblem problem, AbstractReasonerComponent reasoner) {
        super(problem, reasoner);
    }

    public static Collection<Class<? extends AbstractLearningProblem>> supportedLearningProblems() {

        Collection<Class<? extends AbstractLearningProblem>> problems =
                new LinkedList<Class<? extends AbstractLearningProblem>>();

        problems.add(AbstractLearningProblem.class);
        return problems;
    }

    public static String getName() {
        return "CELOE";
    }

    @Override
    public void init() throws ComponentInitException {
        baseURI = reasoner.getBaseURI();
        prefixes = reasoner.getPrefixes();

        if(maxExecutionTimeInSeconds != 0 &&
                maxExecutionTimeInSecondsAfterImprovement != 0) {

            maxExecutionTimeInSeconds = Math.min(maxExecutionTimeInSeconds,
                    maxExecutionTimeInSecondsAfterImprovement);
        }

        // compute used concepts/roles from allowed/ignored concepts/roles
        Set<OWLClass> usedConcepts;
        if(allowedConcepts != null) {
            // sanity check to control if no non-existing concepts are in the list
         // FIXME: the following statement does not have any effect (AFAIU)
            Helper.checkConcepts(reasoner, allowedConcepts);
            usedConcepts = allowedConcepts;

        } else if(ignoredConcepts != null) {
            usedConcepts = Helper.computeConceptsUsingIgnoreList(reasoner, ignoredConcepts);

        } else {
            usedConcepts = Helper.computeConcepts(reasoner);
        }

        // copy class hierarchy and modify it such that each class is only
        // reachable via a single path
        ClassHierarchy classHierarchy =
                (ClassHierarchy) reasoner.getClassHierarchy().cloneAndRestrict(
                        new HashSet<OWLClassExpression>(usedConcepts));
        classHierarchy.thinOutSubsumptionHierarchy();

        // if no one injected a heuristic, we use a default one
        if(heuristic == null) {
            heuristic = new OEHeuristicRuntime();
        }

        minimizer = new OWLClassExpressionMinimizer(dataFactory, reasoner);

        // start at owl:Thing by default
        if(startClass == null) {
            startClass = dataFactory.getOWLThing();
        }

        // create a refinement operator and pass all configuration
        // variables to it
        if(operator == null) {
            // we use a default operator and inject the class hierarchy for now
            operator = new RhoDRDown();
            if(operator instanceof CustomStartRefinementOperator) {
                ((CustomStartRefinementOperator)operator).setStartClass(startClass);
            }
            if(operator instanceof ReasoningBasedRefinementOperator) {
                ((ReasoningBasedRefinementOperator)operator).setReasoner(reasoner);
            }
            operator.init();
        }
        if(operator instanceof CustomHierarchyRefinementOperator) {
            ((CustomHierarchyRefinementOperator)operator).setClassHierarchy(
                    classHierarchy);

            ((CustomHierarchyRefinementOperator)operator).setObjectPropertyHierarchy(
                    reasoner.getObjectPropertyHierarchy());

            ((CustomHierarchyRefinementOperator)operator).setDataPropertyHierarchy(
                    reasoner.getDatatypePropertyHierarchy());
        }

        if(writeSearchTree) {
            File f = new File(searchTreeFile );
            if(f.getParentFile() != null){
                f.getParentFile().mkdirs();
            }
            Files.clearFile(f);
        }

        bestEvaluatedDescriptions = new EvaluatedDescriptionSet(maxNrOfResults);
        isClassLearningProblem = (learningProblem instanceof ClassLearningProblem);

        // we put important parameters in class variables
        noise = noisePercentage/100d;
        // filterFollowsFromKB is automatically set to false if the problem
        // is not a class learning problem
        filterFollowsFromKB = filterDescriptionsFollowingFromKB && isClassLearningProblem;

        // actions specific to ontology engineering
        if(isClassLearningProblem) {
            ClassLearningProblem problem = (ClassLearningProblem) learningProblem;
            classToDescribe = problem.getClassToDescribe();
            isEquivalenceProblem = problem.isEquivalenceProblem();

            examples = reasoner.getIndividuals(classToDescribe);

            /* start class: intersection of super classes for definitions
             * (since it needs to capture all instances), but owl:Thing for
             * learning subclasses (since it is superfluous to add super
             * classes in this case) */
            if(isEquivalenceProblem) {
                Set<OWLClassExpression> existingDefinitions =
                        reasoner.getAssertedDefinitions(classToDescribe);

                if(reuseExistingDescription && (existingDefinitions.size() > 0)) {
                    /* the existing definition is reused, which in the simplest
                     * case means to use it as a start class or, if it is
                     * already too specific, generalise it */

                    // pick the longest existing definition as candidate
                    OWLClassExpression existingDefinition = null;
                    int highestLength = 0;

                    for(OWLClassExpression exDef : existingDefinitions) {
                        if(OWLClassExpressionUtils.getLength(exDef) > highestLength) {
                            existingDefinition = exDef;
                            highestLength = OWLClassExpressionUtils.getLength(exDef);
                        }
                    }

                    LinkedList<OWLClassExpression> startClassCandidates =
                            new LinkedList<OWLClassExpression>();

                    startClassCandidates.add(existingDefinition);

                    // hack for RhoDRDown
                    if(operator instanceof RhoDRDown) {
                        ((RhoDRDown)operator).setDropDisjuncts(true);
                    }

                    LengthLimitedRefinementOperator upwardOperator =
                            new OperatorInverter(operator);

                    // use upward refinement until we find an appropriate start class
                    boolean startClassFound = false;
                    OWLClassExpression candidate;

                    do {
                        candidate = startClassCandidates.pollFirst();

                        if(((ClassLearningProblem)learningProblem).getRecall(candidate)<1.0) {
                            // add upward refinements to list
                            Set<OWLClassExpression> refinements =
                                    upwardOperator.refine(candidate,
                                            OWLClassExpressionUtils.getLength(candidate));

                            LinkedList<OWLClassExpression> refinementList =
                                    new LinkedList<OWLClassExpression>(refinements);

                            startClassCandidates.addAll(refinementList);

                        } else {
                            startClassFound = true;
                        }
                    } while(!startClassFound);

                    startClass = candidate;

                    if(startClass.equals(existingDefinition)) {
                        logger.info("Reusing existing OWLClassExpression " +
                                OWLAPIRenderers.toManchesterOWLSyntax(startClass) +
                                " as start class for learning algorithm.");
                    } else {
                        logger.info("Generalised existing OWLClassExpression " +
                                OWLAPIRenderers.toManchesterOWLSyntax(existingDefinition) +
                                " to " +
                                OWLAPIRenderers.toManchesterOWLSyntax(startClass) +
                                ", which is used as start class for the " +
                                "learning algorithm.");
                    }

                    if(operator instanceof RhoDRDown) {
                        ((RhoDRDown)operator).setDropDisjuncts(false);
                    }

                } else {
                    Set<OWLClassExpression> superClasses =
                            reasoner.getClassHierarchy().getSuperClasses(classToDescribe);

                    if(superClasses.size() > 1) {
                        startClass = dataFactory.getOWLObjectIntersectionOf(superClasses);

                    } else if(superClasses.size() == 1){
                        startClass = (OWLClassExpression) superClasses.toArray()[0];

                    } else {
                        startClass = dataFactory.getOWLThing();
                        logger.warn(classToDescribe + " is equivalent to " +
                                "owl:Thing. Usually, it is not sensible to " +
                                "learn a OWLClassExpression in this case.");
                    }
                }
            }

        } else if(learningProblem instanceof PosOnlyLP) {
            examples = ((PosOnlyLP)learningProblem).getPositiveExamples();

        } else if(learningProblem instanceof PosNegLP) {
            examples = Helper.union(
                    ((PosNegLP)learningProblem).getPositiveExamples(),
                    ((PosNegLP)learningProblem).getNegativeExamples());
        }
    }

    @Override
    public OWLClassExpression getCurrentlyBestDescription() {
        EvaluatedDescription ed = getCurrentlyBestEvaluatedDescription();
        return ed == null ? null : ed.getDescription();
    }

    @Override
    public List<OWLClassExpression> getCurrentlyBestDescriptions() {
        return bestEvaluatedDescriptions.toDescriptionList();
    }

    @Override
    public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
        return bestEvaluatedDescriptions.getBest();
    }

    @Override
    public TreeSet<? extends EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions() {
        return bestEvaluatedDescriptions.getSet();
    }

    public double getCurrentlyBestAccuracy() {
        return bestEvaluatedDescriptions.getBest().getAccuracy();
    }

    @Override
    public void start() {
        stop = false;
        isRunning = true;
        reset();
        nanoStartTime = System.nanoTime();

        MPI.Init(mpiConf.asArgv());
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        // message types
        int refinementMsgType = 1;
        int newBestExprMsgType = 5;
        int nodeAddMsgType = 7;
        int termMsgType = 23;

        // special processes
        int distributor = 0;
        int aggregator = 1;
        int nextTarget = 1;

        /*
         * MPI setup:
         * - 'local': only concerning a single process
         * - 'global': concerns the whole MPI/CELOE setup
         * - 'worker':
         *   - process with rank > 1
         *   - evaluates the input class expression and sends new local best
         *     expression to the aggregator
         *   - terminates after receiving special message
         * - 'distributor' (master):
         *   - process with rank 0
         *   - feeds all processes with rank > 1 ('workers') with new class
         *     expressions for evaluation
         *   - distribution strategies:
         *     - round robin
         *     - TODO: further models?
         *   - receives new nodes to expand from the workers
         *   - initiates termination of the CELOE run by sending special
         *     message to all other processes
         * - 'aggregator'
         *   - process with rank 1
         *   - receives new local best results from processes with rank > 1
         *     ('workers') and updates the global best evaluated descriptions
         *     set
         *   - notifies the distributor that a sufficiently accurate class
         *     expression was found and the CELOE run can be terminated
         *   - prints out the best evaluated descriptions after having received
         *     termination message
         * - work flow:
         *   - distributor holds the nodes to expand
         *   - distributor gets next node to expand and calculates the
         *     refinements
         *   - distributor feeds the workers with the refinements (until
         *     termination requirements are satisfied)
         *   - each worker holds a local best expressions list
         *   - each worker calculates a score for the input refinement; if the
         *     input refinement is a new best class expression it is sent to
         *     the aggregator
         *   - each worker sends back the currently evaluated node if its
         *     accuracy is acceptable
         *   - aggregator holds the global best expressions list
         *   - aggregator updates the global best expressions list if it
         *     receives a new class expression from a worker
         *   - aggregator sends a message to distributor when the
         *     aggregator's termination requirements are satisfied
         *   - distributor sends termination message to all other processes
         *   - worker processes terminate
         *   - aggregator prints out current best expression list
         * - issues
         *   - maxClassExpressionTestsAfterImprovement evaluation is currently
         *     not supported since only the distributor knows the overall
         *     number of checks and only the aggregator knows when the last
         *     improvement occurred
         *   - maxClassExpressionTestsWithoutImprovement evaluation is currently
         *     not supported since only the distributor knows the overall
         *     number of checks and only the aggregator knows when the last
         *     improvement occurred
         * - TODO
         *   - distribution models
         *   - Jamon
         */

        if (rank == distributor) {
            /* <-------------------- distributor --------------------> */
            logger.info("Distributor started");

            logger.info("start class:" + startClass);
            addNode(startClass, null);

            int numClassExpressionTests = 0;
            OENode nextNode;

            while (true) {
                // determine whether termination criteria satisfied
                // 1) non blocking check if message received from aggregator
                Request request = MPI.COMM_WORLD.Irecv(new byte[0], 0, 0,
                        MPI.BYTE, aggregator, newBestExprMsgType);
                if (request.Test() != null) {
                    logger.info("|<--| Distributor\t<<< Aggregator: TERM (RECEIVING)");
                    request.Wait();
                    logger.info("|<--| Distributor\t<-- Aggregator: TERM (RECEIVED)");
                    logger.info("Distributor received termination message " +
                            "from aggregator");
                    break;
                }
                // 2) time's up (may execution time), or already max. number of
                //    class expression tests performed
                if ((maxClassExpressionTests != 0 &&
                            (numClassExpressionTests >= maxClassExpressionTests)) ||
                        (maxExecutionTimeInSeconds != 0 &&
                            ((System.nanoTime() - nanoStartTime) >=
                                (maxExecutionTimeInSeconds*1000000000l)))) {
                    logger.info("Distributor reached termination criterion");
                    break;
                }

                // TODO: think about this... smells like deadlock
                // check for new evaluated nodes
                while (true) {
                    byte[] nodeBuf = new byte[bufferSize];
                    OENode node;
                    request = MPI.COMM_WORLD.Irecv(nodeBuf, 0, bufferSize, MPI.BYTE,
                            MPI.ANY_SOURCE, nodeAddMsgType);
                    if (request.Test() == null) {
                        break;

                    } else {
                        // rebuild node object
                        logger.info("|<--| Distributor\t<<< worker: node (RECEIVING)");
                        Status status = request.Wait();

                        ByteArrayInputStream bis = new ByteArrayInputStream(nodeBuf);
                        ObjectInput in = null;
                        try {
                            in = new ObjectInputStream(bis);
                          Object obj = in.readObject();
                          node = (OENode) obj;
                          logger.info("|<--| Distributor\t<-- worker " +
                                  status.source + ": node " + node + "(RECEIVED");
                          nodes.add(node);

                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                 }

                // get next node and the next nodes refinements
                nextNode = getNextNodeToExpand();
                int horizExp = nextNode.getHorizontalExpansion();
                TreeSet<OWLClassExpression> refinements = refineNode(nextNode);

                // feed refinements to workers
                while (refinements.size() != 0) {
                    OWLClassExpression refinement = refinements.pollFirst();
                    int length = OWLClassExpressionUtils.getLength(refinement);

                    /* we ignore all refinements with lower length and too high
                     * depth (this also avoids duplicate node children) */
                    if(length > horizExp &&
                            OWLClassExpressionUtils.getDepth(refinement) <= maxDepth) {
                        boolean nonRedundant = descriptions.add(refinement);

                        // redundancy check (return if redundant)
                        if (!nonRedundant) {
                            continue;
                        }

                        RefinementData refData =
                                new RefinementData(refinement, nextNode);
                        byte[] buf = new byte[bufferSize];
                        try {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            ObjectOutput out = new ObjectOutputStream(bos);
                            out.writeObject(refData);
                            buf = bos.toByteArray();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        logger.info("|-->| Distributor\t>>> Worker " +
                                nextTarget + ": refinement " + refinement +
                                " (SENDING)");
                        MPI.COMM_WORLD.Send(buf, 0, buf.length, MPI.BYTE,
                                nextTarget, refinementMsgType);
                        logger.info("|-->| Distributor\t--> Worker " +
                                nextTarget + ": refinement " + refinement +
                                " (SENT)");

                        numClassExpressionTests++;

                        nextTarget = (nextTarget + 1) % size;
                        while (nextTarget < 2) {  // skip distributor & aggregator
                            nextTarget = (nextTarget + 1) % size;
                        }

                        updateMinMaxHorizExp(nextNode);

                        // writing the search tree (if configured)
                        if (writeSearchTree) {
                            String treeString = "best node: " +
                                    bestEvaluatedDescriptions.getBest() + "\n";

                            if (refinements.size() > 1) {
                                treeString += "all expanded nodes:\n";
                                for (OWLClassExpression n : refinements) {
                                    treeString += "   " + n + "\n";
                                }
                            }
                            treeString += startNode.toTreeString(baseURI, prefixes);
                            treeString += "\n";

                            if (replaceSearchTree)
                                Files.createFile(new File(searchTreeFile), treeString);
                            else
                                Files.appendToFile(new File(searchTreeFile), treeString);
                        }
                    }
                }
            }

            logger.info("Distributor is going to send termination message to " +
                    "all other processes...");
            for (int i=1; i < size; i++) {
                logger.info("|-->| Distributor\t>>> worker " + i + ": TERM (SENDING)");
                MPI.COMM_WORLD.Send(new byte[0], 0, 0, MPI.BYTE, i, termMsgType);
                logger.info("|-->| Distributor\t--> worker " + i + ": TERM (SENT)");
            }
            stop = true;

            logger.info("Distributor terminated");
            /* </------------------- distributor --------------------> */

        } else if (rank == aggregator) {
            /* <-------------------- aggregator ---------------------> */
            logger.info("Aggregator started");
            byte[] buf = new byte[bufferSize];

            // highest accuracy so far
            double highestAccuracy = 0.0;

            while (true) {
                if(!singleSuggestionMode &&
                        bestEvaluatedDescriptions.getBestAccuracy() > highestAccuracy) {

                    highestAccuracy = bestEvaluatedDescriptions.getBestAccuracy();
                    expressionTestCountLastImprovement = expressionTests;
                    timeLastImprovement = System.nanoTime();
                    long durationInMillis = getCurrentRuntimeInMilliSeconds();
                    String durationStr = getDurationAsString(durationInMillis);
                    logger.info("more accurate (" +
                            dfPercent.format(highestAccuracy) + ") class " +
                            "expression found after " + durationStr + ": " +
                            descriptionToString(
                                    bestEvaluatedDescriptions.getBest().
                                        getDescription()));
                }

                // check termination criteria and notify distributor if necessary
                if ((maxExecutionTimeInSecondsAfterImprovement != 0 &&
                        ((System.nanoTime() - nanoStartTime) >=
                            (maxExecutionTimeInSecondsAfterImprovement*1000000000l))) ||
                    (terminateOnNoiseReached &&
                        (100*getCurrentlyBestAccuracy()>=100-noisePercentage)) ||
                    (stopOnFirstDefinition && (getCurrentlyBestAccuracy() >= 1))) {
                    logger.info("|-->| Aggregator\t>>> distributor: TERM (SENDING)");
                    MPI.COMM_WORLD.Isend(buf, 0, 0, MPI.BYTE, distributor, newBestExprMsgType);
                    logger.info("|-->| Aggregator\t--> distributor: TERM (SENT)");
                }

                logger.info("|<--| Aggregator\t<<< ANY: ANY (RECEIVING)");
                Status status = MPI.COMM_WORLD.Recv(buf, 0, bufferSize,
                        MPI.BYTE, MPI.ANY_SOURCE, MPI.ANY_TAG);

                // check if the read message is a termination signal
                if (status.tag == termMsgType) {
                    logger.info("|<--| Aggregator\t<-- distributor: TERM (RECEIVED)");
                    break;

                } else if (status.tag == newBestExprMsgType){
                    // read new best class expression and update best class
                    // expressions list
                    ByteArrayInputStream bis = new ByteArrayInputStream(buf);
                    ObjectInput in = null;
                    EvaluatedDescription newLocalBestExpr;
                    try {
                        in = new ObjectInputStream(bis);
                        Object obj = in.readObject();
                        newLocalBestExpr = (EvaluatedDescription) obj;
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                    logger.info("|<--| Aggregator\t<-- worker" +
                            status.source + ": new best description " +
                            newLocalBestExpr.getDescription() + " (RECEIVED)");

                    boolean isCandidate = !bestEvaluatedDescriptions.isFull();
                    if (!isCandidate) {
                        EvaluatedDescription worst = bestEvaluatedDescriptions.getWorst();
                        double accThreshold = worst.getAccuracy();
                        isCandidate =
                            (newLocalBestExpr.getAccuracy() > accThreshold ||
                            (newLocalBestExpr.getAccuracy() >= accThreshold &&
                                OWLClassExpressionUtils.getLength(
                                        newLocalBestExpr.getDescription()) <
                                            worst.getDescriptionLength()));
                    }

                    if (isCandidate) {
                        OWLClassExpression niceDescription =
                                rewriteDescription(newLocalBestExpr.getDescription());

                        ConceptTransformation.transformToOrderedForm(niceDescription);

                        if(niceDescription.equals(classToDescribe)) {
                            continue;
                        }

                        /* another test: none of the other suggested
                         * descriptions should be a subdescription of this one
                         * unless accuracy is different
                         * => comment: on the one hand, this appears to be too
                         * strict, because once A is a solution then everything
                         * containing A is not a candidate; on the other hand
                         * this suppresses many meaningless extensions of A
                         */
                        boolean shorterDescriptionExists = false;
                        if(forceMutualDifference) {
                            for(EvaluatedDescription ed : bestEvaluatedDescriptions.getSet()) {
                                if(Math.abs(ed.getAccuracy() - newLocalBestExpr.getAccuracy())
                                            <= 0.00001 &&
                                        ConceptTransformation.isSubdescription(
                                                niceDescription, ed.getDescription())) {

                                    shorterDescriptionExists = true;
                                    break;
                                }
                            }
                        }

                        if(!shorterDescriptionExists) {
                            if(!filterFollowsFromKB ||
                                    !((ClassLearningProblem) learningProblem).
                                        followsFromKB(niceDescription)) {

                                bestEvaluatedDescriptions.add(
                                        niceDescription,
                                        newLocalBestExpr.getAccuracy(),
                                        learningProblem);
                            }
                        }
                    }
                }
            }
            // printing results
            totalRuntimeNs = System.nanoTime()-nanoStartTime;
            logger.info("Algorithm terminated successfully (time: " +
                    Helper.prettyPrintNanoSeconds(totalRuntimeNs) + ", " +
                    expressionTests+" descriptions tested, "  + nodes.size() +
                    " nodes in the search tree).\n");
            logger.info(reasoner.toString());

            if(singleSuggestionMode) {
                bestEvaluatedDescriptions.add(bestDescription, bestAccuracy,
                        learningProblem);
            }

            // print solution(s)
            logger.info("solutions:\n" + getSolutionString());

            logger.info("Aggregator terminated");
            /* </------------------- aggregator ---------------------> */

        } else {
            /* <---------------------- worker -----------------------> */
            logger.info("Worker " + rank + " started");
            byte[] buf = new byte[bufferSize];
            EvaluatedDescriptionSet localBestEvaluatedDescriptions =
                    new EvaluatedDescriptionSet(maxNrOfResults);
            double localBestAccuracy = 0;
            OWLClassExpression localBestDescription = dataFactory.getOWLThing();
            List<OENode> nodeQueue = new ArrayList<OENode>();
            /* idea: dummy request initialized here; later check if == null
             * since requests are null as long as the asynchronous message
             * transfer is not finished --> only send first node from queue to
             * distributor if last transfer is finished */
            Request request = new Request(null);

            while (true) {
                byte[] recvBuf = new byte[bufferSize];
                // check if termination message
                logger.info("|<--| Worker " + rank + "\t<<< distributor: class expr or TERM (RECEIVING)");
                Status status = MPI.COMM_WORLD.Recv(recvBuf, 0, bufferSize,
                        MPI.BYTE, MPI.ANY_SOURCE, MPI.ANY_TAG);

                // check if the read message is a termination signal
                if (status.tag == termMsgType) {
                    logger.info("|<--| Worker " + rank + "\t<-- " +
                            "distributor: TERM (RECEIVED)");
                    break;

                } else if (status.tag == refinementMsgType){
                    RefinementData refinementData;
                    ByteArrayInputStream bis = new ByteArrayInputStream(recvBuf);
                    ObjectInput in = null;
                    try {
                        in = new ObjectInputStream(bis);
                        Object obj = in.readObject();
                        refinementData = (RefinementData) obj;
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }

                    logger.info("|<--| Worker " + rank + "\t<-- distributor: " +
                            "class expression " + refinementData.refinement +
                            " (RECEIVED)");
                    OWLClassExpression description = refinementData.refinement;
                    OENode parentNode = refinementData.nextNode;

                    // check whether the OWLClassExpression is allowed
                    if(!isDescriptionAllowed(description, parentNode)) {
                        continue;
                    }

                    double accuracy = learningProblem.getAccuracyOrTooWeak(
                            description, noise);

                    if(accuracy > 1.0 || (accuracy < 0.0 && accuracy != -1)) {
                        logger.warn("Invalid accuracy value " + accuracy +
                                " for OWLClassExpression " + description +
                                ". This could be caused by a bug in the " +
                                "heuristic measure and should be reported to " +
                                "the DL-Learner bug tracker.");
                        System.exit(0);
                    }

                    if(accuracy == -1) {
                        continue;
                    }

                    OENode node = new OENode(parentNode, description, accuracy);

                    // link to parent (unless start node)
                    if(parentNode == null) {
                        startNode = node;
                    } else {
                        parentNode.addChild(node);
                    }
                    nodeQueue.add(node);  // will be sent to distributor later

                    boolean sendToAggregator = false;

                    if(singleSuggestionMode) {
                        if(accuracy > localBestAccuracy) {
                            localBestAccuracy = accuracy;
                            localBestDescription = description;
                            logger.info("Worker " + rank + " found more " +
                                    "accurate (" +
                                    dfPercent.format(localBestAccuracy) + ") " +
                                    "class expression: " +
                                    descriptionToString(localBestDescription));
                            sendToAggregator = true;
                        }
                    }

                    boolean isCandidate = !localBestEvaluatedDescriptions.isFull();
                    if(!isCandidate) {
                        EvaluatedDescription worst =
                                localBestEvaluatedDescriptions.getWorst();
                        double accThreshold = worst.getAccuracy();
                        isCandidate =
                            (accuracy > accThreshold ||
                            (accuracy >= accThreshold &&
                                OWLClassExpressionUtils.getLength(description) <
                                    worst.getDescriptionLength()));
                    }

                    if(isCandidate) {
                        OWLClassExpression niceDescription = rewriteNode(node);
                        ConceptTransformation.transformToOrderedForm(niceDescription);

                        if(niceDescription.equals(classToDescribe)) {
                            continue;
                        }

                        /* another test: none of the other suggested
                         * descriptions should be a subdescription of this one
                         * unless accuracy is different
                         * => comment: on the one hand, this appears to be too
                         * strict, because once A is a solution then everything
                         * containing A is not a candidate; on the other hand
                         * this suppresses many meaningless extensions of A
                         */
                        boolean shorterDescriptionExists = false;
                        if(forceMutualDifference) {
                            for(EvaluatedDescription ed : localBestEvaluatedDescriptions.getSet()) {
                                if(Math.abs(ed.getAccuracy() - accuracy) <= 0.00001 &&
                                        ConceptTransformation.isSubdescription(
                                                niceDescription,
                                                ed.getDescription())) {
                                    shorterDescriptionExists = true;
                                    break;
                                }
                            }
                        }

                        if(!shorterDescriptionExists) {
                            if(!filterFollowsFromKB ||
                                    !((ClassLearningProblem)learningProblem).followsFromKB(
                                            niceDescription)) {

                                localBestEvaluatedDescriptions.add(
                                        niceDescription, accuracy, learningProblem);
                                sendToAggregator = true;
                            }
                        }
                    }

                    if (sendToAggregator) {
                        EvaluatedDescription ed = learningProblem.evaluate(description);
                        try {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            ObjectOutput out = new ObjectOutputStream(bos);
                            out.writeObject(ed);
                            buf = bos.toByteArray();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        logger.info("|-->| Worker " + rank + "\t>>> " +
                                "aggregator: new best description " +
                                description + " (SENDING)");
                        MPI.COMM_WORLD.Send(buf, 0, buf.length, MPI.BYTE,
                                aggregator, newBestExprMsgType);
                        logger.info("|-->| Worker " + rank + "\t--> " +
                                "aggregator: new best description " +
                                description + " (SENT)");
                    }
                }

                // send out node to distributor (if last transfer already fninshed
                if (request != null && !nodeQueue.isEmpty()) {
                    byte[] nodeSendBuf = new byte[bufferSize];
                    OENode node = nodeQueue.remove(0);

                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutput out = new ObjectOutputStream(bos);
                        out.writeObject(node);
                        nodeSendBuf = bos.toByteArray();
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }

                    logger.info("|-->| Worker " + rank + "\t>>> distributor: " +
                            "node " + node + " (SENDING ASYNC)");
                    request = MPI.COMM_WORLD.Isend(nodeSendBuf, 0,
                            nodeSendBuf.length, MPI.BYTE, distributor,
                            nodeAddMsgType);
                }
            }

            logger.info("Worker " + rank + " terminated");
            /* </--------------------- worker -----------------------> */
        }

        isRunning = false;
        MPI.Finalize();
    }

    private OENode getNextNodeToExpand() {
        /* we expand the best node of those, which have not achieved 100%
         * accuracy already and have a horizontal expansion equal to their
         * length (rationale: further extension is likely to add irrelevant
         * syntactical constructs) */
        Iterator<OENode> it = nodes.descendingIterator();

        while(it.hasNext()) {
            OENode node = it.next();

            if (isExpandAccuracy100Nodes() &&
                    node.getHorizontalExpansion() < OWLClassExpressionUtils.getLength(
                            node.getDescription())) {

                return node;

            } else {
                if(node.getAccuracy() < 1.0 ||
                        node.getHorizontalExpansion() < OWLClassExpressionUtils.getLength(
                                node.getDescription())) {

                    return node;
                }
            }
        }

        /* this should practically never be called, since for any reasonable
         * learning task, we will always have at least one node with less than
         * 100% accuracy */
        return null;
    }

    /** expand node horizontally */
    private TreeSet<OWLClassExpression> refineNode(OENode node) {
        /* we have to remove and add the node since its heuristic evaluation
         * changes through the expansion (you *must not* include any criteria
         * in the heuristic which are modified outside of this method,
         * otherwise you may see rarely occurring but critical false ordering
         * in the nodes set) */
        nodes.remove(node);

        int horizExp = node.getHorizontalExpansion();
        TreeSet<OWLClassExpression> refinements =
                (TreeSet<OWLClassExpression>) operator.refine(node.getDescription(), horizExp+1);

        node.incHorizontalExpansion();
        node.setRefinementCount(refinements.size());
        nodes.add(node);

        return refinements;
    }

    /**
     * add node to search tree if it is not too weak
     * TODO: method not really needed; only called once for the start description
     *
     * @return true if node was added and false otherwise
     */
    private boolean addNode(OWLClassExpression description, OENode parentNode) {

        // redundancy check (return if redundant)
        boolean nonRedundant = descriptions.add(description);
        if(!nonRedundant) return false;

        // check whether the OWLClassExpression is allowed
        if(!isDescriptionAllowed(description, parentNode)) return false;

        // quality of OWLClassExpression (return if too weak)
        Monitor mon = MonitorFactory.start("lp");
        double accuracy = learningProblem.getAccuracyOrTooWeak(description, noise);
        mon.stop();

        // issue a warning if accuracy is not between 0 and 1 or -1 (too weak)
        if(accuracy > 1.0 || (accuracy < 0.0 && accuracy != -1)) {
            logger.warn("Invalid accuracy value " + accuracy + " for " +
                    "OWLClassExpression " + description + ". This could be " +
                    "caused by a bug in the heuristic measure and should be " +
                    "reported to the DL-Learner bug tracker.");
            System.exit(0);
        }

        expressionTests++;
        if(accuracy == -1) return false;

        OENode node = new OENode(parentNode, description, accuracy);

        // link to parent (unless start node)
        if(parentNode == null) {
            startNode = node;

        } else {
            parentNode.addChild(node);
        }
        nodes.add(node);

        /* in some cases (e.g. mutation) fully evaluating even a single
         * OWLClassExpression is too expensive due to the high number of
         * examples -- so we just stick to the approximate accuracy */
        if(singleSuggestionMode) {
            if(accuracy > bestAccuracy) {
                bestAccuracy = accuracy;
                bestDescription = description;
                logger.info("more accurate (" + dfPercent.format(bestAccuracy) +
                        ") class expression found: " +
                        descriptionToString(bestDescription));
            }
            return true;
        }

        // maybe add to best descriptions (method keeps set size fixed);
        // we need to make sure that this does not get called more often than
        // necessary since rewriting is expensive
        boolean isCandidate = !bestEvaluatedDescriptions.isFull();
        if(!isCandidate) {
            EvaluatedDescription worst = bestEvaluatedDescriptions.getWorst();
            double accThreshold = worst.getAccuracy();
            isCandidate =
                (accuracy > accThreshold ||
                (accuracy >= accThreshold && OWLClassExpressionUtils.getLength(description) < worst.getDescriptionLength()));
        }

        if(isCandidate) {
            OWLClassExpression niceDescription = rewriteNode(node);
            ConceptTransformation.transformToOrderedForm(niceDescription);

            if(niceDescription.equals(classToDescribe)) {
                return false;
            }

            /* another test: none of the other suggested descriptions should be
             * a subdescription of this one unless accuracy is different
             * => comment: on the one hand, this appears to be too strict,
             * because once A is a solution then everything containing A is not
             * a candidate; on the other hand this suppresses many meaningless
             * extensions of A */
            boolean shorterDescriptionExists = false;
            if(forceMutualDifference) {
                for(EvaluatedDescription ed : bestEvaluatedDescriptions.getSet()) {
                    if(Math.abs(ed.getAccuracy()-accuracy) <= 0.00001 &&
                            ConceptTransformation.isSubdescription(
                                    niceDescription, ed.getDescription())) {

                        shorterDescriptionExists = true;
                        break;
                    }
                }
            }

            if(!shorterDescriptionExists) {
                if(!filterFollowsFromKB ||
                        !((ClassLearningProblem)learningProblem).followsFromKB(niceDescription)) {

                    bestEvaluatedDescriptions.add(niceDescription, accuracy,
                            learningProblem);
                }
            }
        }

        return true;
    }

    /** checks whether the OWLClassExpression is allowed */
    private boolean isDescriptionAllowed(OWLClassExpression description, OENode parentNode) {

        if(isClassLearningProblem) {
            if(isEquivalenceProblem) {
                // the class to learn must not appear on the outermost property level
                if(occursOnFirstLevel(description, classToDescribe)) {
                    return false;
                }

            } else {
                // none of the superclasses of the class to learn must appear on the
                // outermost property level
                TreeSet<OWLClassExpression> toTest = new TreeSet<OWLClassExpression>();
                toTest.add(classToDescribe);

                while(!toTest.isEmpty()) {
                    OWLClassExpression d = toTest.pollFirst();
                    if(occursOnFirstLevel(description, d)) {
                        return false;
                    }
                    toTest.addAll(reasoner.getClassHierarchy().getSuperClasses(d));
                }
            }
        }

        // perform forall sanity tests
        if(parentNode != null &&
                ConceptTransformation.getForallOccurences(description) >
                    ConceptTransformation.getForallOccurences(parentNode.getDescription())) {

            /* we have an additional \forall construct, so we now fetch the
             * contexts in which it occurs */
            SortedSet<PropertyContext> contexts =
                    ConceptTransformation.getForallContexts(description);
            SortedSet<PropertyContext> parentContexts =
                    ConceptTransformation.getForallContexts(parentNode.getDescription());
            contexts.removeAll(parentContexts);

            /* we now have to perform sanity checks: if \forall is used, then
             * there should be at least on class instance which has a filler at
             * the given context */
            for(PropertyContext context : contexts) {
                // transform [r,s] to \exists r.\exists s.\top
                OWLClassExpression existentialContext = context.toExistentialContext();
                boolean fillerFound = false;

                for(OWLIndividual instance : examples) {
                    if(reasoner.hasType(existentialContext, instance)) {
                        fillerFound = true;
                        break;
                    }
                }
                /* if we do not find a filler, this means that putting \forall
                 * at that position is not meaningful */
                if(!fillerFound) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * determine whether a named class occurs on the outermost level, i.e.
     * property depth 0 (it can still be at higher depth, e.g. if intersections
     * are nested in unions)
     */
    private boolean occursOnFirstLevel(OWLClassExpression description, OWLClassExpression cls) {
        if(cls.isOWLThing()) {
            return false;
        }
        return (description instanceof OWLNaryBooleanClassExpression &&
                ((OWLNaryBooleanClassExpression)description).getOperands().contains(cls));
    }

    /** check whether the node is a potential solution candidate */
    private OWLClassExpression rewriteNode(OENode node) {
        OWLClassExpression description = node.getDescription();
        OWLClassExpression niceDescription = rewriteDescription(description);

        return niceDescription;
    }

    private OWLClassExpression rewriteDescription(OWLClassExpression description) {
        // minimize OWLClassExpression (expensive!) - also performs some human friendly rewrites
        OWLClassExpression niceDescription;

        if(useMinimizer) {
            niceDescription = minimizer.minimizeClone(description);

        } else {
            niceDescription = description;
        }
        niceDescription = ConceptTransformation.replaceRange(niceDescription, reasoner);

        return niceDescription;
    }

    private void reset() {
        // set all values back to their default values (used for running
        // the algorithm more than once)
        nodes = new TreeSet<OENode>(heuristic);
        descriptions = new TreeSet<OWLClassExpression>();
        bestEvaluatedDescriptions.getSet().clear();
        expressionTests = 0;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void stop() {
        stop = true;
    }

    public OENode getSearchTreeRoot() {
        return startNode;
    }

    @SuppressWarnings("unused")
    private String bestDescriptionToString() {
        EvaluatedDescription best = bestEvaluatedDescriptions.getBest();

        return OWLAPIRenderers.toManchesterOWLSyntax(best.getDescription()) +
                " (accuracy: " + dfPercent.format(best.getAccuracy()) + ")";
    }

    private void updateMinMaxHorizExp(OENode node) {
        int newHorizExp = node.getHorizontalExpansion();

        // update maximum value
        maxHorizExp = Math.max(maxHorizExp, newHorizExp);

        // we just expanded a node with minimum horizontal expansion;
        // we need to check whether it was the last one
        if(minHorizExp == newHorizExp - 1) {

            // the best accuracy that a node can achieve
            double scoreThreshold = heuristic.getNodeScore(node) + 1 - node.getAccuracy();

            for(OENode n : nodes.descendingSet()) {
                if(n != node) {
                    if(n.getHorizontalExpansion() == minHorizExp) {
                        // we can stop instantly when another node with min.
                        return;
                    }
                    if(heuristic.getNodeScore(n) < scoreThreshold) {
                        // we can stop traversing nodes when their score is too low
                        break;
                    }
                }
            }

            // inc. minimum since we found no other node which also has min. horiz. exp.
            minHorizExp++;
        }
    }

    public TreeSet<OENode> getNodes() {
        return nodes;
    }

    public int getMaximumHorizontalExpansion() {
        return maxHorizExp;
    }

    public int getMinimumHorizontalExpansion() {
        return minHorizExp;
    }

    public int getClassExpressionTests() {
        return expressionTests;
    }

    public LengthLimitedRefinementOperator getOperator() {
        return operator;
    }

    @Autowired(required=false)
    public void setOperator(LengthLimitedRefinementOperator operator) {
        this.operator = operator;
    }

    public OWLClassExpression getStartClass() {
        return startClass;
    }

    public void setStartClass(OWLClassExpression startClass) {
        this.startClass = startClass;
    }

    public Set<OWLClass> getAllowedConcepts() {
        return allowedConcepts;
    }

    public void setAllowedConcepts(Set<OWLClass> allowedConcepts) {
        this.allowedConcepts = allowedConcepts;
    }

    public Set<OWLClass> getIgnoredConcepts() {
        return ignoredConcepts;
    }

    public void setIgnoredConcepts(Set<OWLClass> ignoredConcepts) {
        this.ignoredConcepts = ignoredConcepts;
    }

    public boolean isWriteSearchTree() {
        return writeSearchTree;
    }

    public void setWriteSearchTree(boolean writeSearchTree) {
        this.writeSearchTree = writeSearchTree;
    }

    public String getSearchTreeFile() {
        return searchTreeFile;
    }

    public void setSearchTreeFile(String searchTreeFile) {
        this.searchTreeFile = searchTreeFile;
    }

    public int getMaxNrOfResults() {
        return maxNrOfResults;
    }

    public void setMaxNrOfResults(int maxNrOfResults) {
        this.maxNrOfResults = maxNrOfResults;
    }

    public double getNoisePercentage() {
        return noisePercentage;
    }

    public void setNoisePercentage(double noisePercentage) {
        this.noisePercentage = noisePercentage;
    }

    public boolean isFilterDescriptionsFollowingFromKB() {
        return filterDescriptionsFollowingFromKB;
    }

    public void setFilterDescriptionsFollowingFromKB(boolean filterDescriptionsFollowingFromKB) {
        this.filterDescriptionsFollowingFromKB = filterDescriptionsFollowingFromKB;
    }

    public boolean isReplaceSearchTree() {
        return replaceSearchTree;
    }

    public void setReplaceSearchTree(boolean replaceSearchTree) {
        this.replaceSearchTree = replaceSearchTree;
    }

    public int getMaxClassDescriptionTests() {
        return maxClassExpressionTests;
    }

    public void setMaxClassDescriptionTests(int maxClassDescriptionTests) {
        this.maxClassExpressionTests = maxClassDescriptionTests;
    }

    public int getMaxExecutionTimeInSeconds() {
        return maxExecutionTimeInSeconds;
    }

    public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
        this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
    }

    public boolean isTerminateOnNoiseReached() {
        return terminateOnNoiseReached;
    }

    public void setTerminateOnNoiseReached(boolean terminateOnNoiseReached) {
        this.terminateOnNoiseReached = terminateOnNoiseReached;
    }

    public boolean isReuseExistingDescription() {
        return reuseExistingDescription;
    }

    public void setReuseExistingDescription(boolean reuseExistingDescription) {
        this.reuseExistingDescription = reuseExistingDescription;
    }

    public boolean isUseMinimizer() {
        return useMinimizer;
    }

    public void setUseMinimizer(boolean useMinimizer) {
        this.useMinimizer = useMinimizer;
    }

    public AbstractHeuristic getHeuristic() {
        return heuristic;
    }

    @Autowired(required=false)
    public void setHeuristic(AbstractHeuristic heuristic) {
        this.heuristic = heuristic;
    }

    public int getMaxExecutionTimeInSecondsAfterImprovement() {
        return maxExecutionTimeInSecondsAfterImprovement;
    }

    public void setMaxExecutionTimeInSecondsAfterImprovement(
            int maxExecutionTimeInSecondsAfterImprovement) {
        this.maxExecutionTimeInSecondsAfterImprovement =
                maxExecutionTimeInSecondsAfterImprovement;
    }

    public boolean isSingleSuggestionMode() {
        return singleSuggestionMode;
    }

    public void setSingleSuggestionMode(boolean singleSuggestionMode) {
        this.singleSuggestionMode = singleSuggestionMode;
    }

    public int getMaxClassExpressionTests() {
        return maxClassExpressionTests;
    }

    public void setMaxClassExpressionTests(int maxClassExpressionTests) {
        this.maxClassExpressionTests = maxClassExpressionTests;
    }

    public double getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(double maxDepth) {
        this.maxDepth = maxDepth;
    }

    public boolean isStopOnFirstDefinition() {
        return stopOnFirstDefinition;
    }

    public void setStopOnFirstDefinition(boolean stopOnFirstDefinition) {
        this.stopOnFirstDefinition = stopOnFirstDefinition;
    }

    public long getTotalRuntimeNs() {
        return totalRuntimeNs;
    }

    public boolean isExpandAccuracy100Nodes() {
        return expandAccuracy100Nodes;
    }

    public void setExpandAccuracy100Nodes(boolean expandAccuracy100Nodes) {
        this.expandAccuracy100Nodes = expandAccuracy100Nodes;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return new DistCELOE(this);
    }

    public static void main(String[] args) throws Exception{
        ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());

        File file = new File("../examples/father.owl");

        OWLClass classToDescribe = new OWLClassImpl(IRI.create(
                "http://example.com/father#male"));

        OWLOntology ontology =
                OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);

        AbstractKnowledgeSource ks = new OWLAPIOntology(ontology);
        ks.init();

        OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
        baseReasoner.setReasonerTypeString("hermit");
        baseReasoner.init();
        ClosedWorldReasoner rc = new ClosedWorldReasoner(ks);
        rc.init();

        ClassLearningProblem lp = new ClassLearningProblem(rc);
//      lp.setEquivalence(false);
        lp.setClassToDescribe(classToDescribe);
        lp.init();

        RhoDRDown op = new RhoDRDown();
        op.setReasoner(rc);
        op.setUseNegation(false);
        op.setUseHasValueConstructor(false);
        op.setUseCardinalityRestrictions(true);
        op.setUseExistsConstructor(true);
        op.setUseAllConstructor(true);
        op.init();

        //(male ⊓ (∀ hasChild.⊤)) ⊔ (∃ hasChild.(∃ hasChild.male))
        OWLDataFactory df = new OWLDataFactoryImpl();
        OWLClassExpression ce = df.getOWLObjectUnionOf(
                df.getOWLObjectIntersectionOf(
                        df.getOWLClass(IRI.create("http://example.com/father#male")),
                        df.getOWLObjectAllValuesFrom(
                                df.getOWLObjectProperty(IRI.create("http://example.com/father#hasChild")),
                                df.getOWLThing())),
                df.getOWLObjectSomeValuesFrom(
                        df.getOWLObjectProperty(IRI.create("http://example.com/father#hasChild")),
                        df.getOWLObjectSomeValuesFrom(
                                df.getOWLObjectProperty(IRI.create("http://example.com/father#hasChild")),
                                df.getOWLClass(IRI.create("http://example.com/father#male"))
                                )
                        )
                );

        DistCELOE alg = new DistCELOE(lp, rc);
        MPIConfig mpiConf = new MPIConfig(
                Integer.parseInt(args[0]),
                Integer.parseInt(args[1]),
                MPIConfig.DeviceName.SMPDEV);
        alg.setMpiConf(mpiConf);
        alg.setMaxExecutionTimeInSeconds(10);
        alg.setOperator(op);
        alg.setWriteSearchTree(true);
        alg.setSearchTreeFile("log/search-tree.log");
        alg.setReplaceSearchTree(true);
        alg.init();

        alg.start();
    }

    public MPIConfig getMpiConf() {
        return mpiConf;
    }

    public void setMpiConf(MPIConfig mpiConf) {
        this.mpiConf = mpiConf;
    }
}

class RefinementData implements Serializable {
    private static final long serialVersionUID = -6288167810767750234L;
    public OWLClassExpression refinement;
    public OENode nextNode;

    public RefinementData(OWLClassExpression refinement, OENode nextNode) {
        this.refinement = refinement;
        this.nextNode = nextNode;
    }
}

