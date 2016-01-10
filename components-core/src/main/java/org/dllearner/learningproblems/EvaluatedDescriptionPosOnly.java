package org.dllearner.learningproblems;

import java.util.Set;

import org.dllearner.core.EvaluatedDescription;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * @author Jens Lehmann
 *
 */
public class EvaluatedDescriptionPosOnly extends EvaluatedDescription {

	private static final long serialVersionUID = 4014754537024635033L;
	private ScorePosOnly score2;
	
	public EvaluatedDescriptionPosOnly(OWLClassExpression description, ScorePosOnly score) {
		super(description, score);
		score2 = score;
	}

	@Override
	public String toString() {
		return hypothesis.toString() + "(accuracy: " + getAccuracy() + ")";
	}
	
	/**
	 * @see org.dllearner.learningproblems.ScorePosNeg#getCoveredPositives()
	 * @return Positive examples covered by the description.
	 */
	public Set<OWLIndividual> getCoveredPositives() {
		return score2.getCoveredInstances();
	}	
	
	public Set<OWLIndividual> getNotCoveredPositives() {
		return score2.getNotCoveredPositives();
	}		
	
	public Set<OWLIndividual> getAdditionalInstances() {
		return score2.getAdditionalInstances();
	}
}
