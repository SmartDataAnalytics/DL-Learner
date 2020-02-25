package org.dllearner.algorithms.parcel;

import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.parcel.split.ParCELDoubleSplitterAbstract;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.OWLObjectUnionOfImplExt;
import org.dllearner.refinementoperators.RefinementOperator;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class for all ParCEL algorithms family
 * 
 * @author An C. Tran
 * 
 */
public abstract class ParCELAbstract extends AbstractCELA implements ParCELearnerMBean {

	protected static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

	// ----------------------------------
	// configuration options 
	// ----------------------------------
	@ConfigOption(defaultValue = "4", description = "Number of workers will be created to serve the learner")
	protected int numberOfWorkers = 4; // 

	@ConfigOption(defaultValue = "0.0", description = "The percentage of noise within the examples")
	protected double noisePercentage = 0.0;
	protected double noiseAllowed; // = this.noisePercentage/100d;

	@ConfigOption(defaultValue = "10", description = "Max number of splits will be applied for data properties with double range. This parameter is not used if a Splitter is provided")
	protected int maxNoOfSplits = 10;

	@ConfigOption(defaultValue = "0", description = "Minimal coverage that a partial definition must approach so that it can be used")
	protected double minimalCoverage = 0;		//0 means no constrain on this condition
	
	@ConfigOption(defaultValue = "false", description = "Use value restriction or not")
	protected boolean useHasValue = false;

	@ConfigOption(defaultValue = "owl:Thing",
			description = "You can specify a start class for the algorithm. To do this, you have to use Manchester OWL syntax either with full IRIs or prefixed IRIs.",
			exampleValue = "ex:Male or http://example.org/ontology/Female")
	protected OWLClassExpression startClass; // description of the root node

	protected ParCELDoubleSplitterAbstract splitter = null;
	
	
	protected int maxHorizExp = 0;
	
	//-------------------------------------------
	//common variables for the learner
	//-------------------------------------------
	
	/**
	 * Hold all generated description to avoid the duplicated descriptions (this may contains only
	 * the weak description but it may take time to check both in this data structure and the search
	 * tree to check for the duplication). Redundancy in this case may help increasing performance.
	 */
	protected ConcurrentSkipListSet<OWLClassExpression> allDescriptions = null;

	
	/**
	 * The search tree holds all evaluated descriptions that are not correct and not weak ==>
	 * candidate for partial definitions. Nodes in the search tree must be sorted so that it can
	 * help the searching more efficiently (best search rather than 'blind' breath first or depth
	 * first)<br>
	 * NOTE: node = (description + accuracy/correctness/completeness/... values)
	 */
	protected ConcurrentSkipListSet<ParCELNode> searchTree = null; 
	

	/**
	 * partial definitions (they should be sorted so that we can get the best
	 * partial definition at any time)
	 */
	protected SortedSet<ParCELExtraNode> partialDefinitions = null;


	
	/**
	 * Heuristic used in the searching expansion (choosing node for expansion)
	 */
	protected ParCELHeuristic heuristic = null;

	
	/**
	 * Reducer which will be used to reduce the partial definitions
	 */
	protected ParCELReducer reducer = null;
	
	
	/**
	 * Pool of workers
	 */
	protected ThreadPoolExecutor workerPool;

	// configuration for worker pool
	protected int minNumberOfWorker = 2;
	protected int maxNumberOfWorker = 4; 	// max number of workers will be created
	protected final int maxTaskQueueLength = 2000;
	protected final long keepAliveTime = 100; 	// ms

	//examples
	protected Set<OWLIndividual> positiveExamples;
	protected Set<OWLIndividual> negativeExamples;

	/**
	 * Refinement operator pool which provides refinement operators
	 */
	protected ParCELRefinementOperatorPool refinementOperatorPool;

	protected RefinementOperator refinementOperator;

	/**
	 * contains tasks submitted to thread pool
	 */
	protected BlockingQueue<Runnable> taskQueue;


	// just for pretty representation of description
	protected String baseURI;
	protected Map<String, String> prefix;

	protected final DecimalFormat df = new DecimalFormat();

	// ---------------------------------------------------------
	// flags to indicate the status of the application
	// ---------------------------------------------------------
	/**
	 * The learner is stopped (reasons: done, timeout, out of memory, etc.)
	 */
	protected volatile boolean stop = false;

	/**
	 * All positive examples are covered
	 */
	protected volatile boolean done = false;

	/**
	 * Learner get timeout
	 */
	protected volatile boolean timeout = false;



	//------------------------------------------
	// Common constructors and methods
	//------------------------------------------
	
	/**
	 * Default constructor
	 */
	public ParCELAbstract() {
		super();
	}

