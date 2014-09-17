/**
 * 
 */
package org.dllearner.algorithms.properties;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLProperty;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class PropertyAxiomLearner<S extends OWLProperty, T extends OWLLogicalAxiom, V extends OWLObject> extends AbstractAxiomLearningAlgorithm<T, V>{
	
	protected static final ParameterizedSparqlString TRIPLES_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(*) as ?cnt) WHERE {?s ?p ?o .}");
	
	protected static final ParameterizedSparqlString DISTINCT_SUBJECTS_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?s)) as ?cnt) WHERE {?s ?p ?o .}");
	
	protected static final ParameterizedSparqlString DISTINCT_OBJECTS_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?o)) as ?cnt) WHERE {?s ?p ?o .}");
	
	protected static final ParameterizedSparqlString GET_SAMPLE_QUERY = new ParameterizedSparqlString(
			"CONSTRUCT {?s ?p ?o.} WHERE {?s ?p ?o}");
	
	protected ParameterizedSparqlString COUNT_QUERY = TRIPLES_COUNT_QUERY;
	
	protected S propertyToDescribe;
	
	protected int popularity;
	
	protected boolean useSample = true;
	
	protected boolean strictOWLMode = true;
	
	/**
	 * @param propertyToDescribe the propertyToDescribe to set
	 */
	public void setPropertyToDescribe(S propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
		
		posExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		negExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		
		COUNT_QUERY.setIri("p", propertyToDescribe.toStringID());
		DISTINCT_SUBJECTS_COUNT_QUERY.setIri("p", propertyToDescribe.toStringID());
		DISTINCT_OBJECTS_COUNT_QUERY.setIri("p", propertyToDescribe.toStringID());
	}
	
	/**
	 * @param strictOWLMode the strictOWLMode to set
	 */
	public void setStrictOWLMode(boolean strictOWLMode) {
		this.strictOWLMode = strictOWLMode;
	}
	
	/**
	 * @return the propertyToDescribe
	 */
	public S getPropertyToDescribe() {
		return propertyToDescribe;
	}
	
	protected ParameterizedSparqlString getSampleQuery(){
		return GET_SAMPLE_QUERY;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		progressMonitor.learningStarted(this.getClass().getName());
		
		// get the popularity of the property
		popularity = getPropertyPopularity();

		// we have to skip here if there are not triples having the property as predicate
		if (popularity == 0) {
			logger.warn("Cannot compute statements for empty property " + propertyToDescribe);
			return;
		}
		
		if(useSample){
			logger.debug("Using sample mode.");
			
			// we have to set up a new query execution factory working on our local model
			QueryExecutionFactory globalQef = qef;
			sample = ModelFactory.createDefaultModel();
			qef = new QueryExecutionFactoryModel(sample);
			SPARQLReasoner globalReasoner = reasoner;
			reasoner = new SPARQLReasoner(qef, false);
			
			// get the page size 
			//TODO put to base class
			long pageSize = 10000;//PaginationUtils.adjustPageSize(globalQef, 10000);
			
			ParameterizedSparqlString sampleQueryTemplate = getSampleQuery();
			sampleQueryTemplate.setIri("p", propertyToDescribe.toStringID());
			Query query = sampleQueryTemplate.asQuery();
			query.setLimit(pageSize);
			
			boolean isEmpty = false;
			int i = 0;
			while(!isTimeout() && !isEmpty){
				
				// get next sample
				logger.debug("Extending sample...");
				query.setOffset(i++ * pageSize);
				QueryExecution qe = globalQef.createQueryExecution(query);
				Model tmp = qe.execConstruct();
				sample.add(tmp);
				
				// if last call returned empty model, we can leave loop
				isEmpty = tmp.isEmpty();
				
				// recompute popularity
				popularity = getPropertyPopularity();
				
				// compute the axioms in each run to ensure any-time property of algorithm
//				run();
			}
			run();
		} else {
			run();
		}
		
		progressMonitor.learningStopped();
	}
	
	/**
	 * @param useSample the useSample to set
	 */
	public void setUseSample(boolean useSample) {
		this.useSample = useSample;
	}
	
	protected int getPropertyPopularity(){
		return getCountValue(COUNT_QUERY.toString());
	}
	
	protected int getPropertyPopularity(Model model){
		return getCountValue(COUNT_QUERY.toString(), model);
	}
	
	protected int getDistinctSubjectsFrequency(){
		return getCountValue(DISTINCT_SUBJECTS_COUNT_QUERY.toString());
	}
	
	protected int getDistinctObjectsFrequency(){
		return getCountValue(DISTINCT_OBJECTS_COUNT_QUERY.toString());
	}
	
	protected int getCountValue(String query){
		ResultSet rs = executeSelectQuery(query);
		return rs.next().getLiteral("cnt").getInt();
	}
	
	/**
	 * Return the integer value of a SPARQL query that just returns a single COUNT value.
	 * It is assumed the the variable of the COUNT value is ?cnt.
	 * @param query
	 * @param model
	 * @return
	 */
	protected int getCountValue(String query, Model model){
		ResultSet rs = executeSelectQuery(query, model);
		return rs.next().getLiteral("cnt").getInt();
	}
	
	protected abstract void run();

}
