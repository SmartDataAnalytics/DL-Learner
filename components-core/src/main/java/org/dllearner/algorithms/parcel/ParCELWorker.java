package org.dllearner.algorithms.parcel;

import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.refinementoperators.LengthLimitedRefinementOperator;
import org.dllearner.refinementoperators.RefinementOperator;
import org.dllearner.utilities.owl.OWLClassExpressionLengthCalculator;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * ParCEL worker which find and evaluate the refinements for a given node.
 * It returns partial definitions and/or new description to the learner if any.
 *
 * @author An C. Tran
 */
public class ParCELWorker extends ParCELWorkerAbstract<ParCELearner> {

    /**
     * Constructor for Worker class. A worker needs the following things: i) reducer (reference),
     * ii) refinement operator, iii) start description, iv) worker name
     *
     * @param learner                A reference to reducer which will be used to make a callback to return the result
     *                               to
     * @param refinementOperatorPool Refinement operator pool used to refine the given node
     * @param learningProblem        A learning problem used to calculate description accuracy, correctness, etc.
     * @param nodeToProcess          Node will being processed
     * @param name                   Name of the worker, assigned by reduce (for tracing purpose only)
     */
    public ParCELWorker(ParCELearner learner, ParCELRefinementOperatorPool refinementOperatorPool,
                        ParCELPosNegLP learningProblem, ParCELNode nodeToProcess, String name) {
        super(learner, refinementOperatorPool, learningProblem, nodeToProcess, name);
    }


    /**
     * Constructor for Worker class. A worker needs the following things: i) reducer (reference),
     * ii) refinement operator, iii) start description, iv) worker name
     *
     * @param learner            A reference to reducer which will be used to make a callback to return the result
     *                           to
     * @param refinementOperator Refinement operator used to refine the given node
     * @param learningProblem    A learning problem used to calculate description accuracy, correctness, etc.
     * @param nodeToProcess      Node will being processed
     * @param name               Name of the worker, assigned by reduce (for tracing purpose only)
     */
    public ParCELWorker(ParCELearner learner, RefinementOperator refinementOperator,
                        ParCELPosNegLP learningProblem, ParCELNode nodeToProcess, String name) {
        super(learner, refinementOperator, learningProblem, nodeToProcess, name);
    }

    /**
     * Start the worker: Call the methods processNode() for processing the current node given by
     * reducer
     */
    @Override
    public void run() {

        if (logger.isTraceEnabled())
            logger.trace("[ParCEL-Worker] Processing node ("
                    + ParCELStringUtilities.replaceString(nodeToProcess.toString(), this.baseURI,
                    this.prefix));


        HashSet<ParCELExtraNode> definitionsFound = new HashSet<>(); // hold the
        // partial
        // definitions
        // if any
        HashSet<ParCELNode> newNodes = new HashSet<>(); // hold the refinements that are
        // not partial definitions
        // (descriptions)

        int horizExp = nodeToProcess.getHorizontalExpansion();

        // 1. refine node
        TreeSet<OWLClassExpression> refinements = refineNode(nodeToProcess);
//        System.out.println("ParCEL Worker " + name  + ":" + refinements);

        if (refinements != null) {
            if (logger.isTraceEnabled())
                logger.trace("Refinement result ("
                        + refinements.size()
                        + "): "
                        + ParCELStringUtilities.replaceString(refinements.toString(), this.baseURI,
                        this.prefix));
        }

        // 2. process the refinement result: calculate the accuracy and completeness and add the new
        // expression into the search tree
        while (refinements != null && refinements.size() > 0) {
            OWLClassExpression refinement = refinements.pollFirst();
            int refinementLength = new OWLClassExpressionLengthCalculator().getLength(refinement);

            // we ignore all refinements with lower length (may it happen?)
            // (this also avoids duplicate children)
            if (refinementLength > horizExp) {

                // calculate accuracy, correctness, positive examples covered by the description,
                // resulted in a node
                long starttime = System.currentTimeMillis();
                ParCELExtraNode addedNode = checkAndCreateNewNode(refinement, nodeToProcess);

                // make decision on the new node (new search tree node or new partial definition)
                if (addedNode != null) {

                    // PARTIAL DEFINITION (correct and not necessary to be complete)
                    if (addedNode.getCorrectness() >= 1.0d - learner.getNoiseAllowed()) {
                        addedNode.setGenerationTime(System.currentTimeMillis());
                        addedNode.setExtraInfo(learner.getTotalDescriptions());
                        definitionsFound.add(addedNode);
                    }
                    // DESCRIPTION
                    else
                        newNodes.add((ParCELNode) addedNode);
                } // if (node != null), i.e. weak description
            }
        } // while (refinements.size > 0)

        horizExp = nodeToProcess.getHorizontalExpansion();

        learner.updateMaxHorizontalExpansion(horizExp);

        newNodes.add(nodeToProcess);

        if (definitionsFound.size() > 0)
            learner.newPartialDefinitionsFound(definitionsFound);

        learner.newRefinementDescriptions(newNodes);

    }

    /**
     * Calculate accuracy, correctness of a description and examples that are covered by this
     * description
     *
     * @param description Description which is being calculated
     * @param parentNode  The node which contains the description which is used in the refinement that
     *                    result the input description
     * @return Null if the description is processed before, or a node which contains the description
     */
    private ParCELExtraNode checkAndCreateNewNode(OWLClassExpression description, ParCELNode parentNode) {

        // redundancy check
        boolean nonRedundant = learner.addDescription(description);
        if (!nonRedundant)
            return null; // false, node cannot be added

        // currently, noise is not processed. it should be processed later
        ParCELEvaluationResult accurateAndCorrectness = learningProblem
                .getAccuracyAndCorrectness2(description, learner.getNoiseAllowed());
//        System.out.println(description + ":" + accurateAndCorrectness);

        // description is too weak, i.e. covered no positive example
        if (accurateAndCorrectness.accuracy == -1.0d)
            return null;

        ParCELExtraNode newNode = new ParCELExtraNode(parentNode, description,
                accurateAndCorrectness.accuracy, accurateAndCorrectness.correctness,
                accurateAndCorrectness.completeness,
                accurateAndCorrectness.coveredPositiveExamples);

        if (parentNode != null)
            parentNode.addChild(newNode);

        return newNode;

    } // addNode()


    /**
     * Get the node which is currently being processed
     *
     * @return The node currently being processed
     */
    public ParCELNode getProcessingNode() {
        return this.nodeToProcess;
    }
}
