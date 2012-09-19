package org.dllearner.algorithms.ParCELEx;

import java.util.SortedSet;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.ParCEL.ParCELAbstract;
import org.dllearner.algorithms.ParCEL.ParCELExtraNode;
import org.dllearner.algorithms.ParCEL.ParCELPosNegLP;
import org.dllearner.core.AbstractReasonerComponent;

/**
 * Abstract class for all PADCELEx (PADCEL with Exceptions) algorithms family
 * 
 * @author An C. Tran
 * 
 */
public abstract class ParCELExAbstract extends ParCELAbstract {

	/**
	 * partial definition (they should be shorted by completeness so that we can get the best
	 * partial definition at any time. Otherwise, we can use a hashset here for a better performance
	 */
	protected SortedSet<ParCELExtraNode> counterPartialDefinitions = null;

	protected int[] partialDefinitionType = new int[5];
	protected int counterPartialDefinitionUsed = 0;

	// ------------------------------------------------
	// variables for statistical purpose
	// ------------------------------------------------
	protected long miliStarttime = Long.MIN_VALUE;
	protected long miliLearningTime = Long.MIN_VALUE;

	private Logger logger = Logger.getLogger(this.getClass());

	/**
	 * ============================================================================================
	 * Default constructor
	 */
	public ParCELExAbstract() {
		super();
	}

	/**
	 * ============================================================================================
	 * Constructor for the learning algorithm
	 * 
	 * @param learningProblem
	 *            Must be a PDLLPosNegLP
	 * @param reasoningService
	 *            A reasoner
	 */
	public ParCELExAbstract(ParCELPosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);
	}

	public void aggregateCounterPartialDefinitionInf() {
		// -----------------------------------------------------
		// calculate some data for analysing the algorithm
		// -----------------------------------------------------

		// number of each type of partial definition
		for (int i = 0; i < this.partialDefinitionType.length; i++)
			this.partialDefinitionType[i] = 0;

		if (this.partialDefinitions == null) {
			logger.error("partial definition set is null");
			return;
		}

		for (ParCELExtraNode def : this.partialDefinitions) {
			int type = (int) def.getType();
			if (type >= this.partialDefinitionType.length)
				logger.error("partial definition type " + type + " is not supported");
			else
				this.partialDefinitionType[type]++;
		}

		// number of partial definition had been used
		this.counterPartialDefinitionUsed = 0;
		for (ParCELExtraNode def : this.counterPartialDefinitions) {
			if (def.getType() > 0)
				this.counterPartialDefinitionUsed++;
		}
	}

	public int getNumberOfPartialDefinitions(int type) {
		if (type < this.partialDefinitionType.length)
			return this.partialDefinitionType[type];
		else
			return -1;
	}

	public int getNumberOfCounterPartialDefinitionUsed() {
		return this.counterPartialDefinitionUsed;
	}

	public long getMiliStarttime() {
		return this.miliStarttime;
	}

	public abstract boolean terminatedByCounterDefinitions();

	public abstract boolean terminatedByPartialDefinitions();

}
