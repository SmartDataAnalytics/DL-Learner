package org.dllearner.learningproblems;

import java.io.Serializable;
import java.util.Set;

import org.dllearner.core.Score;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * The score of a class in ontology engineering.
 * 
 * @author Jens Lehmann
 *
 */
public class ClassScore extends Score implements Serializable{

	private static final long serialVersionUID = 2003326044901308157L;
	private Set<OWLIndividual> coveredInstances;
	private Set<OWLIndividual> notCoveredInstances;

	private Set<OWLIndividual> additionalInstances;
	
	private double coverage;
	private double addition;
	private double accuracy;
	
	private boolean isConsistent;
	private boolean followsFromKB;
	
	public ClassScore(Set<OWLIndividual> coveredInstances, Set<OWLIndividual> notCoveredInstances, double coverage, Set<OWLIndividual> additionalInstances, double protusion, double accuracy) {
		this.coveredInstances = coveredInstances;
		this.notCoveredInstances = notCoveredInstances;
		this.additionalInstances = additionalInstances;
		this.coverage = coverage;
		this.addition = protusion;
		this.accuracy = accuracy;
	}	
	
	public ClassScore(Set<OWLIndividual> coveredInstances, Set<OWLIndividual> notCoveredInstances, double coverage, Set<OWLIndividual> additionalInstances, double protusion, double accuracy, boolean isConsistent, boolean followsFromKB) {
		this(coveredInstances, notCoveredInstances, coverage, additionalInstances, protusion, accuracy);
		this.isConsistent = isConsistent;
		this.followsFromKB = followsFromKB;
	}
	
	/**
	 * @return Coverage of the class description.
	 */
	public double getCoverage() {
		return coverage;
	}

	/**
	 * Let C be the considered class OWLClassExpression and A the class to learn. 
	 * The addition number is calculated as the number of instances of C which are also
	 * instances of A divided by the number of instances of C.
	 * @return Additional instances of the class description.
	 */
	public double getAddition() {
		return addition;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Score#getAccuracy()
	 */
	@Override
	public double getAccuracy() {
//		return 0.5 * (coverage + addition);
		return accuracy;
	}

	/**
	 * @return the coveredInstances
	 */
	public Set<OWLIndividual> getCoveredInstances() {
		return coveredInstances;
	}

	/**
	 * @return the notCoveredInstances
	 */
	public Set<OWLIndividual> getNotCoveredInstances() {
		return notCoveredInstances;
	}	
	
	/**
	 * @return the additionalInstances
	 */
	public Set<OWLIndividual> getAdditionalInstances() {
		return additionalInstances;
	}

	public void setConsistent(boolean isConsistent) {
		this.isConsistent = isConsistent;
	}

	public void setFollowsFromKB(boolean followsFromKB) {
		this.followsFromKB = followsFromKB;
	}

	/**
	 * @return the isConsistent
	 */
	public boolean isConsistent() {
		return isConsistent;
	}

	/**
	 * @return the followsFromKB
	 */
	public boolean followsFromKB() {
		return followsFromKB;
	}		

}
