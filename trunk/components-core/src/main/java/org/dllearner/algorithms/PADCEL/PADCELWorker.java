package org.dllearner.algorithms.PADCEL;

import java.util.HashSet;

import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.owl.Description;
import org.dllearner.refinementoperators.RefinementOperator;

/**
 * PADCEL worker which will do the refinement on a given node and evaluate the refinement result. It
 * will return partial definitions and/or new description to the learner if any.
 * 
 * @author An C. Tran
 * 
 */
public class PADCELWorker implements Runnable {

	// name of worker (for debugging purpose)
	private String name;

	// refinement operator used in refinement
	private PADCELRefinementOperatorPool refinementOperatorPool;
	private RefinementOperator refinementOperator;

	// reducer, used to make the callback to pass the result and get the next description for
	// processing
	private PADCELearner learner;

	// learning proble, provides accuracy & correctness calculation
	private PADCELPosNegLP learningProblem;

	// the node to be processed
	private PADCELNode nodeToProcess;

	private Logger logger = Logger.getLogger(this.getClass());

	// these properties can be referred in Reducer. However, we put it here for faster access
	private String baseURI;
	private Map<String, String> prefix;

	/**
	 * ============================================================================================
	 * Constructor for Worker class. A worker needs the following things: i) reducer (reference),
	 * ii) refinement operator, iii) start description, iv) worker name
	 * 
	 * @param learner
	 *            A reference to reducer which will be used to make a callback to return the result
	 *            to
	 * @param refinementOperator
	 *            Refinement operator used to refine the given node
	 * @param learningProblem
	 *            A learning problem used to calculate description accuracy, correctness, etc.
	 * @param nodeToProcess
	 *            Node will being processed
	 * @param name
	 *            Name of the worker, assigned by reduce (for tracing purpose only)
	 */
	public PADCELWorker(PADCELearner learner, PADCELRefinementOperatorPool refinementOperatorPool,
			PADCELPosNegLP learningProblem, PADCELNode nodeToProcess, String name) {

		super();

		this.learner = learner;
		this.refinementOperatorPool = refinementOperatorPool;
		this.refinementOperator = null;

		this.learningProblem = learningProblem;

		this.nodeToProcess = nodeToProcess;
		this.name = name;

		this.baseURI = learner.getBaseURI();
		this.prefix = learner.getPrefix();
	}

	/**
	 * ============================================================================================
	 * Constructor for Worker class. A worker needs the following things: i) reducer (reference),
	 * ii) refinement operator, iii) start description, iv) worker name
	 * 
	 * @param learner
	 *            A reference to reducer which will be used to make a callback to return the result
	 *            to
	 * @param refinementOperator
	 *            Refinement operator used to refine the given node
	 * @param learningProblem
	 *            A learning problem used to calculate description accuracy, correctness, etc.
	 * @param nodeToProcess
	 *            Node will being processed
	 * @param name
	 *            Name of the worker, assigned by reduce (for tracing purpose only)
	 */
	public PADCELWorker(PADCELearner learner, RefinementOperator refinementOperator,
			PADCELPosNegLP learningProblem, PADCELNode nodeToProcess, String name) {

		super();

		this.learner = learner;
		this.refinementOperator = refinementOperator;
		this.refinementOperatorPool = null;

		this.learningProblem = learningProblem;

		this.nodeToProcess = nodeToProcess;
		this.name = name;

		this.baseURI = learner.getBaseURI();
		this.prefix = learner.getPrefix();
	}

