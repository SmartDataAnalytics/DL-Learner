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

import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.OWL2;

@ComponentAnn(name = "asymmetric objectproperty axiom learner", shortName = "oplasymm", version = 0.1)
public class AsymmetricObjectPropertyAxiomLearner extends
		AbstractAxiomLearningAlgorithm<OWLAsymmetricObjectPropertyAxiom, OWLObjectPropertyAssertionAxiom> {

	private static final Logger logger = LoggerFactory.getLogger(AsymmetricObjectPropertyAxiomLearner.class);

	private OWLObjectProperty propertyToDescribe;

	private boolean declaredAsymmetric;

	public AsymmetricObjectPropertyAxiomLearner(SparqlEndpointKS ks) {
		this.ks = ks;
		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s ?o WHERE {?s ?p ?o. FILTER NOT EXISTS{?o ?p ?s}}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s ?o WHERE {?s ?p ?o. ?o ?p ?s}");

	}

	public OWLObjectProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(OWLObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		// check if property is already declared as asymmetric in knowledge base
		String query = String.format("ASK {<%s> a <%s>}", propertyToDescribe.toStringID(),
				OWL2.AsymmetricProperty.getURI());
		declaredAsymmetric = executeAskQuery(query);
		if (declaredAsymmetric) {
			existingAxioms.add(df.getOWLAsymmetricObjectPropertyAxiom(propertyToDescribe));
			logger.info("Property is already declared as symmetric in knowledge base.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
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

	private void runSPARQL1_0_Mode() {
		workingModel = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery = "CONSTRUCT {?s <%s> ?o.} WHERE {?s <%s> ?o} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, propertyToDescribe.toStringID(), propertyToDescribe.toStringID(),
				limit, offset);
		Model newModel = executeConstructQuery(query);
		while (!terminationCriteriaSatisfied() && newModel.size() != 0) {
			workingModel.add(newModel);
			// get number of instances of s with <s p o>
			query = "SELECT (COUNT(*) AS ?total) WHERE {?s <%s> ?o.}";
			query = query.replace("%s", propertyToDescribe.toStringID());
			ResultSet rs = executeSelectQuery(query, workingModel);
			QuerySolution qs;
			int total = 0;
			while (rs.hasNext()) {
				qs = rs.next();
				total = qs.getLiteral("total").getInt();
			}
			query = "SELECT (COUNT(*) AS ?symmetric) WHERE {?s <%s> ?o. ?o <%s> ?s.}";
			query = query.replace("%s", propertyToDescribe.toStringID());
			rs = executeSelectQuery(query, workingModel);
			int symmetric = 0;
			while (rs.hasNext()) {
				qs = rs.next();
				symmetric = qs.getLiteral("symmetric").getInt();
			}
			int asymmetric = total - symmetric;

			if (total > 0) {
				currentlyBestAxioms.clear();
				currentlyBestAxioms.add(new EvaluatedAxiom<OWLAsymmetricObjectPropertyAxiom>(df
						.getOWLAsymmetricObjectPropertyAxiom(propertyToDescribe), computeScore(total, asymmetric),
						declaredAsymmetric));
			}
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.toStringID(), propertyToDescribe.toStringID(), limit,
					offset);
			newModel = executeConstructQuery(query);
		}
	}

	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getPositiveExamples(
			EvaluatedAxiom<OWLAsymmetricObjectPropertyAxiom> evAxiom) {
		OWLAsymmetricObjectPropertyAxiom axiom = evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("p", axiom.getProperty().asOWLObjectProperty().toStringID());

		Set<OWLObjectPropertyAssertionAxiom> posExamples = new TreeSet<OWLObjectPropertyAssertionAxiom>();

		ResultSet rs;
		if (workingModel != null) {
			rs = executeSelectQuery(posExamplesQueryTemplate.toString(), workingModel);
		} else {
			rs = executeSelectQuery(posExamplesQueryTemplate.toString());
		}

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			OWLIndividual object = df.getOWLNamedIndividual(IRI.create(qs.getResource("o").getURI()));
			posExamples.add(df.getOWLObjectPropertyAssertionAxiom(propertyToDescribe, subject, object));
		}

		return posExamples;
	}

	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getNegativeExamples(
			EvaluatedAxiom<OWLAsymmetricObjectPropertyAxiom> evaluatedAxiom) {
		OWLAsymmetricObjectPropertyAxiom axiom = evaluatedAxiom.getAxiom();
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
			OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			OWLIndividual object = df.getOWLNamedIndividual(IRI.create(qs.getResource("o").getURI()));
			negExamples.add(df.getOWLObjectPropertyAssertionAxiom(propertyToDescribe, subject, object));
		}

		return negExamples;
	}

	private void runSPARQL1_1_Mode() {
		int total = reasoner.getPopularity(propertyToDescribe);

		if (total > 0) {
			int asymmetric = 0;
			String query = "SELECT (COUNT(*) AS ?asymmetric) WHERE {?s <%s> ?o. FILTER NOT EXISTS{?o <%s> ?s.}}";
			query = query.replace("%s", propertyToDescribe.toStringID());
			ResultSet rs = executeSelectQuery(query);
			if (rs.hasNext()) {
				asymmetric = rs.next().getLiteral("asymmetric").getInt();
			}

			currentlyBestAxioms.add(new EvaluatedAxiom<OWLAsymmetricObjectPropertyAxiom>(df
					.getOWLAsymmetricObjectPropertyAxiom(propertyToDescribe), computeScore(total, asymmetric),
					declaredAsymmetric));
		}

	}

	public static void main(String[] args) throws Exception {
		OWLDataFactory df = new OWLDataFactoryImpl();
		AsymmetricObjectPropertyAxiomLearner l = new AsymmetricObjectPropertyAxiomLearner(new SparqlEndpointKS(
				new SparqlEndpoint(new URL("http://live.dbpedia.org/sparql"),
						Collections.singletonList("http://dbpedia.org"), Collections.<String> emptyList())));// .getEndpointDBpediaLiveAKSW()));
		l.setPropertyToDescribe(df.getOWLObjectProperty(IRI.create("http://dbpedia.org/ontology/spouse")));
		l.setMaxExecutionTimeInSeconds(10);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5));
	}

}
