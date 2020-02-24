package org.dllearner.algorithms.parcelex;

import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.parcel.*;
import org.dllearner.refinementoperators.LengthLimitedRefinementOperator;
import org.dllearner.refinementoperators.RefinementOperator;
import org.dllearner.utilities.owl.OWLClassExpressionLengthCalculator;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;


/**
 * ParCELEx Worker which refines the given nodes and 
 * 	evaluate the refinement result. It will return partial definitions 
 * 	and/or new description to the reducer if any.
 * 
 * This worker uses Lazy Combination strategy
 * 
 * @author An C. Tran
 *
 */
public class ParCELWorkerExV1 implements Runnable {

	//name of worker (for debugging purpose)	
	private String name;

	//refinement operator used in refinement
	private ParCELRefinementOperatorPool refinementOperatorPool;
	private RefinementOperator refinementOperator;
	
	//reducer, used to make the callback to pass the result and get the next description for processing
	private ParCELExAbstract learner;

	//learning proble, provides accuracy & correctness calculation
	private ParCELPosNegLP learningProblem;	


	//the node to be processed
	private ParCELNode nodeToProcess;

	private Logger logger = Logger.getLogger(ParCELWorkerExV1.class);

	//these properties can be referred in Reducer. However, we put it here for faster access
	private String baseURI;
	private Map<String, String> prefix;

	private OWLDataFactory df = new OWLDataFactoryImpl();


	/**=========================================================================================================<br>
	 * Constructor for Worker class. A worker needs the following things: 
	 * 		i) reducer (reference), ii) refinement operator, iii) start description, iv) worker name
	 *  
	 * @param learner A reference to reducer which will be used to make a callback to return the result to
	 * @param refinementOperatorPool Refinement operator pool used to refine the given node
	 * @param learningProblem2 A learning problem used to calculate description accuracy, correctness, etc.
	 * @param nodeToProcess Node will being processed
	 * @param name Name of the worker, assigned by reduce (for tracing purpose only)
	 */
	public ParCELWorkerExV1(ParCELExAbstract learner, ParCELRefinementOperatorPool refinementOperatorPool, 
			ParCELPosNegLP learningProblem2, ParCELNode nodeToProcess, String name) {		

		super();

		this.learner = learner;
		this.refinementOperatorPool = refinementOperatorPool;
		this.refinementOperator = null;

		this.learningProblem = learningProblem2;

		this.nodeToProcess = nodeToProcess;
		this.name = name;

		this.baseURI = learner.getBaseURI();
		this.prefix = learner.getPrefix();	
	}

	
	
	/**=========================================================================================================<br>
	 * Constructor for Worker class. A worker needs the following things: 
	 * 		i) reducer (reference), ii) refinement operator, iii) start description, iv) worker name
	 *  
	 * @param reducer A reference to reducer which will be used to make a callback to return the result to
	 * @param refinementOperator Refinement operator used to refine the given node 
	 * @param learningProblem A learning problem used to calculate description accuracy, correctness, etc.
	 * @param nodeToProcess Node will being processed
	 * @param name Name of the worker, assigned by reduce (for tracing purpose only)
	 */
	public ParCELWorkerExV1(ParCELearnerExV1 reducer, RefinementOperator refinementOperator, 
			ParCELPosNegLP learningProblem, ParCELNode nodeToProcess, String name) {		

		super();

		this.learner = reducer;
		this.refinementOperator = refinementOperator;
		this.refinementOperatorPool = null;

		this.learningProblem = learningProblem;

		this.nodeToProcess = nodeToProcess;
		this.name = name;

		this.baseURI = reducer.getBaseURI();
		this.prefix = reducer.getPrefix();	
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
								newNode.setType(0);
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
								newNode.setType(1);
								newPartialDefinitions.add(newNode);
							}
							//DESCRIPTION
							else {
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
		
		learner.newRefinementDescriptions(newNodes);		//don't need to check for empty since newNodes is never empty 

	}

	/**=========================================================================================================<br>
	 * Refine a node using RhoDRDown. The refined node will be increased the max horizontal expansion value by 1
	 * 
	 * @param node Node to be refined
	 * 
	 * @return Set of descriptions that are the results of refinement  
	 */
	private TreeSet<OWLClassExpression> refineNode(ParCELNode node) {
		int horizExp = node.getHorizontalExpansion();

		if (logger.isTraceEnabled())
			logger.trace("[" + this.name + "] Refining: " + ParCELStringUtilities.replaceString(node.toString(), baseURI, prefix));
		
		boolean refirementOperatorBorrowed = false;
		
		//borrow refinement operator if necessary
		if (this.refinementOperator == null) {
			if (this.refinementOperatorPool == null) {
				logger.error("Neither refinement operator nor refinement operator pool provided");
				return null;
			} 
			else { 
				try {
					//logger.info("borrowing a refinement operator (" + refinementOperatorPool.getNumIdle() + ")");
					this.refinementOperator = this.refinementOperatorPool.borrowObject();
					refirementOperatorBorrowed = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
			
		TreeSet<OWLClassExpression> refinements = null;
		try {
			refinements = (TreeSet<OWLClassExpression>) ((LengthLimitedRefinementOperator)refinementOperator).refine(node.getDescription(), horizExp+1);
			node.incHorizontalExpansion();
			node.setRefinementCount(refinements.size());
		}
		catch (Exception e) {
			logger.error("Cannot refine " + node.getDescription());
			return null;
		}
		
		
		//return the refinement operator
		if (refirementOperatorBorrowed) {
			try {
				if (refinementOperator != null)
					refinementOperatorPool.returnObject(refinementOperator);
				else
					logger.error("Cannot return the borrowed refinement operator");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return refinements;
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
	private ParCELExtraNode checkAndCreateNewNodeV2(OWLClassExpression description, ParCELNode parentNode) {

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



	/**
	 * Get the node which is currently being processed
	 * 
	 * @return The node currently being processed
	 */
	public ParCELNode getProcessingNode() {
		return this.nodeToProcess;
	}
}
