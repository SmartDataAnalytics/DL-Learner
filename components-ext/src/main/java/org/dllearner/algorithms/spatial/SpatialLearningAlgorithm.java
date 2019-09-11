package org.dllearner.algorithms.spatial;

import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.core.*;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.refinementoperators.LengthLimitedRefinementOperator;
import org.dllearner.utilities.datastructures.SearchTree;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.TreeSet;

public class SpatialLearningAlgorithm extends AbstractCELA {
    private static final Logger logger = LoggerFactory.getLogger(SpatialLearningAlgorithm.class);

    private double currentHighestAccuracy;

    /**
     * all descriptions in the search tree plus those which were too weak (for
     * fast redundancy check)
     */
    private TreeSet<OWLClassExpression> descriptions;
    private double noise = 0;
    private int expressionTests;
    private SearchTree<OENode> searchTree;
    private int minHorizExp = 0;
    private int maxHorizExp = 0;

    private AbstractHeuristic heuristic;
    private OWLClassExpression startClass;

    @ConfigOption(defaultValue="0.0", description="the (approximated) percentage of noise within the examples")
    private double noisePercentage = 0.0;

    @ConfigOption(description = "the refinement operator instance to use")
    private LengthLimitedRefinementOperator operator;

    @ConfigOption(defaultValue="0", description="The maximum number of " +
            "candidate hypothesis the algorithm is allowed to test (0 = no " +
            "limit). The algorithm will stop afterwards. (The real number of " +
            "tests can be slightly higher, because this criterion usually " +
            "won't be checked after each single test.)")
    private int maxClassExpressionTests = 0;

    @ConfigOption(defaultValue="7", description="The maximum depth of a description")
    private double maxDepth = 7;

    @ConfigOption(defaultValue = "false",  description = "whether to try and " +
            "refine solutions which already have accuracy value of 1")
    private boolean expandAccuracy100Nodes = false;

    // <getter/setter>
    public void setOperator(LengthLimitedRefinementOperator operator) {
        this.operator = operator;
    }

    public void setStartClass(OWLClassExpression startClass) {
        this.startClass = startClass;
    }

    public void setExpandAccuracy100Nodes(boolean expandAccuracy100Nodes) {
        this.expandAccuracy100Nodes = expandAccuracy100Nodes;
    }

    public void setNoisePercentage(double noisePercentage) {
        this.noisePercentage = noisePercentage;
    }
    // </getter/setter>

    private boolean addNode(OWLClassExpression ce, OENode parentNode) {
        boolean nonRedundant = descriptions.add(ce);

        if (!nonRedundant) return false;
        // TODO: add 'description allowed' check
        double accuracy = learningProblem.getAccuracyOrTooWeak(ce, noise);

        expressionTests++;

        if(accuracy == -1) {
            return false;
        }

        OENode node = new OENode(ce, accuracy);
        searchTree.addNode(parentNode, node);

        boolean isCandidate = !bestEvaluatedDescriptions.isFull();
        if(!isCandidate) {
            EvaluatedDescription<? extends Score> worst =
                    bestEvaluatedDescriptions.getWorst();

            double accThreshold = worst.getAccuracy();
            isCandidate = (accuracy > accThreshold || (accuracy >= accThreshold &&
                    OWLClassExpressionUtils.getLength(ce) < worst.getDescriptionLength()));
        }

        if(isCandidate) {
            bestEvaluatedDescriptions.add(ce, accuracy, learningProblem);
        }

        return true;
    }

    private boolean terminationCriteriaSatisfied() {
        return (maxClassExpressionTests != 0 && (expressionTests >= maxClassExpressionTests)) ||
                (maxExecutionTimeInSeconds != 0 &&
                        ((System.nanoTime() - nanoStartTime) >= (maxExecutionTimeInSeconds* 1000000000L)));
    }

