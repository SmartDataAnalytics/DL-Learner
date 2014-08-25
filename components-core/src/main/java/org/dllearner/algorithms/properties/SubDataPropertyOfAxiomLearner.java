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

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@ComponentAnn(name="data subPropertyOf axiom learner", shortName="dplsubprop", version=0.1)
public class SubDataPropertyOfAxiomLearner extends AbstractAxiomLearningAlgorithm<OWLSubDataPropertyOfAxiom, OWLDataPropertyAssertionAxiom> {
	
private static final Logger logger = LoggerFactory.getLogger(SubDataPropertyOfAxiomLearner.class);
	
	private OWLDataProperty propertyToDescribe;

	private int popularity;
	
	private double beta = 3.0;
	
	public SubDataPropertyOfAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s ?o WHERE {?s ?p ?o ; ?p_sup ?o .}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s ?o WHERE {?s ?p ?o . FILTER NOT EXISTS{?s ?p_sup ?o}}");
		super.existingAxiomsTemplate = new ParameterizedSparqlString("SELECT ?p WHERE {?p owl:supPropertyOf ?p_sup .}");
	}
	
	public OWLDataProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(OWLDataProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
		
		existingAxiomsTemplate.setIri("p", propertyToDescribe.toStringID());
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		ResultSet rs = executeSelectQuery(existingAxiomsTemplate.toString());
		while(rs.hasNext()){
			QuerySolution qs = rs.next();
			OWLDataProperty superProperty = df.getOWLDataProperty(IRI.create(qs.getResource("p_sup").getURI()));
			existingAxioms.add(df.getOWLEquivalentDataPropertiesAxiom(propertyToDescribe, superProperty));
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		progressMonitor.learningStarted(AxiomType.EQUIVALENT_DATA_PROPERTIES.getName());
		// get the popularity of the property
		popularity = reasoner.getPopularity(propertyToDescribe);

		// we have to skip here if there are not triples with the property
		if (popularity == 0) {
			logger.warn("Cannot compute equivalence statements for empty property " + propertyToDescribe);
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
				"SELECT (COUNT(*) AS ?overlap) WHERE {?s ?p ?o; ?p_eq ?o.}");
		query.setIri("p", propertyToDescribe.toStringID());
		int i = 1;
		for (OWLDataProperty p : candidates) {
//			logger.info("Progress: " + format.format((double)i++/candidates.size()));
			progressMonitor.learningProgressChanged(i++, candidates.size());
			// get the popularity of the candidate
			int candidatePopularity = reasoner.getPopularity(p);
			
			if(candidatePopularity == 0){// skip empty properties
				logger.debug("Cannot compute equivalence statements for empty candidate property " + p);
				continue;
			}
			
			// get the number of overlapping triples, i.e. triples with the same subject and object
			query.setIri("p_eq", p.toStringID());
			ResultSet rs = executeSelectQuery(query.toString());
			int overlap = rs.next().getLiteral("overlap").getInt();
			
			// compute the estimated precision
			double precision = accuracy(candidatePopularity, overlap);
			
			// compute the estimated recall
			double recall = accuracy(popularity, overlap);
			
			// compute the final score
			double score = Heuristics.getFScore(recall, precision, beta);
			
			currentlyBestAxioms.add(
					new EvaluatedAxiom<OWLSubDataPropertyOfAxiom>(
							df.getOWLSubDataPropertyOfAxiom(propertyToDescribe, p), 
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
				"SELECT ?p_eq (COUNT(*) AS ?overlap) WHERE {"
				+ "?s ?p ?o; ?p_eq ?o . "
				+ "?p_dis a <http://www.w3.org/2002/07/owl#DatatypeProperty>; <http://www.w3.org/2000/01/rdf-schema#range> ?range .}"
				+ " GROUP BY ?p_eq");
		query.setIri("p", propertyToDescribe.toStringID());
		query.setIri("range", range.asOWLDatatype().toStringID());
		System.out.println(query.asQuery());
		ResultSet rs = executeSelectQuery(query.toString());
		ResultSetRewindable rsrw = ResultSetFactory.copyResults(rs);
	    int size = rsrw.size();
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			logger.info("Progress: " + format.format((double) rs.getRowNumber() / size));
			
			OWLDataProperty candidate = df.getOWLDataProperty(IRI.create(qs.getResource("p_eq").getURI()));
			
			// get the popularity of the candidate
			int candidatePopularity = reasoner.getPopularity(candidate);
			
			if(candidatePopularity == 0){// skip empty properties
				logger.warn("Cannot compute equivalence statements for empty candidate property " + candidate);
				continue;
			}
			
			// get the number of overlapping triples, i.e. triples with the same subject and object
			int overlap = rs.next().getLiteral("overlap").getInt();
			
			// compute the estimated precision
			double precision = accuracy(candidatePopularity, overlap);
			
			// compute the estimated recall
			double recall = accuracy(popularity, overlap);
			
			// compute the final score
			double score = fMEasure(precision, recall);
			
			currentlyBestAxioms.add(
					new EvaluatedAxiom<OWLSubDataPropertyOfAxiom>(
							df.getOWLSubDataPropertyOfAxiom(propertyToDescribe, candidate), 
							new AxiomScore(score)));
		}
		logger.info("Progress: 100%");
	}
	
	private void runSingleQueryMode(){
		int total = reasoner.getPopularity(propertyToDescribe);
		
		if(total > 0){
			String query = String.format("SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o. ?s ?p ?o.} GROUP BY ?p", propertyToDescribe.toStringID());
			ResultSet rs = executeSelectQuery(query);
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				OWLDataProperty prop = df.getOWLDataProperty(IRI.create(qs.getResource("p").getURI()));
				int cnt = qs.getLiteral("cnt").getInt();
				if(!prop.equals(propertyToDescribe)){
					currentlyBestAxioms.add(new EvaluatedAxiom<OWLSubDataPropertyOfAxiom>(
							df.getOWLSubDataPropertyOfAxiom(propertyToDescribe, prop), 
							computeScore(total, cnt)));
					
				}
			}
		}
	}
	
	private void runSPARQL1_0_Mode() {
		workingModel = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery  = "CONSTRUCT {?s ?p ?o.} WHERE {?s <%s> ?o. ?s ?p ?o.} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, propertyToDescribe.toStringID(), limit, offset);
		Model newModel = executeConstructQuery(query);
		while(!terminationCriteriaSatisfied() && newModel.size() != 0){
			workingModel.add(newModel);
			// get number of triples
			int all = (int)workingModel.size();
			
			if (all > 0) {
				// get class and number of instances
				query = "SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s ?p ?o.} GROUP BY ?p ORDER BY DESC(?cnt)";
				ResultSet rs = executeSelectQuery(query, workingModel);
				
				currentlyBestAxioms.clear();
				QuerySolution qs;
				OWLDataProperty prop;
				while(rs.hasNext()){
					qs = rs.next();
					prop = df.getOWLDataProperty(IRI.create(qs.get("p").asResource().getURI()));
					//omit property to describe as it is trivial
					if(prop.equals(propertyToDescribe)){
						continue;
					}
					currentlyBestAxioms.add(new EvaluatedAxiom<OWLSubDataPropertyOfAxiom>(
							df.getOWLSubDataPropertyOfAxiom(propertyToDescribe, prop),
							computeScore(all, qs.get("cnt").asLiteral().getInt())));
				}
				
			}
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.toStringID(), limit, offset);
			newModel = executeConstructQuery(query);
		}
	}
	
	@Override
	public Set<OWLDataPropertyAssertionAxiom> getPositiveExamples(EvaluatedAxiom<OWLSubDataPropertyOfAxiom> evAxiom) {
		OWLSubDataPropertyOfAxiom axiom = evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		// we assume a single atomic property
		OWLDataProperty superProperty = axiom.getSuperProperty().asOWLDataProperty();
		posExamplesQueryTemplate.setIri("p_sup", superProperty.toStringID());

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
	public Set<OWLDataPropertyAssertionAxiom> getNegativeExamples(EvaluatedAxiom<OWLSubDataPropertyOfAxiom> evAxiom) {
		OWLSubDataPropertyOfAxiom axiom = evAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		// we assume a single atomic property
		OWLDataProperty superProperty = axiom.getSuperProperty().asOWLDataProperty();
		negExamplesQueryTemplate.setIri("p_sup", superProperty.toStringID());

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
	
}
