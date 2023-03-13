package org.dllearner.algorithms.parcelex;

/**
 * This class implements a extended version of PDLL which make use of the partial definitions, i.e. 
 * 	descriptions that cover none of the positive examples and some (>0) negative examples.
 * 
 * In this implementation, the counter partial definitions will be used directly by the workers to check if they can be used 
 * to combine with the new description to become a partial definition. 
 * 
 * Therefore, there is no usage of the counter partial definition in the learner side   		
 * 
 *	@author An C. Tran
 */

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.algorithms.parcel.*;
import org.dllearner.algorithms.parcel.reducer.ParCELImprovedCoverageGreedyReducer;
import org.dllearner.algorithms.parcel.reducer.ParCELReducer;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionLengthCalculator;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;


@ComponentAnn(name="ParCELearnerExV2", shortName="parcelLearnerExV2", version=0.1, description="Parallel Class Expression Logic Learning with Exception V2")
public class ParCELearnerExV2 extends ParCELExAbstract implements ParCELearnerMBean {
		
	private int noOfReducedPartialDefinition = 0;


	/**=========================================================================================================<br>
	 * Constructor for the learning algorithm
	 * 
	 * @param learningProblem Must be a PDLLPosNegLP
	 * @param reasoningService A reasoner
	 */
	public ParCELearnerExV2(ParCELPosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);
	}
	

	/**
	 * This constructor can be used by SpringDefinition to create bean object
	 * Properties of new bean may be initialised later using setters
	 */
	public ParCELearnerExV2() {
		super();
		//this.compactor = new PDLLGenerationTimeCompactor();		
		this.reducer = new ParCELImprovedCoverageGreedyReducer();
		//this.compactor = new PDLLDefinitionLengthCompactor();
		//this.reducer = new ParCELPredScoreReducer();
	}
	/**=========================================================================================================<br>
	 * Get the name of this learning algorithm
	 * 
	 * @return Name of this learning algorithm: PLLearning
	 */
	public static String getName() {
		return "ParCELLearnerExV2";
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
		positiveTestExamples = ((ParCELPosNegLP) learningProblem).getPositiveTestExamples();
		negativeTestExamples = ((ParCELPosNegLP) learningProblem).getNegativeTestExamples();
		
		//initial heuristic which will be used by reducer to sort the search tree (expansion strategy)
		//the heuristic need to get some constant from the configurator for scoring the description
		if (heuristic == null) {
			heuristic = new ParCELDefaultHeuristic();
		}

		//this will be revise later using least common super class of all observations
		if (startClass == null) {
			startClass = dataFactory.getOWLThing();
		}

		this.uncoveredPositiveExampleAllowed = (int)Math.ceil((getNoisePercentage()/100)*positiveExamples.size());
		logger.info("Uncovered positive examples allowed (noise): " + this.uncoveredPositiveExampleAllowed);
		
		//initialise the existing uncovered positive examples
		((ParCELPosNegLP)this.learningProblem).setUncoveredPositiveExamples(uncoveredPositiveExamples);
		
		//----------------------------------
		//create refinement operator pool
		//----------------------------------
		initOperatorIfAny();
		createRefinementOperatorPool();

		baseURI = reasoner.getBaseURI();
		prefix = reasoner.getPrefixes();
		
		
		//logging the information (will use slf4j)
		if (logger.isInfoEnabled()) {
			logger.info("[pllearning] - Heuristic used: " + heuristic.getClass());
			logger.info("[pllearning] - Positive examples: " + positiveExamples.size() + ", negative examples: " + negativeExamples.size());
		}
		
		minNumberOfWorker = maxNumberOfWorker = numberOfWorkers;

		trainingTime = System.currentTimeMillis();

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

		String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());
		logger.info("Time " + getCurrentCpuMillis() / 1000.0 + "s; " + timeStamp);
		
		//----------------------------------------------------------
		// perform the learning process until the conditions for 
		//	termination meets
		//----------------------------------------------------------
		while (!isTerminateCriteriaSatisfied()) {

			//-------------------
			//check for timeout
            //-------------------
			timeout = (this.maxExecutionTimeInSeconds > 0 && (getCurrentCpuMillis()) > this.maxExecutionTimeInSeconds*1000);
				
			if (timeout)
				break;
			
			
			ParCELNode nodeToProcess;
			
			nodeToProcess = searchTree.pollFirst();
					
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
		/*
		synchronized (this.searchTree) {

			if (logger.isInfoEnabled())
				logger.info("Processing counter partial definition: " + this.counterPartialDefinitions.size() + ", " + this.searchTree.size());

			List<ParCELNode> newSearchTree = new ArrayList<ParCELNode>(this.searchTree);
			Collections.sort(newSearchTree, new ParCELCompletenessComparator());

			if (logger.isInfoEnabled())
				logger.info("Finding new partial definition from COUNTER partial definition: ");

			int newPartialDefCount = 0;

			for (ParCELNode des : newSearchTree) {
				synchronized (this.counterPartialDefinitions) {

					Set<ParCELExtraNode> combinableCounterPartialDefinitions = 
						ParCELExCombineCounterPartialDefinition.getCombinable(des, this.counterPartialDefinitions);

					if (combinableCounterPartialDefinitions != null) {		
						
						//List<Description> counterList = new LinkedList<Description>();
						//counterList.add(des.getDescription());
						
						//for (ParCELExtraNode def : combinableCounterPartialDefinitions)
							//counterList.add(def.getDescription());
							//des.setDescription(new Intersection(des.getDescription(), def.getDescription()));
						//des.setDescription(new Intersection(counterList));
						des.setDescription(ParCELExUtilities.createIntersection(des.getDescription(), 
								combinableCounterPartialDefinitions, true));

						ParCELExtraNode newPD = new ParCELExtraNode(des);
						newPD.setGenerationTime(System.currentTimeMillis() - miliStarttime);
						newPD.setType(ParCELExNodeTypes.PARTIAL_DEFINITION_LAZY_COMBINATION);	//type 2
						//newPD.setCompositeList(combinableCounterPartialDefinitions);
						partialDefinitions.add(newPD);											
					}	//new partial definition found
				}	//synch counter partial definition
			}	//for each description in the search tree
			
			if (logger.isInfoEnabled()) 								
				logger.info(newPartialDefCount + " new partial definition found");
			
		}	//synch search tree
		*/

		
		//-------------------------------
		//post-learning processing
		//-------------------------------
		
		if (logger.isInfoEnabled()) {
			synchronized (searchTree) {
				logger.info("Search tree size: " + searchTree.size());
			}

			synchronized (partialDefinitions) {
				if (this.getCurrentlyOveralMaxCompleteness() == 1)
					logger.info("Learning finishes in: " + this.miliLearningTime + "ms, with: " + partialDefinitions.size() + " definitions");
				else if (this.isTimeout()) {
					logger.info("Learning timeout in " + this.maxExecutionTimeInSeconds + "s. Overall completeness (%): " + this.getCurrentlyOveralMaxCompleteness());
					logger.info("Uncovered positive examples left " + this.uncoveredPositiveExamples.size() + " - " + ParCELStringUtilities.replaceString(this.uncoveredPositiveExamples.toString(), this.baseURI, this.prefix));
				}				
				else {
					logger.info("Learning is manually terminated at " + this.miliLearningTime + "ms. Overall completeness (%): " + this.getCurrentlyOveralMaxCompleteness());
					logger.info("Uncovered positive examples left " + this.uncoveredPositiveExamples.size() + " - " + ParCELStringUtilities.replaceString(this.uncoveredPositiveExamples.toString(), this.baseURI, this.prefix));					
				}

				timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());
				logger.info("Time " + getCurrentCpuMillis() / 1000.0 + "s; " + timeStamp);

				OWLClassExpression bestDescription = getUnionCurrentlyBestDescription();
				double acc = computeAccuracy(bestDescription);
				logger.info("Accuracy: " + acc);
						
				logger.info("**Reduced partial definitions:");
				TreeSet<ParCELExtraNode> compactedDefinitions = (TreeSet<ParCELExtraNode>) this.getReducedPartialDefinition();
				this.noOfReducedPartialDefinition = compactedDefinitions.size();
				int count = 1;
				for (ParCELExtraNode def : compactedDefinitions) {
					int tpTest = learningProblem instanceof ParCELPosNegLP
						? ((ParCELPosNegLP) learningProblem).getNumberOfCoveredPositiveTestExamples(def.getDescription())
						: 0;

					logger.info(count++ + ". " + OWLAPIRenderers.toManchesterOWLSyntax(ParCELExUtilities.groupDefinition(def.getDescription())).replace("and (not", "\nand (not") + //def.getDescription().toManchesterSyntaxString(baseURI, prefix) +
							" (length:" + new OWLClassExpressionLengthCalculator().getLength(def.getDescription()) +
							", accuracy: " + df.format(def.getAccuracy()) + " / " + computeTestAccuracy(def.getDescription()) +
							", coverage: " + def.getNumberOfCoveredPositiveExamples() + " / " + tpTest + ")" +
							", type: " + def.getType() + ")");
					
					//test the new representation of the partial definition 
					//in which the counter partials will be group for readability
					//logger.info("Definition after grouped: " + ParCELEx2DefinitionGrouping.groupDefinition(def.getDescription()).toManchesterSyntaxString(baseURI, prefix).replace("and (not", "\nand (not"));
					
					//print out the learning tree
					if (logger.isTraceEnabled()) {
						ParCELNode parent = (ParCELNode)def.getParent();			
						while (parent != null) {
							logger.trace("  <-- " + OWLAPIRenderers.toManchesterOWLSyntax(parent.getDescription()));
									//" [acc:" +  df.format(parent.getAccuracy()) +
									//", correctness:" + df.format(parent.getCorrectness()) + ", completeness:" + df.format(parent.getCompleteness()) +
									//", score:" + df.format(this.heuristic.getScore(parent)) + "]");
							
							//print out the children nodes
							Collection<OENode> children = parent.getChildren();
							for (OENode child : children) {
								ParCELNode tmp = (ParCELNode)child;
								logger.trace("    --> " + OWLAPIRenderers.toManchesterOWLSyntax(tmp.getDescription()));
										//" [acc:" +  df.format(tmp.getAccuracy()) +
										//", correctness:" + df.format(tmp.getCorrectness()) + ", completeness:" + df.format(tmp.getCompleteness()) + 
										//", score:" + df.format(this.heuristic.getScore(tmp)) + "]");
							}
							parent = (ParCELNode)parent.getParent();				
						}
					} //end of printing the learning tree
				}

				if (learningProblem instanceof ParCELPosNegLP) {
					Set<OWLClassExpression> partialDefs = getReducedPartialDefinition()
							.stream().map(OENode::getDescription).collect(Collectors.toSet());

					((ParCELPosNegLP) learningProblem).printTestEvaluation(partialDefs);
				}

				printBestConceptsTimesAndAccuracies();
			}					
		}		
				
		
		super.aggregateCounterPartialDefinitionInf();
		
	}	//start()

	
	private void createNewTask(ParCELNode nodeToProcess) {
		workerPool.execute(new ParCELWorkerExV2(this, this.refinementOperatorPool,
				(ParCELPosNegLP)learningProblem, nodeToProcess, "ParCELEx2Task-" + (noOfTask++)));
	}
	

	
	/**=========================================================================================================<br>
	 * Callback method for worker when a partial definition found 
	 * 		(callback for an evaluation request from reducer)<br>
	 * If a definition (partial) found, do the following tasks:<br>
	 * 	1. Add the definition into the partial definition set<br>
	 * 	2. Update: uncovered positive examples, max accuracy, best description length
	 * 	2. Check for the completeness of the learning. If yes, stop the learning<br>
	 * 
	 * @param definitions New partial definitions
	 */
	public void newPartialDefinitionsFound(Set<ParCELExtraNode> definitions) {

		for (ParCELExtraNode def : definitions) {

			//NOTE: in the previous version, this node will be added back into the search tree
			//		it is not necessary since in DLLearn, a definition may be revised to get a better one but
			//			in this approach, we do not refine partial definition.
		
			//remove uncovered positive examples by the positive examples covered by the new partial definition
			int uncoveredPositiveExamplesRemoved;
			int uncoveredPositiveExamplesSize;		//for avoiding synchronized uncoveredPositiveExamples later on
			
			synchronized (uncoveredPositiveExamples) {
				uncoveredPositiveExamplesRemoved = this.uncoveredPositiveExamples.size();
				this.uncoveredPositiveExamples.removeAll(def.getCoveredPositiveExamples());
				uncoveredPositiveExamplesSize = this.uncoveredPositiveExamples.size();
			}

			uncoveredPositiveExamplesRemoved -= uncoveredPositiveExamplesSize;
			
			if (uncoveredPositiveExamplesRemoved > 0) {
							
				//set the generation time for the new partial definition
				//def.setGenerationTime(System.currentTimeMillis() - miliStarttime);		
				synchronized (partialDefinitions) {
					partialDefinitions.add(def);
				}
				
				if (logger.isTraceEnabled()) {
					logger.trace("PARTIAL definition found: " + OWLAPIRenderers.toManchesterOWLSyntax(def.getDescription()) +
							"\n\t - covered positive examples (" + def.getNumberOfCoveredPositiveExamples() + "): " +def.getCoveredPositiveExamples() +
							"\n\t - uncovered positive examples left: " + uncoveredPositiveExamplesSize + "/" + positiveExamples.size() +
							", des:" + def.getGenerationTime()
							);					
				}
				else if (logger.isDebugEnabled())
					logger.debug("PARTIAL definition found: " + OWLAPIRenderers.toManchesterOWLSyntax(def.getDescription()) +
							"\n\t - covered positive examples (" + def.getNumberOfCoveredPositiveExamples() + "): " +def.getCoveredPositiveExamples() +
							"\n\t - uncovered positive examples left: " + uncoveredPositiveExamplesSize + "/" + positiveExamples.size()
							);
				else if (logger.isInfoEnabled()) {
					logger.info("PARTIAL definition found. Uncovered positive examples left: " + uncoveredPositiveExamplesSize + "/" + positiveExamples.size());
					String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());
					logger.info(timeStamp + ": " + def);

					double actualTrainingTime = getCurrentCpuMillis() / 1000.0;

					OWLClassExpression bestDescription = getUnionCurrentlyBestDescription();
					double acc = computeAccuracy(bestDescription);
					double testAcc = computeTestAccuracy(bestDescription);

					logger.info("Training time: " + actualTrainingTime + "s Accuracy: " + acc + " Test accuracy: " + testAcc);

					recordBestConceptTimeAndAccuracy(actualTrainingTime, acc, testAcc);
				}

				//update the max accuracy and max description length
				if (def.getAccuracy() > this.maxAccuracy) {
					this.maxAccuracy = def.getAccuracy();
					this.bestDescriptionLength = new OWLClassExpressionLengthCalculator().getLength(def.getDescription());
				}
				
				//check if the complete definition found
				if (uncoveredPositiveExamplesSize <= uncoveredPositiveExampleAllowed) {
					this.done = true;
					//stop();
				}
			}

		}	//for each partial definition
		
	}	//definitionFound()
	
	
	/**=========================================================================================================<br>
	 * This function is used to process the counter partial definitions returned from the workers
	 */
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
				if (logger.isTraceEnabled()) {
					logger.trace("(-) COUNTER PARTIAL definition found: " + OWLAPIRenderers.toManchesterOWLSyntax(def.getDescription()) + //TODO def.getDescription().getChild(0)
							"\n\t - covered negative examples (" + def.getNumberOfCoveredNegativeExamples() + "): " + def.getCoveredNegativeExamples() +
							"\n\t - total covered negative examples: " + numberOfCoveredNegativeExamples + "/" + this.negativeExamples.size() 
							);					
				}
				else if (logger.isDebugEnabled())
					logger.debug("(-) COUNTER PARTIAL definition found: " + OWLAPIRenderers.toManchesterOWLSyntax(def.getDescription()) + //TODO def.getDescription().getChild(0)
							"\n\t - cn (" + def.getNumberOfCoveredNegativeExamples() + "): " + def.getCoveredNegativeExamples() +
							"\n\t - total cn: " + numberOfCoveredNegativeExamples + "/" + this.negativeExamples.size() 
							);
				else if (logger.isInfoEnabled()) {
					logger.info("COUNTER PARTIAL definition found. Total covered negative examples: " + numberOfCoveredNegativeExamples + "/" + this.negativeExamples.size());
					String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());
					logger.info(timeStamp + ": " + def);

					double actualTrainingTime = getCurrentCpuMillis() / 1000.0;

					OWLClassExpression bestDescription = getUnionCurrentlyBestDescription();
					double acc = computeAccuracy(bestDescription);
					double testAcc = computeTestAccuracy(bestDescription);

					logger.info("Training time: " + actualTrainingTime + "s Accuracy: " + acc + " Test accuracy: " + testAcc);

					recordBestConceptTimeAndAccuracy(actualTrainingTime, acc, testAcc);
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
		searchTree = new ConcurrentSkipListSet<>(heuristic.reversed());
		
		//allDescriptions = new TreeSet<Description>(new ConceptComparator());
		this.allDescriptions = new ConcurrentSkipListSet<>();
		
		partialDefinitions = new TreeSet<>(new ParCELCompletenessComparator());
		counterPartialDefinitions = new TreeSet<>(new ParCELCorrectnessComparator());
		
		//clone the positive examples for this set
		this.uncoveredPositiveExamples = new HashSet<>();
		this.uncoveredPositiveExamples.addAll(this.positiveExamples);	//create a copy of positive examples used to check the completeness

		this.coveredNegativeExamples = new HashSet<>();
		
		descriptionTested = 0;
		maxAccuracy = 0;

		stop = false;
		done = false;
		timeout = false;
		counterDone = false;

		this.noOfReducedPartialDefinition = -1;
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
				workerPool.awaitTermination(10, TimeUnit.SECONDS);
			}
			catch (InterruptedException ie) {
				logger.error(ie);
			}

			//put the unexecuted tasks back to the search tree
			synchronized (this.searchTree) {
				logger.debug("Put incompleted task back to the search tree: " + waitingTasks.size());
				for (Runnable node : waitingTasks)
					searchTree.add(((ParCELWorkerExV2)node).getProcessingNode());
			}
			
			//don't stop immediately, wait until all workers finish their jobs
			/*
			while (workerPool.getQueue().size() > 0) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			*/
		}
	}

	public boolean isTimeout() {
		return timeout;
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



	public int getNoOfReducedPartialDefinition() {
		return this.noOfReducedPartialDefinition;
	}
	
	public SortedSet<ParCELExtraNode> getCurrentCounterPartialDefinitions() {
		return this.counterPartialDefinitions;
	}

}
