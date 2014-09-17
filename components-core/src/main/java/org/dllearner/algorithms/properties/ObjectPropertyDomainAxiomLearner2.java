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
import org.dllearner.utilities.owl.OWLClassExpressionToSPARQLConverter;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;

@ComponentAnn(name="objectproperty domain axiom learner", shortName="opldomain", version=0.1)
public class ObjectPropertyDomainAxiomLearner2 extends ObjectPropertyAxiomLearner<OWLObjectPropertyDomainAxiom> {
	
	private static final ParameterizedSparqlString DISTINCT_SUBJECTS_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?s)) as ?cnt) WHERE {?s ?p ?o .}");
	private static final ParameterizedSparqlString SUBJECTS_OF_TYPE_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?s ?p ?o; a ?type .}");
	private static final ParameterizedSparqlString SUBJECTS_OF_TYPE_WITH_INFERENCE_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?s ?p ?o; rdf:type/rdfs:subClassOf* ?type .}");
	private static final ParameterizedSparqlString SUBJECTS_OF_TYPE_COUNT_BATCHED_QUERY = new ParameterizedSparqlString(
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> SELECT ?type (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?s ?p ?o; a ?type . ?type a owl:Class .} GROUP BY ?type");
	private static final ParameterizedSparqlString SUBJECTS_OF_TYPE_WITH_INFERENCE_COUNT_BATCHED_QUERY = new ParameterizedSparqlString(
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> SELECT ?type (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?s ?p ?o; rdf:type/rdfs:subClassOf* ?type . ?type a owl:Class .} GROUP BY ?type");
	
	// a property domain axiom can formally be seen as a subclass axiom \exists r.\top \sqsubseteq \C 
	// so we have to focus more on accuracy, which we can regulate via the parameter beta
	double beta = 3.0;
	
	public ObjectPropertyDomainAxiomLearner2(SparqlEndpointKS ks){
		this.ks = ks;
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s WHERE {?s a ?type}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s WHERE {?s ?p ?o. FILTER NOT EXISTS{?s a ?type}}");
	
		COUNT_QUERY = DISTINCT_SUBJECTS_COUNT_QUERY;
		
		axiomType = AxiomType.OBJECT_PROPERTY_DOMAIN;
	}

	@Override
	public void setPropertyToDescribe(OWLObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
//		negExamplesQueryTemplate.clearParams();
//		posExamplesQueryTemplate.clearParams();
		
		DISTINCT_SUBJECTS_COUNT_QUERY.setIri("p", propertyToDescribe.toStringID());
		SUBJECTS_OF_TYPE_COUNT_QUERY.setIri("p", propertyToDescribe.toStringID());
		SUBJECTS_OF_TYPE_WITH_INFERENCE_COUNT_QUERY.setIri("p", propertyToDescribe.toStringID());
		SUBJECTS_OF_TYPE_COUNT_BATCHED_QUERY.setIri("p", propertyToDescribe.toStringID());
		SUBJECTS_OF_TYPE_WITH_INFERENCE_COUNT_BATCHED_QUERY.setIri("p", propertyToDescribe.toStringID());
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.PropertyAxiomLearner#getSampleQuery()
	 */
	@Override
	protected ParameterizedSparqlString getSampleQuery() {
		return new ParameterizedSparqlString(
				"PREFIX owl:<http://www.w3.org/2002/07/owl#> "
				+ "CONSTRUCT "
				+ "{?s ?p ?o; a ?cls1 . "
				+ (strictOWLMode ? "?cls1 a owl:Class. " : "")
				+ "} "
				+ "WHERE "
				+ "{?s ?p ?o; a ?cls1 . "
				+ (strictOWLMode ? "?cls1 a owl:Class. " : "")
				+ "}");
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		OWLClassExpression existingDomain = reasoner.getDomain(propertyToDescribe);
		logger.info("Existing domain: " + existingDomain);
		if(existingDomain != null){
			existingAxioms.add(df.getOWLObjectPropertyDomainAxiom(propertyToDescribe, existingDomain));
			if(reasoner.isPrepared()){
				if(reasoner.getClassHierarchy().contains(existingDomain)){
					for(OWLClassExpression sup : reasoner.getClassHierarchy().getSuperClasses(existingDomain)){
						existingAxioms.add(df.getOWLObjectPropertyDomainAxiom(propertyToDescribe, existingDomain));
						logger.info("Existing domain(inferred): " + sup);
					}
				}
			}
		}
	}
	
	/**
	 * We can handle the domain axiom Domain(r, C) as a subclass of axiom \exists r.\top \sqsubseteq C
	 * 
	 * A = \exists r.\top
	 * B = C
	 */
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
				logger.debug("Cannot compute domain statements for empty candidate class " + candidate);
				continue;
			}
			
			//get number of instances of (A AND B)
			SUBJECTS_OF_TYPE_COUNT_QUERY.setIri("type", candidate.toStringID());
			int cntAB = executeSelectQuery(SUBJECTS_OF_TYPE_COUNT_QUERY.toString()).next().getLiteral("cnt").getInt();
			logger.debug("Candidate:" + candidate + "\npopularity:" + cntB + "\noverlap:" + cntAB);
			
			//precision (A AND B)/B
			double precision = Heuristics.getConfidenceInterval95WaldAverage(cntB, cntAB);
			
			//recall (A AND B)/A
			double recall = Heuristics.getConfidenceInterval95WaldAverage(popularity, cntAB);
			
			//F score
			double score = Heuristics.getFScore(recall, precision, beta);
			
			currentlyBestAxioms.add(
					new EvaluatedAxiom<OWLObjectPropertyDomainAxiom>(
							df.getOWLObjectPropertyDomainAxiom(propertyToDescribe, candidate), 
							new AxiomScore(score, useSample)));
		}
	}
	
	/**
	 * We can handle the domain axiom Domain(r, C) as a subclass of axiom \exists r.\top \sqsubseteq C
	 */
	private void runBatched(){
		
		reasoner.precomputeClassPopularity();
		
		// get for each subject type the frequency
		ResultSet rs = executeSelectQuery(SUBJECTS_OF_TYPE_COUNT_BATCHED_QUERY.toString());
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
						new EvaluatedAxiom<OWLObjectPropertyDomainAxiom>(
								df.getOWLObjectPropertyDomainAxiom(propertyToDescribe, candidate), 
								new AxiomScore(score, useSample)));
				
			}
		}
	}
	
	private void computeScore(Set<OWLObjectPropertyDomainAxiom> axioms){
		OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();
		for (OWLObjectPropertyDomainAxiom axiom : axioms) {
			OWLSubClassOfAxiom sub = axiom.asOWLSubClassOfAxiom();
			String subClassQuery = converter.convert("?s", sub.getSubClass());
		}
	}
	
	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getPositiveExamples(EvaluatedAxiom<OWLObjectPropertyDomainAxiom> evAxiom) {
		OWLObjectPropertyDomainAxiom axiom = evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("type", axiom.getDomain().asOWLClass().toStringID());
		return super.getPositiveExamples(evAxiom);
	}
	
	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getNegativeExamples(EvaluatedAxiom<OWLObjectPropertyDomainAxiom> evAxiom) {
		OWLObjectPropertyDomainAxiom axiom = evAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("type", axiom.getDomain().asOWLClass().toStringID());
		return super.getNegativeExamples(evAxiom);
	}
	
}
