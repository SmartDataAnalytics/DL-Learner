package org.dllearner.algorithms.parcelex;

/**
 * This class implements a extended version of PDLL which make use of the partial definitions, i.e. 
 * 	descriptions that cover none of the positive examples and some (>0) negative examples.
 * 
 * In this implementation, the counter partial definitions will be used in the two cases:
 * 	1. For the learning termination: The learner will be terminated if one of the following conditions is reached:
 * 		- all positive examples covered by the partial definitions
 * 		- all negative examples covered by the counter partial definitions
 * 		- timeout is reached
 *	2. For getting more partial definition from the combination of counter partial definitions and the descriptions in the search tree   		
 * 
 *	@author An C. Tran
 */

import org.dllearner.algorithms.parcel.*;
import org.dllearner.algorithms.parcel.reducer.ParCELReducer;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionLengthCalculator;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

@ComponentAnn(name="ParCELearnerExV1", shortName="parcelearnerExV1", version=0.1, description="Parallel Class Expression Logic Learning")
public class ParCELearnerExV1 extends ParCELExAbstract {

	private int noOfCompactedPartialDefinition = 0;


    /**
     * Descriptions that can be combined with the counter partial definitions to become partial definitions
     */
    SortedSet<ParCELExtraNode> potentialPartialDefinitions;


