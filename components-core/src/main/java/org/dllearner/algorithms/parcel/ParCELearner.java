package org.dllearner.algorithms.parcel;

/**
 * This class implements a Parallel Description Logic Learner (PDLL) using Worker/Reducer model.
 * Basic configuration for a PDLL including:
 * <ol>
 * 	<li>numberOfWorkers: The number of workers. The default value for this parameter is 2. 
 * 			Basically, the number of workers should be 2 x number of cores</li>
 * 	<li>maxExecutionTimeInSecond: Timeout in ms. By default, there is no timeout for the learning</li>
 * 	<li>maxNoOfSplits: Maximal number os split used for numerical data properties. SHABDDoubleSplitter may be used.</li>
 * </ol>
 * 
 *	@author An C. Tran
 */

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;

import org.dllearner.core.*;
import org.dllearner.utilities.owl.EvaluatedDescriptionComparator;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionLengthCalculator;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

@ComponentAnn(name = "ParCEL", shortName = "parcel", version = 0.1, description = "PARallel Class Expression Learning")
public class ParCELearner extends ParCELAbstract implements ParCELearnerMBean {

	// will be used in MBean for debugging purpose
	private int noOfCompactedPartialDefinition = 0;

	private int noOfUncoveredPositiveExamples;

	/**
	 * This may be considered as the noise allowed in learning, i.e. the maximum number of positive
	 * examples can be discard (uncovered)
	 */
	private int uncoveredPositiveExampleAllowed = 0;
	
	/**
     * Holds the uncovered positive example, this will be updated when the worker found a partial
     * definition since the callback method "definitionFound" is synchronized", there is no need to
     * create a thread-safe for this set
     */
	private HashSet<OWLIndividual> uncoveredPositiveExamples;

	private ParCELNode startNode; 	// root of the search tree


	// ------------------------------------------------
	// variables for statistical purpose
	// ------------------------------------------------
	private long miliStarttime = 0;
	private long miliLearningTime = 0;

	// some properties for statistical purpose
	
	private int currentMaxHorizExp = 0;
	private int bestDescriptionLength = 0;
	private double maxAccuracy = 0.0d;
	

	// number of task created (for debugging purpose only)  
	private int noOfTask = 0;


	/**
	 * ============================================================================================
	 * Constructor for PDLL learning algorithm
	 * 
	 * @param learningProblem
	 *            Must be a PDLLPosNegLP
	 * @param reasoningService
	 *            A reasoner
	 */
	public ParCELearner(ParCELPosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);

