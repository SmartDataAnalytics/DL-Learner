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

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

@ComponentAnn(name = "inversefunctional objectproperty axiom learner", shortName = "oplinvfunc", version = 0.1)
public class InverseFunctionalObjectPropertyAxiomLearner extends
		ObjectPropertyCharacteristicsAxiomLearner<OWLInverseFunctionalObjectPropertyAxiom> {

	public InverseFunctionalObjectPropertyAxiomLearner(SparqlEndpointKS ks) {
		super(ks);
		
		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s ?o ?WHERE {?o ?p ?s. FILTER NOT EXISTS {?o2 ?p ?s. FILTER(?o != ?o2)}}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s ?o1 ?o2 WHERE {?o1 ?p ?s. ?o2 ?p ?s. FILTER(?o1 != ?o2)}");
		
		super.POS_FREQUENCY_QUERY = new ParameterizedSparqlString(
					"SELECT (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?o1 ?p ?s. FILTER NOT EXISTS {?o2 ?p ?s. FILTER(?o1 != ?o2)}}");
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

		Set<OWLObjectPropertyAssertionAxiom> negExamples = new TreeSet<OWLObjectPropertyAssertionAxiom>();

		ResultSet rs;
		if (workingModel != null) {
			rs = executeSelectQuery(negExamplesQueryTemplate.toString(), workingModel);
		} else {
			rs = executeSelectQuery(negExamplesQueryTemplate.toString());
		}

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			OWLIndividual object = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			// ?o1
			OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("o1").getURI()));
			negExamples.add(df.getOWLObjectPropertyAssertionAxiom(propertyToDescribe, subject, object));
			// ?o2
			subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("o2").getURI()));
			negExamples.add(df.getOWLObjectPropertyAssertionAxiom(propertyToDescribe, subject, object));
		}

		return negExamples;
	}
	
	public static void main(String[] args) throws Exception {
		InverseFunctionalObjectPropertyAxiomLearner l = new InverseFunctionalObjectPropertyAxiomLearner(new SparqlEndpointKS(
				SparqlEndpoint.getEndpointDBpedia()));
		l.setPropertyToDescribe(new OWLDataFactoryImpl().getOWLObjectProperty(IRI
				.create("http://dbpedia.org/ontology/birthPlace")));
		l.setMaxExecutionTimeInSeconds(5);
		l.setForceSPARQL_1_0_Mode(true);
		l.init();
		l.start();
		List<EvaluatedAxiom<OWLInverseFunctionalObjectPropertyAxiom>> axioms = l.getCurrentlyBestEvaluatedAxioms(5);
		System.out.println(axioms);

		for (EvaluatedAxiom<OWLInverseFunctionalObjectPropertyAxiom> axiom : axioms) {
			l.explainScore(axiom);
		}
	}
}
