package org.dllearner.algorithms.parcelex;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.parcel.ParCELAbstract;
import org.dllearner.algorithms.parcel.ParCELExtraNode;
import org.dllearner.algorithms.parcel.ParCELPosNegLP;
import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * Abstract class for all ParCELEx (ParCEL with Exceptions or Two-way ParCEL) algorithms family
 * This algorithm learn both positive example and negative examples in the same 
 * 
 * @author An C. Tran
 * 
 */
public abstract class ParCELExAbstract extends ParCELAbstract {

	/**
	 * partial definition (they should be sorted by the completeness so that we can get the best
	 * partial definition at any time. Otherwise, we can use a hashset here for a better performance
	 */
	protected SortedSet<ParCELExtraNode> counterPartialDefinitions = null;

	protected int[] partialDefinitionType = new int[5];
	protected int counterPartialDefinitionUsed = 0;

	protected HashSet<OWLIndividual> coveredNegativeExamples;


	//---------------------------------------------------------
	//flags to indicate the status of the application
	//---------------------------------------------------------
	protected volatile boolean counterDone = false;

	//some properties for statistical purpose
	protected int descriptionTested;	//total number of descriptions tested (generated and calculated accuracy, correctess,...)

	/**
	 * 
	 * Default constructor
	 */
	public ParCELExAbstract() {
		super();
	}

	/**
	 * 
	 * Constructor for the learning algorithm
	 * 
	 * @param learningProblem A learning problem which provides the accuracy calculation for the learner
	 * @param reasoningService A reasoner which will be used by the learner
	 */
	public ParCELExAbstract(ParCELPosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);
	}

	
	/**
	 * This will be called by the workers to pass the new counter partial definitions to the learner
	 * 
	 * @param definitions New counter partial definition
	 */
	public abstract void newCounterPartialDefinitionsFound(Set<ParCELExtraNode> definitions);

	
	/**
	 * Aggregate the information related to the counter partial definitions: 
	 * <ul>
	 * 	<li>total number of counter partial definitions used</li>
	 * 	<li>number of counter partial definitions used group by counter partial definition type</li>
	 * </ul> 
	 */
	public void aggregateCounterPartialDefinitionInf() {
		// -----------------------------------------------------
		// calculate some data for analysing the algorithm
		// -----------------------------------------------------

		// number of each type of partial definition
		Arrays.fill(this.partialDefinitionType, 0);

		if (this.partialDefinitions == null) {
			logger.error("partial definition set is null");
			return;
		}

		for (ParCELExtraNode def : this.partialDefinitions) {
			int type = def.getType();
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

	
	/**
	 * Get all counter partial definitions generated
	 * 
	 * @return All counter partial definitions generated
	 */
	public SortedSet<ParCELExtraNode> getCounterPartialDefinitions() {
		return this.counterPartialDefinitions;
	}
	
	
	/**
	 * Get the number of partial definitions of a certain type
	 * 
	 * @param type Partial definition type
	 * 
	 * @return Number of the counter partial definitions of the given type 
	 */
	public int getNumberOfPartialDefinitions(int type) {
		if (type < this.partialDefinitionType.length)
			return this.partialDefinitionType[type];
		else
			return -1;
	}
	

	/**
	 * Get total number of counter partial definitions 
	 * 
	 * @return Total number of counter partial definitions (not reduced)
	 */
	public int getNumberOfCounterPartialDefinitions() {
		return (this.counterPartialDefinitions == null? 0 : this.counterPartialDefinitions.size());
	}

	
	/**
	 * Get the number of counter partial definition used (in combination with descriptions to constitute 
	 * the partial definitions)
	 * 
	 * @return Number of the partial definitions used (combined with the descriptions to create the partial definitions)
	 */
	public int getNumberOfCounterPartialDefinitionUsed() {
		return this.counterPartialDefinitionUsed;
	}
	

	/**
	 * Check whether the learner terminated by the counter partial definitions
	 * 
	 * @return True if the learner is terminated by the counter partial definitions, false otherwise
	 */
	public boolean terminatedByCounterDefinitions() {
		return this.counterDone;
	}


	/**=========================================================================================================<br>
	 * Check if the learner can be terminated
	 *
	 * @return True if termination condition is true (asked to stop, complete definition found, or timeout),
	 * 			false otherwise
	 */
	@Override
	protected boolean isTerminateCriteriaSatisfied() {
		return 	stop ||
				done ||
				counterDone ||
				timeout;
	}

}
