package org.dllearner.algorithms.spatial;

import com.google.common.collect.Sets;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.core.*;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.refinementoperators.*;
import org.dllearner.refinementoperators.spatial.SpatialRhoDRDown;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.SearchTree;
import org.dllearner.utilities.owl.*;
import org.dllearner.vocabulary.spatial.SpatialVocabulary;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SpatialLearningAlgorithm extends AbstractCELA {
    private static final Logger logger = LoggerFactory.getLogger(SpatialLearningAlgorithm.class);

    @ConfigOption(defaultValue="celoe_heuristic")
    private AbstractHeuristic heuristic;

    @ConfigOption(defaultValue="0", description="The maximum number of " +
            "candidate hypothesis the algorithm is allowed to test (0 = no " +
            "limit). The algorithm will stop afterwards. (The real number " +
            "of tests can be slightly higher, because this criterion usually " +
            "won't be checked after each single test.)")
    private int maxClassExpressionTests = 0;

    @ConfigOption(defaultValue="7", description="maximum depth of description")
    private double maxDepth = 7;

    @ConfigOption(defaultValue="10", description="Sets the maximum number of " +
            "results one is interested in. (Setting this to a lower value " +
            "may increase performance as the learning algorithm has to " +
            "store/evaluate/beautify less descriptions).")
    private int maxNrOfResults = 10;

    @ConfigOption(defaultValue="0.0", description="the (approximated) " +
            "percentage of noise within the examples")
    private double noisePercentage = 0.0;

    @ConfigOption(description = "the refinement operator instance to use")
    private LengthLimitedRefinementOperator operator;

    @ConfigOption(defaultValue="false", description="If true, the algorithm " +
            "tries to find a good starting point close to an existing " +
            "definition/super class of the given class in the knowledge base.")
    private boolean reuseExistingDescription = false;

    private OWLClass classToDescribe;
    private double currentHighestAccuracy;

    // all descriptions in the search tree plus those which were too weak (for
    // fast redundancy check)
    private TreeSet<OWLClassExpression> descriptions;

    // examples are union of pos.+neg. examples
    private Set<OWLIndividual> examples;

    private int expressionTests = 0;
    private int maxHorizExp = 0;
    private int minHorizExp = 1;
    private double noise;
    private boolean keepTrackOfBestScore = false;
    private SortedMap<Long, Double> runtimeVsBestScore = new TreeMap<>();
    private SearchTree<OENode> searchTree;
    private OWLClassExpression startClass;
    private long totalRuntimeNs = 0;

    // -------------------------------------------------------------------------
    // -- getter/setter
    public void setKeepTrackOfBestScore(boolean keepTrackOfBestScore) {
        this.keepTrackOfBestScore = keepTrackOfBestScore;
    }

    public void setNoisePercentage(double noisePercentage) {
        this.noisePercentage = noisePercentage;
    }

    public void setOperator(LengthLimitedRefinementOperator operator) {
        this.operator = operator;
    }

    public void setStartClass(OWLClassExpression startClass) {
        this.startClass = startClass;
    }

    // -------------------------------------------------------------------------
    // -- misc private/protected methods
    private void reset() {
        // set all values back to their default values (used for running
        // the algorithm more than once)
        searchTree = new SearchTree<>(heuristic);
        descriptions = new TreeSet<>();
        bestEvaluatedDescriptions.getSet().clear();
        expressionTests = 0;
        runtimeVsBestScore.clear();
    }

    // checks whether the class expression is allowed
    private boolean isDescriptionAllowed(OWLClassExpression description, OENode parentNode) {
        // perform forall sanity tests
        if (parentNode != null &&
                (ConceptTransformation.getForallOccurences(description)
                        > ConceptTransformation.getForallOccurences(parentNode.getDescription()))) {

            // we have an additional \forall construct, so we now fetch the contexts
            // in which it occurs
            SortedSet<PropertyContext> contexts =
                    ConceptTransformation.getForallContexts(description);

            SortedSet<PropertyContext> parentContexts =
                    ConceptTransformation.getForallContexts(parentNode.getDescription());

            contexts.removeAll(parentContexts);

            // we now have to perform sanity checks: if \forall is used, then there
            // should be at least on class instance which has a filler at the given context
            for (PropertyContext context : contexts) {
                // transform [r,s] to \exists r.\exists s.\top
                OWLClassExpression existentialContext = context.toExistentialContext();

                boolean fillerFound = false;

                for(OWLIndividual instance : examples) {
                    if(reasoner.hasType(existentialContext, instance)) {
                        fillerFound = true;
                        break;
                    }
                }

                // if we do not find a filler, this means that putting \forall at
                // that position is not meaningful
                if(!fillerFound) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Add node to search tree if it is not too weak.
     * @return TRUE if node was added and FALSE otherwise
     */
    private boolean addNode(OWLClassExpression description, OENode parentNode) {
        MonitorFactory.getTimeMonitor("addNode").start();

        // redundancy check (return if redundant)
        boolean nonRedundant = descriptions.add(description);
        if(!nonRedundant) {
            return false;
        }

        // check whether the class expression is allowed
        if(!isDescriptionAllowed(description, parentNode)) {
            return false;
        }

        // quality of class expression (return if too weak)
        Monitor mon = MonitorFactory.start("lp");
        double accuracy = learningProblem.getAccuracyOrTooWeak(description, noise);
        mon.stop();

        // issue a warning if accuracy is not between 0 and 1 or -1 (too weak)
        if(accuracy > 1.0 || (accuracy < 0.0 && accuracy != -1)) {
            throw new RuntimeException(
                    "Invalid accuracy value " + accuracy + " for class " +
                    "expression " + description + ". This could be caused by " +
                    "a bug in the heuristic measure and should be reported " +
                    "to the DL-Learner bug tracker.");
        }

        expressionTests++;

        // return FALSE if 'too weak'
        if (accuracy == -1) {
            return false;
        }

        OENode node = new OENode(description, accuracy);
        searchTree.addNode(parentNode, node);

        // maybe add to best descriptions (method keeps set size fixed);
        // we need to make sure that this does not get called more often than
        // necessary since rewriting is expensive
        boolean isCandidate = !bestEvaluatedDescriptions.isFull();
        if (!isCandidate) {
            EvaluatedDescription<? extends Score> worst = bestEvaluatedDescriptions.getWorst();
            double accThreshold = worst.getAccuracy();
            isCandidate =
                    (accuracy > accThreshold ||
                            (accuracy >= accThreshold
                                    && OWLClassExpressionUtils.getLength(description)
                                    < worst.getDescriptionLength()));
        }

        if (isCandidate) {
            OWLClassExpression niceDescription = rewrite(node.getExpression());

            if(niceDescription.equals(classToDescribe)) {
                return false;
            }

            if(!isDescriptionAllowed(niceDescription, node)) {
                return false;
            }

            bestEvaluatedDescriptions.add(niceDescription, accuracy, learningProblem);
        }

        return true;
    }

    private boolean terminationCriteriaSatisfied() {
        return
            stop ||
                (maxClassExpressionTests != 0
                        && (expressionTests >= maxClassExpressionTests)) ||
                (maxExecutionTimeInSeconds != 0
                        && ((System.nanoTime() - nanoStartTime) >= (maxExecutionTimeInSeconds* 1000000000L)));
    }

    private void showIfBetterSolutionsFound() {
        if(bestEvaluatedDescriptions.getBestAccuracy() > currentHighestAccuracy) {
            currentHighestAccuracy = bestEvaluatedDescriptions.getBestAccuracy();

            long durationInMillis = getCurrentRuntimeInMilliSeconds();
            String durationStr = getDurationAsString(durationInMillis);

            // track new best accuracy if enabled
            if(keepTrackOfBestScore) {
                runtimeVsBestScore.put(getCurrentRuntimeInMilliSeconds(), currentHighestAccuracy);
            }
            logger.info("more accurate (" + dfPercent.format(currentHighestAccuracy) + ") class expression found after " + durationStr + ": " + descriptionToString(bestEvaluatedDescriptions.getBest().getDescription()));
        }
    }

    private OENode getNextNodeToExpand() {
        // we expand the best node of those, which have not achieved 100% accuracy
        // already and have a horizontal expansion equal to their length
        // (rationale: further extension is likely to add irrelevant syntactical constructs)
        Iterator<OENode> it = searchTree.descendingIterator();

        while (it.hasNext()) {
            OENode node = it.next();

            if (node.getAccuracy() < 1.0
                    || node.getHorizontalExpansion() <
                        OWLClassExpressionUtils.getLength(node.getDescription())) {

                return node;
            }
        }

        // this should practically never be called, since for any reasonable learning
        // task, we will always have at least one node with less than 100% accuracy
        throw new RuntimeException("SpatialLearningAlgorithm could not find any node with lesser accuracy.");
    }

    private TreeSet<OWLClassExpression> refineNode(OENode node) {

        MonitorFactory.getTimeMonitor("refineNode").start();

        // we have to remove and add the node since its heuristic evaluation
        // changes through the expansion (you *must not* include any criteria
        // in the heuristic which are modified outside of this method,
        // otherwise you may see rarely occurring but critical false ordering
        // in the nodes set)

        searchTree.updatePrepare(node);
        int horizExp = node.getHorizontalExpansion();

        TreeSet<OWLClassExpression> refinements =
                (TreeSet<OWLClassExpression>) operator.refine(node.getDescription(), horizExp);

        node.incHorizontalExpansion();
        node.setRefinementCount(refinements.size());
        searchTree.updateDone(node);
        MonitorFactory.getTimeMonitor("refineNode").stop();

        return refinements;
    }

    private void updateMinMaxHorizExp(OENode node) {
        int newHorizExp = node.getHorizontalExpansion();

        // update maximum value
        maxHorizExp = Math.max(maxHorizExp, newHorizExp);

        // we just expanded a node with minimum horizontal expansion;
        // we need to check whether it was the last one
        if (minHorizExp == newHorizExp - 1) {

            // the best accuracy that a node can achieve
            double scoreThreshold = heuristic.getNodeScore(node) + 1 - node.getAccuracy();

            for (OENode n : searchTree.descendingSet()) {
                if (n != node) {
                    if (n.getHorizontalExpansion() == minHorizExp) {
                        // we can stop instantly when another node with min.
                        return;
                    }
                    if (heuristic.getNodeScore(n) < scoreThreshold) {
                        // we can stop traversing nodes when their score is too low
                        break;
                    }
                }
            }

            // inc. minimum since we found no other node which also has min. horiz. exp.
            minHorizExp++;
        }
    }

    private void printAlgorithmRunStats() {
        if (stop) {
            logger.info("Algorithm stopped ("+expressionTests+" descriptions tested). " + searchTree.size() + " nodes in the search tree.\n");
        } else {
            totalRuntimeNs = System.nanoTime()-nanoStartTime;
            logger.info("Algorithm terminated successfully (time: " + Helper.prettyPrintNanoSeconds(totalRuntimeNs) + ", "+expressionTests+" descriptions tested, "  + searchTree.size() + " nodes in the search tree).\n");
            logger.info(reasoner.toString());
        }
    }

    // -------------------------------------------------------------------------
    // -- implemented methods


    @Override
    public void start() {
        stop = false;
        isRunning = true;
        reset();

        nanoStartTime = System.nanoTime();

        currentHighestAccuracy = 0.0;
        OENode nextNode;

        logger.info("start class:" + startClass);
        addNode(startClass, null);

        while (!terminationCriteriaSatisfied()) {
            showIfBetterSolutionsFound();

            // chose best node according to heuristics
            nextNode = getNextNodeToExpand();
            int horizExp = nextNode.getHorizontalExpansion();

            // apply refinement operator
            TreeSet<OWLClassExpression> refinements = refineNode(nextNode);

            while(!refinements.isEmpty() && !terminationCriteriaSatisfied()) {
                // pick element from set
                OWLClassExpression refinement = refinements.pollFirst();

                // get length of class expression
                int length = OWLClassExpressionUtils.getLength(refinement);

                // we ignore all refinements with lower length and too high depth
                // (this also avoids duplicate node children)
                if(length >= horizExp && OWLClassExpressionUtils.getDepth(refinement) <= maxDepth) {
                    // add node to search tree
                    addNode(refinement, nextNode);
                }
            }

            showIfBetterSolutionsFound();

            // update the global min and max horizontal expansion values
            updateMinMaxHorizExp(nextNode);
        }

        printAlgorithmRunStats();
        isRunning = false;
    }

    @Override
    public void init() throws ComponentInitException {
        ClassHierarchy classHierarchy = initClassHierarchy();
        ObjectPropertyHierarchy objectPropertyHierarchy = initObjectPropertyHierarchy();
        DatatypePropertyHierarchy datatypePropertyHierarchy = initDataPropertyHierarchy();

        // if no one injected a heuristic, we use a default one
        if(heuristic == null) {
            heuristic = new OEHeuristicRuntime();
            heuristic.init();
        }

        minimizer = new OWLClassExpressionMinimizer(dataFactory, reasoner);

        if (startClass == null)
            startClass = SpatialVocabulary.SpatialFeature;

        bestEvaluatedDescriptions = new EvaluatedDescriptionSet(maxNrOfResults);
        if (!(learningProblem instanceof PosNegLP)) {
            throw new RuntimeException(
                    "Currently only PosNegLP learning problems are supported");
        }

        noise = noisePercentage/100d;

        examples = Sets.union(
                ((PosNegLP) learningProblem).getPositiveExamples(),
                ((PosNegLP) learningProblem).getNegativeExamples());

        // create a refinement operator and pass all configuration
        // variables to it
        if (operator == null) {
            // we use a default operator and inject the class hierarchy for now
            operator = new SpatialRhoDRDown();
            ((CustomStartRefinementOperator) operator).setStartClass(startClass);
            ((ReasoningBasedRefinementOperator) operator).setReasoner(reasoner);
        }

        if (operator instanceof CustomHierarchyRefinementOperator) {
            ((CustomHierarchyRefinementOperator) operator)
                    .setClassHierarchy(classHierarchy);
            ((CustomHierarchyRefinementOperator) operator)
                    .setObjectPropertyHierarchy(objectPropertyHierarchy);
            ((CustomHierarchyRefinementOperator) operator)
                    .setDataPropertyHierarchy(datatypePropertyHierarchy);
        }

        if (!((AbstractRefinementOperator) operator).isInitialized())
            operator.init();

        initialized = true;
    }
}
