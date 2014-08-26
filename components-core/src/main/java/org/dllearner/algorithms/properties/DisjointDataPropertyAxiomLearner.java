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

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

@ComponentAnn(name = "disjoint dataproperty axiom learner", shortName = "dpldisjoint", version = 0.1)
public class DisjointDataPropertyAxiomLearner extends
		AbstractAxiomLearningAlgorithm<OWLDisjointDataPropertiesAxiom, OWLDataPropertyAssertionAxiom> {

	private static final Logger logger = LoggerFactory.getLogger(DisjointDataPropertyAxiomLearner.class);

	private OWLDataProperty propertyToDescribe;

	private Set<OWLDataProperty> allDataProperties;

	private boolean usePropertyPopularity = true;

	private int popularity;

	// if true, we only consider properties with the same range
	private boolean strictMode = true;
	
	public DisjointDataPropertyAxiomLearner(SparqlEndpointKS ks) {
		this.ks = ks;

		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s ?o WHERE {?s ?p ?o. FILTER NOT EXISTS{?s ?p_dis ?o}}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s ?o WHERE {?s ?p ?o; ?p_dis ?o.}");
		super.existingAxiomsTemplate = new ParameterizedSparqlString("SELECT ?p WHERE {?p owl:propertyDisjointWith ?p_dis .}");
	}

	public OWLDataProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(OWLDataProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
		
		existingAxiomsTemplate.setIri("p", propertyToDescribe.toStringID());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		ResultSet rs = executeSelectQuery(existingAxiomsTemplate.toString());
		while(rs.hasNext()){
			QuerySolution qs = rs.next();
			OWLDataProperty disjointProperty = df.getOWLDataProperty(IRI.create(qs.getResource("p_dis").getURI()));
			existingAxioms.add(df.getOWLDisjointDataPropertiesAxiom(propertyToDescribe, disjointProperty));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		progressMonitor.learningStarted(AxiomType.DISJOINT_DATA_PROPERTIES.getName());
		
		// get the popularity of the property
		popularity = reasoner.getPopularity(propertyToDescribe);

		// we have to skip here if there are not triples with the property
		if (popularity == 0) {
			logger.warn("Cannot compute disjointness statements for empty property " + propertyToDescribe);
			return;
		}

		run();
		
//		runBatched();
		
		progressMonitor.learningStopped();
	}

	private void run() {
		// get rdfs:range of the property
		OWLDataRange range = reasoner.getRange(propertyToDescribe);

		// get the candidates that have the same range 
		SortedSet<OWLDataProperty> candidates = new TreeSet<OWLDataProperty>();
		if (range != null && range.isDatatype()) {
			String query = "SELECT ?p WHERE {?p rdfs:range <" + range.asOWLDatatype().toStringID() + ">}";
			ResultSet rs = executeSelectQuery(query);
			while (rs.hasNext()) {
				OWLDataProperty p = df.getOWLDataProperty(IRI.create(rs.next().getResource("p").getURI()));
				candidates.add(p);
			}
		} else {// we have to check all other properties
			candidates = reasoner.getDatatypeProperties();
		}
		candidates.remove(propertyToDescribe);

		// check for each candidate if an overlap exist
		ParameterizedSparqlString query = new ParameterizedSparqlString(
				"SELECT (COUNT(*) AS ?overlap) WHERE {?s ?p ?o; ?p_dis ?o.}");
		query.setIri("p", propertyToDescribe.toStringID());
		int i = 1;
		for (OWLDataProperty p : candidates) {
//			logger.info("Progress: " + format.format((double)i++/candidates.size()));
			progressMonitor.learningProgressChanged(i++, candidates.size());
			// get the popularity of the candidate
			int candidatePopularity = reasoner.getPopularity(p);
			
			if(candidatePopularity == 0){// skip empty properties
				logger.debug("Cannot compute disjointness statements for empty candidate property " + p);
				continue;
			}
			
			// get the number of overlapping triples, i.e. triples with the same subject and object
			query.setIri("p_dis", p.toStringID());
			ResultSet rs = executeSelectQuery(query.toString());
			int overlap = rs.next().getLiteral("overlap").getInt();
			
			// compute the estimated precision
			double precision = accuracy(candidatePopularity, overlap);
			
			// compute the estimated recall
			double recall = accuracy(popularity, overlap);
			
			// compute the final score
			double score = 1 - fMEasure(precision, recall);
			
			currentlyBestAxioms.add(
					new EvaluatedAxiom<OWLDisjointDataPropertiesAxiom>(
							df.getOWLDisjointDataPropertiesAxiom(propertyToDescribe, p), 
							new AxiomScore(score)));
		}
	}
	
	/**
	 * In this method we try to compute the overlap with each property in one single SPARQL query.
	 * This method might be much slower as the query is much more complex.
	 */
	private void runBatched() {
		// get rdfs:range of the property
		OWLDataRange range = reasoner.getRange(propertyToDescribe);

		// check for each candidate if an overlap exist
		ParameterizedSparqlString query = new ParameterizedSparqlString(
				"SELECT ?p_dis (COUNT(*) AS ?overlap) WHERE {"
				+ "?s ?p ?o; ?p_dis ?o . "
				+ "?p_dis a <http://www.w3.org/2002/07/owl#DatatypeProperty>; <http://www.w3.org/2000/01/rdf-schema#range> ?range .}"
				+ " GROUP BY ?p_dis");
		query.setIri("p", propertyToDescribe.toStringID());
		query.setIri("range", range.asOWLDatatype().toStringID());
		System.out.println(query.asQuery());
		ResultSet rs = executeSelectQuery(query.toString());
		ResultSetRewindable rsrw = ResultSetFactory.copyResults(rs);
	    int size = rsrw.size();
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			logger.info("Progress: " + format.format((double) rs.getRowNumber() / size));
			
			OWLDataProperty candidate = df.getOWLDataProperty(IRI.create(qs.getResource("p_dis").getURI()));
			
			// get the popularity of the candidate
			int candidatePopularity = reasoner.getPopularity(candidate);
			
			if(candidatePopularity == 0){// skip empty properties
				logger.warn("Cannot compute disjointness statements for empty candidate property " + candidate);
				continue;
			}
			
			// get the number of overlapping triples, i.e. triples with the same subject and object
			int overlap = rs.next().getLiteral("overlap").getInt();
			
			// compute the estimated precision
			double precision = accuracy(candidatePopularity, overlap);
			
			// compute the estimated recall
			double recall = accuracy(popularity, overlap);
			
			// compute the final score
			double score = 1 - fMEasure(precision, recall);
			
			currentlyBestAxioms.add(
					new EvaluatedAxiom<OWLDisjointDataPropertiesAxiom>(
							df.getOWLDisjointDataPropertiesAxiom(propertyToDescribe, candidate), 
							new AxiomScore(score)));
		}
		logger.info("Progress: 100%");
	}

	private void runSingleQueryMode() {
		// compute the overlap if exist
		Map<OWLDataProperty, Integer> property2Overlap = new HashMap<OWLDataProperty, Integer>();
		String query = String.format("SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o. ?s ?p ?o.} GROUP BY ?p",
				propertyToDescribe.toStringID());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			OWLDataProperty prop = df.getOWLDataProperty(IRI.create(qs.getResource("p").getURI()));
			int cnt = qs.getLiteral("cnt").getInt();
			property2Overlap.put(prop, cnt);
		}
		// for each property in knowledge base
		for (OWLDataProperty p : allDataProperties) {
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

			currentlyBestAxioms.add(new EvaluatedAxiom<OWLDisjointDataPropertiesAxiom>(df
					.getOWLDisjointDataPropertiesAxiom(propertyToDescribe, p), new AxiomScore(score)));
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
		Map<OWLDataProperty, Integer> result = new HashMap<OWLDataProperty, Integer>();
		while (!terminationCriteriaSatisfied() && newModel.size() != 0) {
			workingModel.add(newModel);
			OWLDataProperty prop;
			Integer oldCnt;
			ResultSet rs = executeSelectQuery(countQuery, workingModel);
			QuerySolution qs;
			while (rs.hasNext()) {
				qs = rs.next();
				prop = df.getOWLDataProperty(IRI.create(qs.getResource("p").getURI()));
				int newCnt = qs.getLiteral("count").getInt();
				oldCnt = result.get(prop);
				if (oldCnt == null) {
					oldCnt = Integer.valueOf(newCnt);
				}
				result.put(prop, oldCnt);
				qs.getLiteral("count").getInt();
			}
			if (!result.isEmpty()) {
				currentlyBestAxioms = buildAxioms(result, allDataProperties);
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
		Map<OWLDataProperty, Integer> result = new HashMap<OWLDataProperty, Integer>();
		OWLDataProperty prop;
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
				prop = df.getOWLDataProperty(IRI.create(qs.getResource("p").getURI()));
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
				currentlyBestAxioms = buildAxioms(result, allDataProperties);
				offset += 1000;
			}
		}

	}

	private SortedSet<EvaluatedAxiom<OWLDisjointDataPropertiesAxiom>> buildAxioms(
			Map<OWLDataProperty, Integer> property2Count, Set<OWLDataProperty> allProperties) {
		SortedSet<EvaluatedAxiom<OWLDisjointDataPropertiesAxiom>> axioms = new TreeSet<EvaluatedAxiom<OWLDisjointDataPropertiesAxiom>>();
		Integer all = property2Count.get(propertyToDescribe);
		property2Count.remove(propertyToDescribe);

		// get complete disjoint properties
		Set<OWLDataProperty> completeDisjointProperties = new TreeSet<OWLDataProperty>(allProperties);
		completeDisjointProperties.removeAll(property2Count.keySet());

		EvaluatedAxiom<OWLDisjointDataPropertiesAxiom> evalAxiom;
		// first create disjoint axioms with properties which not occur and give
		// score of 1
		for (OWLDataProperty p : completeDisjointProperties) {
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
			evalAxiom = new EvaluatedAxiom<OWLDisjointDataPropertiesAxiom>(df.getOWLDisjointDataPropertiesAxiom(
					propertyToDescribe, p), new AxiomScore(score));
			axioms.add(evalAxiom);
		}

		// second create disjoint axioms with other properties and score 1 -
		// (#occurence/#all)
		OWLDataProperty p;
		for (Entry<OWLDataProperty, Integer> entry : sortByValues(property2Count)) {
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

			evalAxiom = new EvaluatedAxiom<OWLDisjointDataPropertiesAxiom>(df.getOWLDisjointDataPropertiesAxiom(
					propertyToDescribe, p), new AxiomScore(score));
		}

		property2Count.put(propertyToDescribe, all);
		return axioms;
	}

	@Override
	public Set<OWLDataPropertyAssertionAxiom> getPositiveExamples(EvaluatedAxiom<OWLDisjointDataPropertiesAxiom> evAxiom) {
		OWLDisjointDataPropertiesAxiom axiom = evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		// we assume a single atomic property
		OWLDataProperty disjointProperty = axiom.getPropertiesMinus(propertyToDescribe).iterator().next()
				.asOWLDataProperty();
		posExamplesQueryTemplate.setIri("p_dis", disjointProperty.toStringID());

		Set<OWLDataPropertyAssertionAxiom> posExamples = new TreeSet<OWLDataPropertyAssertionAxiom>();

		ResultSet rs;
		if (workingModel != null) {
			rs = executeSelectQuery(posExamplesQueryTemplate.toString(), workingModel);
		} else {
			rs = executeSelectQuery(posExamplesQueryTemplate.toString());
		}

		OWLIndividual subject;
		OWLLiteral object;
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			object = convertLiteral(qs.getLiteral("o"));
			posExamples.add(df.getOWLDataPropertyAssertionAxiom(propertyToDescribe, subject, object));
		}

		return posExamples;
	}

	@Override
	public Set<OWLDataPropertyAssertionAxiom> getNegativeExamples(EvaluatedAxiom<OWLDisjointDataPropertiesAxiom> evAxiom) {
		OWLDisjointDataPropertiesAxiom axiom = evAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		// we assume a single atomic property
		OWLDataProperty disjointProperty = axiom.getPropertiesMinus(propertyToDescribe).iterator().next()
				.asOWLDataProperty();
		negExamplesQueryTemplate.setIri("p_dis", disjointProperty.toStringID());

		Set<OWLDataPropertyAssertionAxiom> negExamples = new TreeSet<OWLDataPropertyAssertionAxiom>();
		ResultSet rs;
		if (workingModel != null) {
			rs = executeSelectQuery(negExamplesQueryTemplate.toString(), workingModel);
		} else {
			rs = executeSelectQuery(negExamplesQueryTemplate.toString());
		}

		OWLIndividual subject;
		OWLLiteral object;
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			object = convertLiteral(qs.getLiteral("o"));
			negExamples.add(df.getOWLDataPropertyAssertionAxiom(propertyToDescribe, subject, object));
		}

		return negExamples;
	}

	public static void main(String[] args) throws Exception {
		DisjointDataPropertyAxiomLearner l = new DisjointDataPropertyAxiomLearner(new SparqlEndpointKS(
				SparqlEndpoint.getEndpointDBpedia()));
		l.setPropertyToDescribe(new OWLDataFactoryImpl().getOWLDataProperty(IRI
				.create("http://dbpedia.org/ontology/height")));
		l.setMaxExecutionTimeInSeconds(10);
		l.init();
		// l.getReasoner().precomputeDataPropertyPopularity();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5));
	}
}
