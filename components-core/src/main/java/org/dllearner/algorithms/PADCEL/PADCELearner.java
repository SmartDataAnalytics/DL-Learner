package org.dllearner.algorithms.PADCEL;

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
 *	@author Jens Dietrich
 */

import java.text.DecimalFormat;
import java.util.Collection;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dllearner.algorithms.PADCEL.split.PADCELDoubleSplitterAbstract;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.Union;
import org.dllearner.refinementoperators.RefinementOperator;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.EvaluatedDescriptionComparator;
import org.springframework.beans.factory.annotation.Autowired;

@ComponentAnn(name = "PDCCEL", shortName = "PDCCEL", version = 0.1, description = "Parallel and Devide&Conquer Class Exprerssion Learner")
public class PADCELearner extends PADCELAbstract implements PADCELearnerMBean {

	// ----------------------------------
	// configuration options
	// ----------------------------------
	@ConfigOption(name = "numberOfWorkers", defaultValue = "2", description = "This option is used to set the number of workers will be created to serve the leaner. This should be = 2 * total number of cores of CPUs")
	private int numberOfWorkers = 4; // max number of workers will be created

	@ConfigOption(name = "maxExecutionTimeInSeconds", defaultValue = "0", description = "maximum execution of the algorithm in seconds")
	private int maxExecutionTimeInSeconds = 0;

	@ConfigOption(name = "noisePercentage", defaultValue = "0.0", description = "The percentage of noise within the examples")
	private double noisePercentage = 0.0;

	@ConfigOption(name = "maxNoOfSplits", defaultValue = "10", description = "Max number of splits will be applied for data properties with double range")
	private int maxNoOfSplits = 10;

	// set of concepts that will not be used in learning the target concept
	private Set<NamedClass> ignoredConcepts = null;

	private RefinementOperator refinementOperator = null; // auto-wired will be used for this
															// property

	private PADCELDoubleSplitterAbstract splitter = null;

	private static Logger logger = Logger.getLogger(PADCELearner.class);

	// heuristic used in the searching expansion (choosing node for expansion)
	private PADCELHeuristic heuristic;

	private PADCELReducer reducer = null;

	// will be used in MBean for debugging purpose
	private int noOfCompactedPartialDefinition = -1;

	private DecimalFormat df = new DecimalFormat();

	/**
	 * contains tasks submitted to thread pool
	 */
	BlockingQueue<Runnable> taskQueue;

	/**
	 * Pool of workers
	 */
	ThreadPoolExecutor workerPool;

	/**
	 * Refinement operator pool which provides refinement operators
	 */
	private PADCELRefinementOperatorPool refinementOperatorPool;

	// examples
	private Set<Individual> positiveExamples;
	private Set<Individual> negativeExamples;

	private int noOfUncoveredPositiveExamples;

	/**
	 * This may be considered as the noise allowed in learning, i.e. the maximum number of positive
	 * examples can be discard (uncovered)
	 */
	private int uncoveredPositiveExampleAllowed;

	/**
	 * Holds the uncovered positive example, this will be updated when the worker found a partial
	 * definition since the callback method "definitionFound" is synchronized", there is no need to
	 * create a thread-safe for this set
	 */
	private HashSet<Individual> uncoveredPositiveExamples;

	/**
	 * Start description and root node of the search tree
	 */
	private Description startClass; // description of the root node
	private PADCELNode startNode; 	// root of the search tree

	// ---------------------------------------------------------
	// flags to indicate the status of the application
	// ---------------------------------------------------------
	/**
	 * A reducer is stopped (reasons: done, timeout, out of memory, etc.)
	 */
	private boolean stop = false;

	/**
	 * Reducer found a complete definition?
	 */
	private boolean done = false;

	/**
	 * Reducer get the timeout
	 */
	private boolean timeout = false;

