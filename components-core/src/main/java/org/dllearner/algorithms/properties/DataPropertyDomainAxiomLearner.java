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

import org.apache.jena.query.*;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;

import java.util.Set;

@ComponentAnn(name="data property domain axiom learner", shortName="dpldomain", version=0.1, description="A learning algorithm for data property domain axioms.")
public class DataPropertyDomainAxiomLearner extends DataPropertyAxiomLearner<OWLDataPropertyDomainAxiom> {
	
	private static final ParameterizedSparqlString SUBJECTS_OF_TYPE_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?s ?p ?o; a ?type .}");
	private static final ParameterizedSparqlString SUBJECTS_OF_TYPE_WITH_INFERENCE_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?s ?p ?o; rdf:type/rdfs:subClassOf* ?type .}");
	private static final ParameterizedSparqlString SUBJECTS_OF_TYPE_COUNT_BATCHED_QUERY = new ParameterizedSparqlString(
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> SELECT ?type (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?s ?p ?o; a ?type . ?type a owl:Class .} GROUP BY ?type");
	private static final ParameterizedSparqlString SUBJECTS_OF_TYPE_WITH_INFERENCE_COUNT_BATCHED_QUERY = new ParameterizedSparqlString(
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> SELECT ?type (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?s ?p ?o; rdf:type/rdfs:subClassOf* ?type . ?type a owl:Class .} GROUP BY ?type");
	private static final ParameterizedSparqlString SAMPLE_QUERY = new ParameterizedSparqlString(
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> CONSTRUCT {?s ?p ?o; a ?cls . ?cls a owl:Class .} "
			+ "WHERE {?s ?p ?o . OPTIONAL {?s a ?cls . ?cls a owl:Class .}}");
	
	public DataPropertyDomainAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s WHERE {?s a ?type}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s WHERE {?s ?p ?o. FILTER NOT EXISTS{?s a ?type}}");
		
		axiomType = AxiomType.DATA_PROPERTY_DOMAIN;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		OWLClassExpression existingDomain = reasoner.getDomain(entityToDescribe);
		if(existingDomain != null){
			existingAxioms.add(df.getOWLDataPropertyDomainAxiom(entityToDescribe, existingDomain));
			if(reasoner.isPrepared()){
				if(reasoner.getClassHierarchy().contains(existingDomain)){
					for(OWLClassExpression sup : reasoner.getClassHierarchy().getSuperClasses(existingDomain, false)){
						existingAxioms.add(df.getOWLDataPropertyDomainAxiom(entityToDescribe, existingDomain));
						logger.info("Existing domain(inferred): " + sup);
					}
				}
				
			}
		}
	}
	
	@Override
	public void setEntityToDescribe(OWLDataProperty entityToDescribe) {
		super.setEntityToDescribe(entityToDescribe);
		
		DISTINCT_SUBJECTS_COUNT_QUERY.setIri("p", entityToDescribe.toStringID());
		SUBJECTS_OF_TYPE_COUNT_QUERY.setIri("p", entityToDescribe.toStringID());
		SUBJECTS_OF_TYPE_WITH_INFERENCE_COUNT_QUERY.setIri("p", entityToDescribe.toStringID());
		SUBJECTS_OF_TYPE_COUNT_BATCHED_QUERY.setIri("p", entityToDescribe.toStringID());
		SUBJECTS_OF_TYPE_WITH_INFERENCE_COUNT_BATCHED_QUERY.setIri("p", entityToDescribe.toStringID());
		SAMPLE_QUERY.setIri("p", entityToDescribe.toStringID());
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.PropertyAxiomLearner#getSampleQuery()
	 */
	@Override
	protected ParameterizedSparqlString getSampleQuery() {
		return SAMPLE_QUERY;
	}
	
	/**
	 * We can handle the domain axiom Domain(r, C) as a subclass of axiom \exists r.\top \sqsubseteq C
	 * 
	 * A = \exists r.\top
	 * B = C
	 */
	@Override
	protected void run(){
		// get the candidates
		Set<OWLClass> candidates = reasoner.getNonEmptyOWLClasses();
		
		// check for each candidate how often the subject belongs to it
		int i = 1;
		for (OWLClass candidate : candidates) {
			progressMonitor.learningProgressChanged(axiomType, i++, candidates.size());
			
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
			
			// compute score
			AxiomScore score = computeScore(popularity, cntB, cntAB);
			
			currentlyBestAxioms.add(
					new EvaluatedAxiom<>(
							df.getOWLDataPropertyDomainAxiom(entityToDescribe, candidate),
							score));
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
				progressMonitor.learningProgressChanged(axiomType, i++, size);
				
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
						new EvaluatedAxiom<>(
								df.getOWLDataPropertyDomainAxiom(entityToDescribe, candidate),
								new AxiomScore(score, useSampling)));
				
			}
		}
	}
	
	@Override
	public Set<OWLDataPropertyAssertionAxiom> getPositiveExamples(EvaluatedAxiom<OWLDataPropertyDomainAxiom> evAxiom) {
		OWLDataPropertyDomainAxiom axiom = evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("type", axiom.getDomain().asOWLClass().toStringID());
		return super.getPositiveExamples(evAxiom);
	}
	
	@Override
	public Set<OWLDataPropertyAssertionAxiom> getNegativeExamples(EvaluatedAxiom<OWLDataPropertyDomainAxiom> evAxiom) {
		OWLDataPropertyDomainAxiom axiom = evAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("type", axiom.getDomain().asOWLClass().toStringID());
		return super.getNegativeExamples(evAxiom);
	}
	
	public static void main(String[] args) throws Exception{
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		
		DataPropertyDomainAxiomLearner l = new DataPropertyDomainAxiomLearner(ks);
		l.setEntityToDescribe(new OWLDataPropertyImpl(IRI.create("http://dbpedia.org/ontology/AutomobileEngine/height")));
		l.setMaxExecutionTimeInSeconds(10);
		l.addFilterNamespace("http://dbpedia.org/ontology/");
//		l.setReturnOnlyNewAxioms(true);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5));
	}

	
	

}
