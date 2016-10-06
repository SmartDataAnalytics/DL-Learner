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

import java.net.URL;
import java.util.Collections;

import org.dllearner.core.ComponentAnn;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import org.apache.jena.query.ParameterizedSparqlString;

@ComponentAnn(name = "asymmetric object property axiom learner", shortName = "oplasymm", version = 0.1, description="A learning algorithm for asymmetric object property axioms.")
public class AsymmetricObjectPropertyAxiomLearner extends ObjectPropertyCharacteristicsAxiomLearner<OWLAsymmetricObjectPropertyAxiom> {

	public AsymmetricObjectPropertyAxiomLearner(SparqlEndpointKS ks) {
		super(ks);
		
		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s ?o WHERE {?s ?p ?o. FILTER NOT EXISTS{?o ?p ?s}}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s ?o WHERE {?s ?p ?o. ?o ?p ?s}");
		
		super.POS_FREQUENCY_QUERY = new ParameterizedSparqlString(
					"SELECT (COUNT(*) AS ?cnt) WHERE {?s ?p ?o. FILTER NOT EXISTS{?o ?p ?s}}");
		
		axiomType = AxiomType.ASYMMETRIC_OBJECT_PROPERTY;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyCharacteristicsAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	protected OWLAsymmetricObjectPropertyAxiom getAxiom(OWLObjectProperty property) {
		return df.getOWLAsymmetricObjectPropertyAxiom(property);
	}

	public static void main(String[] args) throws Exception {
		OWLDataFactory df = new OWLDataFactoryImpl();
		AsymmetricObjectPropertyAxiomLearner l = new AsymmetricObjectPropertyAxiomLearner(new SparqlEndpointKS(
				new SparqlEndpoint(new URL("http://live.dbpedia.org/sparql"),
						Collections.singletonList("http://dbpedia.org"), Collections.<String> emptyList())));// .getEndpointDBpediaLiveAKSW()));
		l.setEntityToDescribe(df.getOWLObjectProperty(IRI.create("http://dbpedia.org/ontology/spouse")));
		l.setMaxExecutionTimeInSeconds(10);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5));
	}
}
