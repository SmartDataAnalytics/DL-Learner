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

@ComponentAnn(name="object property range learner", shortName="oplrange", version=0.1, description="A learning algorithm for object property range axioms.")
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

	private boolean useSimpleScore = true;
	
	public ObjectPropertyRangeAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?o ?p ?s. ?s a ?type .}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?o ?p ?s. FILTER NOT EXISTS {?s a ?type}}");
		
		COUNT_QUERY = DISTINCT_OBJECTS_COUNT_QUERY;
		
		axiomType = AxiomType.OBJECT_PROPERTY_RANGE;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.PropertyAxiomLearner#setEntityToDescribe(org.semanticweb.owlapi.model.OWLProperty)
	 */
	@Override
	public void setEntityToDescribe(OWLObjectProperty entityToDescribe) {
		super.setEntityToDescribe(entityToDescribe);
		
		DISTINCT_OBJECTS_COUNT_QUERY.setIri("p", entityToDescribe.toStringID());
		OBJECTS_OF_TYPE_COUNT_QUERY.setIri("p", entityToDescribe.toStringID());
		OBJECTS_OF_TYPE_WITH_INFERENCE_COUNT_QUERY.setIri("p", entityToDescribe.toStringID());
		OBJECTS_OF_TYPE_COUNT_BATCHED_QUERY.setIri("p", entityToDescribe.toStringID());
		OBJECTS_OF_TYPE_WITH_INFERENCE_COUNT_BATCHED_QUERY.setIri("p", entityToDescribe.toStringID());
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		OWLClassExpression existingRange = reasoner.getRange(entityToDescribe);
		if (existingRange != null) {
			existingAxioms.add(df.getOWLObjectPropertyRangeAxiom(entityToDescribe, existingRange));
			logger.info("Existing range: " + existingRange);
			if (reasoner.isPrepared()) {
				if (reasoner.getClassHierarchy().contains(existingRange)) {
					for (OWLClassExpression sup : reasoner.getClassHierarchy().getSuperClasses(existingRange)) {
						existingAxioms.add(df.getOWLObjectPropertyRangeAxiom(entityToDescribe, existingRange));
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
			logger.debug("Candidate:" + candidate);
			progressMonitor.learningProgressChanged(axiomType, i++, candidates.size());
			
			//get total number of instances of B
			int cntB = reasoner.getPopularity(candidate);
			logger.debug("Popularity:" + cntB);
			
			if(cntB == 0){// skip empty properties
				logger.debug("Cannot compute range statements for empty candidate class " + candidate);
				continue;
			}
			
			//get number of instances of (A AND B)
			OBJECTS_OF_TYPE_COUNT_QUERY.setIri("type", candidate.toStringID());
			int cntAB = executeSelectQuery(OBJECTS_OF_TYPE_COUNT_QUERY.toString()).next().getLiteral("cnt").getInt();
			logger.debug("Candidate:" + candidate + "\npopularity:" + cntB + "\noverlap:" + cntAB);
			
			// compute score
			AxiomScore score = computeScore(popularity, cntB, cntAB);
						
			currentlyBestAxioms.add(
					new EvaluatedAxiom<>(
							df.getOWLObjectPropertyRangeAxiom(entityToDescribe, candidate),
							score));
		}
	}

	private AxiomScore computeScore(int cntA, int cntB, int cntAB) {
		// precision (A AND B)/B
		double precision = Heuristics.getConfidenceInterval95WaldAverage(cntB, cntAB);
		
		// in the simplest case, the precision is our score
		double score = precision;
		
		// if enabled consider also recall and use F-score
		if(!useSimpleScore ) {
			// recall (A AND B)/A
			double recall = Heuristics.getConfidenceInterval95WaldAverage(popularity, cntAB);
			
			// F score
			score = Heuristics.getFScore(recall, precision, beta);
		}
		
		int nrOfPosExamples = cntAB;
		
		int nrOfNegExamples = popularity - cntAB;
		
		return new AxiomScore(score, score, nrOfPosExamples, nrOfNegExamples, useSampling);
	}
	
	/**
	 * We can handle the domain axiom Domain(r, C) as a subclass of axiom \exists r.\top \sqsubseteq C
	 */
	private void runBatched(){
		
		// we can compute the popularity of the properties once which can avoid sending several single 
		// query later on
		reasoner.precomputeClassPopularity();
		
		// get for each object type the frequency
		ResultSet rs = executeSelectQuery(OBJECTS_OF_TYPE_COUNT_BATCHED_QUERY.toString());
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
								df.getOWLObjectPropertyRangeAxiom(entityToDescribe, candidate),
								new AxiomScore(score, useSampling)));
				
			}
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
