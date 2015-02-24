package org.dllearner.core;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.learningproblems.AxiomScore;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class ProbabilityBasedAxiomScoreCalculator extends AbstractAxiomScoreCalculator{

	public ProbabilityBasedAxiomScoreCalculator(QueryExecutionFactory qef) {
		super(qef);
	}

	@Override
	public AxiomScore calculateScore(OWLAxiom axiom) {
		double score = axiom.accept(this);
		return new AxiomScore(score);
	}
	
	@Override
	public Double visit(OWLSubClassOfAxiom axiom) {
		// TODO Auto-generated method stub
		return super.visit(axiom);
	}
	

}
