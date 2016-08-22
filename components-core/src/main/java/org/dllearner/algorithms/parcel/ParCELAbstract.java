package org.dllearner.algorithms.parcel;

import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.config.ConfigOption;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Abstract class for all ParCEL algorithms family
 * 
 * @author An C. Tran
 * 
 */
public abstract class ParCELAbstract extends AbstractCELA {

	// ----------------------------------
	// configuration options
	// ----------------------------------
	@ConfigOption(defaultValue = "2", description = "This option is used to set the number of workers will be created to serve the leaner. This should be = 2 * total number of cores of CPUs")
	protected int numberOfWorkers = 4; // max number of workers will be created

	@ConfigOption(defaultValue = "0", description = "maximum execution of the algorithm in seconds")
	protected int maxExecutionTimeInSeconds = 100;

	@ConfigOption(defaultValue = "0.0", description = "The percentage of noise within the examples")
	protected double noisePercentage = 0.0;

	@ConfigOption(defaultValue = "10", description = "Max number of splits will be applied for data properties with double range")
	protected int maxNoOfSplits = 10;

	@ConfigOption(defaultValue = "", description = "set of concepts that will be ignored in learning the target concept")
	protected Set<OWLClass> ignoredConcepts = null;

	
	/**
	 * All generated descriptions thread-safe set is used to avoid concurrently
	 * accessing
	 */
	protected ConcurrentSkipListSet<OWLClassExpression> allDescriptions = null;

	/**
	 * Search tree. It hold all evaluated descriptions that are not correct and
	 * not weak ==> candidate for partial definitions Nodes in the search tree
	 * must be ordered using heuristic so that it can help the searching more
	 * efficiently (best search rather than 'blind' breadth first of depth
	 * first) NOTE: node = (description + accuracy/correctness/completeness/...
	 * value)
	 */
	protected ConcurrentSkipListSet<ParCELNode> searchTree = null; // thread safe
																	// set

	/**
	 * partial definitions (they should be sorted so that we can get the best
	 * partial definition at any time
	 */
	protected SortedSet<ParCELExtraNode> partialDefinitions = null;

	/**
	 * Default constructor
	 */
	public ParCELAbstract() {
		super();
	}

	/**
	 * ========================================================================
	 * Constructor for the learning algorithm
	 * 
	 * @param learningProblem
	 *            Must be a PDLLPosNegLP
	 * @param reasoningService
	 *            A reasoner
	 */
	public ParCELAbstract(ParCELPosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);
	}

	public abstract OWLClassExpression getUnionCurrenlyBestDescription();

	public abstract int getNoOfCompactedPartialDefinition();

	public abstract Set<ParCELExtraNode> getPartialDefinitions();

	public abstract double getCurrentlyOveralMaxCompleteness();

	public long getNumberOfPartialDefinitions() {
		return this.partialDefinitions.size();
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


	public int getMaxNoOfSplits() {
		return maxNoOfSplits;
	}

	public void setMaxNoOfSplits(int maxNoOfSplits) {
		this.maxNoOfSplits = maxNoOfSplits;
	}

	public Set<OWLClass> getIgnoredConcepts() {
		return ignoredConcepts;
	}

	public void setIgnoredConcepts(Set<OWLClass> ignoredConcepts) {
		this.ignoredConcepts = ignoredConcepts;
	}


}
