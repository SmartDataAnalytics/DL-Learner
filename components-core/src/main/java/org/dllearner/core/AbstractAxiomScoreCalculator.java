package org.dllearner.core;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.semanticweb.owlapi.util.OWLAxiomVisitorExAdapter;

public abstract class AbstractAxiomScoreCalculator extends OWLAxiomVisitorExAdapter<Double> implements AxiomScoreCalculator{
	
	protected QueryExecutionFactory qef;

	public AbstractAxiomScoreCalculator(QueryExecutionFactory qef) {
		super(0d);
		this.qef = qef;
	}

}
