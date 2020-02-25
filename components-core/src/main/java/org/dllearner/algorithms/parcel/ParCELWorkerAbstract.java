package org.dllearner.algorithms.parcel;

import org.apache.log4j.Logger;
import org.dllearner.refinementoperators.RefinementOperator;

import java.util.Map;

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

}
