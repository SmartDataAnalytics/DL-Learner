/**
 * 
 */
package org.dllearner.algorithms.properties;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.model.OWLEntity;
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
public abstract class PropertyAxiomLearner<S extends OWLProperty, T extends OWLLogicalAxiom, V extends OWLObject> extends AbstractAxiomLearningAlgorithm<T, V, S>{
	
	protected static final ParameterizedSparqlString TRIPLES_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(*) as ?cnt) WHERE {?s ?p ?o .}");
	
	protected static final ParameterizedSparqlString DISTINCT_SUBJECTS_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?s)) as ?cnt) WHERE {?s ?p ?o .}");
	
	protected static final ParameterizedSparqlString DISTINCT_OBJECTS_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?o)) as ?cnt) WHERE {?s ?p ?o .}");
	
	protected static final ParameterizedSparqlString GET_SAMPLE_QUERY = new ParameterizedSparqlString(
			"CONSTRUCT {?s ?p ?o.} WHERE {?s ?p ?o}");
	
	protected ParameterizedSparqlString COUNT_QUERY = TRIPLES_COUNT_QUERY;
	
	protected int popularity;
	
	protected boolean strictOWLMode = true;
	
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#setEntityToDescribe(org.semanticweb.owlapi.model.OWLEntity)
	 */
	@Override
	public void setEntityToDescribe(S entityToDescribe) {
		super.setEntityToDescribe(entityToDescribe);
		
		posExamplesQueryTemplate.setIri("p", entityToDescribe.toStringID());
		negExamplesQueryTemplate.setIri("p", entityToDescribe.toStringID());
		
		COUNT_QUERY.setIri("p", entityToDescribe.toStringID());
		DISTINCT_SUBJECTS_COUNT_QUERY.setIri("p", entityToDescribe.toStringID());
		DISTINCT_OBJECTS_COUNT_QUERY.setIri("p", entityToDescribe.toStringID());
	}
	
	/**
	 * @param strictOWLMode the strictOWLMode to set
	 */
	public void setStrictOWLMode(boolean strictOWLMode) {
		this.strictOWLMode = strictOWLMode;
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
			logger.warn("Cannot compute statements for empty property " + entityToDescribe);
			return;
		}
		
		run();
		
		progressMonitor.learningStopped();
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
