package org.dllearner.algorithms.parcelex;

import org.dllearner.algorithms.parcel.*;
import org.dllearner.refinementoperators.RefinementOperator;
import org.dllearner.utilities.owl.OWLClassExpressionLengthCalculator;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


/**
 * Worker class which will do the refinement on the given nodes and 
 * 	evaluate the refinement result. It will return partial definitions  
 * 	and/or new description to the reducer if any.   
 * 
 * This worker uses Online Combination strategy, i.e. it combine refinements with current 
 * 	counter partial definition set to create more cpdef
 * 
 * @author actran
 *
 */
public class ParCELWorkerExV2 extends ParCELExWorkerAbstract<ParCELearnerExV2> {


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
	public ParCELWorkerExV2(ParCELearnerExV2 learner, ParCELRefinementOperatorPool refinementOperatorPool,
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
	public ParCELWorkerExV2(ParCELearnerExV2 learner, RefinementOperator refinementOperator,
			ParCELPosNegLP learningProblem, ParCELNode nodeToProcess, String name) {
		super(learner, refinementOperator, learningProblem, nodeToProcess, name);
	}

	
	
	/**=========================================================================================================<br>
	 * Start the worker: Refine the given node and evaluate the refinements 
	 * <br>
	 * This method will call back the learner  to return:
	 * <ol>
	 * 	<li>New descriptions</li>
	 * 	<li>New partial definitions</li>
	 * 	<li>New counter partial definition</li>
	 * </ol>
	 * 
	 * NOTE: Partial definition generation time in this learner is used to represent the definition "type":
	 *  <br>
	 *  1: partial definitions that are generated directly by the refinement<br>
	 *  2: partial definitions that are generated by the combination of the descriptions in the search tree 
	 *  	and the counter partial definitions after the learning finishes 
	 *  3: partial definitions that are generated by the combination of new refinement and the counter partial definitions 
	 *  4: partial definitions that are generated by the combination of the input node and the counter partial definitions in the refinement
	 */
	@Override
	public void run() {
		
		if (logger.isTraceEnabled())
			logger.trace("[PLLearning] Processing node (" + ParCELStringUtilities.replaceString(nodeToProcess.toString(), this.baseURI, this.prefix));


		TreeSet<OWLClassExpression> refinements;	//will hold the refinement result (set of Descriptions)
		
		HashSet<ParCELExtraNode> newPartialDefinitions = new HashSet<>();	//hold the partial definitions if any
		HashSet<ParCELExtraNode> newCounterPartialDefinitions = new HashSet<>();
		
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
														
							//COUNTER PARTIAL DEFINITION: completeness=0 (cp=0) and correctness<1 (cn>0)
							//NOTE: Note that the counter partial will be return in the negative form
							if (newNode.getCorrectness() < 1d) {
								newNode.setType(ParCELExNodeTypes.COUNTER_PARTIAL_DEFINITION_UNUSED);	//0
								newNode.setGenerationTime(System.currentTimeMillis() - learner.getMiliStarttime());
								newNode.setExtraInfo(learner.getTotalDescriptions());
								newNode.setDescription(df.getOWLObjectComplementOf(newNode.getDescription()));
								newCounterPartialDefinitions.add(newNode);							
							}
						}
						else {						
							//PARTIAL DEFINITION
							if (newNode.getCorrectness() == 1.0d) {
								newNode.setGenerationTime(System.currentTimeMillis() - learner.getMiliStarttime());
								newNode.setExtraInfo(learner.getTotalDescriptions());
								newNode.setType(ParCELExNodeTypes.PARTIAL_DEFINITION_DIRECT_REFINED);	//1
								newPartialDefinitions.add(newNode);
							}
							//DESCRIPTION
							else {
								
								//check for the combination with the current counter partial definition set
								Set<ParCELExtraNode> combinableCounterPartialDefinitions = 
									ParCELExCombineCounterPartialDefinition.getCombinable(newNode, learner.getCurrentCounterPartialDefinitions());
								
								//PARTIAL DEFINITION
								//the new description may be combined with the counter partial definitions to become a partial definition
								if (combinableCounterPartialDefinitions != null) {	
									//for (ParCELExtraNode def : combinableCounterPartialDefinitions) {
										newNode.setDescription(ParCELExUtilities.createIntersection(newNode.getDescription(), 
												combinableCounterPartialDefinitions, true));										
										//def.setType(ParCELExNodeTypes.COUNTER_PARTIAL_DEFINITION_USED);	//2 - to mark the counter definition had been used to generate the partial definition
									//}
									
									//3 - means the partial definition is the result of the combination of a new refined description and a counter partial definition  
									newNode.setType(ParCELExNodeTypes.PARTIAL_DEFINITION_ONLINE_COMBINATION);	
									newNode.setGenerationTime(System.currentTimeMillis() - learner.getMiliStarttime());
									newNode.setExtraInfo(learner.getTotalDescriptions());
									newPartialDefinitions.add(newNode);
								}
								else	//the new node cannot be combined ==> this is a DESCRIPTION  
									newNodes.add(newNode);
							}
						}
					}	//if (node != null)
				}
			}	// while (refinements.size > 0)
			
			horizExp = nodeToProcess.getHorizontalExpansion();
			learner.updateMaxHorizontalExpansion(horizExp);
		}
		
		//process the input node: check if is it potentially a partial definition
		Set<ParCELExtraNode> combinableCounterPartialDefinitions = 
			ParCELExCombineCounterPartialDefinition.getCombinable(nodeToProcess, learner.getCurrentCounterPartialDefinitions());
				
		if (combinableCounterPartialDefinitions != null) {		
			//for (ParCELExtraNode def : combinableCounterPartialDefinitions)
				nodeToProcess.setDescription(ParCELExUtilities.createIntersection(
						nodeToProcess.getDescription(), combinableCounterPartialDefinitions, true));
			
			ParCELExtraNode newPD = new ParCELExtraNode(nodeToProcess);
			newPD.setGenerationTime(System.currentTimeMillis() - learner.getMiliStarttime());
			newPD.setExtraInfo(learner.getTotalDescriptions());
			newPD.setType(ParCELExNodeTypes.PARTIAL_DEFINITION_REFINED_NODE);	//4 - (refined node + counter pdef) 
			newPartialDefinitions.add(newPD);
		}
		else
			newNodes.add(nodeToProcess);
		

		if (newPartialDefinitions.size() > 0)
			learner.newPartialDefinitionsFound(newPartialDefinitions);
		
		if (newCounterPartialDefinitions.size() > 0)
			learner.newCounterPartialDefinitionsFound(newCounterPartialDefinitions);

		learner.newRefinementDescriptions(newNodes);		//don't need to check for empty since newNodes is never empty 

	}

}