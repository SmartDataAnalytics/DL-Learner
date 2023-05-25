package org.dllearner.algorithms.parcel;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.algorithms.parcel.reducer.ParCELImprovedCoverageGreedyReducer;
import org.dllearner.algorithms.parcel.reducer.ParCELReducer;
import org.dllearner.algorithms.parcel.split.ParCELDoubleSplitterAbstract;
import org.dllearner.core.*;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.OWLObjectUnionOfImplExt;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.refinementoperators.*;
import org.dllearner.utilities.owl.EvaluatedDescriptionComparator;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
	
	@ConfigOption(defaultValue = "true", description = "Use negation or not")
	protected boolean useNegation = true;

	@ConfigOption(defaultValue = "false", description = "Use data restriction or not")
	protected boolean useHasData = false;

	@ConfigOption(defaultValue = "true", description = "Use cardinality restrictions or not")
	protected boolean useCardinalityRestrictions = true;

	@ConfigOption(defaultValue = "5", description = "Cardinality limit")
	protected int cardinalityLimit = 5;

	@ConfigOption(description="support of disjunction (owl:unionOf) within a qualified number restriction or a universal quantification", defaultValue="false")
	protected boolean useRestrictedDisjunction = false;

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
	protected Set<OWLIndividual> positiveTestExamples;
	protected Set<OWLIndividual> negativeTestExamples;

	/**
	 * Refinement operator pool which provides refinement operators
	 */
	protected ParCELRefinementOperatorPool refinementOperatorPool;

	@ConfigOption(description = "The refinement operator to use (currently only rho is supported)")
	protected RefinementOperator operator;

	/**
	 * contains tasks submitted to thread pool
	 */
	protected BlockingQueue<Runnable> taskQueue;


	// just for pretty representation of description
	protected String baseURI;
	protected Map<String, String> prefix;

	protected final DecimalFormat df = new DecimalFormat();

	/**
	 * This may be considered as the noise allowed in learning, i.e. the maximum number of positive
	 * examples can be discard (uncovered)
	 */
	protected int uncoveredPositiveExampleAllowed = 0;

	/**
	 * Holds the uncovered positive example, this will be updated when the worker found a partial
	 * definition since the callback method "definitionFound" is synchronized", there is no need to
	 * create a thread-safe for this set
	 */
	protected Set<OWLIndividual> uncoveredPositiveExamples;

    /**
     * 	Holds the covered negative examples, this will be updated when the worker found a partial definition
     *	since the callback method "partialDefinitionFound" is synchronized,
     * 	there is no need to create a thread-safe for this set
     */
    protected Set<OWLIndividual> coveredNegativeExamples;

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

	// ------------------------------------------------
	// variables for statistical purpose
	// ------------------------------------------------
	protected long miliStarttime = Long.MIN_VALUE;
	protected long miliLearningTime = Long.MIN_VALUE;

	// some properties for statistical purpose
	protected int currentMaxHorizExp = 0;
	protected int bestDescriptionLength = 0;
	protected double maxAccuracy = 0.0d;

	// will be used in MBean for debugging purpose
	protected int noOfCompactedPartialDefinition;
	protected int noOfUncoveredPositiveExamples;

	// number of task created (for debugging purpose only)
	protected int noOfTask = 0;

	protected long trainingTime = 0;

	public ParCELAbstract() {
		super();
		this.reducer = new ParCELImprovedCoverageGreedyReducer();
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

		// default compactor used by this algorithm
		this.reducer = new ParCELImprovedCoverageGreedyReducer();
		//this.reducer = new ParCELPredScoreReducer();
	}

	protected void initOperatorIfAny() {
		if (operator == null) {
			return;
		}

		if (operator instanceof CustomHierarchyRefinementOperator) {
			ClassHierarchy classHierarchy = initClassHierarchy();
			ObjectPropertyHierarchy objectPropertyHierarchy = initObjectPropertyHierarchy();
			DatatypePropertyHierarchy datatypePropertyHierarchy = initDataPropertyHierarchy();

			((CustomHierarchyRefinementOperator) operator).setClassHierarchy(classHierarchy);
			((CustomHierarchyRefinementOperator) operator).setObjectPropertyHierarchy(objectPropertyHierarchy);
			((CustomHierarchyRefinementOperator) operator).setDataPropertyHierarchy(datatypePropertyHierarchy);
		}

		if (operator instanceof RhoDRDown) {
			((RhoDRDown) operator).setUseDisjunction(false);
			((RhoDRDown) operator).setUseRestrictedDisjunction(useRestrictedDisjunction);
		}
	}

	protected void initSearchTree() {
		// TODO: only ParCELPosNegLP supported

		ParCELNode.enableCompactCoverageRepresentation(learningProblem);

		// create a start node in the search tree
		allDescriptions.add(startClass);

		ParCELEvaluationResult accAndCorr = getAccuracyAndCorrectness(null, startClass);

		ParCELNode startNode = new ParCELNode(
			null, startClass,
			accAndCorr.accuracy, accAndCorr.correctness, accAndCorr.completeness
		);

		startNode.setCoveredPositiveExamples(accAndCorr.coveredPositiveExamples);
		startNode.setCoveredNegativeExamples(accAndCorr.coveredNegativeExamples);

		searchTree.add(startNode);
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

                synchronized (coveredNegativeExamples) {
                    coveredNegativeExamples.addAll(def.getCoveredNegativeExamples());
                }

				// for used in bean (for tracing purpose)
				this.noOfUncoveredPositiveExamples -= uncoveredPositiveExamplesRemoved;

				if (logger.isTraceEnabled() || logger.isDebugEnabled()) {
					logger.trace("PARTIAL definition found: "
										 + OWLAPIRenderers.toManchesterOWLSyntax(def.getDescription())
										 + "\n\t - covered positive examples ("
										 + def.getNumberOfCoveredPositiveExamples() + "): "
										 + def.getCoveredPositiveExamples()
										 + "\n\t - uncovered positive examples left: "
										 + uncoveredPositiveExamplesSize + "/" + positiveExamples.size());
				} else if (logger.isInfoEnabled()) {
					logger.info("PARTIAL definition found. Uncovered positive examples left: "
										+ uncoveredPositiveExamplesSize + "/" + positiveExamples.size()
										+ "\n" + OWLAPIRenderers.toManchesterOWLSyntax(def.getDescription()));
					double actualTrainingTime = getCurrentCpuMillis() / 1000.0;

					OWLClassExpression bestDescription = getUnionCurrentlyBestDescription();
					double acc = computeAccuracy(bestDescription);
					double testAcc = computeTestAccuracy(bestDescription);

					logger.info("Training time: " + actualTrainingTime + "s Accuracy: " + acc + " Test accuracy: " + testAcc);

					recordBestConceptTimeAndAccuracy(actualTrainingTime, acc, testAcc);
				}

			}

			// update the max accuracy and max description length
			if (def.getAccuracy() > this.maxAccuracy) {
				this.maxAccuracy = def.getAccuracy();
				this.bestDescriptionLength = OWLClassExpressionUtils.getLength(def.getDescription());
			}

			// check if the complete definition found
			if (uncoveredPositiveExamplesSize <= uncoveredPositiveExampleAllowed) {
				this.done = true;
				// stop();
			}
		}
	}

	protected double computeAccuracy(OWLClassExpression description) {
		if (learningProblem instanceof ParCELPosNegLP) {
			return ((ParCELPosNegLP) learningProblem).getAccuracy(description);
		}

		return 0.0;
	}


	@Override
	protected double computeTestAccuracy(OWLClassExpression description) {
		if (learningProblem instanceof ParCELPosNegLP) {
			return ((ParCELPosNegLP) learningProblem).getTestAccuracy(description);
		}

		return 0.0;
	}

	protected void createRefinementOperatorPool() throws ComponentInitException {
		if (operator == null || !(operator instanceof RhoDRDown)) {
			// -----------------------------------------
			// prepare for refinement operator creation
			// -----------------------------------------
			Set<OWLClass> usedConcepts = new TreeSet<>(reasoner.getClasses());

			// remove the ignored concepts out of the list of concepts will be used by refinement
			// operator
			if (this.ignoredConcepts != null) {
				usedConcepts.removeAll(ignoredConcepts);
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

			refinementOperatorPool.getFactory().setUseNegation(useNegation);
			refinementOperatorPool.getFactory().setUseHasValue(useHasValue);
			refinementOperatorPool.getFactory().setUseHasData(useHasData);
			refinementOperatorPool.getFactory().setCardinalityLimit(cardinalityLimit);
			refinementOperatorPool.getFactory().setUseRestrictedDisjunction(useRestrictedDisjunction);
			refinementOperatorPool.getFactory().setUseCardinalityRestrictions(useCardinalityRestrictions);
		} else {
			ParCELRefinementOperatorFactory opFactory;

			// create a splitter and refinement operator pool
			// there are two options: i) using object pool, ii) using set of objects (removed from
			// this revision)
			if (this.splitter != null) {
				splitter.setReasoner(reasoner);
				splitter.setPositiveExamples(positiveExamples);
				splitter.setNegativeExamples(negativeExamples);
				splitter.init();

				Map<OWLDataProperty, List<Double>> splits = splitter.computeSplits();

				opFactory = new ParCELRefinementOperatorFactory((RhoDRDown) operator, splits);
			} else { // no splitter provided create an object pool
				opFactory = new ParCELRefinementOperatorFactory((RhoDRDown) operator);
			}

			refinementOperatorPool = new ParCELRefinementOperatorPool(opFactory);
			refinementOperatorPool.setMaxIdle(numberOfWorkers + 1);
		}
	}

	protected void createWorkerPool() {
		taskQueue = new LinkedBlockingQueue<>(maxTaskQueueLength);

		workerPool = new ThreadPoolExecutor(minNumberOfWorker, maxNumberOfWorker, keepAliveTime,
											TimeUnit.MILLISECONDS, taskQueue, new ParCELWorkerThreadFactory());

		if (logger.isInfoEnabled())
			logger.info("Worker pool created, core pool size: " + workerPool.getCorePoolSize() +
								", max pool size: " + workerPool.getMaximumPoolSize());
	}

	public ParCELEvaluationResult getAccuracyAndCorrectness(OENode parent, OWLClassExpression refinement) {
		if (parent == null) {
			return getAccuracyAndCorrectnessRoot(refinement);
		}

		if (refinementOperatorPool.getFactory().getOperatorPrototype() instanceof DownwardRefinementOperator) {
			return getAccuracyAndCorrectnessDownward(parent, refinement);
		}

		return getAccuracyAndCorrectnessUpward(parent, refinement);
	}

	protected ParCELEvaluationResult getAccuracyAndCorrectnessRoot(OWLClassExpression refinement) {
		// TODO: only ParCELPosNegLP supported

		ParCELPosNegLP posNegLP = (ParCELPosNegLP) learningProblem;

		Set<OWLIndividual> potentiallyCoveredPositives = posNegLP.getPositiveExamples();
		Set<OWLIndividual> potentiallyCoveredNegatives = posNegLP.getNegativeExamples();

		return posNegLP.getAccuracyAndCorrectness5(
                refinement, potentiallyCoveredPositives, potentiallyCoveredNegatives
        );
	}

	protected ParCELEvaluationResult getAccuracyAndCorrectnessDownward(OENode parent, OWLClassExpression refinement) {
		// TODO: only ParCELPosNegLP supported

		Set<OWLIndividual> potentiallyCoveredPositives = parent.getCoveredPositiveExamples();
		Set<OWLIndividual> potentiallyCoveredNegatives = parent.getCoveredNegativeExamples();

		return ((ParCELPosNegLP) learningProblem).getAccuracyAndCorrectness5(
                refinement, potentiallyCoveredPositives, potentiallyCoveredNegatives
        );
	}

	protected ParCELEvaluationResult getAccuracyAndCorrectnessUpward(OENode parent, OWLClassExpression refinement) {
		// TODO: only ParCELPosNegLP supported

		ParCELPosNegLP posNegLP = (ParCELPosNegLP) learningProblem;

		Set<OWLIndividual> coveredPositives = getCoveredPositiveExamplesUpward(parent, refinement);
		Set<OWLIndividual> coveredNegatives = getCoveredNegativeExamplesUpward(parent, refinement);

		return posNegLP.getAccuracyAndCorrectness4(coveredPositives, coveredNegatives);
	}

    protected Set<OWLIndividual> getCoveredPositiveExamplesUpward(OENode parent, OWLClassExpression refinement) {
        // TODO: only ParCELPosNegLP supported

        ParCELPosNegLP posNegLP = (ParCELPosNegLP) learningProblem;

        Set<OWLIndividual> uncoveredPositives = new TreeSet<>(posNegLP.getPositiveExamples());
        uncoveredPositives.removeAll(parent.getCoveredPositiveExamples());

        Set<OWLIndividual> coveredPositives = reasoner.hasType(refinement, uncoveredPositives);
        coveredPositives.addAll(parent.getCoveredPositiveExamples());

        return coveredPositives;
    }

    protected Set<OWLIndividual> getCoveredNegativeExamplesUpward(OENode parent, OWLClassExpression refinement) {
        // TODO: only ParCELPosNegLP supported

        ParCELPosNegLP posNegLP = (ParCELPosNegLP) learningProblem;

        Set<OWLIndividual> uncoveredNegatives = new TreeSet<>(posNegLP.getNegativeExamples());
        uncoveredNegatives.removeAll(parent.getCoveredNegativeExamples());

        Set<OWLIndividual> coveredNegatives = reasoner.hasType(refinement, uncoveredNegatives);
        coveredNegatives.addAll(parent.getCoveredNegativeExamples());

        return coveredNegatives;
    }

	/**
	 * Get the union of all the best (reduced) partial definitions
	 *
	 * @return An union of all reduced partial definitions
	 */
	public OWLClassExpression getUnionCurrentlyBestDescription() {
		List<OWLClassExpression> compactedDescriptions = getReducedPartialDefinition().stream()
				.map(OENode::getDescription)
				.collect(Collectors.toList());

		return new OWLObjectUnionOfImplExt(compactedDescriptions);
	}


	/**
	 * Get the union of all the best (reduced) partial definitions using a given reducer
	 *
	 * @return An union of all reduced partial definitions
	 */
	public OWLClassExpression getUnionCurrentlyBestDescription(ParCELReducer reducer) {
		List<OWLClassExpression> compactedDescriptions = getReducedPartialDefinition(reducer).stream()
				.map(OENode::getDescription)
				.collect(Collectors.toList());

		return new OWLObjectUnionOfImplExt(compactedDescriptions);
	}


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
	public int getNoOfReducedPartialDefinition() {
		return noOfCompactedPartialDefinition;
	}

		
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
	public void newRefinementDescriptions(Set<ParCELNode> newNodes) {
		searchTree.addAll(newNodes);
	}
	
	
	/*
	 * 
	 * Get the learning time in milisecond. Learning time does not include the reduction time
	 */
	public long getLearningTime() {
		return miliLearningTime;
	}
	
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

	public boolean getUseHasData() {
		return this.useHasData;
	}

	public void setUseNegation(boolean useNegation) {
		this.useNegation = useNegation;
	}

	public boolean getUseNegation() {
		return this.useNegation;
	}
	
	public void setUseHasData(boolean useHasData) {
		this.useHasData = useHasData;
	}

	public void setUseCardinalityRestrictions(boolean useCardinalityRestrictions) {
		this.useCardinalityRestrictions = useCardinalityRestrictions;
	}

	public boolean getUseCardinalityRestrictions() {
		return this.useCardinalityRestrictions;
	}

	public void setCardinalityLimit(int cardinalityLimit) {
		this.cardinalityLimit = cardinalityLimit;
	}

	public int getCardinalityLimit() {
		return this.cardinalityLimit;
	}

	public boolean isUseRestrictedDisjunction() {
		return useRestrictedDisjunction;
	}

	public void setUseRestrictedDisjunction(boolean useRestrictedDisjunction) {
		this.useRestrictedDisjunction = useRestrictedDisjunction;
	}

	public void setOperator(RefinementOperator refinementOp) {
		this.operator = refinementOp;
	}

	public RefinementOperator getOperator() {
		return this.operator;
	}

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
		timeout = (this.maxExecutionTimeInSeconds > 0 && (getCurrentCpuMillis()) > this.maxExecutionTimeInSeconds * 1000);
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
		return stop || done || isTimeout();
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

	/**
	 * ============================================================================================
	 * Get the currently best description in the set of partial definition
	 */
	@Override
	public OWLClassExpression getCurrentlyBestDescription() {
		if (!partialDefinitions.isEmpty()) {
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
	public EvaluatedDescription<? extends Score> getCurrentlyBestEvaluatedDescription() {
		if (!partialDefinitions.isEmpty()) {
			ParCELNode firstNode = partialDefinitions.first();
			return new EvaluatedDescription<>(firstNode.getDescription(), new ParCELScore(firstNode));
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
			result.add(new EvaluatedDescription<>(node.getDescription(), new ParCELScore(node)));
		}
		return result;
	}
	/**
	 * Get all unreduced partial definitions
	 *
	 * @return unreduced partial definitions
	 */
	public Set<ParCELExtraNode> getPartialDefinitions() {
		return partialDefinitions;
	}


	public Collection<ParCELNode> getSearchTree() {
		return searchTree;
	}

	public ParCELHeuristic getHeuristic() {
		return heuristic;
	}

	public int getSearchTreeSize() {
		return (searchTree != null ? searchTree.size() : -1);
	}

	public long getMiliStarttime() {
		return this.miliStarttime;
	}

	public long getMiliLearningTime() {
		return miliLearningTime;
	}

	public double getMaxAccuracy() {
		return maxAccuracy;
	}

	public int getCurrentlyBestDescriptionLength() {
		return bestDescriptionLength;
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

	protected void printSearchTree(ParCELExtraNode node) {
		List<OENode> processingNodes = new LinkedList<>();

		processingNodes.add(node);

		processingNodes.addAll(node.getCompositeNodes());

		for (OENode n : processingNodes) {
			OENode parent = n.getParent();
			while (parent != null) {
				logger.debug("  <-- " + OWLAPIRenderers.toManchesterOWLSyntax(parent.getDescription()));
				//" [acc:" +  df.format(parent.getAccuracy()) +
				//", correctness:" + df.format(parent.getCorrectness()) + ", completeness:" + df.format(parent.getCompleteness()) +
				//", score:" + df.format(this.heuristic.getScore(parent)) + "]");

				//print out the children nodes
				Collection<OENode> children = parent.getChildren();
				for (OENode child : children) {
					OENode tmp = child;
					logger.debug("    --> " + OWLAPIRenderers.toManchesterOWLSyntax(tmp.getDescription()));
					//" [acc:" +  df.format(tmp.getAccuracy()) +
					//", correctness:" + df.format(tmp.getCorrectness()) + ", completeness:" + df.format(tmp.getCompleteness()) +
					//", score:" + df.format(this.heuristic.getScore(tmp)) + "]");
				}
				parent = parent.getParent();
			}	//while parent is not null

			logger.debug("===============");

		}
	}
}
