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

import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.algorithms.parcel.reducer.ParCELReducer;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionLengthCalculator;
import org.semanticweb.owlapi.model.OWLClassExpression;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

@ComponentAnn(name = "ParCEL", shortName = "parcel", version = 0.1, description = "PARallel Class Expression Learning")
public class ParCELearner extends ParCELAbstract implements ParCELearnerMBean {

	/**
	 * ============================================================================================
	 * Constructor for PDLL learning algorithm
	 * 
	 * @param learningProblem
	 *            Must be a ParCELPosNegLP
	 * @param reasoningService
	 *            A reasoner
	 */
	public ParCELearner(ParCELPosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);
	}

	/**
	 * This constructor can be used by SpringDefinition to create bean object Properties of new bean
	 * may be initialised later using setters
	 */
	public ParCELearner() {
		super();
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
	 * Initialize the learning algorithm:
	 */
	@Override
	public void init() throws ComponentInitException {

		// check the learning problem, this learning algorithm support ParCELPosNegLP only
		if (!(learningProblem instanceof ParCELPosNegLP))
			throw new ComponentInitException(learningProblem.getClass() + " is not supported by '"
					+ getName() + "' learning algorithm. Only ParCELPosNegLP is supported.");

		// get the positive and negative examples from the learning problem
		positiveExamples = ((ParCELPosNegLP) learningProblem).getPositiveExamples();
		negativeExamples = ((ParCELPosNegLP) learningProblem).getNegativeExamples();
		positiveTestExamples = ((ParCELPosNegLP) learningProblem).getPositiveTestExamples();
		negativeTestExamples = ((ParCELPosNegLP) learningProblem).getNegativeTestExamples();

		// clone the positive examples for this set to avoid affecting the Learning Problem
		// this will be used to check the coverage of the partial definition (completeness)
		this.uncoveredPositiveExamples = new HashSet<>(this.positiveExamples);

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
		initOperatorIfAny();
		createRefinementOperatorPool();

		baseURI = reasoner.getBaseURI();
		prefix = reasoner.getPrefixes();

		// logging the information (will use slf4j)
		if (logger.isInfoEnabled()) {
			logger.info("Heuristic used: " + heuristic.getClass());
			logger.info("Training -> Positive examples: " + positiveExamples.size()
					+ ", negative examples: " + negativeExamples.size());
			logger.info("Testing -> Positive examples: " + positiveTestExamples.size()
					+ ", negative examples: " + negativeTestExamples.size());
		}

		minNumberOfWorker = maxNumberOfWorker = numberOfWorkers;

	}

	protected void reset() {
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

		trainingTime = getCurrentCpuMillis();

		//allDescriptions = new TreeSet<Description>(new ConceptComparator());
		allDescriptions = new ConcurrentSkipListSet<>();

		searchTree = new ConcurrentSkipListSet<>(heuristic.reversed());

		partialDefinitions = new TreeSet<>(new ParCELCorrectnessComparator());

		maxAccuracy = 0;
		noOfCompactedPartialDefinition = 0;
		noOfUncoveredPositiveExamples = this.positiveExamples.size();
	}

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
		reset();

		initSearchTree();

		createWorkerPool();

		// start time of the learner
		miliStarttime = System.currentTimeMillis();
		logger.info("Time " + getCurrentCpuMillis() / 1000.0 + "s");

		// ----------------------------------------------------------
		// perform the learning process until the conditions for
		// termination meets
		// ----------------------------------------------------------
		while (!isTerminateCriteriaSatisfied()) {

			ParCELNode nodeToProcess = searchTree.pollFirst();

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
				if (this.getCurrentlyOveralMaxCompleteness() == 1)
					logger.info("Learning finishes in: " + this.miliLearningTime + "ms, with: "
							+ partialDefinitions.size() + " definitions");
				else if (this.isTimeout()) {
					logger.info("Learning timeout in " + this.maxExecutionTimeInSeconds
							+ "s. Overall completeness: "
							+ df.format(this.getCurrentlyOveralMaxCompleteness()));

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

				OWLClassExpression bestDescription = getUnionCurrentlyBestDescription();
				double acc = computeAccuracy(bestDescription);
				logger.info("Accuracy: " + acc);
				
				logger.info("Total descriptions generated: " + allDescriptions.size()
						+ ", best description length: "  + getCurrentlyBestDescriptionLength()
						+ ", max expansion length: " + getMaximumHorizontalExpansion());

				logger.info("Compacted partial definitions:");
				TreeSet<ParCELExtraNode> compactedDefinitions = (TreeSet<ParCELExtraNode>) this
						.getReducedPartialDefinition();
				this.noOfCompactedPartialDefinition = compactedDefinitions.size();
				int count = 1;
				for (ParCELExtraNode def : compactedDefinitions) {
					int tpTest = learningProblem instanceof ParCELPosNegLP
						? ((ParCELPosNegLP) learningProblem).getNumberOfCoveredPositiveTestExamples(def.getDescription())
						: 0;

					logger.info(count++ + ". "
							+ OWLAPIRenderers.toManchesterOWLSyntax(def.getDescription())
							+ " (length:" + new OWLClassExpressionLengthCalculator().getLength(def.getDescription())
							+ ", accuracy: " + df.format(def.getAccuracy()) + " / " + computeTestAccuracy(def.getDescription())
							+ ", coverage: " + def.getCoveredPositiveExamples().size() + " / " + tpTest + ")");

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

				if (learningProblem instanceof ParCELPosNegLP) {
					Set<OWLClassExpression> partialDefs = getReducedPartialDefinition()
						.stream().map(OENode::getDescription).collect(Collectors.toSet());

					((ParCELPosNegLP) learningProblem).printTestEvaluation(partialDefs);
				}

				printBestConceptsTimesAndAccuracies();
			}
		}

	}

	//create a new task given a PDLLNode
	private void createNewTask(ParCELNode nodeToProcess) {
		workerPool.execute(new ParCELWorker(this, this.refinementOperatorPool,
											(ParCELPosNegLP) learningProblem, nodeToProcess, "ParCELTask-" + (noOfTask++)));
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
}
