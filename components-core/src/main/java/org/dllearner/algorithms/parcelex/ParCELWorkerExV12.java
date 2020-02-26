package org.dllearner.algorithms.parcelex;

import org.dllearner.algorithms.parcel.*;
import org.dllearner.refinementoperators.RefinementOperator;
import org.dllearner.utilities.owl.OWLClassExpressionLengthCalculator;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.HashSet;
import java.util.TreeSet;


/**
 * Worker class which will do the refinement on the given nodes and 
 * 	evaluate the refinement result. It will return partial definitions 
 * 	and/or new description to the reducer if any.   
 * 
 * @author An C. Tran
 *
 */
public class ParCELWorkerExV12 extends ParCELExWorkerAbstract<ParCELExAbstract> {

	/**=========================================================================================================<br>
	 * Constructor for Worker class. A worker needs the following things: 
	 * 		i) reducer (reference), ii) refinement operator, iii) start description, iv) worker name
	 *  
	 * @param learner A reference to reducer which will be used to make a callback to return the result to
	 * @param refinementOperatorPool Refinement operator pool used to refine the given node
	 * @param learningProblem A learning problem used to calculate description accuracy, correctness, etc.
	 * @param nodeToProcess Node will being processed
	 * @param name Name of the worker, assigned by reduce (for tracing purpose only)
	 */
	public ParCELWorkerExV12(ParCELExAbstract learner, ParCELRefinementOperatorPool refinementOperatorPool, 
			ParCELPosNegLP learningProblem, ParCELNode nodeToProcess, String name) {
		super(learner, refinementOperatorPool, learningProblem, nodeToProcess, name);
	}

	
	
	/**=========================================================================================================<br>
	 * Constructor for Worker class. A worker needs the following things: 
	 * 		i) reducer (reference), ii) refinement operator, iii) start description, iv) worker name
	 *  
	 * @param learner A reference to reducer which will be used to make a callback to return the result to
	 * @param refinementOperator Refinement operator used to refine the given node 
	 * @param learningProblem A learning problem used to calculate description accuracy, correctness, etc.
	 * @param nodeToProcess Node will being processed
	 * @param name Name of the worker, assigned by reduce (for tracing purpose only)
	 */
	public ParCELWorkerExV12(ParCELearnerExV1 learner, RefinementOperator refinementOperator,
			ParCELPosNegLP learningProblem, ParCELNode nodeToProcess, String name) {
		super(learner, refinementOperator, learningProblem, nodeToProcess, name);
	}

	
	
	/**=========================================================================================================<br>
	 * Start the worker: Call the methods processNode() for processing the current node given by reducer
	 */
	@Override
	public void run() {
		
		if (logger.isTraceEnabled())
			logger.trace("[PLLearning] Processing node (" + ParCELStringUtilities.replaceString(nodeToProcess.toString(), this.baseURI, this.prefix));


		TreeSet<OWLClassExpression> refinements;	//will hold the refinement result (set of Descriptions)
		
		HashSet<ParCELExtraNode> newPartialDefinitions = new HashSet<>();	//hold the partial definitions if any
		HashSet<ParCELExtraNode> newCounterPartialDefinitions = new HashSet<>();
		HashSet<ParCELExtraNode> newPotentialPartialDefinitions = new HashSet<>();
		
		HashSet<ParCELNode> newNodes = new HashSet<>();	//hold the refinements that are not partial definitions
		
		int horizExp = nodeToProcess.getHorizontalExpansion();

		//1. refine node			
		refinements = refineNode(nodeToProcess);

		if (refinements != null) {
			if (logger.isTraceEnabled())
				logger.trace("[PLLearning] Refinement result (" + refinements.size() + "): " + 
						ParCELStringUtilities.replaceString(refinements.toString(), this.baseURI, this.prefix));


			//2. process the refinement result: calculate the accuracy and completeness and add the new expression into the search tree
			while (refinements.size() > 0) {
				OWLClassExpression refinement = refinements.pollFirst();
				int refinementLength = new OWLClassExpressionLengthCalculator().getLength(refinement);


				// we ignore all refinements with lower length (may it happen?)
				// (this also avoids duplicate node children)
				if(refinementLength > horizExp) { 

					//calculate accuracy, correctness, positive examples covered by the description, resulted in a node
					ParCELExtraNode newNode = checkAndCreateNewNodeV2(refinement, nodeToProcess);

					//check for the type of new node: weak description, partial definition, counter partial definition or potential description
					if (newNode != null) {

						/*
						 *	- completeness(D) = 0, i.e. cp(D) = empty:
						 *		+ correctness(D) = 1, i.e. cn(D) = empty ==> weak description
						 *		+ correctness(D) < 1, i.e. cn(D) != empty ==> counter partial definition
						 *	- completeness(D) > 0, i.e. cp(D) != empty
						 *		+ correctness(D) = 1, i.e. cn(D) = empty ==> partial definition
						 *		+ correctness(D) < 1, i.e. cn(D) != empty ==> potential description 
						 */

						if (newNode.getCompleteness() == 0.0d) {

							//COUNTER PARTIAL DEFINITION
							if (newNode.getCorrectness() < 1d) {
								newNode.setType(ParCELExNodeTypes.COUNTER_PARTIAL_DEFINITION_UNUSED);
								newNode.setGenerationTime(learner.getMiliStarttime() - System.currentTimeMillis());
								newNode.setDescription(df.getOWLObjectComplementOf(newNode.getDescription()));
								newCounterPartialDefinitions.add(newNode);								
							}
							//else: weak description ==> ignore the description: do nothing 
						}
						else {	//i.e. completeness > 0

							//PARTIAL DEFINITION
							if (newNode.getCorrectness() == 1.0d) {
								newNode.setGenerationTime(System.currentTimeMillis() - learner.getMiliStarttime());
								newNode.setType(ParCELExNodeTypes.PARTIAL_DEFINITION_DIRECT_REFINED);
								newPartialDefinitions.add(newNode);
							}
							//DESCRIPTION
							else {
								//TODO: check if the description may become a partial definition 
								
								//given the current counter partial definition set					
								
								if (ParCELExCombineCounterPartialDefinition.getCombinable(newNode, learner.getCounterPartialDefinitions()) != null) {
									newNode.setType(ParCELExNodeTypes.POTENTIAL_PARTIAL_DEFINITION);
									newPotentialPartialDefinitions.add(newNode);
								}
								//else 								
								newNodes.add(newNode);
							}
						}
						
					}	//if (node != null)
				}
			}	// while (refinements.size > 0)

			horizExp = nodeToProcess.getHorizontalExpansion();
			learner.updateMaxHorizontalExpansion(horizExp);
		}
		
		newNodes.add(nodeToProcess);


		if (newPartialDefinitions.size() > 0)
			learner.newPartialDefinitionsFound(newPartialDefinitions);
		
		if (newCounterPartialDefinitions.size() > 0)
			learner.newCounterPartialDefinitionsFound(newCounterPartialDefinitions);
		
		
		if (newPotentialPartialDefinitions.size() > 0)
			((ParCELearnerExV12)learner).newPotentialPartialDefinition(newPotentialPartialDefinitions);
		 
		
		learner.newRefinementDescriptions(newNodes);		//don't need to check for empty since newNodes is never empty 

	}

}
