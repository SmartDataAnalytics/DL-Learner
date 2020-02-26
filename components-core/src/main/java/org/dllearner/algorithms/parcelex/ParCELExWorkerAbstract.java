package org.dllearner.algorithms.parcelex;

import org.dllearner.algorithms.parcel.*;
import org.dllearner.refinementoperators.RefinementOperator;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public abstract class ParCELExWorkerAbstract<L extends ParCELExAbstract> extends ParCELWorkerAbstract<L> {

	protected OWLDataFactory df = new OWLDataFactoryImpl();

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
	public ParCELExWorkerAbstract(L learner, ParCELRefinementOperatorPool refinementOperatorPool,
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
	public ParCELExWorkerAbstract(L learner, RefinementOperator refinementOperator,
								  ParCELPosNegLP learningProblem, ParCELNode nodeToProcess, String name) {
		super(learner, refinementOperator, learningProblem, nodeToProcess, name);
	}

	/**
	 * ============================================================================================
	 * =============<br>
	 * Calculate accuracy, correctness of a description and examples that are covered by this
	 * description<br>
	 * This version is used in the learning with exception
	 *
	 * @param description
	 *            Description which is being calculated
	 * @param parentNode
	 *            The node which contains the description which is used in the refinement that
	 *            result the input description
	 *
	 * @return Null if the description is processed before, or a node which contains the description
	 */
	protected ParCELExtraNode checkAndCreateNewNodeV2(OWLClassExpression description, ParCELNode parentNode) {

		// redundancy check
		boolean nonRedundant = learner.addDescription(description);
		if (!nonRedundant)
			return null; // false, node cannot be added

		/**
		 * <ol>
		 * <li>cp(D) = empty</li>
		 * <ul>
		 * <li>cn(D) = empty: weak description ==> may be ignored</li>
		 * <li>cn(D) != empty: counter partial definition, especially used in learning with
		 * exceptions</li>
		 * </ul>
		 * <li>cp(D) != empty</li>
		 * <ul>
		 * <li>cn(D) = empty: partial definition</li>
		 * <li>cn(D) != empty: potential description</li>
		 * </ul>
		 * </ol>
		 */
		ParCELEvaluationResult evaluationResult = learningProblem
				.getAccuracyAndCorrectnessEx(description);

		// cover no positive example && no negative example ==> weak description
		if ((evaluationResult.getCompleteness() == 0) && (evaluationResult.getCorrectness() == 1))
			return null;

		ParCELExtraNode newNode = new ParCELExtraNode(parentNode, description,
													  evaluationResult.getAccuracy(), evaluationResult.getCorrectness(),
													  evaluationResult.getCompleteness(), evaluationResult.getCoveredPositiveExamples());

		// newNode.setCorrectness(evaluationResult.getCorrectness());
		// newNode.setCompleteness(evaluationResult.getCompleteness());
		// newNode.setCoveredPositiveExamples(evaluationResult.getCoveredPossitiveExamples());
		newNode.setCoveredNegativeExamples(evaluationResult.getCoveredNegativeExamples());

		if (parentNode != null)
			parentNode.addChild(newNode);

		return newNode;

	} // addNode()

}
