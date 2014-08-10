package org.dllearner.algorithms.ParCEL;

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

import org.dllearner.algorithms.ParCEL.split.ParCELDoubleSplitterAbstract;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
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

@ComponentAnn(name = "ParCEL", shortName = "parcel", version = 0.1, description = "PARallel divide and conque Class Exprerssion Learning")
public class ParCELearner extends ParCELAbstract implements ParCELearnerMBean {


	private RefinementOperator refinementOperator = null; 

	private ParCELDoubleSplitterAbstract splitter = null;

	private static Logger logger = Logger.getLogger(ParCELearner.class);

	// heuristic used in the searching expansion (choosing node for expansion)
	private ParCELHeuristic heuristic;

	private ParCELReducer reducer = null;

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
	private ParCELRefinementOperatorPool refinementOperatorPool;

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
	 * The description and root node of the search tree
	 */
	private Description startClass; // description of the root node
	private ParCELNode startNode; 	// root of the search tree

	// ---------------------------------------------------------
	// flags to indicate the status of the application
	// ---------------------------------------------------------
	/**
	 * The learner is stopped (reasons: done, timeout, out of memory, etc.)
	 */
	private boolean stop = false;

	
	/**
	 * All positive examples are covered
	 */
	private boolean done = false;

	
	/**
	 * Learner get timeout
	 */
	private boolean timeout = false;
	
	
	// configuration for worker pool
	private int minNumberOfWorker = 2;
	private int maxNumberOfWorker = 2; 	// max number of workers will be created
	private int maxTaskQueueLength = 1000;
	private long keepAliveTime = 100; 	// ms

	// ------------------------------------------------
	// variables for statistical purpose
	// ------------------------------------------------
	private long miliStarttime;
	private long miliLearningTime = 0;

	// some properties for statistical purpose
	private int descriptionTested; // total number of descriptions tested (generated and calculated
									// accuracy, correctness,...)
	private int currentMaxHorizExp = 0;
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
	public ParCELearner(ParCELPosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);