	/**
	 * 
	 * Constructor for the learning algorithm
	 * 
	 * @param learningProblem
	 *            Learning problem, must be a PDLLPosNegLP
	 * @param reasoningService
	 *            Reasoner
	 */
	public ParCELAbstract(ParCELPosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);
	}

	protected void createRefinementOperatorPool() throws ComponentInitException {
		if (refinementOperator == null) {
			// -----------------------------------------
			// prepare for refinement operator creation
			// -----------------------------------------
			Set<OWLClass> usedConcepts = new TreeSet<>(reasoner.getClasses());

			// remove the ignored concepts out of the list of concepts will be used by refinement
			// operator
			if (this.ignoredConcepts != null) {
				try {
					usedConcepts.removeAll(ignoredConcepts);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} //set ignored concept is applicable

			ClassHierarchy classHierarchy = (ClassHierarchy) reasoner.getClassHierarchy().cloneAndRestrict(usedConcepts);

			// create a splitter and refinement operator pool
			// there are two options: i) using object pool, ii) using set of objects (removed from
			// this revision)
			if (this.splitter != null) {
				splitter.setReasoner(reasoner);
				splitter.setPositiveExamples(positiveExamples);
				splitter.setNegativeExamples(negativeExamples);
				splitter.init();

				Map<OWLDataProperty, List<Double>> splits = splitter.computeSplits();

				// i) option 1: create an object pool
				refinementOperatorPool = new ParCELRefinementOperatorPool(reasoner, classHierarchy,
						startClass, splits, numberOfWorkers + 1);
			}
			else { // no splitter provided create an object pool
				refinementOperatorPool = new ParCELRefinementOperatorPool(reasoner, classHierarchy,
						startClass, numberOfWorkers + 1, maxNoOfSplits);
			}

			refinementOperatorPool.getFactory().setUseDisjunction(false);
			refinementOperatorPool.getFactory().setUseNegation(true);
			refinementOperatorPool.getFactory().setUseHasValue(this.useHasValue);
		}
	}

	/**
	 * Get the union of all the best (reduced) partial definitions
	 *
	 * @return An union of all reduced partial definitions
	 */
	public OWLClassExpression getUnionCurrenlyBestDescription() {
		List<OWLClassExpression> compactedDescriptions = new LinkedList<>();

		SortedSet<ParCELExtraNode> compactedPartialdefinition = this.getReducedPartialDefinition();

		for (ParCELExtraNode def : compactedPartialdefinition)
			compactedDescriptions.add(def.getDescription());

		return new OWLObjectUnionOfImplExt(compactedDescriptions);
	}


	/**
	 * Get the union of all the best (reduced) partial definitions using a given reducer
	 *
	 * @return An union of all reduced partial definitions
	 */
	public OWLClassExpression getUnionCurrenlyBestDescription(ParCELReducer reducer) {
		List<OWLClassExpression> compactedDescriptions = new LinkedList<>();

		SortedSet<ParCELExtraNode> compactedPartialdefinition = this.getReducedPartialDefinition(reducer);

		for (ParCELExtraNode def : compactedPartialdefinition)
			compactedDescriptions.add(def.getDescription());

		return new OWLObjectUnionOfImplExt(compactedDescriptions);
	}


	/**
	 * Get all unreduced partial definitions
	 * 
	 * @return unreduced partial definitions
	 */
	public abstract Set<ParCELExtraNode> getPartialDefinitions();

	
	/**
	 * Get the max overall completeness so far 
	 * 
	 * @return max overall completeness
	 */
	public abstract double getCurrentlyOveralMaxCompleteness();

	
	/**
	 * Get the set of reduced partial definitions using default reducer
	 * 
	 * @return set of reduced partial definitions
	 */
	public abstract SortedSet<ParCELExtraNode> getReducedPartialDefinition();
	
	
	/**
	 * Get the number of reduced partial definitions
	 * 
	 * @return number of reduced partial definitions
	 */
	public abstract int getNoOfReducedPartialDefinition();

		
	/**
	 * Get the reduced partial definitions using the given reducer
	 * 
	 * @param reducer Reducer which will be used to reduce the partial definitions
	 * 
	 * @return reduced partial definitions
	 */
	public abstract SortedSet<ParCELExtraNode> getReducedPartialDefinition(ParCELReducer reducer);

	
	//===========================================
	// call-back methods for workers
	//===========================================
	/**
	 * Update the max horizontal expansion 
	 * 
	 * @param newHozExp New horizontal expansion
	 */
	public synchronized void updateMaxHorizontalExpansion(int newHozExp) {			
		if (maxHorizExp < newHozExp) {
			maxHorizExp = newHozExp;
		}
	}
	
	
	
	public int getMaximumHorizontalExpansion() {
		return maxHorizExp;
	}
	
	
	/**
	 * This will be called by the workers to return the new partial definitions found  
	 * 
	 * @param definitions New partial definitions
	 */
	public abstract void newPartialDefinitionsFound(Set<ParCELExtraNode> definitions);
	
	
	/**
	 * This will be called by the workers to pass the new refinements descriptions 
	 * 
	 * @param newNodes New refinement descriptions
	 */
	public abstract void newRefinementDescriptions(Set<ParCELNode> newNodes);
	
	
	/*
	 * 
	 * Get the learning time in milisecond. Learning time does not include the reduction time
	 */
	public abstract long getLearningTime();	
	
	/**
	 * Get total number of partial definitions found so far
	 * 
	 * @return Number of partial definitions
	 */
	public long getNumberOfPartialDefinitions() {
		return this.partialDefinitions.size();
	}
	
	/**
	 * Add a description into search tree. No synchronization is needed since safe-thread is using
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
	
	// -------------------------------------------------------
	// setters and getters for learner configuration options
	// -------------------------------------------------------

	//number of workers
	public void setNumberOfWorkers(int numberOfWorkers) {
		this.numberOfWorkers = numberOfWorkers;
	}

	public int getNumberOfWorkers() {
		return numberOfWorkers;
	}

	//time out (max execution time)
	public void setMaxExecutionTimeInSeconds(int maxExecutionTime) {
		this.maxExecutionTimeInSeconds = maxExecutionTime;
	}

	public long getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	//noise allowed
	public void setNoisePercentage(double noise) {
		this.noisePercentage = noise;
	}

	public double getNoisePercentage() {
		return this.noisePercentage;
	}
	
	//max no of splits
	public int getMaxNoOfSplits() {
		return maxNoOfSplits;
	}

	public void setMaxNoOfSplits(int maxNoOfSplits) {
		this.maxNoOfSplits = maxNoOfSplits;
	}

	//ignored concepts
	public Set<OWLClass> getIgnoredConcepts() {
		return ignoredConcepts;
	}

	public void setIgnoredConcepts(Set<OWLClass> ignoredConcepts) {
		this.ignoredConcepts = ignoredConcepts;
	}
	
	//minimal covered of the partial definitions
	public double getMinimalCoverage() {
		return minimalCoverage;
	}

	public void setMinimalCoverage(double minimalCoverage) {
		this.minimalCoverage = minimalCoverage;
	}
	
	public ParCELReducer getReducer() {
		return this.reducer;
	}

	public String getBaseURI() {
		return reasoner.getBaseURI();
	}
	
	public Map<String, String> getPrefix() {
		return reasoner.getPrefixes();
	}
	
	public long getTotalNumberOfDescriptionsGenerated() {
		return this.allDescriptions.size();
	}
	
	public boolean getUseHasValue() {
		return this.useHasValue;
	}
	
	public void setUseHasValue(boolean useHasValue) {
		this.useHasValue = useHasValue;
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

	public void setStartClass(OWLClassExpression startClass) {
		this.startClass = startClass;
	}

	@Override
	public int getWorkerPoolSize() {
		return this.workerPool.getQueue().size();
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

	/**=========================================================================================================<br>
	 * Set heuristic will be used
	 *
	 * @param newHeuristic
	 */
	public void setHeuristic(ParCELHeuristic newHeuristic) {
		this.heuristic = newHeuristic;

		if (logger.isInfoEnabled())
			logger.info("Changing heuristic to " + newHeuristic.getClass().getName());
	}

	public boolean isTimeout() {
		return timeout;
	}

	public boolean isDone() {
		return done;
	}

	@Override
	public boolean isRunning() {
		return !stop && !done && !timeout;
	}

	/**
	 * ============================================================================================
	 * Check if the learner can be terminated
	 *
	 * @return True if termination condition is true (manual stop inquiry, complete definition
	 *         found, or timeout), false otherwise
	 */
	protected boolean isTerminateCriteriaSatisfied() {
		return stop || done || timeout;
		//return stop || done || timeout;// ||
		// (Runtime.getRuntime().totalMemory() >= this.maxHeapSize
		// && Runtime.getRuntime().freeMemory() < this.outOfMemory);
	}

	/**
	 * Check whether the learner is terminated by the partial definitions
	 *
	 * @return True if the learner is terminated by the partial definitions, false otherwise
	 */
	public boolean terminatedByPartialDefinitions() {
		return this.done;
	}

	protected double getNoiseAllowed() {
		return noiseAllowed;
	}
}