	/**=========================================================================================================<br>
	 * Constructor for the learning algorithm
	 * 
	 * @param learningProblem Must be a PDLLPosNegLP
	 * @param reasoningService A reasoner
	 */
	public ParCELearnerExV1(ParCELPosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);
	}
	

	/**
	 * This constructor can be used by SpringDefinition to create bean object
	 * Properties of new bean may be initialised later using setters
	 */
	public ParCELearnerExV1() {
		super();
	}
	/**=========================================================================================================<br>
	 * Get the name of this learning algorithm
	 * 
	 * @return Name of this learning algorithm: PLLearning
	 */
	public static String getName() {
		return "PLLearningReducer";
	}
	 

	/**=========================================================================================================<br>
	 * Initial the learning algorithm
	 * 	- Create distance data
	 * 	- Prepare positive and negative examples (get from the learning problem (PLLearningPosNegLP)
	 * 	- Create a class hierarchy for refinement operator (for fast class herarchy checking)
	 * 	- Create expansion heuristic, which will be used to choose the expansion node in the search tree
	 * 	- Create refinement operator (RhoDRDown)  
	 */
	@Override
	public void init() throws ComponentInitException {
				
		//get the negative and positive examples
		if (!(learningProblem instanceof ParCELPosNegLP)) 
			throw new ComponentInitException(learningProblem.getClass() + " is not supported by '" + getName() + "' learning algorithm");
		
		
		//get the positive and negative examples from the learning problem
		positiveExamples = ((ParCELPosNegLP)learningProblem).getPositiveExamples();
		negativeExamples = ((ParCELPosNegLP)learningProblem).getNegativeExamples();
		
		
		((ParCELPosNegLP)this.learningProblem).setUncoveredPositiveExamples(this.positiveExamples);
		
		//initial heuristic which will be used by reducer to sort the search tree (expansion strategy)
		//the heuristic need to get some constant from the configurator for scoring the description
		heuristic = new ParCELDefaultHeuristic();		
		
		startClass = dataFactory.getOWLThing();	//this will be revise later using least common super class of all observations
		

		this.uncoveredPositiveExampleAllowed = (int)Math.ceil(getNoisePercentage()*positiveExamples.size());
		
		//initialise the existing uncovered positive examples
		((ParCELPosNegLP)this.learningProblem).setUncoveredPositiveExamples(uncoveredPositiveExamples);
		
		//----------------------------------
		//create refinement operator pool
		//----------------------------------
		createRefinementOperatorPool();

		baseURI = reasoner.getBaseURI();
		prefix = reasoner.getPrefixes();
		
		
		//logging the information (will use slf4j)
		if (logger.isInfoEnabled()) {
			logger.info("[pllearning] - Heuristic used: " + heuristic.getClass());
			logger.info("[pllearning] - Positive examples: " + positiveExamples.size() + ", negative examples: " + negativeExamples.size());
		}
		
		minNumberOfWorker = maxNumberOfWorker = numberOfWorkers;

	}	//init()
	

	
	/**=========================================================================================================<br>
	 * Start reducer
	 * 	1. Reset the status of reducer (stop, isRunning, done, timeout)<br> 
	 *	2. Reset the data structure (using reset() method)<br>
	 *	3. Create a set of workers and add them into the worker pool<br> 
	 *		NOTE: Each worker will have it own refinement operator<br>
	 *	4. Prepare some data: pos/neg examples, uncovered positive examples, etc.<br>
	 *	5. Start the learning progress: <br>
	 *		i) refine nodes in the (tree set) <br>
	 *	   ii) evaluate nodes in unevaluated nodes (hash set) <br> 
	 *
	 */
	@Override
	public void start() {
		reset();

		initSearchTree();

		createWorkerPool();


		//start time of reducer, statistical purpose only
		miliStarttime = System.currentTimeMillis();
		
		//----------------------------------------------------------
		// perform the learning process until the conditions for 
		//	termination meets
		//----------------------------------------------------------
		while (!isTerminateCriteriaSatisfied()) {

			ParCELNode nodeToProcess = searchTree.pollLast();
					
			//TODO: why this? why "blocking" concept does not help in this case?
			//remove this checking will exploit the heap memory and no definition found
			//NOTE: i) instead of using sleep, can we use semaphore here?
			//		ii) if using semaphore or blocking, timeout checking may not be performed on time?
			 while ((workerPool.getQueue().size() >= maxTaskQueueLength)) {                
                 try {
                         Thread.sleep(20);
                 } catch (InterruptedException e) {
                         e.printStackTrace();
                 }
			 }
			
			if ((nodeToProcess != null) && !workerPool.isShutdown() && !workerPool.isTerminating()) {
				try {
					this.createNewTask(nodeToProcess);
				}
				catch (RejectedExecutionException re) {
					logger.error(re);
					this.searchTree.add(nodeToProcess);
				}
			}
		}	//while the algorithm is not finish
		
		this.miliLearningTime = System.currentTimeMillis() - miliStarttime;
		
		stop();				
		
		//-----------------------------------------------------------------------
		//try to combine descriptions in the search tree with the counter partial 
		//definition to find more partial definition
		//-----------------------------------------------------------------------
		
		int newPartialDefCount = 0;
		
		//potential partial definitions
		synchronized (this.potentialPartialDefinitions) {
			logger.info("Processing potential partial definition: " + this.potentialPartialDefinitions.size());
			
			for (ParCELExtraNode ppd : this.potentialPartialDefinitions) {
				Set<ParCELExtraNode> combinableCounterPartialDefinitions = 
					ParCELExCombineCounterPartialDefinition.getCombinable(ppd, this.counterPartialDefinitions);
				
				//new partial definition found if the description can be combined with the set of counter partial definitions
				if (combinableCounterPartialDefinitions != null) {		
					
					ParCELExtraNode newPD = new ParCELExtraNode(ppd);
					
					
					//LinkedList<Description> tmpCounterDes = new LinkedList<Description>();
					//tmpCounterDes.add(ppd.getDescription());
					
					//for (ParCELExtraNode def : combinableCounterPartialDefinitions) {
						//tmpCounterDes.add(def.getDescription());
						//newPD.setDescription(new Intersection(newPD.getDescription(), def.getDescription()));
						//def.setType(1);
					//}
					
					newPD.setDescription(ParCELExUtilities.createIntersection(ppd.getDescription(), 
							combinableCounterPartialDefinitions, true));
					
					this.uncoveredPositiveExamples.removeAll(ppd.getCoveredPositiveExamples());

					newPD.setCompositeList(combinableCounterPartialDefinitions);
										
					newPD.setGenerationTime(System.currentTimeMillis() - miliStarttime);
					newPD.setType(2);
					
					if (allDescriptions.add(newPD.getDescription())) {
						partialDefinitions.add(newPD);
						newPartialDefCount++;
					}
					
				}	//new partial definition found
			}
		}
		
		
		//descriptions in the search tree
		synchronized (this.searchTree) {

			if (logger.isInfoEnabled())
				logger.info("Finding partial defintions from the search tree: " + this.searchTree.size());

			List<ParCELNode> newSearchTree = new ArrayList<>(this.searchTree);
			newSearchTree.sort(new ParCELCompletenessComparator());
			

			for (ParCELNode des : newSearchTree) {
				
				synchronized (this.counterPartialDefinitions) {
					Set<ParCELExtraNode> combinableCounterPartialDefinitions = 
						ParCELExCombineCounterPartialDefinition.getCombinable(des, this.counterPartialDefinitions);

					//new partial definition found if the description can be combined with the set of counter partial definitions
					if (combinableCounterPartialDefinitions != null) {		
						
						ParCELExtraNode newPD = new ParCELExtraNode(des);
						
						
						//LinkedList<Description> tmpCounterDes = new LinkedList<Description>();
						//tmpCounterDes.add(des.getDescription());
						
						//for (ParCELExtraNode def : combinableCounterPartialDefinitions) {
							//tmpCounterDes.add(def.getDescription());
							//newPD.setDescription(new Intersection(newPD.getDescription(), def.getDescription()));
							//def.setType(1);
						//}
						
						newPD.setDescription(ParCELExUtilities.createIntersection(des.getDescription(), 
								combinableCounterPartialDefinitions, true));
						
						this.uncoveredPositiveExamples.removeAll(des.getCoveredPositiveExamples());

						newPD.setCompositeList(combinableCounterPartialDefinitions);
						
						//PDLLExtraNode newPD = new PDLLExtraNode(des);
						newPD.setGenerationTime(System.currentTimeMillis() - miliStarttime);
						newPD.setType(2);
						
						if (allDescriptions.add(newPD.getDescription())) {
							partialDefinitions.add(newPD);
							newPartialDefCount++;
						}
						
					}	//new partial definition found
				}	//synch counter partial definition for processing
								
			}	//for each description in the search tree
			
		}	//synch search tree

		if (logger.isInfoEnabled()) 								
			logger.info(newPartialDefCount + " new partial definition found");

		
		//------------------------------------
		// post-learning processing:
		// reduce the partial definitions
		//------------------------------------
		if (logger.isInfoEnabled()) {			
			synchronized (partialDefinitions) {
				if (this.getCurrentlyOveralMaxCompleteness() == 1)
					logger.info("Learning finishes in: " + this.miliLearningTime + "ms, with: " + partialDefinitions.size() + " definitions");
				else if (this.isTimeout()) {
					logger.info("Learning timeout in " + this.maxExecutionTimeInSeconds + "ms. Overall completeness (%): " + this.getCurrentlyOveralMaxCompleteness());
					logger.info("Uncovered positive examples left " + this.uncoveredPositiveExamples.size() + " - " + ParCELStringUtilities.replaceString(this.uncoveredPositiveExamples.toString(), this.baseURI, this.prefix));
				}				
				else {
					logger.info("Learning is manually terminated at " + this.miliLearningTime + "ms. Overall completeness (%): " + this.getCurrentlyOveralMaxCompleteness());
					logger.info("Uncovered positive examples left " + this.uncoveredPositiveExamples.size() + " - " + ParCELStringUtilities.replaceString(this.uncoveredPositiveExamples.toString(), this.baseURI, this.prefix));					
				}
						
				logger.info("**Reduced partial definitions:");
				TreeSet<ParCELExtraNode> compactedDefinitions = (TreeSet<ParCELExtraNode>) this.getReducedPartialDefinition();
				this.noOfCompactedPartialDefinition = compactedDefinitions.size();
				int count = 1;
				for (ParCELExtraNode def : compactedDefinitions) {
					logger.info(count++ + ". " + OWLAPIRenderers.toManchesterOWLSyntax(ParCELExUtilities.groupDefinition(def.getDescription())).replace("and (not", "\nand (not") + //def.getDescription().toManchesterSyntaxString(baseURI, prefix) +
							" (length:" + new OWLClassExpressionLengthCalculator().getLength(def.getDescription()) +
							", accuracy: " + df.format(def.getAccuracy()) + 
							", type: " + def.getType() + ")");
					
					//print out the learning tree
					if (logger.isDebugEnabled()) {
						printSearchTree(def);
					}
				}
			}					
		}		
		
		super.aggregateCounterPartialDefinitionInf();
		
	}



	
	private void createNewTask(ParCELNode nodeToProcess) {
		workerPool.execute(new ParCELWorkerExV1(this, this.refinementOperatorPool,
				(ParCELPosNegLP)learningProblem, nodeToProcess, "PDLLTask-" + (noOfTask++)));
	}
	
	/**
	 * This function is used to process the counter partial definitions: description which 
	 */
	@Override
	public void newCounterPartialDefinitionsFound(Set<ParCELExtraNode> counterPartialDefinitions) {
		
		for (ParCELExtraNode def : counterPartialDefinitions) {
		
			//calculate the "actual" number of negative examples covered by the new definition
			int numberOfNewCoveredNegativeExamples;
			int numberOfCoveredNegativeExamples;	///for avoiding synchronized coveredNegativeExamples later on
			
			synchronized (this.coveredNegativeExamples) {
				numberOfNewCoveredNegativeExamples = this.coveredNegativeExamples.size();
				this.coveredNegativeExamples.addAll(def.getCoveredNegativeExamples());
				numberOfNewCoveredNegativeExamples = this.coveredNegativeExamples.size() - numberOfNewCoveredNegativeExamples;
				numberOfCoveredNegativeExamples = this.coveredNegativeExamples.size();
			}
			
			//process the counter partial definition if it covers at least 1 new negative example
			if (numberOfNewCoveredNegativeExamples > 0) {
							
				//add the new counter partial definition into the set of counter partial definitions
				synchronized (this.counterPartialDefinitions) {
					this.counterPartialDefinitions.add(def);					
				}
				
				//NOTE: when a partial definition found, we update the set of uncovered positive examples for the Learning Problem
				//			but there is no need to do it for the counter partial definition, i.e. no update for covered negative examples
				if (logger.isTraceEnabled() || logger.isDebugEnabled()) {
					logger.trace("COUNTER PARTIAL definition found: " + OWLAPIRenderers.toManchesterOWLSyntax(def.getDescription()) +
							"\n\t - covered negative examples (" + def.getCoveredNegativeExamples().size() + "): " + def.getCoveredNegativeExamples() +
							"\n\t - total covered negative examples: " + numberOfCoveredNegativeExamples + "/" + this.negativeExamples.size() 
							);					
				} else if (logger.isInfoEnabled()) {
					logger.info("COUNTER PARTIAL definition found. Total covered negative examples: " + numberOfCoveredNegativeExamples + "/" + this.negativeExamples.size());
				}
				
				//complete counter definition found
				if (this.coveredNegativeExamples.size() >= this.negativeExamples.size()) {
					this.counterDone = true;
					//this.stop();
				}
			}
			
		}
		
	}
	
	/**=========================================================================================================<br>
	 * Reset all necessary properties for a new learning
	 * 	1. Create new search tree
	 * 	2. Create an empty description set, which hold all generated description (to avoid redundancy)
	 * 	3. Create an empty 
	 */
	private void reset() {
		stop = false;
		done = false;
		timeout = false;
		counterDone = false;

		this.noOfCompactedPartialDefinition = 0;

		this.searchTree = new ConcurrentSkipListSet<>(heuristic);
		
		//allDescriptions = new TreeSet<Description>(new ConceptComparator());
		this.allDescriptions = new ConcurrentSkipListSet<>();
		
		this.partialDefinitions = new TreeSet<>(new ParCELCompletenessComparator());
		this.counterPartialDefinitions = new TreeSet<>(new ParCELCoveredNegativeExampleComparator());
		this.potentialPartialDefinitions = new TreeSet<>(new ParCELCompletenessComparator());
		
		//clone the positive examples for this set
		this.uncoveredPositiveExamples = new HashSet<>();
		this.uncoveredPositiveExamples.addAll(this.positiveExamples);	//create a copy of positive examples used to check the completeness

		this.coveredNegativeExamples = new HashSet<>();
		
		descriptionTested = 0;
		maxAccuracy = 0;

		//create a start node in the search tree
		allDescriptions.add(startClass);	//currently, start class is always Thing

	}


	/**=========================================================================================================<br>
	 * Stop the learning algorithm: Stop the workers and set the "stop" flag to true
	 */
	@Override
	public void stop() {
		
		if (!stop) {			
			stop = true;
			
			List<Runnable> waitingTasks = workerPool.shutdownNow();

			try {
				workerPool.awaitTermination(0, TimeUnit.SECONDS);
			}
			catch (InterruptedException ie) {
				logger.error(ie);
			}
			
			//put the unexecuted tasks back to the search tree
			synchronized (this.searchTree) {		
				logger.debug("Put incompleted task back to the search tree: " + waitingTasks.size());
				for (Runnable node : waitingTasks)
					searchTree.add(((ParCELWorkerExV1)node).getProcessingNode());
			}
		}
	}


	
	/**=========================================================================================================<br>
	 * Get the overall completeness of all partial definition found
	 * 
	 * @return Overall completeness so far
	 */
	public double getCurrentlyOveralMaxCompleteness() {
		return 1 - (uncoveredPositiveExamples.size()/(double)positiveExamples.size());
	}

	//methods related to the compactness: get compact definition, set compactor
	public SortedSet<ParCELExtraNode> getReducedPartialDefinition(ParCELReducer reducer) {
		return reducer.reduce(partialDefinitions, positiveExamples, uncoveredPositiveExamples.size());
	}
	
	
	public SortedSet<ParCELExtraNode> getReducedPartialDefinition() {
		return this.getReducedPartialDefinition(this.reducer);
	}
	
	
	public void setCompactor(ParCELReducer newReducer) {
		this.reducer = newReducer;
	}


	//------------------------------------------
	// getters for learning results
	//------------------------------------------
	public int getClassExpressionTests() {
		return descriptionTested;
	}
	
	//------------------------------------------------
	// setters and getters for configuration options
	//------------------------------------------------
	public int getNoOfReducedPartialDefinition() {
		return this.noOfCompactedPartialDefinition;
	}

	public SortedSet<ParCELExtraNode> getCurrentCounterPartialDefinitions() {
		return this.counterPartialDefinitions;
	}

}
