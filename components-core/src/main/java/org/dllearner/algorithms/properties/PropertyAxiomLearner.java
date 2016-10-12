/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.properties;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLProperty;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

/**
 * A learning algorithm for property axioms. 
 * @author Lorenz Buehmann
 *
 */
public abstract class PropertyAxiomLearner<S extends OWLProperty, T extends OWLLogicalAxiom, V extends OWLObject> extends AbstractAxiomLearningAlgorithm<T, V, S>{
	
	protected static final ParameterizedSparqlString TRIPLES_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(*) as ?cnt) WHERE {?s ?p ?o .}");
	
	protected static final ParameterizedSparqlString DISTINCT_SUBJECTS_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?s)) as ?cnt) WHERE {?s ?p ?o .}");
	
	protected static final ParameterizedSparqlString DISTINCT_OBJECTS_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?o)) as ?cnt) WHERE {?s ?p ?o .}");
	
	protected static final ParameterizedSparqlString GET_SAMPLE_QUERY = new ParameterizedSparqlString(
			"CONSTRUCT {?s ?p ?o.} WHERE {?s ?p ?o}");
	
	protected ParameterizedSparqlString COUNT_QUERY = TRIPLES_COUNT_QUERY;

	@ConfigOption(defaultValue = "true", description = "make SPARQL OWL queries a bit more strict (currently: also test " +
			"if a class is an owl:Class in some cases)")
	protected boolean strictOWLMode = true;

	// a property domain axiom can formally be seen as a subclass axiom \exists r.\top \sqsubseteq \C
	// so we have to focus more on accuracy, which we can regulate via the parameter beta
	double beta = 3.0;

	private boolean useSimpleScore = true;
	
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#setEntityToDescribe(org.semanticweb.owlapi.model.OWLEntity)
	 */
	@Override
	public void setEntityToDescribe(S entityToDescribe) {
		super.setEntityToDescribe(entityToDescribe);
		
		posExamplesQueryTemplate.setIri("p", entityToDescribe.toStringID());
		negExamplesQueryTemplate.setIri("p", entityToDescribe.toStringID());
		
		COUNT_QUERY.setIri("p", entityToDescribe.toStringID());
		DISTINCT_SUBJECTS_COUNT_QUERY.setIri("p", entityToDescribe.toStringID());
		DISTINCT_OBJECTS_COUNT_QUERY.setIri("p", entityToDescribe.toStringID());
	}

	/**
	 * Declare the property for which axiom(s) will be computed.
	 *
	 * @param property the property
	 */
	public void setPropertyToDescribe(S property) {
		setEntityToDescribe(property);
	}
	
	/**
	 * @param strictOWLMode the strictOWLMode to set
	 */
	public void setStrictOWLMode(boolean strictOWLMode) {
		this.strictOWLMode = strictOWLMode;
	}
	
	@Override
	protected ParameterizedSparqlString getSampleQuery(){
		return GET_SAMPLE_QUERY;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		run();
	}
	
	protected int getPropertyPopularity(){
		return getCountValue(COUNT_QUERY.toString());
	}
	
	protected int getPropertyPopularity(Model model){
		return getCountValue(COUNT_QUERY.toString(), model);
	}
	
	protected int getDistinctSubjectsFrequency(){
		return getCountValue(DISTINCT_SUBJECTS_COUNT_QUERY.toString());
	}
	
	protected int getDistinctObjectsFrequency(){
		return getCountValue(DISTINCT_OBJECTS_COUNT_QUERY.toString());
	}
	
	protected int getCountValue(String query){
		ResultSet rs = executeSelectQuery(query);
		return rs.next().getLiteral("cnt").getInt();
	}
	
	/**
	 * Return the integer value of a SPARQL query that just returns a single COUNT value.
	 * It is assumed the the variable of the COUNT value is ?cnt.
	 * @param query the SPARQL query
	 * @param model the model containing the data
	 * @return the count value
	 */
	protected int getCountValue(String query, Model model){
		ResultSet rs = executeSelectQuery(query, model);
		return rs.next().getLiteral("cnt").getInt();
	}

	/**
	 * Compute the score of the axiom:
	 *
	 * @param cntA |A|
	 * @param cntB |B|
	 * @param cntAB |A AND B|
	 * @return
	 */
	protected AxiomScore computeScore(int cntA, int cntB, int cntAB) {
		// precision (A AND B)/B
		double precision = Heuristics.getConfidenceInterval95WaldAverage(cntB, cntAB);

		// in the simplest case, the precision is our score
		double score = precision;

		// if enabled consider also recall and use F-score
		if(!useSimpleScore ) {
			// recall (A AND B)/A
			double recall = Heuristics.getConfidenceInterval95WaldAverage(cntA, cntAB);

			// F score
			score = Heuristics.getFScore(recall, precision, beta);
		}

		int nrOfNegExamples = cntA - cntAB;

		return new AxiomScore(score, score, cntAB, nrOfNegExamples, useSampling);
	}

	/**
	 * Whether to use only Precision or F-Measure.
	 *
	 * @param usePrecisionOnly
	 */
	public void setUsePrecisionOnly(boolean usePrecisionOnly) {
		this.useSimpleScore = usePrecisionOnly;
	}

	protected abstract void run();

}
