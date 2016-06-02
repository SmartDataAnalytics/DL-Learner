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

import java.util.SortedSet;

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

@ComponentAnn(name = "inverse object property axiom learner", shortName = "oplinv", version = 0.1, description="A learning algorithm for inverse object property axioms.")
public class InverseObjectPropertyAxiomLearner extends
		ObjectPropertyAxiomLearner<OWLInverseObjectPropertiesAxiom> {
	
	private static final ParameterizedSparqlString POS_EXAMPLES_QUERY = new ParameterizedSparqlString(
			"SELECT ?p_inv ?s ?o WHERE { ?s ?p ?o . ?o ?p_inv ?s . FILTER(!sameTerm(?p, ?p_inv))}");
	
	private static final ParameterizedSparqlString NEG_EXAMPLES_QUERY = new ParameterizedSparqlString(
			"SELECT ?p_inv ?s ?o WHERE { ?s ?p ?o . FILTER NOT EXISTS {?o ?p_inv ?s . FILTER(!sameTerm(?p, ?p_inv))}}");
	
	private static final ParameterizedSparqlString QUERY = new ParameterizedSparqlString(
			"SELECT ?p_inv (COUNT(*) AS ?cnt) WHERE { ?s ?p ?o . ?o ?p_inv ?s . FILTER(!sameTerm(?p, ?p_inv))} GROUP BY ?p_inv");
	
	private static final ParameterizedSparqlString SAMPLE_QUERY = new ParameterizedSparqlString(
			"CONSTRUCT {?s ?p ?o . ?o ?p_inv ?s . } WHERE {?s ?p ?o . OPTIONAL{ ?o ?p_inv ?s . FILTER(!sameTerm(?p, ?p_inv))}}");

	public InverseObjectPropertyAxiomLearner(SparqlEndpointKS ks) {
		super.ks = ks;
		
		super.posExamplesQueryTemplate = POS_EXAMPLES_QUERY;
		super.negExamplesQueryTemplate = NEG_EXAMPLES_QUERY;
		
		axiomType = AxiomType.INVERSE_OBJECT_PROPERTIES;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.PropertyAxiomLearner#setEntityToDescribe(org.semanticweb.owlapi.model.OWLProperty)
	 */
	@Override
	public void setEntityToDescribe(OWLObjectProperty entityToDescribe) {
		super.setEntityToDescribe(entityToDescribe);
		
		QUERY.setIri("p", entityToDescribe.toStringID());
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.PropertyAxiomLearner#getSampleQuery()
	 */
	@Override
	protected ParameterizedSparqlString getSampleQuery() {
		return SAMPLE_QUERY;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		SortedSet<OWLObjectProperty> existingInverseObjectProperties = reasoner
				.getInverseObjectProperties(entityToDescribe);
		for (OWLObjectProperty invProp : existingInverseObjectProperties) {
			existingAxioms.add(df.getOWLInverseObjectPropertiesAxiom(invProp, entityToDescribe));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.PropertyAxiomLearner#run()
	 */
	@Override
	protected void run() {
		ResultSet rs = executeSelectQuery(QUERY.toString());
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			
			// candidate
			OWLObjectProperty candidate = df.getOWLObjectProperty(IRI.create(qs.getResource("p_inv").getURI()));
			
			// frequency
			int frequency = qs.getLiteral("cnt").getInt();
			
			// score
			AxiomScore score = computeScore(popularity, frequency, useSampling);
			
			currentlyBestAxioms.add(new EvaluatedAxiom<>(df
					.getOWLInverseObjectPropertiesAxiom(entityToDescribe, candidate), score
			));
		}
	}

	public static void main(String[] args) throws Exception {
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia());

		InverseObjectPropertyAxiomLearner l = new InverseObjectPropertyAxiomLearner(ks);
		l.setEntityToDescribe(new OWLDataFactoryImpl().getOWLObjectProperty(IRI
				.create("http://dbpedia.org/ontology/routeEnd")));
		l.setMaxExecutionTimeInSeconds(60);
		//		l.setForceSPARQL_1_0_Mode(true);
		//		l.setReturnOnlyNewAxioms(true);
		l.init();
		l.start();

		System.out.println(l.getCurrentlyBestEvaluatedAxioms(10, 0.2));
	}

	

}
