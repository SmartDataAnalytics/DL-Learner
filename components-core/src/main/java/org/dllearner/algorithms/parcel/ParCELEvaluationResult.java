package org.dllearner.algorithms.parcel;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * This class represents the result of an evaluation result return from the ParCEL learning problem
 * (ParCELPosNegLP) <br>
 * Information included in a ParCEL evaluation includes:
 * <ol>
 * <li>- accuracy: double</li>
 * <li>- correctness: double</li>
 * <li>- completeness: double</li>
 * <li>- covered positive examples: Set&lt;Individual&gt;</li>
 * <li>- covered negative examples: Set&lt;Individual&gt;</li>
 * </ol>
 * 
 * @author An C. Tran
 * 
 */
public class ParCELEvaluationResult {

	protected double accuracy = 0.0;
	protected double correctness = 0.0;
	protected double completeness = 0.0;
	protected Set<OWLIndividual> coveredPositiveExamples = null;
	protected Set<OWLIndividual> coveredNegativeExamples = null;

	/**
	 * Default constructor, use default value for all properties
	 */
	
	public ParCELEvaluationResult() {

	}
	

	/**
	 * This use to create an evaluation result that is not for a correct definition. So, we don't
	 * need to hold the set of covered positive examples
	 * 
	 * @param accuracy
	 * @param correctness
	 */
	public ParCELEvaluationResult(double accuracy, double correctness, double completeness) {
		this.accuracy = accuracy;
		this.correctness = correctness;
		this.completeness = completeness;
	}

	/**
	 * Used to create an evaluation result for a correct definition.<br>
	 * Covered positive examples have to be kept to be used in the result compactness later on.<br>
	 * This is usually used in case of partial definition
	 * 
	 * @param accuracy
	 * @param correctness
	 * @param completeness
	 * @param coveredPositiveExamples
	 */
	public ParCELEvaluationResult(double accuracy, double correctness, double completeness,
			Set<OWLIndividual> coveredPositiveExamples) {
		this.accuracy = accuracy;
		this.correctness = correctness;
		this.completeness = completeness;
		this.coveredPositiveExamples = coveredPositiveExamples;
	}

	/**
	 * Used to create an evaluation result for a correct definition.<br>
	 * Both covered positive examples and covered negative examples will be kept to be used in the
	 * result compactness later on.
	 * 
	 * @param accuracy
	 * @param correctness
	 * @param completeness
	 * @param coveredPositiveExamples
	 * @param coveredNegativeExamples
	 */
	public ParCELEvaluationResult(double accuracy, double correctness, double completeness,
                                  Set<OWLIndividual> coveredPositiveExamples, Set<OWLIndividual> coveredNegativeExamples) {

		this.accuracy = accuracy;
		this.correctness = correctness;
		this.completeness = completeness;
		this.coveredPositiveExamples = coveredPositiveExamples;
		this.coveredNegativeExamples = coveredNegativeExamples;
	}

	// ---------------------------------------
	// Getters and setters
	// ---------------------------------------
	public double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	public double getCorrectness() {
		return correctness;
	}

	public void setCorrectness(double correctness) {
		this.correctness = correctness;
	}

	public double getCompleteness() {
		return completeness;
	}

	public void setCompleteness(double completeness) {
		this.completeness = completeness;
	}

	public Set<OWLIndividual> getCoveredPositiveExamples() {
		return coveredPositiveExamples;
	}

	public void setCoveredPositiveExamples(Set<OWLIndividual> coveredPositiveExamples) {
		this.coveredPositiveExamples = coveredPositiveExamples;
	}

	public Set<OWLIndividual> getCoveredNegativeExamples() {
		return coveredNegativeExamples;
	}

	public void setCoveredNegativeExamples(Set<OWLIndividual> coveredNegativeExamples) {
		this.coveredNegativeExamples = coveredNegativeExamples;
	}

	@Override
	public String toString() {
		return "ParCELEvaluationResult{" +
				"accuracy=" + accuracy +
				", correctness=" + correctness +
				", completeness=" + completeness +
				", coveredPositiveExamples=" + Optional.ofNullable(coveredPositiveExamples).map(Set::size).map(Object::toString).orElse("n/a") +
				", coveredNegativeExamples=" + Optional.ofNullable(coveredNegativeExamples).map(Set::size).map(Objects::toString).orElse("n/a") +
				'}';
	}
}
