/**
 * 
 */
package org.dllearner.algorithms;

import java.util.SortedSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import com.hp.hpl.jena.query.ParameterizedSparqlString;

/**
 * A wrapper class for CELOE that allows for returning the result in forms of OWL axioms.
 * @author Lorenz Buehmann
 *
 */
public class CELOEWrapper extends AbstractAxiomLearningAlgorithm<OWLClassAxiom, OWLIndividual, OWLClass> {
	
	private boolean equivalence = true;
	
	private static final ParameterizedSparqlString SAMPLE_QUERY = new ParameterizedSparqlString(
			"CONSTRUCT {?s a ?entity . ?s a ?cls . ?cls a <http://www.w3.org/2002/07/owl#Class> .} "
			+ "WHERE {?s a ?entity . OPTIONAL {?s a ?cls . ?cls a <http://www.w3.org/2002/07/owl#Class> . FILTER(!sameTerm(?cls, ?entity))}}");
	
	public CELOEWrapper(SparqlEndpointKS ks) {
		super.ks = ks;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		if(equivalence){
			SortedSet<OWLClassExpression> equivalentClasses = reasoner.getEquivalentClasses(entityToDescribe);
			for (OWLClassExpression equivCls : equivalentClasses) {
				existingAxioms.add(df.getOWLEquivalentClassesAxiom(entityToDescribe, equivCls));
			}
		} else {
			SortedSet<OWLClassExpression> superClasses = reasoner.getSuperClasses(entityToDescribe);
			for (OWLClassExpression supCls : superClasses) {
				existingAxioms.add(df.getOWLSubClassOfAxiom(entityToDescribe, supCls));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		progressMonitor.learningStarted(this.getClass().getName());
		
		// get the popularity of the class
		int popularity = reasoner.getPopularity(entityToDescribe);

		// we have to skip here if there are not instances for the given class
		if (popularity == 0) {
			logger.warn("Cannot compute statements for empty class " + entityToDescribe);
			return;
		}
		
		ConciseBoundedDescriptionGenerator cbdGenerator = new ConciseBoundedDescriptionGeneratorImpl(null);
		
		
		progressMonitor.learningStopped();
	}
	

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getSampleQuery()
	 */
	@Override
	protected ParameterizedSparqlString getSampleQuery() {
		return SAMPLE_QUERY;
	}

}
