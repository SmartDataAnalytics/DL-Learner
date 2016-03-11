package org.dllearner.learningproblems.sampling.r2v;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.learningproblems.sampling.strategy.FEXStrategy;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource2Vec model.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class R2VModel {
	
	private final static Logger logger = LoggerFactory.getLogger(R2VModel.class);
	
	private OWLOntology o;
	
	// TODO as many indexes (tf-idf) as properties
	private HashMap<String, R2VProperty> properties = new HashMap<>();
	private HashMap<String, R2VInstance> instances = new HashMap<>();
	
	private FEXStrategy strategy;

	public R2VModel(OWLOntology ontology, FEXStrategy strategy) {
		super();
		this.o = ontology;
		this.strategy = strategy;
	}

	public FEXStrategy getStrategy() {
		return strategy;
	}
	
	private void index(String propertyURI, String objValue) {
		
	}
	
	public void add(OWLNamedIndividual ind) {
		
		logger.info("Processing individual "+ind);

		// get CBD
		Set<OWLAxiom> cbd = new HashSet<>();
		cbd.addAll(o.getAnnotationAssertionAxioms(ind.getIRI()));
		cbd.addAll(o.getDataPropertyAssertionAxioms(ind));
		cbd.addAll(o.getObjectPropertyAssertionAxioms(ind));
		
		logger.info("CBD size = "+cbd.size());
				
		// compute sparse vector
		
		// for each triple
		for(OWLAxiom axiom : cbd) {
			logger.info(axiom.toString());
			// check object type
			// string -> add to property index (property->index)
			// numeric/date -> add to sparse vectors
			// uri -> add to sparse vectors (boolean value)
		}
		
//		try {
//			o = m.loadOntologyFromOntologyDocument(IRI.create(file.toURI()));
//		} catch (OWLOntologyCreationException e) {
//			return null;
//		}
	}
	
	
}