	/**
	 * ============================================================================================
	 * Start the worker: Call the methods processNode() for processing the current node given by
	 * reducer
	 */
	@Override
	public void run() {

		if (logger.isTraceEnabled())
			logger.trace("[PLLearning] Processing node ("
					+ PADCELStringUtilities.replaceString(nodeToProcess.toString(), this.baseURI,
							this.prefix));

		TreeSet<Description> refinements; // will hold the refinement result (set of Descriptions)

		HashSet<PADCELExtraNode> definitionsFound = new HashSet<PADCELExtraNode>(); // hold the
																					// partial
																					// definitions
																					// if any
		HashSet<PADCELNode> newNodes = new HashSet<PADCELNode>(); // hold the refinements that are
																	// not partial definitions
																	// (descriptions)

		int horizExp = nodeToProcess.getHorizontalExpansion();

		// 1. refine node
		refinements = refineNode(nodeToProcess);

		if (logger.isTraceEnabled())
			logger.trace("[PLLearning] Refinement result ("
					+ refinements.size()
					+ "): "
					+ PADCELStringUtilities.replaceString(refinements.toString(), this.baseURI,
							this.prefix));

		// 2. process the refinement result: calculate the accuracy and completeness and add the new
		// expression into the search tree
		while (refinements.size() > 0) {
			Description refinement = refinements.pollFirst();
			int refinementLength = refinement.getLength();

			// we ignore all refinements with lower length (may it happen?)
			// (this also avoids duplicate children)
			if (refinementLength > horizExp) {

				// calculate accuracy, correctness, positive examples covered by the description,
				// resulted in a node
				long starttime = System.currentTimeMillis();
				PADCELExtraNode addedNode = checkAndCreateNewNode(refinement, nodeToProcess);

				// make decision on the new node (new search tree node or new partial definition)
				if (addedNode != null) {

					// PARTIAL DEFINITION (correct and not necessary to be complete)
					if (addedNode.getCorrectness() == 1.0d) {
						addedNode.setGenerationTime(System.currentTimeMillis() - starttime);
						definitionsFound.add(addedNode);
					}
					// DESCRIPTION
					else
						newNodes.add((PADCELNode) addedNode);
				} // if (node != null), i.e. weak description
			}
		} // while (refinements.size > 0)

		horizExp = nodeToProcess.getHorizontalExpansion();

		learner.updateMaxHorizontalExpansion(horizExp);

		newNodes.add(nodeToProcess);

		if (definitionsFound.size() > 0)
			learner.definitionsFound(definitionsFound);

		learner.newRefinementDescriptions(newNodes);

	}

	/**
	 * ============================================================================================
	 * Refine a node using RhoDRDown. The refined node will be increased the max horizontal
	 * expansion value by 1
	 * 
	 * @param node
	 *            Node to be refined
	 * 
	 * @return Set of descriptions that are the results of refinement
	 */
	private TreeSet<Description> refineNode(PADCELNode node) {
		int horizExp = node.getHorizontalExpansion();

		if (logger.isTraceEnabled())
			logger.trace("[" + this.name + "] Refining: "
					+ PADCELStringUtilities.replaceString(node.toString(), baseURI, prefix));

		boolean refirementOperatorBorrowed = false;

		// borrow refinement operator if necessary
		if (this.refinementOperator == null) {
			if (this.refinementOperatorPool == null) {
				logger.error("Neither refinement operator nor refinement operator pool provided");
				return null;
			} else {
				try {
					// logger.info("borrowing a refinement operator (" +
					// refinementOperatorPool.getNumIdle() + ")");
					this.refinementOperator = this.refinementOperatorPool.borrowObject();
					refirementOperatorBorrowed = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		TreeSet<Description> refinements = (TreeSet<Description>) refinementOperator.refine(
				node.getDescription(), horizExp + 1);
		node.incHorizontalExpansion();
		node.setRefinementCount(refinements.size());

		// return the refinement operator
		if (refirementOperatorBorrowed) {
			try {
				if (refinementOperator != null)
					refinementOperatorPool.returnObject(refinementOperator);
				else
					logger.error("Cannot return the borrowed refinement operator");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return refinements;
	}

	/**============================================================================================
	 * Calculate accuracy, correctness of a description and examples that are covered by this
	 * description
	 * 
	 * @param description
	 *            Description which is being calculated
	 * @param parentNode
	 *            The node which contains the description which is used in the refinement that
	 *            result the input description
	 * 
	 * @return Null if the description is processed before, or a node which contains the description
	 */
	private PADCELExtraNode checkAndCreateNewNode(Description description, PADCELNode parentNode) {

		// redundancy check
		boolean nonRedundant = learner.addDescription(description);
		if (!nonRedundant)
			return null; // false, node cannot be added

		// currently, noise is not processed. it should be processed later
		PADCELEvaluationResult accurateAndCorrectness = learningProblem
				.getAccuracyAndCorrectness2(description);

		// description is too weak, i.e. covered no positive example
		if (accurateAndCorrectness.accuracy == -1.0d)
			return null;

		PADCELExtraNode newNode = new PADCELExtraNode(parentNode, description,
				accurateAndCorrectness.accuracy, accurateAndCorrectness.correctness,
				accurateAndCorrectness.coveredPossitiveExamples);

		if (parentNode != null)
			parentNode.addChild(newNode);

		return newNode;

	} // addNode()

	
	/**============================================================================================
	 * Get the node which is currently being processed
	 * 
	 * @return The node currently being processed
	 */
	public PADCELNode getProcessingNode() {
		return this.nodeToProcess;
	}
}
