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

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.ObjectPropertyEditor;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.OWL;

@ComponentAnn(name = "functional objectproperty axiom learner", shortName = "oplfunc", version = 0.1)
public class FunctionalObjectPropertyAxiomLearner extends
		AbstractAxiomLearningAlgorithm<OWLFunctionalObjectPropertyAxiom, OWLIndividual> {

	private static final Logger logger = LoggerFactory.getLogger(FunctionalObjectPropertyAxiomLearner.class);

	@ConfigOption(name = "propertyToDescribe", description = "", propertyEditorClass = ObjectPropertyEditor.class)
	private OWLObjectProperty propertyToDescribe;

	private boolean declaredAsFunctional;

	public FunctionalObjectPropertyAxiomLearner(SparqlEndpointKS ks) {
		this.ks = ks;
		posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s WHERE {?s ?p ?o1. FILTER NOT EXISTS {?s ?p ?o2. FILTER(?o1 != ?o2)} }");
		negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s WHERE {?s ?p ?o1. ?s ?p ?o2. FILTER(?o1 != ?o2)}");
	}

	public OWLObjectProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(OWLObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
		posExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		negExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		String query = String.format("ASK {<%s> a <%s>}", propertyToDescribe, OWL.FunctionalProperty.getURI());
		declaredAsFunctional = executeAskQuery(query);
		if (declaredAsFunctional) {
			existingAxioms.add(df.getOWLFunctionalObjectPropertyAxiom(propertyToDescribe));
			logger.info("Property is already declared as functional in knowledge base.");
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
			query = String.format("SELECT (COUNT(DISTINCT ?s) AS ?all) WHERE {?s <%s> ?o.}",
					propertyToDescribe.toStringID());
			ResultSet rs = executeSelectQuery(query, workingModel);
			QuerySolution qs;
			int all = 1;
			while (rs.hasNext()) {
				qs = rs.next();
				all = qs.getLiteral("all").getInt();
			}

			// get number of instances of s with <s p o> <s p o1> where o != o1
			query = "SELECT (COUNT(DISTINCT ?s) AS ?functional) WHERE {?s <%s> ?o1. FILTER NOT EXISTS {?s <%s> ?o2. FILTER(?o1 != ?o2)} }";
			query = query.replace("%s", propertyToDescribe.toStringID());
			rs = executeSelectQuery(query, workingModel);
			int functional = 1;
			while (rs.hasNext()) {
				qs = rs.next();
				functional = qs.getLiteral("functional").getInt();
			}

			if (all > 0) {
				currentlyBestAxioms.clear();
				currentlyBestAxioms.add(new EvaluatedAxiom<OWLFunctionalObjectPropertyAxiom>(df
						.getOWLFunctionalObjectPropertyAxiom(propertyToDescribe), computeScore(all, functional),
						declaredAsFunctional));
			}
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.toStringID(), propertyToDescribe.toStringID(), limit,
					offset);
			newModel = executeConstructQuery(query);
		}
	}

	private void runSPARQL1_1_Mode() {
		// get number of instances of s with <s p o>
		int numberOfSubjects = reasoner.getSubjectCountForProperty(propertyToDescribe);// TODO,
																						// getRemainingRuntimeInMilliSeconds());
		if (numberOfSubjects == -1) {
			logger.warn("Early termination: Got timeout while counting number of distinct subjects for given property.");
			return;
		}

		if (numberOfSubjects > 0) {
			// get number of instances of s with <s p o> <s p o1> where o != o1
			String query = "SELECT (COUNT(DISTINCT ?s) AS ?functional) WHERE {?s <%s> ?o1. FILTER NOT EXISTS {?s <%s> ?o2. FILTER(?o1 != ?o2)} }";
			query = query.replace("%s", propertyToDescribe.toStringID());
			ResultSet rs = executeSelectQuery(query);
			QuerySolution qs;
			int functional = 1;
			while (rs.hasNext()) {
				qs = rs.next();
				functional = qs.getLiteral("functional").getInt();
			}

			currentlyBestAxioms.add(new EvaluatedAxiom<OWLFunctionalObjectPropertyAxiom>(df
					.getOWLFunctionalObjectPropertyAxiom(propertyToDescribe),
					computeScore(numberOfSubjects, functional), declaredAsFunctional));
		}
	}

	public static void main(String[] args) throws Exception {
		FunctionalObjectPropertyAxiomLearner l = new FunctionalObjectPropertyAxiomLearner(new SparqlEndpointKS(
				SparqlEndpoint.getEndpointDBpedia()));
		l.setPropertyToDescribe(new OWLDataFactoryImpl().getOWLObjectProperty(IRI
				.create("http://dbpedia.org/property/father")));
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
