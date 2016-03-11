package org.dllearner.learningproblems.sampling.r2v;

import java.util.HashMap;
import java.util.Set;

import org.dllearner.learningproblems.sampling.strategy.FEXStrategy;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
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
			
	private OWLOntology ont;
	
	// TODO as many indexes (tf-idf) as properties
	private HashMap<String, R2VProperty> properties = new HashMap<>();
	private HashMap<String, R2VInstance> instances = new HashMap<>();
	
	private FEXStrategy strategy;

	public R2VModel(OWLOntology ont, FEXStrategy strategy) {
		super();
		this.ont = ont;
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
		Set<OWLIndividualAxiom> cbd = ont.getAxioms(ind, Imports.INCLUDED);
		logger.info(cbd.toString());
		
		// compute sparse vector
		
		// for each triple
			// check object type
			// string -> add to property index (property->index)
			// numeric/date -> add to sparse vectors
			// uri -> add to sparse vectors (boolean value)

		
//		try {
//			o = m.loadOntologyFromOntologyDocument(IRI.create(file.toURI()));
//		} catch (OWLOntologyCreationException e) {
//			return null;
//		}
	}
	
	
}
