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
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

@ComponentAnn(name = "transitive object property axiom learner", shortName = "opltrans", version = 0.1, description="A learning algorithm for transitive object property axioms.")
public class TransitiveObjectPropertyAxiomLearner extends ObjectPropertyCharacteristicsAxiomLearner<OWLTransitiveObjectPropertyAxiom> {
	
	private static final ParameterizedSparqlString SAMPLE_QUERY = new ParameterizedSparqlString(
			"CONSTRUCT {?s ?p ?o . ?o ?p ?o1 . ?s ?p ?o1 .} WHERE {?s ?p ?o . ?o ?p ?o1 . OPTIONAL {?s ?p ?o1 .}}");

	public TransitiveObjectPropertyAxiomLearner(SparqlEndpointKS ks) {
		super(ks);

		posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s ?o1 ?o2 WHERE {?s ?p ?o1. ?o1 ?p ?o2. ?s ?p ?o2}");
		negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s ?o1 ?o2 WHERE {?s ?p ?o1. ?o1 ?p ?o2. FILTER NOT EXISTS {?s ?p ?o2 }}");
		
		axiomType = AxiomType.TRANSITIVE_OBJECT_PROPERTY;
		
		COUNT_QUERY = new ParameterizedSparqlString(
				"SELECT (COUNT(*) AS ?cnt) WHERE {?s ?p ?o1. ?o1 ?p ?o2. }");
		
		POS_FREQUENCY_QUERY = new ParameterizedSparqlString(
				"SELECT (COUNT(*) AS ?cnt) WHERE {?s ?p ?o1. ?o1 ?p ?o2. ?s ?p ?o2}");
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyCharacteristicsAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	protected OWLTransitiveObjectPropertyAxiom getAxiom(OWLObjectProperty property) {
		return df.getOWLTransitiveObjectPropertyAxiom(property);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.PropertyAxiomLearner#getSampleQuery()
	 */
	@Override
	protected ParameterizedSparqlString getSampleQuery() {
		return SAMPLE_QUERY;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyCharacteristicsAxiomLearner#getNegativeExamples(org.dllearner.core.EvaluatedAxiom)
	 */
	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getNegativeExamples(
			EvaluatedAxiom<OWLTransitiveObjectPropertyAxiom> evaluatedAxiom) {
		OWLTransitiveObjectPropertyAxiom axiom = evaluatedAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("p", axiom.getProperty().asOWLObjectProperty().toStringID());

		Set<OWLObjectPropertyAssertionAxiom> negExamples = new TreeSet<>();

		ResultSet rs = executeSelectQuery(negExamplesQueryTemplate.toString());

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			// ?s
			OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			// ?o1
			OWLIndividual object1 = df.getOWLNamedIndividual(IRI.create(qs.getResource("o1").getURI()));
			// ?o2
			OWLIndividual object2 = df.getOWLNamedIndividual(IRI.create(qs.getResource("o2").getURI()));
			
			// ?s -> ?o1
			negExamples.add(df.getOWLObjectPropertyAssertionAxiom(entityToDescribe, subject, object1));
			// ?o1 -> ?o2
			negExamples.add(df.getOWLObjectPropertyAssertionAxiom(entityToDescribe, object1, object2));
		}

		return negExamples;
	}
	
	public static void main(String[] args) throws Exception {
		TransitiveObjectPropertyAxiomLearner l = new TransitiveObjectPropertyAxiomLearner(new SparqlEndpointKS(
				SparqlEndpoint.getEndpointDBpediaLiveAKSW()));
		l.setEntityToDescribe(new OWLObjectPropertyImpl(IRI.create("http://dbpedia.org/ontology/birthPlace")));
		l.setMaxExecutionTimeInSeconds(5);
		l.init();
		l.start();
		List<EvaluatedAxiom<OWLTransitiveObjectPropertyAxiom>> axioms = l.getCurrentlyBestEvaluatedAxioms(5, 0.0);
		System.out.println(axioms);

		for (EvaluatedAxiom<OWLTransitiveObjectPropertyAxiom> axiom : axioms) {
			l.explainScore(axiom);
		}
	}

}
