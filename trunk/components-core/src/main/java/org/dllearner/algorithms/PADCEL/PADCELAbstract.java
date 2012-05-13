package org.dllearner.algorithms.PADCEL;

import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Description;

/**
 * Abstract class for all PDL-Learner algorithms family
 * 
 * @author An C. Tran
 * 
 */
public abstract class PADCELAbstract extends AbstractCELA {

	/**
	 * All generated descriptions thread-safe set is used to avoid concurrently
	 * accessing
	 */
	protected ConcurrentSkipListSet<Description> allDescriptions = null;

	/**
	 * Search tree. It hold all evaluated descriptions that are not correct and
	 * not weak ==> candidate for partial definitions Nodes in the search tree
	 * must be ordered using heuristic so that it can help the searching more
	 * efficiently (best search rather than 'blind' breadth first of depth
	 * first) NOTE: node = (description + accuracy/correctness/completeness/...
	 * value)
	 */
	protected ConcurrentSkipListSet<PADCELNode> searchTree = null; // thread safe
																	// set

	/**
	 * partial definitions (they should be sorted so that we can get the best
	 * partial definition at any time
	 */
	protected SortedSet<PADCELExtraNode> partialDefinitions = null;

	/**
	 * Default constructor
	 */
	public PADCELAbstract() {
		super();
	}

	/**
	 * ========================================================================
	 * Constructor for the learning algorithm
	 * 
	 * @param learningProblem
	 *            Must be a PDLLPosNegLP
	 * @param reasoningService
	 *            A reasoner
	 */
	public PADCELAbstract(PADCELPosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);
	}

	public abstract Description getUnionCurrenlyBestDescription();

	public abstract int getNoOfCompactedPartialDefinition();

	public abstract Set<PADCELExtraNode> getPartialDefinitions();

	public abstract double getCurrentlyOveralMaxCompleteness();

	public long getNumberOfPartialDefinitions() {
		return this.partialDefinitions.size();
	}

}
