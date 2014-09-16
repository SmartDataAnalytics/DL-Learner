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

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

@ComponentAnn(name="objectproperty range learner", shortName="oplrange", version=0.1)
public class ObjectPropertyRangeAxiomLearner extends ObjectPropertyAxiomLearner<OWLObjectPropertyRangeAxiom> {
	
	private static final ParameterizedSparqlString DISTINCT_OBJECTS_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?o)) as ?cnt) WHERE {?s ?p ?o .}");
	
	private static final ParameterizedSparqlString OBJECTS_OF_TYPE_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?o)) AS ?cnt) WHERE {?s ?p ?o . ?o a ?type .}");
	private static final ParameterizedSparqlString OBJECTS_OF_TYPE_WITH_INFERENCE_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?o)) AS ?cnt) WHERE {?s ?p ?o . ?o rdf:type/rdfs:subClassOf* ?type .}");
	
	private static final ParameterizedSparqlString OBJECTS_OF_TYPE_COUNT_BATCHED_QUERY = new ParameterizedSparqlString(
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> SELECT ?type (COUNT(DISTINCT(?o)) AS ?cnt) WHERE {?s ?p ?o . ?o a ?type . ?type a owl:Class .} GROUP BY ?type");
	private static final ParameterizedSparqlString OBJECTS_OF_TYPE_WITH_INFERENCE_COUNT_BATCHED_QUERY = new ParameterizedSparqlString(
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> SELECT ?type (COUNT(DISTINCT(?o)) AS ?cnt) WHERE {?s ?p ?o . ?o rdf:type/rdfs:subClassOf* ?type . ?type a owl:Class .} GROUP BY ?type");
	
	// a property range axiom can formally be seen as a subclass axiom \top \sqsubseteq \forall r.C 
	// so we have to focus more on accuracy, which we can regulate via the parameter beta
	double beta = 3.0;
	
	public ObjectPropertyRangeAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?o ?p ?s. ?s a ?type .}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?o ?p ?s. FILTER NOT EXISTS {?s a ?type}}");
		
		COUNT_QUERY = DISTINCT_OBJECTS_COUNT_QUERY;
		
		axiomType = AxiomType.OBJECT_PROPERTY_RANGE;
	}

	public void setPropertyToDescribe(OWLObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
		
		DISTINCT_OBJECTS_COUNT_QUERY.setIri("p", propertyToDescribe.toStringID());
		OBJECTS_OF_TYPE_COUNT_QUERY.setIri("p", propertyToDescribe.toStringID());
		OBJECTS_OF_TYPE_WITH_INFERENCE_COUNT_QUERY.setIri("p", propertyToDescribe.toStringID());
		OBJECTS_OF_TYPE_COUNT_BATCHED_QUERY.setIri("p", propertyToDescribe.toStringID());
		OBJECTS_OF_TYPE_WITH_INFERENCE_COUNT_BATCHED_QUERY.setIri("p", propertyToDescribe.toStringID());
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		OWLClassExpression existingRange = reasoner.getRange(propertyToDescribe);
		if (existingRange != null) {
			existingAxioms.add(df.getOWLObjectPropertyRangeAxiom(propertyToDescribe, existingRange));
			logger.info("Existing range: " + existingRange);
			if (reasoner.isPrepared()) {
				if (reasoner.getClassHierarchy().contains(existingRange)) {
					for (OWLClassExpression sup : reasoner.getClassHierarchy().getSuperClasses(existingRange)) {
						existingAxioms.add(df.getOWLObjectPropertyRangeAxiom(propertyToDescribe, existingRange));
						logger.info("Existing range(inferred): " + sup);
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.PropertyAxiomLearner#getSampleQuery()
	 */
	@Override
	protected ParameterizedSparqlString getSampleQuery() {
		return new ParameterizedSparqlString(
				"PREFIX owl:<http://www.w3.org/2002/07/owl#> "
				+ "CONSTRUCT "
				+ "{?s ?p ?o . ?o a ?cls . "
				+ (strictOWLMode ? "?cls a owl:Class . " : "")
				+ "} "
				+ "WHERE "
				+ "{?s ?p ?o . ?o a ?cls . "
				+ (strictOWLMode ? "?cls a owl:Class . " : "")
				+ "}");
	}
	
	/**
	 * We can handle the domain axiom Domain(r, C) as a subclass of axiom \exists r.\top \sqsubseteq C
	 */
	@Override
	protected void run(){
		// get the candidates
		Set<OWLClass> candidates = reasoner.getNonEmptyOWLClasses();
		
		// check for each candidate how often the subject belongs to it
		int i = 1;
		for (OWLClass candidate : candidates) {
			progressMonitor.learningProgressChanged(i++, candidates.size());
			
			//get total number of instances of B
			int cntB = reasoner.getPopularity(candidate);
			
			if(cntB == 0){// skip empty properties
				logger.debug("Cannot compute range statements for empty candidate class " + candidate);
				continue;
			}
			
			//get number of instances of (A AND B)
			OBJECTS_OF_TYPE_COUNT_QUERY.setIri("type", candidate.toStringID());
			int cntAB = executeSelectQuery(OBJECTS_OF_TYPE_COUNT_QUERY.toString()).next().getLiteral("cnt").getInt();
			logger.debug("Candidate:" + candidate + "\npopularity:" + cntB + "\noverlap:" + cntAB);
			
			//precision (A AND B)/B
			double precision = Heuristics.getConfidenceInterval95WaldAverage(cntB, cntAB);
			
			//recall (A AND B)/A
			double recall = Heuristics.getConfidenceInterval95WaldAverage(popularity, cntAB);
			
			//F score
			double score = Heuristics.getFScore(recall, precision, beta);
			
			currentlyBestAxioms.add(
					new EvaluatedAxiom<OWLObjectPropertyRangeAxiom>(
							df.getOWLObjectPropertyRangeAxiom(propertyToDescribe, candidate), 
							new AxiomScore(score, useSample)));
		}
	}
	
	/**
	 * We can handle the domain axiom Domain(r, C) as a subclass of axiom \exists r.\top \sqsubseteq C
	 */
	private void runBatched(){
		
		// we can compute the popularity of the properties once which can avoid sending several single 
		// query later on
		reasoner.precomputeClassPopularity();
		
		// get for each subject type the frequency
		ResultSet rs = executeSelectQuery(OBJECTS_OF_TYPE_COUNT_BATCHED_QUERY.toString());
		ResultSetRewindable rsrw = ResultSetFactory.copyResults(rs);
		int size = rsrw.size();
		rsrw.reset();
		int i = 1;
		while(rsrw.hasNext()){
			QuerySolution qs = rsrw.next();
			if(qs.getResource("type").isURIResource()){
				progressMonitor.learningProgressChanged(i++, size);
				
				OWLClass candidate = df.getOWLClass(IRI.create(qs.getResource("type").getURI()));
				
				//get total number of instances of B
				int cntB = reasoner.getPopularity(candidate);
				
				//get number of instances of (A AND B)
				int cntAB = qs.getLiteral("cnt").getInt();
				
				//precision (A AND B)/B
				double precision = Heuristics.getConfidenceInterval95WaldAverage(cntB, cntAB);
				
				//recall (A AND B)/A
				double recall = Heuristics.getConfidenceInterval95WaldAverage(popularity, cntAB);
				
				//F score
				double score = Heuristics.getFScore(recall, precision, beta);
				
				currentlyBestAxioms.add(
						new EvaluatedAxiom<OWLObjectPropertyRangeAxiom>(
								df.getOWLObjectPropertyRangeAxiom(propertyToDescribe, candidate), 
								new AxiomScore(score, useSample)));
				
			}
		}
	}
	
	private void runSingleQueryMode(){
		
		String query = String.format("SELECT (COUNT(DISTINCT ?o) AS ?cnt) WHERE {?s <%s> ?o.}", propertyToDescribe.toStringID());
		ResultSet rs = executeSelectQuery(query);
		int nrOfSubjects = rs.next().getLiteral("cnt").getInt();
		
		query = String.format("SELECT ?type (COUNT(DISTINCT ?o) AS ?cnt) WHERE {?s <%s> ?o . ?o a ?type . ?type a owl:Class} GROUP BY ?type", propertyToDescribe.toStringID());
		rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("type") != null){
				OWLClass range = df.getOWLClass(IRI.create(qs.getResource("type").getURI()));
				int cnt = qs.getLiteral("cnt").getInt();
				currentlyBestAxioms.add(new EvaluatedAxiom<OWLObjectPropertyRangeAxiom>(
						df.getOWLObjectPropertyRangeAxiom(propertyToDescribe, range), computeScore(nrOfSubjects, cnt)));
			} 
		}
	}

	private void runSPARQL1_0_Mode() {
		workingModel = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery  = "CONSTRUCT {?s <%s> ?o . ?o a ?type .} WHERE {?s <%s> ?o. ?o a ?type .} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, propertyToDescribe.toStringID(), propertyToDescribe.toStringID(), limit, offset);
		Model newModel = executeConstructQuery(query);
		while(!terminationCriteriaSatisfied() && newModel.size() != 0){
			workingModel.add(newModel);
			// get number of distinct subjects
			query = "SELECT (COUNT(?o) AS ?all) WHERE {?s ?p ?o.}";
			ResultSet rs = executeSelectQuery(query, workingModel);
			QuerySolution qs;
			int all = 1;
			while (rs.hasNext()) {
				qs = rs.next();
				all = qs.getLiteral("all").getInt();
			}
			
			// get class and number of instances
//			query = "SELECT (DATATYPE(?o) AS ?dt) (COUNT(?o) AS ?cnt) WHERE{?s ?p ?o} GROUP BY DATATYPE(?o) ORDER BY DESC(?cnt)";
			query = "SELECT ?type (COUNT(?o) AS ?cnt) " +
					"WHERE {?s ?p ?o . ?o a ?type .} GROUP BY ?type";
			rs = executeSelectQuery(query, workingModel);
			
			if (all > 0) {
				currentlyBestAxioms.clear();
				while(rs.hasNext()){
					qs = rs.next();
					Resource type = qs.get("type").asResource();
					currentlyBestAxioms.add(new EvaluatedAxiom<OWLObjectPropertyRangeAxiom>(
							df.getOWLObjectPropertyRangeAxiom(propertyToDescribe, df.getOWLClass(IRI.create(type.getURI()))),
							computeScore(all, qs.get("cnt").asLiteral().getInt())));
				}
				
			}
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.toStringID(), propertyToDescribe.toStringID(), limit, offset);
			newModel = executeConstructQuery(query);
		}
	}
	
	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getPositiveExamples(EvaluatedAxiom<OWLObjectPropertyRangeAxiom> evAxiom) {
		OWLObjectPropertyRangeAxiom axiom = evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("type", axiom.getRange().asOWLClass().toStringID());
		return super.getPositiveExamples(evAxiom);
	}
	
	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getNegativeExamples(EvaluatedAxiom<OWLObjectPropertyRangeAxiom> evAxiom) {
		OWLObjectPropertyRangeAxiom axiom = evAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("type", axiom.getRange().asOWLClass().toStringID());
		return super.getNegativeExamples(evAxiom);
	}
}
