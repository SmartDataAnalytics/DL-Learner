/**
 * 
 */
package org.dllearner.algorithms.properties;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class ObjectPropertyAxiomLearner<T extends OWLObjectPropertyAxiom> extends AbstractAxiomLearningAlgorithm<T, OWLObjectPropertyAssertionAxiom> {
	
	protected OWLObjectProperty propertyToDescribe;
	
	protected int popularity;
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		progressMonitor.learningStarted(this.getClass().getName());
		// get the popularity of the property
		popularity = reasoner.getPopularity(propertyToDescribe);

		// we have to skip here if there are not triples with the property
		if (popularity == 0) {
			logger.warn("Cannot compute statements for empty property " + propertyToDescribe);
			return;
		}

		run();
		
		progressMonitor.learningStopped();
	}
	
	protected abstract void run();
	
	/**
	 * @return the propertyToDescribe
	 */
	public OWLObjectProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}
	
	/**
	 * @param propertyToDescribe the propertyToDescribe to set
	 */
	public void setPropertyToDescribe(OWLObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
		
		posExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		negExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
	}

}
