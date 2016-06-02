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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

@ComponentAnn(name = "functional object property axiom learner", shortName = "oplfunc", version = 0.1, description="A learning algorithm for functional object property axioms.")
public class FunctionalObjectPropertyAxiomLearner extends
		ObjectPropertyCharacteristicsAxiomLearner<OWLFunctionalObjectPropertyAxiom> {

	public FunctionalObjectPropertyAxiomLearner(SparqlEndpointKS ks) {
		super(ks);
		
		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s (?o1 AS ?o) ?WHERE {?s ?p ?o1. FILTER NOT EXISTS {?s ?p ?o2. FILTER(?o1 != ?o2)}}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s ?o1 ?o2 WHERE {?s ?p ?o1. ?s ?p ?o2. FILTER(?o1 != ?o2)}");
		
		super.POS_FREQUENCY_QUERY = new ParameterizedSparqlString(
					"SELECT (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?s ?p ?o1. FILTER NOT EXISTS {?s ?p ?o2. FILTER(?o1 != ?o2)}}");
		
		COUNT_QUERY = DISTINCT_SUBJECTS_COUNT_QUERY;
		
		axiomType = AxiomType.FUNCTIONAL_OBJECT_PROPERTY;
		
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyCharacteristicsAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	protected OWLFunctionalObjectPropertyAxiom getAxiom(OWLObjectProperty property) {
		return df.getOWLFunctionalObjectPropertyAxiom(property);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyCharacteristicsAxiomLearner#getNegativeExamples(org.dllearner.core.EvaluatedAxiom)
	 */
	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getNegativeExamples(
			EvaluatedAxiom<OWLFunctionalObjectPropertyAxiom> evaluatedAxiom) {
		OWLFunctionalObjectPropertyAxiom axiom = evaluatedAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("p", axiom.getProperty().asOWLObjectProperty().toStringID());

		Set<OWLObjectPropertyAssertionAxiom> negExamples = new TreeSet<>();

		ResultSet rs = executeSelectQuery(negExamplesQueryTemplate.toString());

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			// ?o1
			OWLIndividual object = df.getOWLNamedIndividual(IRI.create(qs.getResource("o1").getURI()));
			negExamples.add(df.getOWLObjectPropertyAssertionAxiom(entityToDescribe, subject, object));
			// ?o2
			object = df.getOWLNamedIndividual(IRI.create(qs.getResource("o2").getURI()));
			negExamples.add(df.getOWLObjectPropertyAssertionAxiom(entityToDescribe, subject, object));
		}

		return negExamples;
	}

	public static void main(String[] args) throws Exception {
		FunctionalObjectPropertyAxiomLearner l = new FunctionalObjectPropertyAxiomLearner(new SparqlEndpointKS(
				SparqlEndpoint.getEndpointDBpedia()));
		l.setEntityToDescribe(new OWLDataFactoryImpl().getOWLObjectProperty(IRI
				.create("http://dbpedia.org/ontology/birthPlace")));
		l.setMaxExecutionTimeInSeconds(20);
		l.setForceSPARQL_1_0_Mode(true);
		l.init();
		l.start();
		List<EvaluatedAxiom<OWLFunctionalObjectPropertyAxiom>> axioms = l.getCurrentlyBestEvaluatedAxioms(5);
		System.out.println(axioms);

		for (EvaluatedAxiom<OWLFunctionalObjectPropertyAxiom> axiom : axioms) {
			printSubset(l.getPositiveExamples(axiom), 10);
			printSubset(l.getNegativeExamples(axiom), 10);
			l.explainScore(axiom);
		}
	}
}
