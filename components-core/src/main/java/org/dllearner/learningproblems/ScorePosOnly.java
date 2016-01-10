package org.dllearner.learningproblems;

import java.util.Set;

import org.dllearner.core.Score;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * @author Jens Lehmann
 *
 */
public class ScorePosOnly<T extends OWLEntity> extends Score {

	private static final long serialVersionUID = 2191608162129054464L;
	
	private Set<T> coveredInstances;
	private Set<T> notCoveredPositives;
	private Set<T> additionalInstances;
	
	private double coverage;
	private double addition;
	private double accuracy;	
	
	public ScorePosOnly(Set<T> coveredInstances, Set<T> notCoveredPositives, double coverage, Set<T> additionalInstances, double protusion, double accuracy) {
		this.coveredInstances = coveredInstances;
		this.notCoveredPositives = notCoveredPositives;
		this.additionalInstances = additionalInstances;
		this.coverage = coverage;
		this.addition = protusion;
		this.accuracy = accuracy;		
	}
	
	/**
	 * @return Coverage of the class description.
	 */
	public double getCoverage() {
		return coverage;
	}

	/**
	 * Let C be the considered class expression and A the class to learn. 
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
	public Set<T> getCoveredInstances() {
		return coveredInstances;
	}

	/**
	 * @return the coveredInstances
	 */
	public Set<T> getNotCoveredPositives() {
		return notCoveredPositives;
	}
	
	/**
	 * @return the additionalInstances
	 */
	public Set<T> getAdditionalInstances() {
		return additionalInstances;
	}		

}