    private OENode getNextNodeToExpand() {
        // we expand the best node of those, which have not achieved 100% accuracy
        // already and have a horizontal expansion equal to their length
        // (rationale: further extension is likely to add irrelevant syntactical constructs)
        Iterator<OENode> it = searchTree.descendingIterator();

        while(it.hasNext()) {
            OENode node = it.next();
            if (expandAccuracy100Nodes &&
                    node.getHorizontalExpansion() < OWLClassExpressionUtils.getLength(node.getDescription())) {
                return node;

            } else if (node.getHorizontalExpansion() < OWLClassExpressionUtils.getLength(node.getDescription())) {
                return node;
            }
        }

        // this should practically never be called, since for any reasonable learning
        // task, we will always have at least one node with less than 100% accuracy
        throw new RuntimeException("CELOE could not find any node with lesser accuracy.");
    }


    private TreeSet<OWLClassExpression> refineNode(OENode node) {
        // we have to remove and add the node since its heuristic evaluation changes through the expansion
        // (you *must not* include any criteria in the heuristic which are modified outside of this method,
        // otherwise you may see rarely occurring but critical false ordering in the nodes set)
        searchTree.updatePrepare(node);
        int horizExp = node.getHorizontalExpansion();

        TreeSet<OWLClassExpression> refinements =
                (TreeSet<OWLClassExpression>) operator.refine(node.getDescription(), horizExp+1);

        node.incHorizontalExpansion();
        node.setRefinementCount(refinements.size());
        searchTree.updateDone(node);

        return refinements;
    }

    private void updateMinMaxHorizExp(OENode node) {
        int newHorizExp = node.getHorizontalExpansion();

        // update maximum value
        maxHorizExp = Math.max(maxHorizExp, newHorizExp);

        // we just expanded a node with minimum horizontal expansion; we need
        // to check whether it was the last one
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

    private void showIfBetterSolutionsFound() {
        if(bestEvaluatedDescriptions.getBestAccuracy() > currentHighestAccuracy) {
            currentHighestAccuracy = bestEvaluatedDescriptions.getBestAccuracy();
            long durationInMillis = getCurrentRuntimeInMilliSeconds();
            String durationStr = getDurationAsString(durationInMillis);

            logger.info("more accurate (" +
                    dfPercent.format(currentHighestAccuracy) + ") class " +
                    "expression found after " + durationStr + ": " +
                    descriptionToString(bestEvaluatedDescriptions.getBest()
                            .getDescription()));
        }
    }

    public double getCurrentlyBestAccuracy() {
        return bestEvaluatedDescriptions.getBest().getAccuracy();
    }

    @Override
    public void start() {
        currentHighestAccuracy = 0.0;
        nanoStartTime = System.nanoTime();

        OENode nextNode;

        if (startClass == null) startClass = OWL_THING;
        logger.info("start class:" + startClass);
        addNode(startClass, null);

        while (!terminationCriteriaSatisfied()) {
            showIfBetterSolutionsFound();

            nextNode = getNextNodeToExpand();

            int horizExp = nextNode.getHorizontalExpansion();
            logger.debug("Refining class expression " + nextNode.getDescription());
            TreeSet<OWLClassExpression> refinements = refineNode(nextNode);

            while(!refinements.isEmpty() && !terminationCriteriaSatisfied()) {
                OWLClassExpression refinement = refinements.pollFirst();
                int length = OWLClassExpressionUtils.getLength(refinement);

                if (length > horizExp &&
                        OWLClassExpressionUtils.getDepth(refinement) <= maxDepth) {
                    addNode(refinement, nextNode);
                }
            }

            updateMinMaxHorizExp(nextNode);
        }

        logger.info("Algorithm stopped (" + expressionTests + " descriptions " +
                "tested). " + searchTree.size() + " nodes in the search tree.\n");
        logger.info("solutions:\n" + getSolutionString());
    }

    @Override
    public void init() throws ComponentInitException {
        descriptions = new TreeSet<>();
        expressionTests = 0;
        noise = noisePercentage/100d;
        heuristic = new OEHeuristicRuntime();
        searchTree = new SearchTree<>(heuristic);
    }
}
