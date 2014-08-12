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

import java.util.SortedSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@ComponentAnn(name = "inverse objectproperty axiom learner", shortName = "oplinv", version = 0.1)
public class InverseObjectPropertyAxiomLearner extends
		AbstractAxiomLearningAlgorithm<OWLInverseObjectPropertiesAxiom, OWLObjectPropertyAssertionAxiom> {

	private static final Logger logger = LoggerFactory.getLogger(InverseObjectPropertyAxiomLearner.class);

	private OWLObjectProperty propertyToDescribe;

	public InverseObjectPropertyAxiomLearner(SparqlEndpointKS ks) {
		this.ks = ks;
	}

	public OWLObjectProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(OWLObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		SortedSet<OWLObjectProperty> existingInverseObjectProperties = reasoner
				.getInverseObjectProperties(propertyToDescribe);
		for (OWLObjectProperty invProp : existingInverseObjectProperties) {
			existingAxioms.add(df.getOWLInverseObjectPropertiesAxiom(invProp, propertyToDescribe));
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		if (!forceSPARQL_1_0_Mode && ks.supportsSPARQL_1_1()) {
			runSPARQL1_1_Mode();
		} else {
			runSPARQL1_0_Mode();
		}
	}

	private void runSingleQueryMode() {
		int total = reasoner.getPopularity(propertyToDescribe);

		String query = String
				.format("PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o. ?o ?p ?s.} GROUP BY ?p",
						propertyToDescribe.toStringID());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			currentlyBestAxioms.add(new EvaluatedAxiom<OWLInverseObjectPropertiesAxiom>(df
					.getOWLInverseObjectPropertiesAxiom(
							df.getOWLObjectProperty(IRI.create(qs.getResource("p").getURI())), propertyToDescribe),
					computeScore(total, qs.getLiteral("cnt").getInt())));
		}
	}

	private void runSPARQL1_0_Mode() {
		Model model = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery = "CONSTRUCT {?s <%s> ?o. ?o ?p ?s} WHERE {?s <%s> ?o. OPTIONAL{?o ?p ?s. ?p a <http://www.w3.org/2002/07/owl#ObjectProperty>}} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, propertyToDescribe.toStringID(), propertyToDescribe.toStringID(),
				limit, offset);
		Model newModel = executeConstructQuery(query);
		while (!terminationCriteriaSatisfied() && newModel.size() != 0) {
			model.add(newModel);
			// get number of instances of s with <s p o>
			query = "SELECT (COUNT(*) AS ?total) WHERE {?s <%s> ?o.}";
			query = query.replace("%s", propertyToDescribe.toStringID());
			ResultSet rs = executeSelectQuery(query, model);
			QuerySolution qs;
			int total = 0;
			while (rs.hasNext()) {
				qs = rs.next();
				total = qs.getLiteral("total").getInt();
			}

			query = String.format("SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o. ?o ?p ?s.} GROUP BY ?p",
					propertyToDescribe.toStringID());
			rs = executeSelectQuery(query, model);
			while (rs.hasNext()) {
				qs = rs.next();
				currentlyBestAxioms.add(new EvaluatedAxiom<OWLInverseObjectPropertiesAxiom>(df
						.getOWLInverseObjectPropertiesAxiom(
								df.getOWLObjectProperty(IRI.create(qs.getResource("p").getURI())), propertyToDescribe),
						computeScore(total, qs.getLiteral("cnt").getInt())));
			}
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.toStringID(), propertyToDescribe.toStringID(), limit,
					offset);
			newModel = executeConstructQuery(query);
		}
	}

	private void runSPARQL1_1_Mode() {
		String query = "SELECT (COUNT(*) AS ?total) WHERE {?s <%s> ?o.}";
		query = query.replace("%s", propertyToDescribe.toStringID());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		int total = 0;
		while (rs.hasNext()) {
			qs = rs.next();
			total = qs.getLiteral("total").getInt();
		}

		query = String
				.format("PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o. ?o ?p ?s. ?p a <http://www.w3.org/2002/07/owl#ObjectProperty>} GROUP BY ?p",
						propertyToDescribe.toStringID());
		rs = executeSelectQuery(query);
		while (rs.hasNext()) {
			qs = rs.next();
			currentlyBestAxioms.add(new EvaluatedAxiom<OWLInverseObjectPropertiesAxiom>(df
					.getOWLInverseObjectPropertiesAxiom(
							df.getOWLObjectProperty(IRI.create(qs.getResource("p").getURI())), propertyToDescribe),
					computeScore(total, qs.getLiteral("cnt").getInt())));
		}

	}

	public static void main(String[] args) throws Exception {
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW());

		InverseObjectPropertyAxiomLearner l = new InverseObjectPropertyAxiomLearner(ks);
		l.setPropertyToDescribe(new OWLDataFactoryImpl().getOWLObjectProperty(IRI
				.create("http://dbpedia.org/ontology/routeEnd")));
		l.setMaxExecutionTimeInSeconds(60);
		//		l.setForceSPARQL_1_0_Mode(true);
		//		l.setReturnOnlyNewAxioms(true);
		l.init();
		l.start();

		System.out.println(l.getCurrentlyBestEvaluatedAxioms(10, 0.2));
	}

}
