package org.dllearner.algorithms.parcel;

import org.apache.log4j.Logger;
import org.dllearner.refinementoperators.LengthLimitedRefinementOperator;
import org.dllearner.refinementoperators.RefinementOperator;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.Map;
import java.util.TreeSet;

public abstract class ParCELWorkerAbstract<L extends ParCELAbstract> implements Runnable {
	
	// name of worker (for debugging purpose)
	protected String name;

	// refinement operator used in refinement
	protected final ParCELRefinementOperatorPool refinementOperatorPool;
	protected RefinementOperator refinementOperator;

	// reducer, used to make the callback to pass the result and get the next description for
	// processing
	protected final L learner;

	// learning proble, provides accuracy & correctness calculation
	protected final ParCELPosNegLP learningProblem;

	// the node to be processed
	protected final ParCELNode nodeToProcess;

	protected final Logger logger = Logger.getLogger(this.getClass());

	// these properties can be referred in Reducer. However, we put it here for faster access
	protected final String baseURI;
	protected final Map<String, String> prefix;

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
	public ParCELWorkerAbstract(L learner, ParCELRefinementOperatorPool refinementOperatorPool,
						ParCELPosNegLP learningProblem, ParCELNode nodeToProcess, String name) {
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
	public ParCELWorkerAbstract(L learner, RefinementOperator refinementOperator,
				 ParCELPosNegLP learningProblem, ParCELNode nodeToProcess, String name) {
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
	 * Refine a node using RhoDRDown. The refined node will be increased the max horizontal
	 * expansion value by 1
	 *
	 * @param node Node to be refined
	 * @return Set of descriptions that are the results of refinement
	 */
	protected TreeSet<OWLClassExpression> refineNode(ParCELNode node) {
		int horizExp = node.getHorizontalExpansion();

		if (logger.isTraceEnabled())
			logger.trace("[" + this.name + "] Refining: "
								 + ParCELStringUtilities.replaceString(node.toString(), baseURI, prefix));

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

		TreeSet<OWLClassExpression> refinements = null;
		try {
			// TODO that's odd, we should just restrict the whole code to LengthLimitedRefinementOperator
			if (refinementOperator instanceof LengthLimitedRefinementOperator) {
				refinements = (TreeSet<OWLClassExpression>) ((LengthLimitedRefinementOperator) refinementOperator).refine(node.getDescription(), horizExp + 1);
			} else {
				refinements = (TreeSet<OWLClassExpression>) refinementOperator.refine(node.getDescription());
			}

			node.incHorizontalExpansion();
			node.setRefinementCount(refinements.size());
		} catch (Exception e) {
			e.printStackTrace();
		}

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



	/**
	 * Get the node which is currently being processed
	 *
	 * @return The node currently being processed
	 */
	public ParCELNode getProcessingNode() {
		return this.nodeToProcess;
	}

}
