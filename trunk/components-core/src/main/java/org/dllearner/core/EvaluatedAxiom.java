package org.dllearner.core;

import org.dllearner.core.owl.Axiom;

public class EvaluatedAxiom {
	
	private Axiom axiom;
	private Score score;
	
	public EvaluatedAxiom(Axiom axiom, Score score) {
		this.axiom = axiom;
		this.score = score;
	}

	public Axiom getAxiom() {
		return axiom;
	}

	public Score getScore() {
		return score;
	}
	
	@Override
	public String toString() {
		return axiom + "(" + score.getAccuracy()+ ")";
	}

	

}
