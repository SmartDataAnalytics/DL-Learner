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
import org.dllearner.core.ConsoleAxiomLearningProgressMonitor;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import java.util.Set;
import java.util.TreeSet;

@ComponentAnn(name="data property domain axiom learner", shortName="dpldomain", version=0.1, description="A learning algorithm for data property domain axioms.")
public class DataPropertyDomainAxiomLearner extends DataPropertyAxiomLearner<OWLDataPropertyDomainAxiom> {

	private static final ParameterizedSparqlString POPULARITY_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?s ?p ?o .}");

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

	@ConfigOption(defaultValue = "false", description = "compute everything in a single SPARQL query")
	protected boolean batchMode = false;

	public DataPropertyDomainAxiomLearner(){
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s ?o WHERE {?s ?p ?o. ?s a ?type}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s ?o WHERE {?s ?p ?o. FILTER NOT EXISTS{?s a ?type}}");

		axiomType = AxiomType.DATA_PROPERTY_DOMAIN;
	}
	
	public DataPropertyDomainAxiomLearner(SparqlEndpointKS ks){
		this();
		this.ks = ks;
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

	protected int getPopularity() {
		POPULARITY_COUNT_QUERY.setIri("p", entityToDescribe.toStringID());
		String query = POPULARITY_COUNT_QUERY.toString();
		ResultSet rs = executeSelectQuery(query);
		int popularity = rs.next().getLiteral("cnt").getInt();
		return popularity;
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

	@Override
	protected void run(){
		if(batchMode) {
			runBatched();
		} else {
			runIterative();
		}
	}

	/**
	 * We can handle the domain axiom Domain(r, C) as a subclass of axiom \exists r.\top \sqsubseteq C
	 * 
	 * A = \exists r.\top
	 * B = C
	 */
	private void runIterative(){
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
	protected Set<OWLDataPropertyAssertionAxiom> getExamples(ParameterizedSparqlString queryTemplate,
															   EvaluatedAxiom<OWLDataPropertyDomainAxiom> evAxiom) {
		OWLDataPropertyDomainAxiom axiom = evAxiom.getAxiom();
		queryTemplate.setIri("p", entityToDescribe.toStringID());
		queryTemplate.setIri("type", axiom.getDomain().asOWLClass().toStringID());

		Set<OWLDataPropertyAssertionAxiom> examples = new TreeSet<>();

		ResultSet rs = executeSelectQuery(queryTemplate.toString());

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			OWLLiteral object = OwlApiJenaUtils.getOWLLiteral(qs.getLiteral("o"));
			examples.add(df.getOWLDataPropertyAssertionAxiom(entityToDescribe, subject, object));
		}

		return examples;
	}

	public void setBatchMode(boolean batchMode) {
		this.batchMode = batchMode;
	}

	public boolean isBatchMode() {
		return batchMode;
	}
	
	public static void main(String[] args) throws Exception{
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia());
		ks.init();

		DataPropertyDomainAxiomLearner l = new DataPropertyDomainAxiomLearner(ks);
		l.setEntityToDescribe(new OWLDataPropertyImpl(IRI.create("http://dbpedia.org/ontology/birthDate")));
		l.setUseSampling(false);
		l.setBatchMode(true);
		l.setUsePrecisionOnly(false);
		l.setProgressMonitor(new ConsoleAxiomLearningProgressMonitor());
		l.init();

		l.start();

		l.getCurrentlyBestEvaluatedAxioms(0.3).forEach(ax -> {
			System.out.println("---------------\n" + ax);
			l.getPositiveExamples(ax).stream().limit(5).forEach(System.out::println);
		});
	}

	
	

}
