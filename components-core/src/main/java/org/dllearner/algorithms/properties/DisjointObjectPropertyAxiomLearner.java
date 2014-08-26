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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
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
import com.hp.hpl.jena.rdf.model.RDFNode;

@ComponentAnn(name = "disjoint objectproperty axiom learner", shortName = "opldisjoint", version = 0.1)
public class DisjointObjectPropertyAxiomLearner extends ObjectPropertyHierarchyAxiomLearner<OWLDisjointObjectPropertiesAxiom> {

	private Set<OWLObjectProperty> allObjectProperties;

	private boolean usePropertyPopularity = true;

	private int popularity;

	public DisjointObjectPropertyAxiomLearner(SparqlEndpointKS ks) {
		super(ks);

		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s ?o WHERE {?s ?p ?o. FILTER NOT EXISTS{?s ?p_other ?o}}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s ?o WHERE {?s ?p ?o; ?p_other ?o.}");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		SortedSet<OWLObjectProperty> existingDisjointProperties = reasoner.getEquivalentProperties(propertyToDescribe);
		if (existingDisjointProperties != null && !existingDisjointProperties.isEmpty()) {
			for (OWLObjectProperty eqProp : existingDisjointProperties) {
				existingAxioms.add(df.getOWLDisjointObjectPropertiesAxiom(propertyToDescribe, eqProp));
			}
			logger.info("Existing axioms:" + existingAxioms);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyHierarchyAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLObjectProperty, org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	public OWLDisjointObjectPropertiesAxiom getAxiom(OWLObjectProperty property, OWLObjectProperty otherProperty) {
		return df.getOWLDisjointObjectPropertiesAxiom(property, otherProperty);
	}
	
	public double computeScore(int candidatePopularity, int popularity, int overlap) {
		return 1 - super.computeScore(candidatePopularity, popularity, overlap);
	};

	private void runSingleQueryMode() {
		// compute the overlap if exist
		Map<OWLObjectProperty, Integer> property2Overlap = new HashMap<OWLObjectProperty, Integer>();
		String query = String.format("SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o. ?s ?p ?o.} GROUP BY ?p",
				propertyToDescribe.toStringID());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			OWLObjectProperty prop = df.getOWLObjectProperty(IRI.create(qs.getResource("p").getURI()));
			int cnt = qs.getLiteral("cnt").getInt();
			property2Overlap.put(prop, cnt);
		}
		// for each property in knowledge base
		for (OWLObjectProperty p : allObjectProperties) {
			// get the popularity
			int otherPopularity = reasoner.getPopularity(p);
			if (otherPopularity == 0) {// skip empty properties
				continue;
			}
			// get the overlap
			int overlap = property2Overlap.containsKey(p) ? property2Overlap.get(p) : 0;
			// compute the estimated precision
			double precision = accuracy(otherPopularity, overlap);
			// compute the estimated recall
			double recall = accuracy(popularity, overlap);
			// compute the final score
			double score = 1 - fMEasure(precision, recall);

			currentlyBestAxioms.add(new EvaluatedAxiom<OWLDisjointObjectPropertiesAxiom>(df
					.getOWLDisjointObjectPropertiesAxiom(propertyToDescribe, p), new AxiomScore(score)));
		}
	}

	private void runSPARQL1_0_Mode() {
		workingModel = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery = "CONSTRUCT {?s ?p ?o.} WHERE {?s <%s> ?o. ?s ?p ?o.} LIMIT %d OFFSET %d";
		String countQuery = "SELECT ?p (COUNT(?s) AS ?count) WHERE {?s ?p ?o.} GROUP BY ?p";
		String query = String.format(baseQuery, propertyToDescribe.toStringID(), limit, offset);
		Model newModel = executeConstructQuery(query);
		Map<OWLObjectProperty, Integer> result = new HashMap<OWLObjectProperty, Integer>();
		while (!terminationCriteriaSatisfied() && newModel.size() != 0) {
			workingModel.add(newModel);
			OWLObjectProperty prop;
			Integer oldCnt;
			ResultSet rs = executeSelectQuery(countQuery, workingModel);
			QuerySolution qs;
			while (rs.hasNext()) {
				qs = rs.next();
				prop = df.getOWLObjectProperty(IRI.create(qs.getResource("p").getURI()));
				int newCnt = qs.getLiteral("count").getInt();
				oldCnt = result.get(prop);
				if (oldCnt == null) {
					oldCnt = Integer.valueOf(newCnt);
				}
				result.put(prop, oldCnt);
				qs.getLiteral("count").getInt();
			}
			if (!result.isEmpty()) {
				currentlyBestAxioms = buildAxioms(result, allObjectProperties);
			}

			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.toStringID(), limit, offset);
			newModel = executeConstructQuery(query);
		}

	}

