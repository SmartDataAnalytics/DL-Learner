/**
 * 
 */
package org.dllearner.algorithms.properties;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class DataPropertyAxiomLearner<T extends OWLDataPropertyAxiom> extends AbstractAxiomLearningAlgorithm<T, OWLDataPropertyAssertionAxiom> {
	
	protected static final ParameterizedSparqlString TRIPLES_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(*) as ?cnt) WHERE {?s ?p ?o .}");
	
	protected static final ParameterizedSparqlString DISTINCT_SUBJECTS_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?s)) as ?cnt) WHERE {?s ?p ?o .}");
	
	protected static final ParameterizedSparqlString DISTINCT_OBJECTS_COUNT_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(DISTINCT(?o)) as ?cnt) WHERE {?s ?p ?o .}");
	
	protected ParameterizedSparqlString COUNT_QUERY = TRIPLES_COUNT_QUERY;
	
	protected OWLDataProperty propertyToDescribe;
	
	protected int popularity;
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		progressMonitor.learningStarted(this.getClass().getName());
		
		// get the popularity of the property
		popularity = getPropertyPopularity();

		// we have to skip here if there are not triples with the property
		if (popularity == 0) {
			logger.warn("Cannot compute statements for empty property " + propertyToDescribe);
			return;
		}

		run();
		
		progressMonitor.learningStopped();
	}
	
	protected int getPropertyPopularity(){
		return reasoner.getPopularity(propertyToDescribe);
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
	
	/**
	 * @return the propertyToDescribe
	 */
	public OWLDataProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}
	
	/**
	 * @param propertyToDescribe the propertyToDescribe to set
	 */
	public void setPropertyToDescribe(OWLDataProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
		
		posExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		negExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		
		COUNT_QUERY.setIri("p", propertyToDescribe.toStringID());
		DISTINCT_SUBJECTS_COUNT_QUERY.setIri("p", propertyToDescribe.toStringID());
		DISTINCT_OBJECTS_COUNT_QUERY.setIri("p", propertyToDescribe.toStringID());
	}

}
