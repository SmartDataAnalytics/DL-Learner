package org.dllearner.algorithms.distributed.amqp;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.JMSException;

import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.algorithms.distributed.containers.NodeContainer;
import org.dllearner.algorithms.distributed.containers.RefinementAndScoreContainer;
import org.dllearner.algorithms.distributed.containers.RefinementDataContainer;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractHeuristic;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.Score;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassAsInstanceLearningProblem;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.refinementoperators.CustomHierarchyRefinementOperator;
import org.dllearner.refinementoperators.CustomStartRefinementOperator;
import org.dllearner.refinementoperators.LengthLimitedRefinementOperator;
import org.dllearner.refinementoperators.OperatorInverter;
import org.dllearner.refinementoperators.ReasoningBasedRefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
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

import com.google.common.collect.Sets;

/**
 * The CELOE (Class Expression Learner for Ontology Engineering) algorithm.
 * It adapts and extends the standard supervised learning algorithm for the
 * ontology engineering use case.
 *
 * @author Jens Lehmann
 * @author Patrick Westphal
 *
 */
public class DistScoreAndRefinementCELOEAMQP extends AbstractMultiChannelAMQPAgent
        implements Cloneable {

    private static Logger logger = LoggerFactory.getLogger(DistScoreAndRefinementCELOEAMQP.class);

    private double bestAccuracy = Double.MIN_VALUE;
    private OWLClassExpression bestDescription;
    private OWLClass classToDescribe;

    /**
     * all descriptions in the search tree plus those which were too weak
     * (for fast redundancy check) */
    private TreeSet<OWLClassExpression> descriptions;

    /**
     * examples are either
     * 1.) instances of the class to describe
     * 2.) positive examples
     * 3.) union of pos.+neg. examples depending on the learning problem at hand
     */
    private Set<OWLIndividual> examples;

    private boolean expandAccuracy100Nodes = false;
    private int expressionTests = 0;
    private int expressionTestCountLastImprovement;

    @ConfigOption(name = "filterDescriptionsFollowingFromKB",
            defaultValue="false", description="If true, then the results " +
                    "will not contain suggestions, which already follow " +
                    "logically from the knowledge base. Be careful, since " +
                    "this requires a potentially expensive consistency check " +
                    "for candidate solutions.")
    private boolean filterDescriptionsFollowingFromKB = false;

    private boolean filterFollowsFromKB = false;

    /**
     * forces that one solution cannot be subexpression of another
     * expression; this option is useful to get diversity but it can also
     * suppress quite useful expressions */
    private boolean forceMutualDifference = false;

    private AbstractHeuristic heuristic;
    private double currentHighestAccuracy;
    private boolean isClassLearningProblem;
    private boolean isEquivalenceProblem;
    private boolean isRunning = false;

    @ConfigOption(name = "maxClassExpressionTests", defaultValue="0",
            description="The maximum number of candidate hypothesis the " +
                    "algorithm is allowed to test (0 = no limit). The " +
                    "algorithm will stop afterwards. (The real number of " +
                    "tests can be slightly higher, because this criterion " +
                    "usually won't be checked after each single test.)")
    private int maxClassExpressionTests = 0;

    @ConfigOption(name = "maxClassExpressionTestsAfterImprovement",
            defaultValue="0", description = "The maximum number of " +
                    "candidate hypothesis the algorithm is allowed after an " +
                    "improvement in accuracy (0 = no limit). The algorithm " +
                    "will stop afterwards. (The real number of tests can be " +
                    "slightly higher, because this criterion usually won't " +
                    "be checked after each single test.)")
    private int maxClassExpressionTestsAfterImprovement = 0;

    @ConfigOption(name = "maxDepth", defaultValue="7",
            description="maximum depth of description")
    private double maxDepth = 7;

    @ConfigOption(defaultValue = "10", name = "maxExecutionTimeInSeconds",
            description = "maximum execution of the algorithm in seconds")
    private int maxExecutionTimeInSeconds = 10;

    @ConfigOption(defaultValue = "0",
            name = "maxExecutionTimeInSecondsAfterImprovement",
            description = "maximum execution of the algorithm in seconds")
    private int maxExecutionTimeInSecondsAfterImprovement = 0;

    private int maxHorizExp = 0;

    @ConfigOption(name = "maxNrOfResults", defaultValue="10",
            description="Sets the maximum number of results one is " +
                    "interested in. (Setting this to a lower value may " +
                    "increase performance as the learning algorithm has to " +
                    "store/evaluate/beautify less descriptions).")
    private int maxNrOfResults = 10;

    /**
     * Maximum number of pending score requests. Within one learning step it is
     * guaranteed that not more pending (i.e. not yet answered) request exist.
     * This means that the master will have to wait until enough answers came
     * in, not being able to feed further workers.
     */
    private final int maxPendingScoreResponsesThreshold = 1000;

    private int minHorizExp = 0;
    private OWLClassExpressionMinimizer minimizer;
    private OENode nextNode;

    /** all nodes in the search tree (used for selecting most promising node) */
    private TreeSet<OENode> nodes;
    private int nodesHash;
    private HashSet<OENode> nodesWPenfindRefinementRequest;
    private double noise;

    @ConfigOption(name = "noisePercentage", defaultValue="0.0",
            description="the (approximated) percentage of noise within the examples")
    private double noisePercentage = 0.0;

    private LengthLimitedRefinementOperator operator;
    private int pendingScoreRequests;
    private final String refinementRequetsQ = "refinement_requests";
    private final String refinementResponsesQ = "refinement_responses";
    /**
     * defines whether a worker has the tendency to serve refinement requests
     * (<0.5) or to serve scoring requests (>0.5)
     */
    private double refinementScoreRatio = 0.5;
    private TreeSet<OWLClassExpression> refinementsWPendingScorerequests;


    @ConfigOption(name = "replaceSearchTree", defaultValue="false",
            description="specifies whether to replace the search tree in the " +
                    "log file after each run or append the new search tree")
    private boolean replaceSearchTree = false;

    @ConfigOption(name = "reuseExistingDescription", defaultValue="false",
            description="If true, the algorithm tries to find a good " +
                    "starting point close to an existing definition/super " +
                    "class of the given class in the knowledge base.")
    private boolean reuseExistingDescription = false;

    private final String scoreRequestsQ = "score_requests";
    private final String scoreResponsessQ = "score_responses";

    @ConfigOption(name = "searchTreeFile", defaultValue="log/searchTree.txt",
            description="file to use for the search tree")
    private String searchTreeFile = "log/searchTree.txt";

    @ConfigOption(name = "singleSuggestionMode", defaultValue="false",
            description="Use this if you are interested in only one " +
                    "suggestion and your learning problem has many (more " +
                    "than 1000) examples.")
    private boolean singleSuggestionMode;

    /** the class with which we start the refinement process */
    @ConfigOption(name = "startClass", defaultValue="owl:Thing",
            description="You can specify a start class for the algorithm. To " +
                    "do this, you have to use Manchester OWL syntax without " +
                    "using prefixes.")
    private OWLClassExpression startClass;

    /** root of search tree */
    private OENode startNode;

    private boolean stop = false;

    @ConfigOption(name = "stopOnFirstDefinition", defaultValue="false",
            description="algorithm will terminate immediately when a correct " +
                    "definition is found")
    private boolean stopOnFirstDefinition = false;

    @ConfigOption(name = "terminateOnNoiseReached", defaultValue="false",
            description="specifies whether to terminate when noise criterion " +
                    "is met")
    private boolean terminateOnNoiseReached = false;

    private long timeLastImprovement = 0;
    private long totalRuntimeNs = 0;

    @ConfigOption(name="useMinimizer", defaultValue="true",
            description="Specifies whether returned expressions should be " +
                    "minimised by removing those parts, which are not " +
                    "needed. (Basically the minimiser tries to find the " +
                    "shortest expression which is equivalent to the learned " +
                    "expression). Turning this feature off may improve " +
                    "performance.")
    private boolean useMinimizer = true;

    @ConfigOption(name = "writeSearchTree", defaultValue="false",
            description="specifies whether to write a search tree")
    private boolean writeSearchTree = false;

    /**
     * important: do not initialise this with empty sets!
     * null = no settings for allowance / ignorance
     * empty set = allow / ignore nothing (it is often not desired to allow no
     * class!) */
    Set<OWLClass> allowedConcepts = null;
    /**
     * important: do not initialise this with empty sets!
     * null = no settings for allowance / ignorance
     * empty set = allow / ignore nothing (it is often not desired to allow no
     * class!) */
    Set<OWLClass> ignoredConcepts = null;

    public DistScoreAndRefinementCELOEAMQP() {

    }

    public DistScoreAndRefinementCELOEAMQP(DistScoreAndRefinementCELOEAMQP celoe) {
        setReasoner(celoe.reasoner);
        setLearningProblem(celoe.learningProblem);

        setAllowedConcepts(celoe.getAllowedConcepts());
        setAllowedObjectProperties(celoe.getAllowedObjectProperties());
        setAllowedDataProperties(celoe.getAllowedDataProperties());

        setIgnoredConcepts(celoe.ignoredConcepts);
        setIgnoredObjectProperties(celoe.getIgnoredObjectProperties());
        setIgnoredDataProperties(celoe.getIgnoredDataProperties());

        setExpandAccuracy100Nodes(celoe.expandAccuracy100Nodes);
        setFilterDescriptionsFollowingFromKB(
                celoe.filterDescriptionsFollowingFromKB);
        setHeuristic(celoe.heuristic);

        setMaxClassExpressionTests(celoe.maxClassExpressionTests);
        setMaxClassExpressionTestsAfterImprovement(
                celoe.maxClassExpressionTestsAfterImprovement);
        setMaxDepth(celoe.maxDepth);
        setMaxExecutionTimeInSeconds(celoe.maxExecutionTimeInSeconds);
        setMaxExecutionTimeInSecondsAfterImprovement(
                celoe.maxClassExpressionTestsAfterImprovement);
        setMaxNrOfResults(celoe.maxNrOfResults);
        setNoisePercentage(celoe.noisePercentage);

        RhoDRDown op = new RhoDRDown((RhoDRDown) celoe.operator);
        try {
            op.init();
        } catch (ComponentInitException e) {
            e.printStackTrace();
        }
        setOperator(op);

        setReuseExistingDescription(celoe.reuseExistingDescription);
        setSingleSuggestionMode(celoe.singleSuggestionMode);
        setStartClass(celoe.startClass);
        setStopOnFirstDefinition(celoe.stopOnFirstDefinition);
        setTerminateOnNoiseReached(celoe.terminateOnNoiseReached);
        setUseMinimizer(celoe.useMinimizer);

        setWriteSearchTree(celoe.writeSearchTree);
        setReplaceSearchTree(celoe.replaceSearchTree);

    }

    public DistScoreAndRefinementCELOEAMQP(AbstractClassExpressionLearningProblem problem,
            AbstractReasonerComponent reasoner) {

        super(problem, reasoner);
    }

    // <------------------------- interface methods ------------------------->
    @Override
    public void start() {
        stop = false;
        isRunning = true;
        reset();

        nanoStartTime = System.nanoTime();

        try {
            // <-------- AMQP specific -------->
            addTopic(refinementRequetsQ);
            addTopic(refinementResponsesQ);
            addTopic(scoreRequestsQ);
            addTopic(scoreResponsessQ);
            initMessaging();
            pendingScoreRequests = 0;

            if (isMaster()) startMaster();
            else listenForRequests();

            finalizeMessaging();
            // </------- AMQP specific -------->
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void init() throws ComponentInitException {
        baseURI = reasoner.getBaseURI();
        prefixes = reasoner.getPrefixes();

        if (maxExecutionTimeInSeconds != 0 &&
                maxExecutionTimeInSecondsAfterImprovement != 0) {

            maxExecutionTimeInSeconds = Math.min(
                    maxExecutionTimeInSeconds,
                    maxExecutionTimeInSecondsAfterImprovement);
        }

        // copy class hierarchy and modify it such that each class is only
        // reachable via a single path
        ClassHierarchy classHierarchy = initClassHierarchy();
        ObjectPropertyHierarchy objectPropertyHierarchy = initObjectPropertyHierarchy();
        DatatypePropertyHierarchy datatypePropertyHierarchy = initDataPropertyHierarchy();

        // if no one injected a heuristic, we use a default one
        if (heuristic == null) {
            heuristic = new OEHeuristicRuntime();
        }

        minimizer = new OWLClassExpressionMinimizer(dataFactory, reasoner);

        // start at owl:Thing by default
        if (startClass == null) {
            startClass = dataFactory.getOWLThing();
        }

        // create a refinement operator and pass all configuration variables to it
        if (operator == null) {
            // we use a default operator and inject the class hierarchy for now
            operator = new RhoDRDown();
            if (operator instanceof CustomStartRefinementOperator) {
                ((CustomStartRefinementOperator) operator).setStartClass(startClass);
            }

            if (operator instanceof ReasoningBasedRefinementOperator) {
                ((ReasoningBasedRefinementOperator) operator).setReasoner(reasoner);
            }
            operator.init();
        }

        if (operator instanceof CustomHierarchyRefinementOperator) {
            ((CustomHierarchyRefinementOperator) operator).setClassHierarchy(classHierarchy);
            ((CustomHierarchyRefinementOperator) operator).setObjectPropertyHierarchy(
                    objectPropertyHierarchy);
            ((CustomHierarchyRefinementOperator) operator).setDataPropertyHierarchy(
                    datatypePropertyHierarchy);
        }

        if (writeSearchTree) {
            File f = new File(searchTreeFile);

            if (f.getParentFile() != null) {
                f.getParentFile().mkdirs();
            }
            Files.clearFile(f);
        }

        bestEvaluatedDescriptions = new EvaluatedDescriptionSet(maxNrOfResults);

        isClassLearningProblem = (learningProblem instanceof ClassLearningProblem);
        noise = noisePercentage/100d;
        filterFollowsFromKB =
                filterDescriptionsFollowingFromKB && isClassLearningProblem;

        // actions specific to ontology engineering
        if (isClassLearningProblem) {
            ClassLearningProblem problem = (ClassLearningProblem) learningProblem;
            classToDescribe = problem.getClassToDescribe();
            isEquivalenceProblem = problem.isEquivalenceProblem();

            examples = reasoner.getIndividuals(classToDescribe);

            // start class: intersection of super classes for definitions
            // (since it needs to capture all instances), but owl:Thing for
            // learning subclasses (since it is superfluous to add super
            // classes in this case)
            if (isEquivalenceProblem) {
                Set<OWLClassExpression> existingDefinitions =
                        reasoner.getAssertedDefinitions(classToDescribe);

                if (reuseExistingDescription && (existingDefinitions.size() > 0)) {
                    // the existing definition is reused, which in the
                    // simplest case means to use it as a start class or,
                    // if it is already too specific, generalise it

                    // pick the longest existing definition as candidate
                    OWLClassExpression existingDefinition = null;
                    int highestLength = 0;

                    for (OWLClassExpression exDef : existingDefinitions) {
                        if (OWLClassExpressionUtils.getLength(exDef) > highestLength) {
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
                        double candidateRecall = ((ClassLearningProblem) learningProblem).getRecall(candidate);
                        if (candidateRecall < 1.0) {
                            // add upward refinements to list
                            Set<OWLClassExpression> refinements =
                                    upwardOperator.refine(
                                            candidate,
                                            OWLClassExpressionUtils.getLength(candidate));

                            LinkedList<OWLClassExpression> refinementList =
                                    new LinkedList<OWLClassExpression>(refinements);

                            startClassCandidates.addAll(refinementList);
                        } else {
                            startClassFound = true;
                        }
                    } while (!startClassFound);

                    startClass = candidate;

                    if (startClass.equals(existingDefinition)) {
                        logger.info("Reusing existing OWLClassExpression " +
                                OWLAPIRenderers.toManchesterOWLSyntax(startClass) +
                                " as start class for learning algorithm.");
                    } else {
                        logger.info("Generalised existing OWLClassExpression " +
                                OWLAPIRenderers.toManchesterOWLSyntax(existingDefinition) +
                                " to " + OWLAPIRenderers.toManchesterOWLSyntax(startClass) +
                                ", which is used as start class for the learning algorithm.");
                    }

                    if (operator instanceof RhoDRDown) {
                        ((RhoDRDown) operator).setDropDisjuncts(false);
                    }

                } else {
                    Set<OWLClassExpression> superClasses =
                            reasoner.getClassHierarchy().getSuperClasses(classToDescribe);

                    if (superClasses.size() > 1) {
                        startClass = dataFactory.getOWLObjectIntersectionOf(superClasses);

                    } else if (superClasses.size() == 1){
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
            examples = ((PosOnlyLP) learningProblem).getPositiveExamples();

        } else if(learningProblem instanceof PosNegLP) {
            examples = Sets.union(
                    ((PosNegLP) learningProblem).getPositiveExamples(),
                    ((PosNegLP) learningProblem).getNegativeExamples());
        }
    }

    @Override
    public void stop() {
        stop = true;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public OWLClassExpression getCurrentlyBestDescription() {
        EvaluatedDescription<? extends Score> ed = getCurrentlyBestEvaluatedDescription();
        return ed == null ? null : ed.getDescription();
    }

    @Override
    public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
        return bestEvaluatedDescriptions.getBest();
    }

    @Override
    public List<OWLClassExpression> getCurrentlyBestDescriptions() {
        return bestEvaluatedDescriptions.toDescriptionList();
    }

    @Override
    public NavigableSet<? extends EvaluatedDescription<? extends Score>> getCurrentlyBestEvaluatedDescriptions() {
        return bestEvaluatedDescriptions.getSet();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new DistScoreAndRefinementCELOEAMQP(this);
    }
    // </------------------------ interface methods ------------------------->

    // <------------------------- AMPQ master methods ----------------------->
    private void startMaster() throws Exception {
        // some initializations
        currentHighestAccuracy = 0.0;

        // <--------- addNode code for startClass --------->
        double accuracy = learningProblem.getAccuracyOrTooWeak(startClass, noise);
        expressionTests++;
        if(accuracy == -1) {
            throw new Exception("start concept cannot be too weak");
        }
        startNode = new OENode(null, startClass, accuracy);
        nodes.add(startNode);
        if(singleSuggestionMode) {
            bestAccuracy = accuracy;
            bestDescription = startClass;
        }

        OWLClassExpression niceDescription = rewriteNode(startNode);

        if(niceDescription.equals(classToDescribe)) {
            throw new Exception("start concept must not equal the class to describe");
        }

        if(!isDescriptionAllowed(niceDescription, startNode)) {
            throw new Exception("start concept not allowed");
        }

        boolean shorterDescriptionExists = false;
        if(forceMutualDifference) {
            for(EvaluatedDescription<? extends Score> ed : bestEvaluatedDescriptions.getSet()) {

                boolean isSubDescr = ConceptTransformation.isSubdescription(
                                niceDescription, ed.getDescription());

                if(Math.abs(ed.getAccuracy()-accuracy) <= 0.00001 && isSubDescr) {
                    shorterDescriptionExists = true;
                    break;
                }
            }
        }

        //                              added due to class cast exception
        if(!shorterDescriptionExists && isClassLearningProblem) {
            boolean descrFollowsFromKB =
                    ((ClassLearningProblem) learningProblem).followsFromKB(niceDescription);

            if(!filterFollowsFromKB || !descrFollowsFromKB) {
                bestEvaluatedDescriptions.add(niceDescription, accuracy, learningProblem);
            }
        }
        // </-------- addNode code for startClass --------->

        nodesHash = 0;

        // loop while term criteria not satisfied
        while (!terminationCriteriaSatisfied()) {
            performLearningStep();

            if(singleSuggestionMode) {
                bestEvaluatedDescriptions.add(
                        bestDescription, bestAccuracy, learningProblem);
            }
        }

        // print out solutions
        if (stop) {
            logger.info("Algorithm stopped (" +expressionTests +
                    " descriptions tested). " + nodes.size() + " nodes in " +
                    "the search tree.\n");
        } else {
            totalRuntimeNs = System.nanoTime()-nanoStartTime;
            logger.info("Algorithm terminated successfully (time: " +
                    Helper.prettyPrintNanoSeconds(totalRuntimeNs) + ", " +
                    expressionTests+" descriptions tested, "  + nodes.size() +
                    " nodes in the search tree).\n");
            logger.info(reasoner.toString());
        }

        if(singleSuggestionMode) {
            bestEvaluatedDescriptions.add(bestDescription, bestAccuracy,
                    learningProblem);
        }

        logger.info("solutions:\n" + getSolutionString());
        logger.info("Master sent " + getSentMessagesCount() + " messages");

        // terminate other agents and self
        terminateAgents();
        isRunning = false;
    }

    private void performLearningStep() throws Exception {
        showIfBetterSolutionsFound();

        // get next node
        nextNode = getNextNodeToExpand();
        if (nextNode != null) {
            // request refinements
            NodeContainer nodeContainer = new NodeContainer(nextNode);
            nonBlockingSend(nodeContainer, refinementRequetsQ);
        }

        // receive refinements
        RefinementDataContainer refinementData;
        RefinementAndScoreContainer refinementScore;
        OENode refinedNode;
        TreeSet<OWLClassExpression> refinements;

        while (true) {
            refinementData = (RefinementDataContainer) nonBlockingReceive(refinementResponsesQ);

            if (refinementData == null) break;

            refinedNode = refinementData.getRefinedNode();
            nodesWPenfindRefinementRequest.remove(refinedNode);
            int horizExp = refinedNode.getHorizontalExpansion();
            refinements = refinementData.getRefinements();
            nodes.remove(refinedNode);
            refinedNode.incHorizontalExpansion();
            refinedNode.setRefinementCount(refinements.size());
            nodes.add(refinedNode);

            for (OWLClassExpression refinement : refinements) {
                if (refinementsWPendingScorerequests.contains(refinement)) {
                    continue;
                } else {
                    refinementsWPendingScorerequests.add(refinement);
                }
                // get length of class expression
                int length = OWLClassExpressionUtils.getLength(refinement);

                // ignore all refinements with lower length and too high depth
                // (this also avoids duplicate node children)
                if(!(length > horizExp &&
                        OWLClassExpressionUtils.getDepth(refinement) <= maxDepth)) {
                    continue;
                }

                refinementScore = new RefinementAndScoreContainer(refinement,
                        refinementData.getRefinedNode());
                nonBlockingSend(refinementScore, scoreRequestsQ);
                pendingScoreRequests++;
                if (terminationCriteriaSatisfied()) break;
            }

            // write the search tree (if configured)
            if (writeSearchTree) {
                writeSearchTree(refinements);
            }

            if (terminationCriteriaSatisfied()) break;
        }

        while (true) {
            refinementScore =
                    (RefinementAndScoreContainer) nonBlockingReceive(scoreResponsessQ);

            if (refinementScore == null) break;

            processRefinementScore(refinementScore);

            if (terminationCriteriaSatisfied()) break;
        }

        // force to reduce the number of pending score requests (switch to
        // blocking receive for a while)
        if (pendingScoreRequests > maxPendingScoreResponsesThreshold) {
            while (pendingScoreRequests < (maxPendingScoreResponsesThreshold * .75)) {
                refinementScore =
                        (RefinementAndScoreContainer) blockingReceive(scoreResponsessQ);
                processRefinementScore(refinementScore);

                if (terminationCriteriaSatisfied()) break;
            }
        }

        showIfBetterSolutionsFound();

        if (nextNode != null) updateMinMaxHorizExp(nextNode);
    }

    private OENode getNextNodeToExpand() throws Exception {
        // first check if nodes changed
        int currHash = nodes.hashCode();
        if (nodesHash == currHash) return null;

        nodesHash = currHash;

        /* we expand the best node of those, which have not achieved 100%
         * accuracy already and have a horizontal expansion equal to their
         * length (rationale: further extension is likely to add irrelevant
         * syntactical constructs) */
        Iterator<OENode> it = nodes.descendingIterator();

        while(it.hasNext()) {
            OENode node = it.next();

            // skip nodes that were already sent to be refined
            if (nodesWPenfindRefinementRequest.contains(node)) continue;

            int nodeLength = OWLClassExpressionUtils.getLength(node.getDescription());

            if (isExpandAccuracy100Nodes() &&
                    node.getHorizontalExpansion() < nodeLength) {
                return node;

            } else {
                int descLength =
                        OWLClassExpressionUtils.getLength(node.getDescription());

                if(node.getAccuracy() < 1.0 ||
                        node.getHorizontalExpansion() < descLength) {
                    return node;
                }
            }
        }

        // this should practically never be called, since for any reasonable learning
        // task, we will always have at least one node with less than 100% accuracy
        throw new Exception("Foooooo");
//        return null;//nodes.last();
    }

    private void processRefinementScore(RefinementAndScoreContainer refScore) {
        /* This may happen since the blocking receive in an AMQP setup as
         * currently implemented just blocks until a timeout is reached and
         * then goes on */
        if (refScore == null) return;

        pendingScoreRequests--;
        OWLClassExpression description = refScore.getRefinement();
        refinementsWPendingScorerequests.remove(description);

        OENode parentNode = refScore.getParentNode();
        double accuracy = refScore.getAccuracy();

        // <------- copied from addNode(...) ------->
        // redundancy check (return if redundant)
        boolean nonRedundant = descriptions.add(description);
        if(!nonRedundant) {
            logger.debug(description + " redundant");
            return;
        }

        // TODO: the isDescriptionAllowed check could also be parallelized
        // check whether the OWLClassExpression is allowed
        if(!isDescriptionAllowed(description, parentNode)) {
            logger.debug(description + " not allowed");
            return;
        }

        // issue a warning if accuracy is not between 0 and 1 or -1 (too weak)
        if(accuracy > 1.0 || (accuracy < 0.0 && accuracy != -1)) {
            logger.warn("Invalid accuracy value " + accuracy + " for " +
                    "OWLClassExpression " + description + ". This could be " +
                    "caused by a bug in the heuristic measure and should be " +
                    "reported to the DL-Learner bug tracker.");
            System.exit(0);
        }

        expressionTests++;
        if(accuracy == -1) {
            logger.debug(description + " too weak");
            return;
        }

        OENode node = new OENode(parentNode, description, accuracy);
        parentNode.addChild(node);
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
            } else {
                logger.info("single suggestion mode; " + description + " not best");
            }
            return;
        }
        /* maybe add to best descriptions (method keeps set size fixed);
         * we need to make sure that this does not get called more often than
         * necessary since rewriting is expensive */
        boolean isCandidate = !bestEvaluatedDescriptions.isFull();
        if(!isCandidate) {
            EvaluatedDescription worst = bestEvaluatedDescriptions.getWorst();
            double accThreshold = worst.getAccuracy();
            isCandidate =
                (accuracy > accThreshold ||
                (accuracy >= accThreshold
                    && OWLClassExpressionUtils.getLength(description) < worst.getDescriptionLength()));
        }

        if(isCandidate) {
            OWLClassExpression niceDescription = rewriteNode(node);

            if(niceDescription.equals(classToDescribe)) {
                logger.info(description + " equals class to describe");
                return;
            }

            if(!isDescriptionAllowed(niceDescription, node)) {
                return;
            }

            /* another test: none of the other suggested descriptions should be
             * a subdescription of this one unless accuracy is different
             * => comment: on the one hand, this appears to be too strict,
             * because once A is a solution then everything containing A is
             * not a candidate; on the other hand this suppresses many
             * meaningless extensions of A */
            boolean shorterDescriptionExists = false;
            if(forceMutualDifference) {
                for(EvaluatedDescription<? extends Score> ed : bestEvaluatedDescriptions.getSet()) {
                    if(Math.abs(ed.getAccuracy()-accuracy) <= 0.00001
                            && ConceptTransformation.isSubdescription(niceDescription, ed.getDescription())) {
                        shorterDescriptionExists = true;
                        break;
                    }
                }
            }

            if (!shorterDescriptionExists) {
                if(!filterFollowsFromKB ||
                        !((ClassLearningProblem)learningProblem).followsFromKB(niceDescription)) {
                    bestEvaluatedDescriptions.add(niceDescription, accuracy, learningProblem);
                    logger.info("Added " + description + " to best evaluated descriptions");
                }
            } else {
                logger.info("Shorter description for " + description + " exists");
            }
        } else {
            logger.info(description + " wasn't a candidate");
        }
        // </------ copied from addNode(...) ------->
    }

    /**
     * checks whether the OWLClassExpression is allowed
     *
     * TODO: parallelize
     */
    private boolean isDescriptionAllowed(OWLClassExpression description, OENode parentNode) {
        if(isClassLearningProblem) {
            if(isEquivalenceProblem) {
                // the class to learn must not appear on the outermost property level
                if(occursOnFirstLevel(description, classToDescribe)) {
                    return false;
                }
            } else {
                // none of the superclasses of the class to learn must appear on
                // the outermost property level
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
        } else if (learningProblem instanceof ClassAsInstanceLearningProblem) {
            return true;
        }

        // perform forall sanity tests
        boolean forAllSanTrue =
                ConceptTransformation.getForallOccurences(description) >
                    ConceptTransformation.getForallOccurences(parentNode.getDescription());

        if(parentNode != null && forAllSanTrue) {
            // we have an additional \forall construct, so we now fetch the contexts
            // in which it occurs
            SortedSet<PropertyContext> contexts =
                    ConceptTransformation.getForallContexts(description);
            SortedSet<PropertyContext> parentContexts =
                    ConceptTransformation.getForallContexts(parentNode.getDescription());
            contexts.removeAll(parentContexts);
            // we now have to perform sanity checks: if \forall is used, then
            // there should be at least on class instance which has a filler at
            // the given context
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
                // if we do not find a filler, this means that putting \forall
                // at that position is not meaningful
                if(!fillerFound) {
                    return false;
                }
            }
        }

        return true;
    }
    // </------------------------ AMPQ master methods ----------------------->

    // <------------------------- AMPQ worker methods ----------------------->
    private void listenForRequests() throws JMSException {
        while (true) {
            if (checkTerminateMsg()) break;

            if (Math.random() > refinementScoreRatio) {
                listenForRefinementRequests();
            } else {
                listenForScoreRequests();
            }
        }

        logger.info("Worker " + myID + " terminated");
        logger.info("Worker " + myID + " received " +
                getReceivedMessagesCount() + " messages");
        isRunning = false;
    }

    private void listenForRefinementRequests() throws JMSException {
        NodeContainer nodeContainer = (NodeContainer) blockingReceive(refinementRequetsQ);

        if (nodeContainer == null) return;

        OENode nodeToRefine = nodeContainer.getNode();

        int origNodeLen = nodeToRefine.getHorizontalExpansion();
        TreeSet<OWLClassExpression> refinements = refineNodeDistributed(nodeToRefine);

        nonBlockingSend(new RefinementDataContainer(nodeToRefine,
                refinements, origNodeLen), refinementResponsesQ);
    }

    private TreeSet<OWLClassExpression> refineNodeDistributed(OENode node) {
        int horizExp = node.getHorizontalExpansion();
        TreeSet<OWLClassExpression> refinements =
                (TreeSet<OWLClassExpression>) operator.refine(
                        node.getDescription(), horizExp+1);
        return refinements;
    }

    private void listenForScoreRequests() throws JMSException {
        RefinementAndScoreContainer refinementScoreContainer =
                (RefinementAndScoreContainer) blockingReceive(scoreRequestsQ);

        if (refinementScoreContainer == null) return;

        OWLClassExpression description = refinementScoreContainer.getRefinement();
        double accuracy = learningProblem.getAccuracyOrTooWeak(description, noise);
        refinementScoreContainer.setAccuracy(accuracy);

        nonBlockingSend(refinementScoreContainer, scoreResponsessQ);
    }
    // </------------------------ AMPQ worker methods ----------------------->

    // <---------------------------- misc methods --------------------------->
    public static String getName() {
        return "CELOE";
    }

    public double getCurrentlyBestAccuracy() {
        return bestEvaluatedDescriptions.getBest().getAccuracy();
    }

    /**
     * determine whether a named class occurs on the outermost level, i.e.
     * property depth 0
     * (it can still be at higher depth, e.g. if intersections are nested in
     * unions)
     */
    private boolean occursOnFirstLevel(OWLClassExpression description, OWLClassExpression cls) {
        if(cls.isOWLThing()) {
            return false;
        }
        return (description instanceof OWLNaryBooleanClassExpression &&
                ((OWLNaryBooleanClassExpression)description).getOperands().contains(cls));
    }

    /**
     * Sets all values back to their default values (used for running the
     * algorithm more than once) */
    private void reset() {
        nodes = new TreeSet<OENode>(heuristic);
        nodesWPenfindRefinementRequest = new HashSet<OENode>();
        refinementsWPendingScorerequests = new TreeSet<OWLClassExpression>();
        descriptions = new TreeSet<OWLClassExpression>();
        bestEvaluatedDescriptions.getSet().clear();
        expressionTests = 0;
    }

    /** check whether the node is a potential solution candidate */
    private OWLClassExpression rewriteNode(OENode node) {
        OWLClassExpression description = node.getDescription();
        // minimize OWLClassExpression (expensive!) - also performs some human friendly rewrites
        OWLClassExpression niceDescription;
        if(useMinimizer) {
            niceDescription = minimizer.minimizeClone(description);
        } else {
            niceDescription = description;
        }
        System.out.println(node + ":" + niceDescription);
        // replace \exists r.\top with \exists r.range(r) which is easier to read for humans
        niceDescription = ConceptTransformation.replaceRange(niceDescription, reasoner);
        return niceDescription;
    }

    private void showIfBetterSolutionsFound() {
        if(!singleSuggestionMode &&
                bestEvaluatedDescriptions.getBestAccuracy() > currentHighestAccuracy) {

            currentHighestAccuracy = bestEvaluatedDescriptions.getBestAccuracy();
            expressionTestCountLastImprovement = expressionTests;

            timeLastImprovement = System.nanoTime();
            long durationInMillis = getCurrentRuntimeInMilliSeconds();
            String durationStr = getDurationAsString(durationInMillis);

            logger.info("more accurate (" +
                    dfPercent.format(currentHighestAccuracy) +
                    ") class expression found after " + durationStr + ": " +
                    descriptionToString(bestEvaluatedDescriptions.getBest().getDescription()));
        }
    }

    public static Collection<Class<? extends AbstractClassExpressionLearningProblem>> supportedLearningProblems() {

        Collection<Class<? extends AbstractClassExpressionLearningProblem>> problems =
                new LinkedList<Class<? extends AbstractClassExpressionLearningProblem>>();

        problems.add(AbstractClassExpressionLearningProblem.class);
        return problems;
    }

    private boolean terminationCriteriaSatisfied() {
        boolean exprTestsAfterImprovementExhausted =
                (maxClassExpressionTestsAfterImprovement != 0 &&
                    (expressionTests - expressionTestCountLastImprovement >=
                        maxClassExpressionTestsAfterImprovement));
        boolean exprTestsExhausted =
                (maxClassExpressionTests != 0 &&
                    (expressionTests >= maxClassExpressionTests));
        boolean execTimeAfterImprovementExhausted =
                (maxExecutionTimeInSecondsAfterImprovement != 0 &&
                    ((System.nanoTime() - nanoStartTime) >=
                        (maxExecutionTimeInSecondsAfterImprovement*1000000000l)));
        boolean execTimeExhausted =
                (maxExecutionTimeInSeconds != 0 &&
                    ((System.nanoTime() - nanoStartTime) >=
                        (maxExecutionTimeInSeconds*1000000000l)));
        boolean noiseReached =
                (terminateOnNoiseReached &&
                        (100*getCurrentlyBestAccuracy() >= 100-noisePercentage));
        return stop || exprTestsAfterImprovementExhausted || exprTestsExhausted
                || execTimeExhausted || execTimeAfterImprovementExhausted
                || noiseReached ||
                (stopOnFirstDefinition && (getCurrentlyBestAccuracy() >= 1));
    }

    private void updateMinMaxHorizExp(OENode node) {
        int newHorizExp = node.getHorizontalExpansion();

        // update maximum value
        maxHorizExp = Math.max(maxHorizExp, newHorizExp);

        // we just expanded a node with minimum horizontal expansion;
        // we need to check whether it was the last one
        if(minHorizExp == newHorizExp - 1) {

            // the best accuracy that a node can achieve
            double scoreThreshold = heuristic.getNodeScore(node) + 1 -
                    node.getAccuracy();

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

            // inc. minimum since we found no other node which also has min.
            // horiz. exp.
            minHorizExp++;
        }
    }

    private void writeSearchTree(TreeSet<OWLClassExpression> refinements) {
        StringBuilder treeString = new StringBuilder("best node: ").append(
                bestEvaluatedDescriptions.getBest()).append("\n");

        if (refinements.size() > 1) {
            treeString.append("all expanded nodes:\n");
            for (OWLClassExpression ref : refinements) {
                treeString.append("   ").append(ref).append("\n");
            }
        }

        treeString.append(startNode.toTreeString(baseURI, prefixes)).append("\n");

        // replace or append
        if (replaceSearchTree) {
            Files.createFile(new File(searchTreeFile), treeString.toString());
        } else {
            Files.appendToFile(new File(searchTreeFile), treeString.toString());
        }
    }
    // </--------------------------- misc methods --------------------------->

    // <------------------------ getters and setters ------------------------>
    // allowedConcepts
    @Override
    public Set<OWLClass> getAllowedConcepts() {
        return allowedConcepts;
    }

    @Override
    public void setAllowedConcepts(Set<OWLClass> allowedConcepts) {
        this.allowedConcepts = allowedConcepts;
    }

    // expandAccuracy100Nodes
    /** @return the expandAccuracy100Nodes */
    public boolean isExpandAccuracy100Nodes() {
        return expandAccuracy100Nodes;
    }

    /** @param expandAccuracy100Nodes the expandAccuracy100Nodes to set */
    public void setExpandAccuracy100Nodes(boolean expandAccuracy100Nodes) {
        this.expandAccuracy100Nodes = expandAccuracy100Nodes;
    }

    // expressionTests
    public int getClassExpressionTests() {
        return expressionTests;
    }

    // filterDescriptionsFollowingFromKB
    public boolean isFilterDescriptionsFollowingFromKB() {
        return filterDescriptionsFollowingFromKB;
    }

    public void setFilterDescriptionsFollowingFromKB(
            boolean filterDescriptionsFollowingFromKB) {
        this.filterDescriptionsFollowingFromKB =
                filterDescriptionsFollowingFromKB;
    }

    // heuristic
    @Autowired(required=false)
    public void setHeuristic(AbstractHeuristic heuristic) {
        this.heuristic = heuristic;
    }

    // ignoredConcepts
    @Override
    public Set<OWLClass> getIgnoredConcepts() {
        return ignoredConcepts;
    }

    @Override
    public void setIgnoredConcepts(Set<OWLClass> ignoredConcepts) {
        this.ignoredConcepts = ignoredConcepts;
    }

    // maxClassExpressionTests
    public int getMaxClassExpressionTests() {
        return maxClassExpressionTests;
    }

    public void setMaxClassExpressionTests(int maxClassExpressionTests) {
        this.maxClassExpressionTests = maxClassExpressionTests;
    }

    // maxClassExpressionTestsAfterImprovement
    public int getMaxClassExpressionTestsAfterImprovement() {
        return maxClassExpressionTestsAfterImprovement;
    }

    public void setMaxClassExpressionTestsAfterImprovement(
            int maxClassExpressionTestsAfterImprovement) {
        this.maxClassExpressionTestsAfterImprovement =
                maxClassExpressionTestsAfterImprovement;
    }

    // maxDepth
    public double getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(double maxDepth) {
        this.maxDepth = maxDepth;
    }

    // maxExecutionTimeInSeconds
    @Override
	public int getMaxExecutionTimeInSeconds() {
        return maxExecutionTimeInSeconds;
    }

    @Override
	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
        this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
    }

    // maxExecutionTimeInSecondsAfterImprovement
    public int getMaxExecutionTimeInSecondsAfterImprovement() {
        return maxExecutionTimeInSecondsAfterImprovement;
    }

    public void setMaxExecutionTimeInSecondsAfterImprovement(
            int maxExecutionTimeInSecondsAfterImprovement) {
        this.maxExecutionTimeInSecondsAfterImprovement =
                maxExecutionTimeInSecondsAfterImprovement;
    }

    // maxNrOfResults
    public int getMaxNrOfResults() {
        return maxNrOfResults;
    }

    public void setMaxNrOfResults(int maxNrOfResults) {
        this.maxNrOfResults = maxNrOfResults;
    }

    // nodes
    public TreeSet<OENode> getNodes() {
        return nodes;
    }

    // noisePercentage
    public double getNoisePercentage() {
        return noisePercentage;
    }

    public void setNoisePercentage(double noisePercentage) {
        this.noisePercentage = noisePercentage;
    }

    // operator
    public LengthLimitedRefinementOperator getOperator() {
        return operator;
    }

    @Autowired(required=false)
    public void setOperator(LengthLimitedRefinementOperator operator) {
        this.operator = operator;
    }

    // refinementScoreRatio
    public void setRefinementScoreRatio(double ratio) {
        refinementScoreRatio = ratio;
    }

    // replaceSearchTree
    public boolean isReplaceSearchTree() {
        return replaceSearchTree;
    }

    public void setReplaceSearchTree(boolean replaceSearchTree) {
        this.replaceSearchTree = replaceSearchTree;
    }

    // reuseExistingDescription
    public boolean isReuseExistingDescription() {
        return reuseExistingDescription;
    }

    public void setReuseExistingDescription(boolean reuseExistingDescription) {
        this.reuseExistingDescription = reuseExistingDescription;
    }

    // searchTreeFile
    public String getSearchTreeFile() {
        return searchTreeFile;
    }

    public void setSearchTreeFile(String searchTreeFile) {
        this.searchTreeFile = searchTreeFile;
    }

    // singleSuggestionMode
    public boolean isSingleSuggestionMode() {
        return singleSuggestionMode;
    }

    public void setSingleSuggestionMode(boolean singleSuggestionMode) {
        this.singleSuggestionMode = singleSuggestionMode;
    }

    // startClass
    public OWLClassExpression getStartClass() {
        return startClass;
    }

    public void setStartClass(OWLClassExpression startClass) {
        this.startClass = startClass;
    }

    // stopOnFirstDefinition
    public boolean isStopOnFirstDefinition() {
        return stopOnFirstDefinition;
    }

    public void setStopOnFirstDefinition(boolean stopOnFirstDefinition) {
        this.stopOnFirstDefinition = stopOnFirstDefinition;
    }

    // terminateOnNoiseReached
    public boolean isTerminateOnNoiseReached() {
        return terminateOnNoiseReached;
    }

    public void setTerminateOnNoiseReached(boolean terminateOnNoiseReached) {
        this.terminateOnNoiseReached = terminateOnNoiseReached;
    }

    // useMinimizer
    @Override
	public boolean isUseMinimizer() {
        return useMinimizer;
    }

    @Override
	public void setUseMinimizer(boolean useMinimizer) {
        this.useMinimizer = useMinimizer;
    }

    // writeSearchTree
    public boolean isWriteSearchTree() {
        return writeSearchTree;
    }

    public void setWriteSearchTree(boolean writeSearchTree) {
        this.writeSearchTree = writeSearchTree;
    }

    // </----------------------- getters and setters ------------------------>
    // <---------------------------- main method ---------------------------->
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new Exception("Please provide an worker ID (int) as first " +
                    "parameter and an indicator whether DistScoreCELOE runs " +
                    "as master (1) or worker (0) as second parameter");
        }

        int id = Integer.parseInt(args[0]);
        boolean isMaster = Integer.parseInt(args[1]) > 0;

        ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
        File file = new File("father.owl");
        OWLClass classToDescribe = new OWLClassImpl(
                IRI.create("http://example.com/father#male"));

        OWLOntology ontology = OWLManager.createOWLOntologyManager().
                loadOntologyFromOntologyDocument(file);

        AbstractKnowledgeSource ks = new OWLAPIOntology(ontology);
        ks.init();

        OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
        baseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        baseReasoner.init();
        ClosedWorldReasoner rc = new ClosedWorldReasoner(ks);
        rc.setReasonerComponent(baseReasoner);
        rc.init();

        ClassLearningProblem lp = new ClassLearningProblem(rc);
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

        // (male ⊓ (∀ hasChild.⊤)) ⊔ (∃ hasChild.(∃ hasChild.male))
        OWLDataFactory df = new OWLDataFactoryImpl();
        OWLClassExpression ce = df.getOWLObjectUnionOf(
                df.getOWLObjectIntersectionOf(
                        df.getOWLClass(
                                IRI.create("http://example.com/father#male")),
                        df.getOWLObjectAllValuesFrom(
                                df.getOWLObjectProperty(
                                        IRI.create("http://example.com/father#hasChild")),
                                df.getOWLThing())),
                df.getOWLObjectSomeValuesFrom(
                        df.getOWLObjectProperty(
                                IRI.create("http://example.com/father#hasChild")),
                        df.getOWLObjectSomeValuesFrom(
                                df.getOWLObjectProperty(
                                        IRI.create("http://example.com/father#hasChild")),
                                df.getOWLClass(
                                        IRI.create("http://example.com/father#male"))
                                )
                        )
                );

        DistScoreAndRefinementCELOEAMQP alg =
                new DistScoreAndRefinementCELOEAMQP(lp, rc);
        alg.setMaxExecutionTimeInSeconds(60);
        alg.setOperator(op);
        alg.setWriteSearchTree(true);
        alg.setSearchTreeFile("log/search-tree.log");
        alg.setReplaceSearchTree(true);
        // AMQP specific
        alg.updateAMQPSettings("amqp.properties");
        if (isMaster) alg.setMaster();
        alg.setAgentID(id);

        alg.init();
        alg.start();
    }
    // </--------------------------- main method ---------------------------->
}
