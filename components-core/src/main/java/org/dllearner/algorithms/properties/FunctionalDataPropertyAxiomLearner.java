/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

import java.util.List;

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@ComponentAnn(name="functional dataproperty axiom learner", shortName="dplfunc", version=0.1)
public class FunctionalDataPropertyAxiomLearner extends DataPropertyAxiomLearner<OWLFunctionalDataPropertyAxiom> {
	
	private final ParameterizedSparqlString GET_SAMPLE_QUERY = new ParameterizedSparqlString(
			"CONSTRUCT {?s ?p ?o.} WHERE {?s ?p ?o}");
	
	private final ParameterizedSparqlString POS_FREQUENCY_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?s ?p ?o1. FILTER NOT EXISTS {?s ?p ?o2. FILTER(?o1 != ?o2)} }");
	
	private boolean declaredAsFunctional;

	public FunctionalDataPropertyAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		
		posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?s ?p ?o1. FILTER NOT EXISTS {?s ?p ?o2. FILTER(?o1 != ?o2)} }");
		negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?s ?p ?o1. ?s ?p ?o2. FILTER(?o1 != ?o2)}");
		
		COUNT_QUERY = DISTINCT_SUBJECTS_COUNT_QUERY;
		
		axiomType = AxiomType.FUNCTIONAL_DATA_PROPERTY;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		declaredAsFunctional = reasoner.isFunctional(propertyToDescribe);
		if(declaredAsFunctional) {
			existingAxioms.add(df.getOWLFunctionalDataPropertyAxiom(propertyToDescribe));
			logger.warn("Data property " + propertyToDescribe + " is already declared as functional in knowledge base.");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.PropertyAxiomLearner#setPropertyToDescribe(org.semanticweb.owlapi.model.OWLProperty)
	 */
	@Override
	public void setPropertyToDescribe(OWLDataProperty propertyToDescribe) {
		super.setPropertyToDescribe(propertyToDescribe);
		
		POS_FREQUENCY_QUERY.setIri("p", propertyToDescribe.toStringID());
		GET_SAMPLE_QUERY.setIri("p", propertyToDescribe.toStringID());
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.DataPropertyAxiomLearner#run()
	 */
	@Override
	protected void run() {
		runSPARQL1_0_Mode();
//		runSPARQL1_1_Mode();
	}
	
	private void runSPARQL1_0_Mode() {
		boolean declared = !existingAxioms.isEmpty();
		
		workingModel = ModelFactory.createDefaultModel();
		
		//TODO determine page size in super class or even better in the KB object
		int DEFAULT_PAGE_SIZE = 10000;
		long limit = DEFAULT_PAGE_SIZE; //PaginationUtils.adjustPageSize(qef, DEFAULT_PAGE_SIZE);
		long offset = 0;
		
		Query query = GET_SAMPLE_QUERY.asQuery();
		query.setLimit(limit);
		Model newModel = executeConstructQuery(query.toString());
		
		while (!terminationCriteriaSatisfied() && newModel.size() != 0) {
			workingModel.add(newModel);
			
			popularity = getPropertyPopularity(workingModel);
			
			// get number of pos examples
			int frequency = getCountValue(POS_FREQUENCY_QUERY.toString(), workingModel);

			if (popularity > 0) {
				currentlyBestAxioms.clear();
				currentlyBestAxioms.add(new EvaluatedAxiom<OWLFunctionalDataPropertyAxiom>(
						df.getOWLFunctionalDataPropertyAxiom(propertyToDescribe), 
						computeScore(popularity, frequency, true),
						declared));
			}
			offset += limit;
			query.setOffset(offset);
			newModel = executeConstructQuery(query.toString());
		}
	}
	
	private void runSPARQL1_1_Mode() {
		boolean declared = !existingAxioms.isEmpty();
		
		int frequency = getCountValue(POS_FREQUENCY_QUERY.toString());

		currentlyBestAxioms.add(new EvaluatedAxiom<OWLFunctionalDataPropertyAxiom>(
				df.getOWLFunctionalDataPropertyAxiom(propertyToDescribe), 
				computeScore(popularity, frequency, false),
				declared));
	}
	
	public static void main(String[] args) throws Exception {
		FunctionalDataPropertyAxiomLearner l = new FunctionalDataPropertyAxiomLearner(new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW()));
		l.setPropertyToDescribe(new OWLDataFactoryImpl().getOWLDataProperty(IRI.create("http://dbpedia.org/ontology/birthDate")));
		l.setMaxExecutionTimeInSeconds(10);
		l.setForceSPARQL_1_0_Mode(true);
		l.init();
		l.start();
		List<EvaluatedAxiom<OWLFunctionalDataPropertyAxiom>> axioms = l.getCurrentlyBestEvaluatedAxioms(5);
		System.out.println(axioms);
		
		for(EvaluatedAxiom<OWLFunctionalDataPropertyAxiom> axiom : axioms){
			printSubset(l.getPositiveExamples(axiom), 10);
			printSubset(l.getNegativeExamples(axiom), 10);
			l.explainScore(axiom);
			System.out.println(((AxiomScore)axiom.getScore()).getTotalNrOfExamples());
			System.out.println(((AxiomScore)axiom.getScore()).getNrOfPositiveExamples());
		}
	}
}
