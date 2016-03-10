package org.dllearner.learningproblems.sampling.r2v;

import java.util.HashMap;

import org.dllearner.learningproblems.sampling.strategy.FEXStrategy;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Resource2Vec model.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class R2VModel {
	
	private OWLOntology o;
	
	// TODO as many indexes (tf-idf) as properties
	private HashMap<String, R2VProperty> properties = new HashMap<>();
	private HashMap<String, R2VInstance> instances = new HashMap<>();
	
	private FEXStrategy strategy;

	public R2VModel(FEXStrategy strategy) {
		super();
		this.strategy = strategy;
//		this.o = OWLManager.createOWLOntologyManager().createOntology();
	}

	public FEXStrategy getStrategy() {
		return strategy;
	}
	
	private void index(String propertyURI, String objValue) {
		
	}
	
	public void add(OWLNamedIndividual ind) {
		// TODO get CBD, handle object cases

		
//		try {
//			o = m.loadOntologyFromOntologyDocument(IRI.create(file.toURI()));
//		} catch (OWLOntologyCreationException e) {
//			return null;
//		}
	}
	
	
}