		// default compactor used by this algorithm
		this.reducer = new ParCELImprovedCovegareGreedyReducer();
	}

	/**
	 * This constructor can be used by SpringDefinition to create bean object Properties of new bean
	 * may be initialised later using setters
	 */
	public ParCELearner() {
		super();
		
		this.reducer = new ParCELImprovedCovegareGreedyReducer();
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
		return "PADCELearner";
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
					+ getName() + "' learning algorithm");

		// get the positive and negative examples from the learning problem
		positiveExamples = ((ParCELPosNegLP) learningProblem).getPositiveExamples();
		negativeExamples = ((ParCELPosNegLP) learningProblem).getNegativeExamples();

		// clone the positive examples for this set to avoid affecting the Learning Problem
		// this will be used to check the coverage of the partial definition (completeness)
		this.uncoveredPositiveExamples = new HashSet<Individual>();
		this.uncoveredPositiveExamples.addAll(this.positiveExamples);

		((ParCELPosNegLP) this.learningProblem).setUncoveredPositiveExamples(this.positiveExamples);

		// initial heuristic which will be used by reducer to sort the search tree
		// the heuristic need to get some constant from the configurator for scoring the description
		heuristic = new ParCELDefaultHeuristic();

		// this will be revise later using least common super class of all observations
		startClass = Thing.instance;

		this.uncoveredPositiveExampleAllowed = (int) Math.ceil(getNoisePercentage()
				* positiveExamples.size());

		// initial the existing uncovered positive examples
		((ParCELPosNegLP) this.learningProblem)
				.setUncoveredPositiveExamples(uncoveredPositiveExamples);

		// ----------------------------------
		// create refinement operator pool
		// ----------------------------------
		if (refinementOperator == null) {
			// -----------------------------------------
			// prepare for refinement operator creation
			// -----------------------------------------
			Set<OWLClass> usedConcepts = new TreeSet<OWLClass>(reasoner.getNamedClasses());

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
			Map<OWLDataProperty, List<Double>> splits = null;

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
				refinementOperatorPool = new ParCELRefinementOperatorPool(reasoner, classHiearachy,
						startClass, splits, numberOfWorkers + 1);
			} else { // no splitter provided
				// i) option 1: create an object pool
				refinementOperatorPool = new ParCELRefinementOperatorPool(reasoner, classHiearachy,
						startClass, numberOfWorkers + 1);
			}

			refinementOperatorPool.getFactory().setUseDisjunction(false);
			refinementOperatorPool.getFactory().setUseNegation(true);

		}

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

		stop = false;
		done = false;
		timeout = false;

		this.noOfCompactedPartialDefinition = -1;
		this.noOfUncoveredPositiveExamples = this.positiveExamples.size();

		reset();

		// create a start node in the search tree
		allDescriptions.add(startClass); // currently, start class is always Thing

		// add the first node into the search tree
		startNode = new ParCELNode((ParCELNode) null, startClass, this.positiveExamples.size()
				/ (double) (this.positiveExamples.size() + this.negativeExamples.size()), 0, 100);

		searchTree.add(startNode); // add the root node into the search tree

		// ---------------------------------------------
		// create worker pool
		// ---------------------------------------------
		// taskQueue = new ArrayBlockingQueue<Runnable>(maxTaskQueueLength);
		taskQueue = new LinkedBlockingQueue<Runnable>(maxTaskQueueLength);

		workerPool = new ThreadPoolExecutor(minNumberOfWorker, maxNumberOfWorker, keepAliveTime,
				TimeUnit.MILLISECONDS, taskQueue, new ParCELWorkerThreadFactory());

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

			ParCELNode nodeToProcess;

			nodeToProcess = searchTree.pollLast();

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

				logger.info("Compacted partial definitions:");
				TreeSet<ParCELExtraNode> compactedDefinitions = (TreeSet<ParCELExtraNode>) this
						.compactPartialDefinition();
				this.noOfCompactedPartialDefinition = compactedDefinitions.size();
				int count = 1;
				for (ParCELExtraNode def : compactedDefinitions) {
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
	private void createNewTask(ParCELNode nodeToProcess) {
		workerPool.execute(new ParCELWorker(this, this.refinementOperatorPool,
				(ParCELPosNegLP) learningProblem, nodeToProcess, "PDLLTask-" + (noOfTask++)));
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
	public void newDefinitionsFound(Set<ParCELExtraNode> definitions) {

		for (ParCELExtraNode def : definitions) {
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

				((ParCELPosNegLP) this.learningProblem)
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

	} // newDefinitionFound()

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
	public void newRefinementDescriptions(Set<ParCELNode> newNodes) {
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
	public boolean addDescription(OWLClassExpression des) {
		return this.allDescriptions.add(des);
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
	 * Reset all necessary properties for a new learning
	 * <ol>
	 * <li>Create new search tree</li>
	 * <li>Create an empty description set, which hold all generated description (to avoid redundancy)</li>
	 * <li>Create an empty partial definition set</li>
	 * <li>Reset some other properties for the learner</li>
	 * </ol>
	 */
	private void reset() {
		searchTree = new ConcurrentSkipListSet<ParCELNode>(heuristic);

		// allDescriptions = new TreeSet<OWLClassExpression>(new ConceptComparator());
		allDescriptions = new ConcurrentSkipListSet<OWLClassExpression>(new ConceptComparator());

		partialDefinitions = new TreeSet<ParCELExtraNode>(new ParCELCorrectnessComparator());

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
		return stop || ((searchTree.size() == 0) && (done == true)) || timeout;
		//return stop || done || timeout;// ||
		// (Runtime.getRuntime().totalMemory() >= this.maxHeapSize
		// && Runtime.getRuntime().freeMemory() < this.outOfMemory);
	}

	/**
	 * ============================================================================================
	 * Set heuristic will be used
	 * 
	 * @param newHeuristic
	 */
	public void setHeuristic(ParCELHeuristic newHeuristic) {
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
				//System.out.println("-------------Waiting for worker pool----------------");
				workerPool.awaitTermination(10, TimeUnit.SECONDS);
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
		List<OWLClassExpression> result = new LinkedList<OWLClassExpression>();
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
			Set<ParCELExtraNode> partialDefs) {
		TreeSet<EvaluatedDescription> result = new TreeSet<EvaluatedDescription>(
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

	/**
	 * ============================================================================================
	 * =============<br>
	 * Get the list of learning problem supported by this learning algorithm
	 * 
	 * @return List of supported learning problem
	 */
	public static Collection<Class<? extends AbstractLearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends AbstractLearningProblem>> problems = new LinkedList<Class<? extends AbstractLearningProblem>>();
		problems.add(ParCELPosNegLP.class);
		return problems;
	}

	// methods related to the compactness: get compact definition, set compactor
	public SortedSet<ParCELExtraNode> compactPartialDefinition(ParCELReducer reducer) {
		return reducer.compact(partialDefinitions, positiveExamples,
				uncoveredPositiveExamples.size());
	}

	public Description getUnionCurrenlyBestDescription() {
		List<OWLClassExpression> compactedDescriptions = new LinkedList<OWLClassExpression>();

		SortedSet<ParCELExtraNode> compactedPartialdefinition = this.compactPartialDefinition();

		for (ParCELExtraNode def : compactedPartialdefinition)
			compactedDescriptions.add(def.getDescription());

		return new Union(compactedDescriptions);
	}

	public SortedSet<ParCELExtraNode> compactPartialDefinition() {
		return this.compactPartialDefinition(this.reducer);
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

	@Autowired(required = false)
	public void setRefinementOperator(RefinementOperator refinementOp) {
		this.refinementOperator = refinementOp;
	}

	public RefinementOperator getRefinementOperator() {
		return this.refinementOperator;
	}

	@Autowired(required = false)
	public void setSplitter(ParCELDoubleSplitterAbstract splitter) {
		this.splitter = splitter;
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