		// default compactor used by this algorithm
		this.reducer = new ParCELImprovedCoverageGreedyReducer();
	}

	/**
	 * This constructor can be used by SpringDefinition to create bean object Properties of new bean
	 * may be initialised later using setters
	 */
	public ParCELearner() {
		super();
		
		this.reducer = new ParCELImprovedCoverageGreedyReducer();
		// this.compactor = new PDLLGenerationTimeCompactor();
		// this.compactor = new PDLLDefinitionLengthCompactor();
	}

	/**
	 * ============================================================================================
	 * Get the name of this learning algorithm
	 * 
	 * @return Name of this learning algorithm: PLLearning
	 */
	public static String getName() {
		return "ParCELearner";
	}

	/**
	 * ============================================================================================
	 * Initial the learning algorithm:
	 */
	@Override
	public void init() throws ComponentInitException {

		// check the learning problem, this learning algorithm support PDLLPosNegLP only
		if (!(learningProblem instanceof ParCELPosNegLP))
			throw new ComponentInitException(learningProblem.getClass() + " is not supported by '"
					+ getName() + "' learning algorithm. Only ParCELPosNegLP is supported.");

		// get the positive and negative examples from the learning problem
		positiveExamples = ((ParCELPosNegLP) learningProblem).getPositiveExamples();
		negativeExamples = ((ParCELPosNegLP) learningProblem).getNegativeExamples();

		// clone the positive examples for this set to avoid affecting the Learning Problem
		// this will be used to check the coverage of the partial definition (completeness)
		this.uncoveredPositiveExamples = new HashSet<>(this.positiveExamples);

		((ParCELPosNegLP) this.learningProblem).setUncoveredPositiveExamples(this.positiveExamples);

		// initial heuristic which will be used by reducer to sort the search tree
		// the heuristic need to get some constant from the configurator for scoring the description
		if (this.heuristic == null)
			heuristic = new ParCELDefaultHeuristic();

		// this will be revised later using least common super class of all observations
		if (startClass == null) {
			startClass = dataFactory.getOWLThing();
		}

		//TODO check this - what is noise? for positive or negative examples?
		//----------------------
		//this.uncoveredPositiveExampleAllowed = (int) Math.ceil(getNoisePercentage() * positiveExamples.size());
		this.uncoveredPositiveExampleAllowed = 0;		
		noiseAllowed = this.noisePercentage/100d;
		//----------------------

		// initial the existing uncovered positive examples
		((ParCELPosNegLP) this.learningProblem).setUncoveredPositiveExamples(uncoveredPositiveExamples);

		// ----------------------------------
		// create refinement operator pool
		// ----------------------------------
		createRefinementOperatorPool();

		baseURI = reasoner.getBaseURI();
		prefix = reasoner.getPrefixes();

		// logging the information (will use slf4j)
		if (logger.isInfoEnabled()) {
			logger.info("Heuristic used: " + heuristic.getClass());
			logger.info("Positive examples: " + positiveExamples.size()
					+ ", negative examples: " + negativeExamples.size());
		}

		minNumberOfWorker = maxNumberOfWorker = numberOfWorkers;

	} // init()

	/**
	 * ============================================================================================
	 * Start reducer: <br>
	 * 1. Reset the status of reducer (stop, isRunning, done, timeout)<br>
	 * 2. Reset the data structure (using reset() method)<br>
	 * 3. Create a set of workers and add them into the worker pool<br>
	 * NOTE: Each worker will have it own refinement operator<br>
	 * 4. Prepare some data: pos/neg examples, uncovered positive examples, etc.<br>
	 * 5. Start the learning progress: <br>
	 * i) refine nodes in the (tree set) <br>
	 * ii) evaluate nodes in unevaluated nodes (hash set) <br>
	 * 
	 */
	/*
	 * (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#start()
	 */
	@Override
	public void start() {

		
		// register a MBean for debugging purpose
		try {
			ObjectName parCELearnerBean = new ObjectName(
					"org.dllearner.algorithms.parcel.ParCELearnerMBean:type=ParCELearnerBean");
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			if (!mbs.isRegistered(parCELearnerBean))
				mbs.registerMBean(this, parCELearnerBean);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		stop = false;
		done = false;
		timeout = false;
	
		//allDescriptions = new TreeSet<Description>(new ConceptComparator());		
		allDescriptions = new ConcurrentSkipListSet<>();
		
		searchTree = new ConcurrentSkipListSet<>(heuristic);
		
		//System.out.println("[ParCELLearner] Heuristic ExpansionPenaltyFactor: " + ((ParCELDefaultHeuristic)heuristic).getExpansionPenaltyFactor());

		partialDefinitions = new TreeSet<>(new ParCELCorrectnessComparator());

		maxAccuracy = 0;		//currently max accuracy	
		this.noOfCompactedPartialDefinition = 0;
		this.noOfUncoveredPositiveExamples = this.positiveExamples.size();


		// create a start node in the search tree
		// currently, start class is always Thing (initialised in the init() method)
		allDescriptions.add(startClass); 

		// create a start node and add it into the search tree
		startNode = new ParCELNode((ParCELNode) null, startClass, this.positiveExamples.size()
				/ (double) (this.positiveExamples.size() + this.negativeExamples.size()), 0, 1.0);

		searchTree.add(startNode); // add the root node into the search tree

		// ---------------------------------------------
		// create worker pool
		// ---------------------------------------------
		// taskQueue = new ArrayBlockingQueue<Runnable>(maxTaskQueueLength);
		taskQueue = new LinkedBlockingQueue<>(maxTaskQueueLength);

		workerPool = new ThreadPoolExecutor(minNumberOfWorker, maxNumberOfWorker, keepAliveTime,
				TimeUnit.MILLISECONDS, taskQueue, new ParCELWorkerThreadFactory());


		if (logger.isInfoEnabled())
			logger.info("Worker pool created, core pool size: " + workerPool.getCorePoolSize()
					+ ", max pool size: " + workerPool.getMaximumPoolSize());

		// start time of the learner
		miliStarttime = System.currentTimeMillis();

		// ----------------------------------------------------------
		// perform the learning process until the conditions for
		// termination meets
		// ----------------------------------------------------------
		while (!isTerminateCriteriaSatisfied()) {

			// -------------------
			// check for timeout
			// -------------------
			timeout = (this.maxExecutionTimeInSeconds > 0 && (System.currentTimeMillis() - miliStarttime) > this.maxExecutionTimeInSeconds * 1000);

			if (timeout)
				break;

			ParCELNode nodeToProcess = searchTree.pollLast();

			// TODO: why this? why "blocking" concept does not help in this case?
			// remove this checking will exploit the heap memory and no definition found
			// NOTE: i) instead of using sleep, can we use semaphore here?
			// ii) if using semaphore or blocking, timeout checking may not be performed on time?
			while ((workerPool.getQueue().size() >= maxTaskQueueLength) && !done) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			//NOTE: the above WHILE loop and the following IF statement require checking "done" 
			//		condition to prevent waiting for the full+finish job or the full+terminating workerpool 
			
			if ((nodeToProcess != null) && !done && !workerPool.isShutdown() && !workerPool.isTerminating()) {
				try {
					this.createNewTask(nodeToProcess);
				} catch (RejectedExecutionException re) {
					logger.error(re);
					
					//if cannot submit the new task, return the node back to the search tree
					this.searchTree.add(nodeToProcess);	
				}
			}
		} // while the algorithm is not finish

		this.miliLearningTime = System.currentTimeMillis() - miliStarttime;

		stop();

		// -------------------------------
		// post-learning processing
		// -------------------------------
		if (logger.isInfoEnabled()) {
			synchronized (partialDefinitions) {
				double acc = (this.negativeExamples.size() + this.positiveExamples.size() - 
						this.uncoveredPositiveExamples.size())/
						(double) (this.positiveExamples.size() + this.negativeExamples.size());
				
				if (this.getCurrentlyOveralMaxCompleteness() == 1)
					logger.info("Learning finishes in: " + this.miliLearningTime + "ms, with: "
							+ partialDefinitions.size() + " definitions");
				else if (this.isTimeout()) {
					logger.info("Learning timeout in " + this.maxExecutionTimeInSeconds
							+ "ms. Overall completeness: "
							+ df.format(this.getCurrentlyOveralMaxCompleteness()) + ", accuracy: "
							+ df.format(acc));

					logger.info("Uncovered positive examples left "
							+ this.uncoveredPositiveExamples.size()
							+ " - "
							+ ParCELStringUtilities.replaceString(
									this.uncoveredPositiveExamples.toString(), this.baseURI,
									this.prefix));
				} else {
					logger.info("Learning is manually terminated at " + this.miliLearningTime
							+ "ms. Overall completeness: "
							+ df.format(this.getCurrentlyOveralMaxCompleteness()));
					logger.info("Uncovered positive examples left "
							+ this.uncoveredPositiveExamples.size()
							+ " - "
							+ ParCELStringUtilities.replaceString(
									this.uncoveredPositiveExamples.toString(), this.baseURI,
									this.prefix));
				}
				
				logger.info("Total descriptions generated: " + allDescriptions.size()
						+ ", best description length: "  + getCurrentlyBestDescriptionLength()
						+ ", max expansion length: " + getMaximumHorizontalExpansion());

				logger.info("Compacted partial definitions:");
				TreeSet<ParCELExtraNode> compactedDefinitions = (TreeSet<ParCELExtraNode>) this
						.getReducedPartialDefinition();
				this.noOfCompactedPartialDefinition = compactedDefinitions.size();
				int count = 1;
				for (ParCELExtraNode def : compactedDefinitions) {
					logger.info(count++ + ". "
							+ OWLAPIRenderers.toManchesterOWLSyntax(def.getDescription())
							+ " (length:" + new OWLClassExpressionLengthCalculator().getLength(def.getDescription()) + ", accuracy: "
							+ df.format(def.getAccuracy()) + ", coverage: "
							+ def.getCoveredPositiveExamples().size() + ")");

					// print out the learning tree
					/*
					 * if (logger.isDebugEnabled()) { OENode parent = def.getParent(); while (parent
					 * != null) { logger.debug("  <-- " +
					 * parent.getDescription().toManchesterSyntaxString(baseURI, prefix)); // print
					 * out the children nodes List<OENode> children = parent.getChildren(); for
					 * (OENode child : children) logger.debug("    --> " +
					 * child.getDescription().toManchesterSyntaxString(baseURI, prefix)); parent =
					 * parent.getParent(); } }
					 */
					
				}	//for each reduced partial definitions
			}	//synchronise partial definitions for reduction
		}

	} // start()

	
	//create a new task given a PDLLNode
	private void createNewTask(ParCELNode nodeToProcess) {
		workerPool.execute(new ParCELWorker(this, this.refinementOperatorPool,
				(ParCELPosNegLP) learningProblem, nodeToProcess, "ParCELTask-" + (noOfTask++)));
	}

	/**
	 * ============================================================================================
	 * Callback method for worker when partial definitions found (callback for an evaluation request
	 * from reducer)<br>
	 * If a definition (partial) found, do the following tasks:<br>
	 * 1. Add the definition into the partial definition set<br>
	 * 2. Update: uncovered positive examples, max accuracy, best description length<br>
	 * 3. Check for the completeness of the learning. If yes, stop the learning<br>
	 * 
	 * @param definitions
	 *            New partial definitions
	 */
	@Override
	public void newPartialDefinitionsFound(Set<ParCELExtraNode> definitions) {

		for (ParCELExtraNode def : definitions) {
			// NOTE: in the previous version, this node will be added back into the search tree
			// it is not necessary since in DLLearn, a definition may be revised to get a better one
			// but
			// in this approach, we do not refine partial definition.

			// remove uncovered positive examples by the positive examples covered by the new
			// partial definition
			int uncoveredPositiveExamplesRemoved;
			int uncoveredPositiveExamplesSize;
			
			//re-calculate the generation time of pdef
			def.setGenerationTime(def.getGenerationTime() - miliStarttime);
			
			synchronized (uncoveredPositiveExamples) {
				uncoveredPositiveExamplesRemoved = this.uncoveredPositiveExamples.size();
				this.uncoveredPositiveExamples.removeAll(def.getCoveredPositiveExamples());
				uncoveredPositiveExamplesSize = this.uncoveredPositiveExamples.size();
			}	//end of uncovere dPositive examples synchronise 

			uncoveredPositiveExamplesRemoved -= uncoveredPositiveExamplesSize;

			if (uncoveredPositiveExamplesRemoved > 0) {

				// set the generation time for the new partial definition
				//def.setGenerationTime(System.currentTimeMillis() - miliStarttime);	//this is set by workers
				synchronized (partialDefinitions) {
					partialDefinitions.add(def);
				}

				// for used in bean (for tracing purpose)
				this.noOfUncoveredPositiveExamples -= uncoveredPositiveExamplesRemoved;

				((ParCELPosNegLP) this.learningProblem)
						.setUncoveredPositiveExamples(uncoveredPositiveExamples);

				if (logger.isTraceEnabled()) {
					logger.trace("PARTIAL definition found: "
							+ OWLAPIRenderers.toManchesterOWLSyntax(def.getDescription())
							+ "\n\t - covered positive examples ("
							+ def.getCoveredPositiveExamples().size() + "): "
							+ def.getCoveredPositiveExamples()
							+ "\n\t - uncovered positive examples left: "
							+ uncoveredPositiveExamplesSize + "/" + positiveExamples.size());
				} else if (logger.isDebugEnabled())
					logger.debug("PARTIAL definition found: "
							+ OWLAPIRenderers.toManchesterOWLSyntax(def.getDescription())
							+ "\n\t - covered positive examples ("
							+ def.getCoveredPositiveExamples().size() + "): "
							+ def.getCoveredPositiveExamples()
							+ "\n\t - uncovered positive examples left: "
							+ uncoveredPositiveExamplesSize + "/" + positiveExamples.size());
				else if (logger.isInfoEnabled()) {
					logger.info("PARTIAL definition found, uncovered positive examples left: "
							+ uncoveredPositiveExamplesSize + "/" + positiveExamples.size());
				}

			}

			// update the max accuracy and max description length
			if (def.getAccuracy() > this.maxAccuracy) {
				this.maxAccuracy = def.getAccuracy();
				this.bestDescriptionLength = new OWLClassExpressionLengthCalculator().getLength(def.getDescription());
			}

			// check if the complete definition found
			if (uncoveredPositiveExamplesSize <= uncoveredPositiveExampleAllowed) {
				this.done = true;
				// stop();
			}

		} // for each partial definition

	} // newDefinitionFound()

	/**
	 * ============================================================================================
	 * Callback method for worker when the evaluated node is not a partial definition and weak node
	 * either<br>
	 * 
	 * NOTE: there is not need for using synchronisation for this method since the thread safe data
	 * structure is currently using
	 * 
	 * @param newNodes
	 *            New nodes to add to the search tree
	 */
	@Override
	public void newRefinementDescriptions(Set<ParCELNode> newNodes) {
			searchTree.addAll(newNodes);
	}

	
	/**
	 * ============================================================================================
	 * Update the max horizontal expansion used
	 * 
	 * @param newHozExp
	 */
	public synchronized void updateMaxHorizontalExpansion(int newHozExp) {
		if (newHozExp > currentMaxHorizExp)
			currentMaxHorizExp = newHozExp;
	}
	
	/**
	 * ============================================================================================
	 * Get the currently best description in the set of partial definition
	 */
	@Override
	public OWLClassExpression getCurrentlyBestDescription() {
		if (partialDefinitions.size() > 0) {
			return partialDefinitions.iterator().next().getDescription();
		} else
			return null;
	}

	/**
	 * ============================================================================================
	 * Get all partial definition without any associated information such as accuracy, correctness,
	 * etc.
	 */
	@Override
	public List<OWLClassExpression> getCurrentlyBestDescriptions() {
		return PLOENodesToDescriptions(partialDefinitions);
	}

	/**
	 * ============================================================================================
	 * Convert a set of PLOENode into a list of descriptions
	 * 
	 * @param nodes
	 *            Set of PLOENode need to be converted
	 * 
	 * @return Set of descriptions corresponding to the given set of PLOENode
	 */
	private List<OWLClassExpression> PLOENodesToDescriptions(Set<ParCELExtraNode> nodes) {
		List<OWLClassExpression> result = new LinkedList<>();
		for (ParCELExtraNode node : nodes)
			result.add(node.getDescription());
		return result;
	}

	/**
	 * ============================================================================================
	 * The same as getCurrentBestDescription. An evaluated description is a description with its
	 * evaluated properties including accuracy and correctness
	 */
	@Override
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
		if (partialDefinitions.size() > 0) {
			ParCELNode firstNode = partialDefinitions.iterator().next();
			return new EvaluatedDescription(firstNode.getDescription(), new ParCELScore(firstNode));
		} else
			return null;
	}

	/**
	 * ============================================================================================
	 * Get all partial definitions found so far
	 */
	@Override
	public NavigableSet<? extends EvaluatedDescription<? extends Score>> getCurrentlyBestEvaluatedDescriptions() {
		return extraPLOENodesToEvaluatedDescriptions(partialDefinitions);
	}

	/**
	 * ============================================================================================
	 * Method for PLOENode - EvaluatedDescription conversion
	 * 
	 * @param partialDefs
	 *            Set of ExtraPLOENode nodes which will be converted into EvaluatedDescription
	 * 
	 * @return Set of corresponding EvaluatedDescription
	 */
	private NavigableSet<? extends EvaluatedDescription<? extends Score>> extraPLOENodesToEvaluatedDescriptions(
			Set<ParCELExtraNode> partialDefs) {
		TreeSet<EvaluatedDescription<? extends Score>> result = new TreeSet<>(
				new EvaluatedDescriptionComparator());
		for (ParCELExtraNode node : partialDefs) {
			result.add(new EvaluatedDescription(node.getDescription(), new ParCELScore(node)));
		}
		return result;
	}

	/**
	 * ============================================================================================
	 * Get the overall completeness of all partial definition found
	 * 
	 * @return Overall completeness so far
	 */
	public double getCurrentlyOveralMaxCompleteness() {
		return 1 - (uncoveredPositiveExamples.size() / (double) positiveExamples.size());
	}

	// methods related to the compactness: get compact definition, set compactor
	public SortedSet<ParCELExtraNode> getReducedPartialDefinition(ParCELReducer reducer) {
		return reducer.reduce(partialDefinitions, positiveExamples,
				uncoveredPositiveExamples.size());
	}

	public SortedSet<ParCELExtraNode> getReducedPartialDefinition() {
		return this.getReducedPartialDefinition(this.reducer);
	}

	public void setCompactor(ParCELReducer newCompactor) {
		this.reducer = newCompactor;
	}

	// ------------------------------------------
	// getters for learning results
	// ------------------------------------------

	public double getMaxAccuracy() {
		return maxAccuracy;
	}

	public int getCurrentlyBestDescriptionLength() {
		return bestDescriptionLength;
	}

	public int getSearchTreeSize() {
		return (searchTree != null ? searchTree.size() : -1);
	}

	public int getMaximumHorizontalExpansion() {
		return currentMaxHorizExp;
	}

	public Set<ParCELExtraNode> getPartialDefinitions() {
		return partialDefinitions;
	}

	/*
	 * public Set<PDLLNode> getSearchTree() { return searchTree; }
	 */

	public Collection<ParCELNode> getSearchTree() {
		return searchTree;
	}

	public ParCELHeuristic getHeuristic() {
		return heuristic;
	}

	@Override
	public long getLearningTime() {
		return miliLearningTime;
	}

	public int getNoOfReducedPartialDefinition() {
		return this.noOfCompactedPartialDefinition;
	}
	

	// =============== MBean section =====================
	/*
	public int getActiveCount() {
		return this.workerPool.getActiveCount();
	}

	public long getCompleteTaskCount() {
		return this.workerPool.getCompletedTaskCount();
	}

	public long getTaskCount() {
		return this.workerPool.getTaskCount();
	}

	public boolean isTerminiated() {
		return this.workerPool.isTerminated();
	}

	public boolean isShutdown() {
		return this.workerPool.isShutdown();
	}

	public int getUncoveredPositiveExamples() {
		return this.noOfUncoveredPositiveExamples;
	}
	*/
	
	@Override
	public long getTotalNumberOfDescriptionsGenerated() {
		return allDescriptions.size();
	}

	@Override
	public long getTotalDescriptions() {
		
		return allDescriptions.size();
	}

	@Override
	public double getCurrentlyBestAccuracy() {		
		return 	((positiveExamples.size() - uncoveredPositiveExamples.size()) + negativeExamples.size()) /
				(double)(positiveExamples.size() + negativeExamples.size());
	}

	@Override
	public int getCurrentlyMaxExpansion() {
		return this.currentMaxHorizExp;
	}



}