	private void runSPARQL1_1_Mode() {
		// get properties and how often they occur
		int limit = 1000;
		int offset = 0;
		String queryTemplate = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p (COUNT(?s) as ?count) WHERE {?p a owl:DatatypeProperty. ?s ?p ?o."
				+ "{SELECT ?s ?o WHERE {?s <%s> ?o.} LIMIT %d OFFSET %d}" + "}";
		String query;
		Map<OWLObjectProperty, Integer> result = new HashMap<OWLObjectProperty, Integer>();
		OWLObjectProperty prop;
		Integer oldCnt;
		boolean repeat = true;

		ResultSet rs = null;
		while (!terminationCriteriaSatisfied() && repeat) {
			query = String.format(queryTemplate, propertyToDescribe, limit, offset);
			rs = executeSelectQuery(query);
			QuerySolution qs;
			repeat = false;
			while (rs.hasNext()) {
				qs = rs.next();
				prop = df.getOWLObjectProperty(IRI.create(qs.getResource("p").getURI()));
				int newCnt = qs.getLiteral("count").getInt();
				oldCnt = result.get(prop);
				if (oldCnt == null) {
					oldCnt = Integer.valueOf(newCnt);
				} else {
					oldCnt += newCnt;
				}
				result.put(prop, oldCnt);
				repeat = true;
			}
			if (!result.isEmpty()) {
				currentlyBestAxioms = buildAxioms(result, allObjectProperties);
				offset += 1000;
			}
		}

	}

	private SortedSet<EvaluatedAxiom<OWLDisjointObjectPropertiesAxiom>> buildAxioms(
			Map<OWLObjectProperty, Integer> property2Count, Set<OWLObjectProperty> allProperties) {
		SortedSet<EvaluatedAxiom<OWLDisjointObjectPropertiesAxiom>> axioms = new TreeSet<EvaluatedAxiom<OWLDisjointObjectPropertiesAxiom>>();
		Integer all = property2Count.get(propertyToDescribe);
		property2Count.remove(propertyToDescribe);

		// get complete disjoint properties
		Set<OWLObjectProperty> completeDisjointProperties = new TreeSet<OWLObjectProperty>(allProperties);
		completeDisjointProperties.removeAll(property2Count.keySet());

		EvaluatedAxiom<OWLDisjointObjectPropertiesAxiom> evalAxiom;
		// first create disjoint axioms with properties which not occur and give
		// score of 1
		for (OWLObjectProperty p : completeDisjointProperties) {
			double score;
			if (usePropertyPopularity) {
				int overlap = 0;
				int pop;
				if (ks.isRemote()) {
					pop = reasoner.getPopularity(p);
				} else {
					Model model = ((LocalModelBasedSparqlEndpointKS) ks).getModel();
					pop = model.listStatements(null, model.getProperty(p.toStringID()), (RDFNode) null).toSet().size();
				}
				// we skip classes with no instances
				if (pop == 0)
					continue;

				// we compute the estimated precision
				double precision = accuracy(pop, overlap);
				// we compute the estimated recall
				double recall = accuracy(popularity, overlap);
				// compute the overall score
				score = 1 - fMEasure(precision, recall);
			} else {
				score = 1;
			}
			evalAxiom = new EvaluatedAxiom<OWLDisjointObjectPropertiesAxiom>(df.getOWLDisjointObjectPropertiesAxiom(
					propertyToDescribe, p), new AxiomScore(score));
			axioms.add(evalAxiom);
		}

		// second create disjoint axioms with other properties and score 1 -
		// (#occurence/#all)
		OWLObjectProperty p;
		for (Entry<OWLObjectProperty, Integer> entry : sortByValues(property2Count)) {
			p = entry.getKey();
			int overlap = entry.getValue();
			int pop;
			if (ks.isRemote()) {
				pop = reasoner.getPopularity(p);
			} else {
				Model model = ((LocalModelBasedSparqlEndpointKS) ks).getModel();
				pop = model.listStatements(null, model.getProperty(p.toStringID()), (RDFNode) null).toSet().size();
			}
			// we skip classes with no instances
			if (pop == 0)
				continue;

			// we compute the estimated precision
			double precision = accuracy(pop, overlap);
			// we compute the estimated recall
			double recall = accuracy(popularity, overlap);
			// compute the overall score
			double score = 1 - fMEasure(precision, recall);

			evalAxiom = new EvaluatedAxiom<OWLDisjointObjectPropertiesAxiom>(df.getOWLDisjointObjectPropertiesAxiom(
					propertyToDescribe, p), new AxiomScore(score));
		}

		property2Count.put(propertyToDescribe, all);
		return axioms;
	}

	public static void main(String[] args) throws Exception {
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		DisjointObjectPropertyAxiomLearner l = new DisjointObjectPropertyAxiomLearner(new SparqlEndpointKS(endpoint));// .getEndpointDBpediaLiveAKSW()));
		l.setPropertyToDescribe(new OWLDataFactoryImpl().getOWLObjectProperty(IRI
				.create("http://dbpedia.org/ontology/league")));
		l.setMaxExecutionTimeInSeconds(10);
		l.init();
		// l.getReasoner().precomputeObjectPropertyPopularity();
		l.start();
		for (EvaluatedAxiom ax : l.getCurrentlyBestEvaluatedAxioms(Integer.MAX_VALUE)) {
			System.out.println(ax);
		}
	}
}
