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

import java.util.Set;

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

@ComponentAnn(name="data property range learner", shortName="dblrange", version=0.1, description="A learning algorithm for reflexive data property range axioms.")
public class DataPropertyRangeAxiomLearner extends DataPropertyAxiomLearner<OWLDataPropertyRangeAxiom> {
	
	private static final ParameterizedSparqlString DATATYPE_FREQUENCY_QUERY = new ParameterizedSparqlString(
			"SELECT  ?dt (count(distinct ?o) AS ?cnt)\n" + 
			"WHERE\n" + 
			"  { ?s ?p ?o }\n" + 
			"GROUP BY (datatype(?o) AS ?dt)");
	
	public DataPropertyRangeAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?o ?p ?s. FILTER (DATATYPE(?s) = ?dt)}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?o ?p ?s. FILTER (DATATYPE(?s) != ?dt)}");
		
		COUNT_QUERY = DISTINCT_OBJECTS_COUNT_QUERY;
		
		axiomType = AxiomType.DATA_PROPERTY_RANGE;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		OWLDataRange existingRange = reasoner.getRange(entityToDescribe);
		if(existingRange != null){
			existingAxioms.add(df.getOWLDataPropertyRangeAxiom(entityToDescribe, existingRange));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#setEntityToDescribe(org.semanticweb.owlapi.model.OWLEntity)
	 */
	@Override
	public void setEntityToDescribe(OWLDataProperty entityToDescribe) {
		super.setEntityToDescribe(entityToDescribe);
		
		DATATYPE_FREQUENCY_QUERY.setIri("p", entityToDescribe.toStringID());
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.PropertyAxiomLearner#run()
	 */
	@Override
	protected void run() {
		// get the frequency for each datatype
		ResultSet rs = executeSelectQuery(DATATYPE_FREQUENCY_QUERY.toString());
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();

			// datatype
			String datatypeURI = qs.getResource("dt").getURI();

			// frequency of datatype
			int frequency = qs.getLiteral("cnt").getInt();

			// precision (A AND B)/B
			double precision = Heuristics.getConfidenceInterval95WaldAverage(popularity, frequency);

			// score
			/* TODO: currently the score is rather simple, but it's not clear whether it makes sense to take
			 also the total number of literals with given datatype into account
			 */
			double score = precision;
			
			int nrOfPosExamples = frequency;
			
			int nrOfNegExamples = popularity - nrOfPosExamples;
			
			currentlyBestAxioms.add(new EvaluatedAxiom<>(
					df.getOWLDataPropertyRangeAxiom(entityToDescribe, df.getOWLDatatype(IRI.create(datatypeURI))),
					new AxiomScore(score, score, nrOfPosExamples, nrOfNegExamples, useSampling)));

		}

	}
	
	@Override
	public Set<OWLDataPropertyAssertionAxiom> getPositiveExamples(EvaluatedAxiom<OWLDataPropertyRangeAxiom> evAxiom) {
		OWLDataPropertyRangeAxiom axiom = evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("dt", axiom.getRange().toString());
		return super.getPositiveExamples(evAxiom);
	}
	
	@Override
	public Set<OWLDataPropertyAssertionAxiom> getNegativeExamples(EvaluatedAxiom<OWLDataPropertyRangeAxiom> evAxiom) {
		OWLDataPropertyRangeAxiom axiom = evAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("dt", axiom.getRange().toString());
		return super.getNegativeExamples(evAxiom);
	}

	
}
