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
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

@ComponentAnn(name = "inverse functional object property axiom learner", shortName = "oplinvfunc", version = 0.1, description="A learning algorithm for inverse functional object property axioms.")
public class InverseFunctionalObjectPropertyAxiomLearner extends
		ObjectPropertyCharacteristicsAxiomLearner<OWLInverseFunctionalObjectPropertyAxiom> {

	public InverseFunctionalObjectPropertyAxiomLearner(SparqlEndpointKS ks) {
		super(ks);
		
		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s ?o ?WHERE {?s ?p ?o. FILTER NOT EXISTS {?s2 ?p ?o. FILTER(?s != ?s2)}}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s ?s2 ?o WHERE {?s ?p ?o. ?s2 ?p ?o. FILTER(?s != ?s2)}");
		
		super.POS_FREQUENCY_QUERY = new ParameterizedSparqlString(
					"SELECT (COUNT(DISTINCT(?o)) AS ?cnt) WHERE {?s ?p ?o. FILTER NOT EXISTS {?s2 ?p ?o. FILTER(?s != ?s2)}}");
//				"SELECT (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?o1 ?p ?s. ?o2 ?p ?s. FILTER(?o1 != ?o2)}");
		
		axiomType = AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY;
		COUNT_QUERY = DISTINCT_OBJECTS_COUNT_QUERY;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyCharacteristicsAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	protected OWLInverseFunctionalObjectPropertyAxiom getAxiom(OWLObjectProperty property) {
		return df.getOWLInverseFunctionalObjectPropertyAxiom(property);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyCharacteristicsAxiomLearner#getNegativeExamples(org.dllearner.core.EvaluatedAxiom)
	 */
	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getNegativeExamples(
			EvaluatedAxiom<OWLInverseFunctionalObjectPropertyAxiom> evaluatedAxiom) {
		OWLInverseFunctionalObjectPropertyAxiom axiom = evaluatedAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("p", axiom.getProperty().asOWLObjectProperty().toStringID());

		Set<OWLObjectPropertyAssertionAxiom> negExamples = new TreeSet<>();

		ResultSet rs = executeSelectQuery(negExamplesQueryTemplate.toString());

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			// ?o
			OWLIndividual object = df.getOWLNamedIndividual(IRI.create(qs.getResource("o").getURI()));
			// ?s
			OWLIndividual subject1 = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			// ?s2
			OWLIndividual subject2 = df.getOWLNamedIndividual(IRI.create(qs.getResource("s2").getURI()));
			// ?s -> ?o
			negExamples.add(df.getOWLObjectPropertyAssertionAxiom(entityToDescribe, subject1, object));
			// ?s2 -> ?o
			negExamples.add(df.getOWLObjectPropertyAssertionAxiom(entityToDescribe, subject2, object));
		}

		return negExamples;
	}
	
	public static void main(String[] args) throws Exception {
		InverseFunctionalObjectPropertyAxiomLearner l = new InverseFunctionalObjectPropertyAxiomLearner(new SparqlEndpointKS(
				SparqlEndpoint.getEndpointDBpediaLiveAKSW()));
		l.setEntityToDescribe(new OWLDataFactoryImpl().getOWLObjectProperty(IRI
				.create("http://dbpedia.org/ontology/birthPlace")));
		l.setMaxExecutionTimeInSeconds(5);
		l.init();
		l.start();
		List<EvaluatedAxiom<OWLInverseFunctionalObjectPropertyAxiom>> axioms = l.getCurrentlyBestEvaluatedAxioms(5);
		System.out.println(axioms);

		for (EvaluatedAxiom<OWLInverseFunctionalObjectPropertyAxiom> axiom : axioms) {
			l.explainScore(axiom);
		}
	}
}