	// configuration for worker pool
	private int minNumberOfWorker = 2;
	private int maxNumberOfWorker = 2; // max number of workers will be created
	private int maxTaskQueueLength = 1000;
	private long keepAliveTime = 100; // ms

	// ------------------------------------------------
	// variables for statistical purpose
	// ------------------------------------------------
	private long miliStarttime;
	private long miliLearningTime = 0;

	// some properties for statistical purpose
	private int descriptionTested; // total number of descriptions tested (generated and calculated
									// accuracy, correctness,...)
	private int maxHorizExp = 0;
	private int bestDescriptionLength = 0;
	private double maxAccuracy = 0.0d;
	

	// number of task created (for debugging purpose only)  
	private int noOfTask = 0;

	// just for pretty representation of description
	private String baseURI = null;
	private Map<String, String> prefix = null;

	/**
	 * ============================================================================================
	 * Constructor for PDLL learning algorithm
	 * 
	 * @param learningProblem
	 *            Must be a PDLLPosNegLP
	 * @param reasoningService
	 *            A reasoner
	 */
	public PADCELearner(PADCELPosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);

		// default compactor used by this algorithm
		this.reducer = new PADCELImprovedCovegareGreedyReducer();
	}

	/**
	 * This constructor can be used by SpringDefinition to create bean object Properties of new bean
	 * may be initialised later using setters
	 */
	public PADCELearner() {
		super();
		
		this.reducer = new PADCELImprovedCovegareGreedyReducer();
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
		return "PLLearningReducer";
	}

	/**
	 * ============================================================================================
	 * Initial the learning algorithm:
	 */
	@Override
	public void init() throws ComponentInitException {

		// check the learning problem, this learning algorithm support PDLLPosNegLP only
		if (!(learningProblem instanceof PADCELPosNegLP))
			throw new ComponentInitException(learningProblem.getClass() + " is not supported by '"
					+ getName() + "' learning algorithm");

		// get the positive and negative examples from the learning problem
		positiveExamples = ((PADCELPosNegLP) learningProblem).getPositiveExamples();
		negativeExamples = ((PADCELPosNegLP) learningProblem).getNegativeExamples();

		// clone the positive examples for this set to avoid affecting the Learning Problem
		// this will be used to check the coverage of the partial definition (completeness)
		this.uncoveredPositiveExamples = new HashSet<Individual>();
		this.uncoveredPositiveExamples.addAll(this.positiveExamples);

		((PADCELPosNegLP) this.learningProblem).setUncoveredPositiveExamples(this.positiveExamples);

		// initial heuristic which will be used by reducer to sort the search tree
		// the heuristic need to get some constant from the configurator for scoring the description
		heuristic = new PADCELDefaultHeuristic();

		// this will be revise later using least common super class of all observations
		startClass = Thing.instance;

		this.uncoveredPositiveExampleAllowed = (int) Math.ceil(getNoisePercentage()
				* positiveExamples.size());

		// initial the existing uncovered positive examples
		((PADCELPosNegLP) this.learningProblem)
				.setUncoveredPositiveExamples(uncoveredPositiveExamples);

		// ----------------------------------
		// create refinement operator pool
		// ----------------------------------
		if (refinementOperator == null) {
			// -----------------------------------------
			// prepare for refinement operator creation
			// -----------------------------------------
			Set<NamedClass> usedConcepts = new TreeSet<NamedClass>(reasoner.getNamedClasses());

			// remove the ignored concepts out of the list of concepts will be used by refinement
			// operator
			if (this.ignoredConcepts != null) {
				try {
					usedConcepts.removeAll(ignoredConcepts);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			ClassHierarchy classHiearachy = reasoner.getClassHierarchy().cloneAndRestrict(
					usedConcepts);
			Map<DatatypeProperty, List<Double>> splits = null;

			// create a splitter and refinement operator pool
			// there are two options: i) using object pool, ii) using set of objects (removed from
			// this revision)
			if (this.splitter != null) {
				splitter.setReasoner(reasoner);
				splitter.setPositiveExamples(positiveExamples);
				splitter.setNegativeExamples(negativeExamples);
				splitter.init();

				splits = splitter.computeSplits();

				// i) option 1: create an object pool
				refinementOperatorPool = new PADCELRefinementOperatorPool(reasoner, classHiearachy,
						startClass, splits, numberOfWorkers + 1);
			} else { // no splitter provided
				// i) option 1: create an object pool
				refinementOperatorPool = new PADCELRefinementOperatorPool(reasoner, classHiearachy,
						startClass, numberOfWorkers + 1);
			}

			refinementOperatorPool.getFactory().setUseDisjunction(false);
			refinementOperatorPool.getFactory().setUseNegation(true);

		}

		baseURI = reasoner.getBaseURI();
		prefix = reasoner.getPrefixes();

		// logging the information (will use slf4j)
		if (logger.isInfoEnabled()) {
			logger.info("[pllearning] - Heuristic used: " + heuristic.getClass());
			logger.info("[pllearning] - Positive examples: " + positiveExamples.size()
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

		stop = false;
		done = false;
		timeout = false;

		this.noOfCompactedPartialDefinition = -1;
		this.noOfUncoveredPositiveExamples = this.positiveExamples.size();

		reset();

		// create a start node in the search tree
		allDescriptions.add(startClass); // currently, start class is always Thing

		// add the first node into the search tree
		startNode = new PADCELNode((PADCELNode) null, startClass, this.positiveExamples.size()
				/ (double) (this.positiveExamples.size() + this.negativeExamples.size()), 0);

		searchTree.add(startNode); // add the root node into the search tree

		// ---------------------------------------------
		// create worker pool
		// ---------------------------------------------
		// taskQueue = new ArrayBlockingQueue<Runnable>(maxTaskQueueLength);
		taskQueue = new LinkedBlockingQueue<Runnable>(maxTaskQueueLength);

		workerPool = new ThreadPoolExecutor(minNumberOfWorker, maxNumberOfWorker, keepAliveTime,
				TimeUnit.MILLISECONDS, taskQueue, new PADCELWorkerThreadFactory());

		/*
		 * //register a MBean for debugging purpose try { ObjectName workerPoolName = new
		 * ObjectName("nz.ac.massey.seat.abd.learner.pdllearning.PDLLMBean:type=PDLLReducerMBean");
		 * MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); if
		 * (!mbs.isRegistered(workerPoolName)) mbs.registerMBean(this, workerPoolName); } catch
		 * (Exception e) { e.printStackTrace(); }
		 */

		if (logger.isInfoEnabled())
			logger.info("Worker pool created, core pool size: " + workerPool.getCorePoolSize()
					+ ", max pool size: " + workerPool.getMaximumPoolSize());

		// start time of reducer, statistical purpose only
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

			PADCELNode nodeToProcess;

			nodeToProcess = searchTree.pollLast();

			// TODO: why this? why "blocking" concept does not help in this case?
			// remove this checking will exploit the heap memory and no definition found
			// NOTE: i) instead of using sleep, can we use semaphore here?
			// ii) if using semaphore or blocking, timeout checking may not be performed on time?
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
				} catch (RejectedExecutionException re) {
					logger.error(re);
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
							+ df.format(this.getCurrentlyOveralMaxCompleteness()*100) + ", accuracy: "
							+ df.format(acc));

					logger.info("Uncovered positive examples left "
							+ this.uncoveredPositiveExamples.size()
							+ " - "
							+ PADCELStringUtilities.replaceString(
									this.uncoveredPositiveExamples.toString(), this.baseURI,
									this.prefix));
				} else {
					logger.info("Learning is manually terminated at " + this.miliLearningTime
							+ "ms. Overall completeness: "
							+ df.format(this.getCurrentlyOveralMaxCompleteness()));
					logger.info("Uncovered positive examples left "
							+ this.uncoveredPositiveExamples.size()
							+ " - "
							+ PADCELStringUtilities.replaceString(
									this.uncoveredPositiveExamples.toString(), this.baseURI,
									this.prefix));
				}

				logger.info("Compacted partial definitions:");
				TreeSet<PADCELExtraNode> compactedDefinitions = (TreeSet<PADCELExtraNode>) this
						.compactPartialDefinition();
				this.noOfCompactedPartialDefinition = compactedDefinitions.size();
				int count = 1;
				for (PADCELExtraNode def : compactedDefinitions) {
					logger.info(count++ + ". "
							+ def.getDescription().toManchesterSyntaxString(baseURI, prefix)
							+ " (length:" + def.getDescription().getLength() + ", accuracy: "
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
				}
			}
		}

	} // start()

	
	//create a new task given a PDLLNode
	private void createNewTask(PADCELNode nodeToProcess) {
		workerPool.execute(new PADCELWorker(this, this.refinementOperatorPool,
				(PADCELPosNegLP) learningProblem, nodeToProcess, "PDLLTask-" + (noOfTask++)));
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
	 * @param partialDefinition
	 *            New partial definition
	 */
	public void definitionsFound(Set<PADCELExtraNode> definitions) {

		for (PADCELExtraNode def : definitions) {
			// NOTE: in the previous version, this node will be added back into the search tree
			// it is not necessary since in DLLearn, a definition may be revised to get a better one
			// but
			// in this approach, we do not refine partial definition.

			// remove uncovered positive examples by the positive examples covered by the new
			// partial definition
			int uncoveredPositiveExamplesRemoved;
			int uncoveredPositiveExamplesSize;
			synchronized (uncoveredPositiveExamples) {
				uncoveredPositiveExamplesRemoved = this.uncoveredPositiveExamples.size();
				this.uncoveredPositiveExamples.removeAll(def.getCoveredPositiveExamples());
				uncoveredPositiveExamplesSize = this.uncoveredPositiveExamples.size();
			}

			uncoveredPositiveExamplesRemoved -= uncoveredPositiveExamplesSize;

			if (uncoveredPositiveExamplesRemoved > 0) {

				// set the generation time for the new partial definition
				def.setGenerationTime(System.currentTimeMillis() - miliStarttime);
				synchronized (partialDefinitions) {
					partialDefinitions.add(def);
				}

				// for used in bean (for tracing purpose)
				this.noOfUncoveredPositiveExamples -= uncoveredPositiveExamplesRemoved;

				((PADCELPosNegLP) this.learningProblem)
						.setUncoveredPositiveExamples(uncoveredPositiveExamples);

				if (logger.isTraceEnabled()) {
					logger.trace("PARTIAL definition found: "
							+ def.getDescription().toManchesterSyntaxString(baseURI, prefix)
							+ "\n\t - covered positive examples ("
							+ def.getCoveredPositiveExamples().size() + "): "
							+ def.getCoveredPositiveExamples()
							+ "\n\t - uncovered positive examples left: "
							+ uncoveredPositiveExamplesSize + "/" + positiveExamples.size());
				} else if (logger.isDebugEnabled())
					logger.debug("PARTIAL definition found: "
							+ def.getDescription().toManchesterSyntaxString(baseURI, prefix)
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
				this.bestDescriptionLength = def.getDescription().getLength();
			}

			// check if the complete definition found
			if (uncoveredPositiveExamplesSize <= uncoveredPositiveExampleAllowed) {
				this.done = true;
				// stop();
			}

		} // for each partial definition

	} // definitionFound()

	/**
	 * ============================================================================================
	 * Callback method for worker when the evaluated node is not a partial definition and weak node
	 * either<br>
	 * 
	 * NOTE: there is not need for using synchronisation for this method since the thread safe data
	 * structure is currently using
	 * 
	 * @param newNode
	 *            New node to add to the search tree
	 */
	public void newRefinementDescriptions(Set<PADCELNode> newNodes) {
		searchTree.addAll(newNodes);
	}

	
	/**
	 * Add a description into search tree. No synchronisation is needed since safe-thread is using
	 * 
	 * @param des
	 *            Description to be added
	 * 
	 * @return True is the description can be added (has not been in the search tree/all
	 *         descriptions set
	 */
	public boolean addDescription(Description des) {
		return this.allDescriptions.add(des);
	}

	/**
	 * ============================================================================================
	 * Update the max horizontal expansion used
	 * 
	 * @param newHozExp
	 */
	public synchronized void updateMaxHorizontalExpansion(int newHozExp) {
		if (newHozExp > maxHorizExp)
			maxHorizExp = newHozExp;
	}

	/**
	 * ============================================================================================
	 * Reset all necessary properties for a new learning 1. Create new search tree 2. Create an
	 * empty description set, which hold all generated description (to avoid redundancy) 3. Create
	 * an empty
	 */
	private void reset() {
		searchTree = new ConcurrentSkipListSet<PADCELNode>(heuristic);

		// allDescriptions = new TreeSet<Description>(new ConceptComparator());
		allDescriptions = new ConcurrentSkipListSet<Description>(new ConceptComparator());

		partialDefinitions = new TreeSet<PADCELExtraNode>(new PADCELCorrectnessComparator());

		descriptionTested = 0;
		maxAccuracy = 0;
	}

	/**
	 * ============================================================================================
	 * Check if the learner can be terminated
	 * 
	 * @return True if termination condition is true (manual stop inquiry, complete definition
	 *         found, or timeout), false otherwise
	 */
	private boolean isTerminateCriteriaSatisfied() {
		return stop || done || timeout;// ||
		// (Runtime.getRuntime().totalMemory() >= this.maxHeapSize
		// && Runtime.getRuntime().freeMemory() < this.outOfMemory);
	}

	/**
	 * ============================================================================================
	 * Set heuristic will be used
	 * 
	 * @param newHeuristic
	 */
	public void setHeuristic(PADCELHeuristic newHeuristic) {
		this.heuristic = newHeuristic;

		if (logger.isInfoEnabled())
			logger.info("Changing heuristic to " + newHeuristic.getClass().getName());
	}

	/**
	 * ============================================================================================
	 * Stop the learning algorithm: Stop the workers and set the "stop" flag to true
	 */
	@Override
	public void stop() {

		if (!stop) {
			stop = true;
			workerPool.shutdownNow();
			
			//wait until all workers are terminated
			try {
				workerPool.awaitTermination(0, TimeUnit.SECONDS);
			}
			catch (InterruptedException ie) {
				logger.error(ie);
			}
		}
	}

	/**
	 * ============================================================================================
	 * Get the currently best description in the set of partial definition
	 */
	@Override
	public Description getCurrentlyBestDescription() {
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
	public List<Description> getCurrentlyBestDescriptions() {
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
	private List<Description> PLOENodesToDescriptions(Set<PADCELExtraNode> nodes) {
		List<Description> result = new LinkedList<Description>();
		for (PADCELExtraNode node : nodes)
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
			PADCELNode firstNode = partialDefinitions.iterator().next();
			return new EvaluatedDescription(firstNode.getDescription(), new PADCELScore(firstNode));
		} else
			return null;
	}

	/**
	 * ============================================================================================
	 * Get all partial definitions found so far
	 */
	@Override
	public TreeSet<? extends EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions() {
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
	private TreeSet<? extends EvaluatedDescription> extraPLOENodesToEvaluatedDescriptions(
			Set<PADCELExtraNode> partialDefs) {
		TreeSet<EvaluatedDescription> result = new TreeSet<EvaluatedDescription>(
				new EvaluatedDescriptionComparator());
		for (PADCELExtraNode node : partialDefs) {
			result.add(new EvaluatedDescription(node.getDescription(), new PADCELScore(node)));
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

	/**
	 * ============================================================================================
	 * =============<br>
	 * Get the list of learning problem supported by this learning algorithm
	 * 
	 * @return List of supported learning problem
	 */
	public static Collection<Class<? extends AbstractLearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends AbstractLearningProblem>> problems = new LinkedList<Class<? extends AbstractLearningProblem>>();
		problems.add(PADCELPosNegLP.class);
		return problems;
	}

	// methods related to the compactness: get compact definition, set compactor
	public SortedSet<PADCELExtraNode> compactPartialDefinition(PADCELReducer reducer) {
		return reducer.compact(partialDefinitions, positiveExamples,
				uncoveredPositiveExamples.size());
	}

	public Description getUnionCurrenlyBestDescription() {
		List<Description> compactedDescriptions = new LinkedList<Description>();

		SortedSet<PADCELExtraNode> compactedPartialdefinition = this.compactPartialDefinition();

		for (PADCELExtraNode def : compactedPartialdefinition)
			compactedDescriptions.add(def.getDescription());

		return new Union(compactedDescriptions);
	}

	public SortedSet<PADCELExtraNode> compactPartialDefinition() {
		return this.compactPartialDefinition(this.reducer);
	}

	public void setCompactor(PADCELReducer newCompactor) {
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

	@Override
	public boolean isRunning() {
		return !stop && !done && !timeout;
	}

	public int getClassExpressionTests() {
		return descriptionTested;
	}

	public int getSearchTreeSize() {
		return (searchTree != null ? searchTree.size() : -1);
	}

	public int getMaximumHorizontalExpansion() {
		return maxHorizExp;
	}

	public Set<PADCELExtraNode> getPartialDefinitions() {
		return partialDefinitions;
	}

	/*
	 * public Set<PDLLNode> getSearchTree() { return searchTree; }
	 */

	public Collection<PADCELNode> getSearchTree() {
		return searchTree;
	}

	public PADCELHeuristic getHeuristic() {
		return heuristic;
	}

	public String getBaseURI() {
		return baseURI;
	}

	public Map<String, String> getPrefix() {
		return prefix;
	}

	public boolean isTimeout() {
		return timeout;
	}

	public boolean isDone() {
		return done;
	}

	public long getLearningTime() {
		return miliLearningTime;
	}

	// ------------------------------------------------
	// setters and getters for configuration options
	// ------------------------------------------------

	public void setNumberOfWorkers(int numberOfWorkers) {
		this.numberOfWorkers = numberOfWorkers;
	}

	public int getNumberOfWorkers() {
		return numberOfWorkers;
	}

	public void setMaxExecutionTimeInSeconds(int maxExecutionTime) {
		this.maxExecutionTimeInSeconds = maxExecutionTime;
	}

	public int getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	public void setNoisePercentage(double noise) {
		this.noisePercentage = noise;
	}

	public double getNoisePercentage() {
		return this.noisePercentage;
	}

	@Autowired(required = false)
	public void setRefinementOperator(RefinementOperator refinementOp) {
		this.refinementOperator = refinementOp;
	}

	public RefinementOperator getRefinementOperator() {
		return this.refinementOperator;
	}

	@Autowired(required = false)
	public void setSplitter(PADCELDoubleSplitterAbstract splitter) {
		this.splitter = splitter;
	}

	public int getMaxNoOfSplits() {
		return maxNoOfSplits;
	}

	public void setMaxNoOfSplits(int maxNoOfSplits) {
		this.maxNoOfSplits = maxNoOfSplits;
	}

	public Set<NamedClass> getIgnoredConcepts() {
		return ignoredConcepts;
	}

	public void setIgnoredConcepts(Set<NamedClass> ignoredConcepts) {
		this.ignoredConcepts = ignoredConcepts;
	}

	public int getNoOfCompactedPartialDefinition() {
		return this.noOfCompactedPartialDefinition;
	}

	// =============== MBean section =====================
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

}
