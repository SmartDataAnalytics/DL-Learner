package org.dllearner.learningproblems.clustering;

import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class CeloePlusSampling {
	
	final static Logger logger = Logger.getLogger(CeloePlusSampling.class);
	
	private static enum Type {
		POS, NEG;
	}
	
	private String className;
	private HashMap<Type, Collection<OWLIndividual>> examples = new HashMap<>();
	
	private static CeloePlusSampling instance;
	
	protected CeloePlusSampling() {
		super();
	}
	
	public static CeloePlusSampling getInstance() {
		return (instance == null) ? instance = new CeloePlusSampling() : instance;
	}
	
	public void sample(String className, Collection<OWLIndividual> pos, Collection<OWLIndividual> neg) {
		
		this.className = className;
		this.examples.put(Type.POS, pos);
		this.examples.put(Type.NEG, neg);
		
		logger.info("CELOE+ sampling started.");
		
		// TODO
		
		// declare sparse vectors by instance
		
		// for each class
			// for each individual
				// get CBD
				// compute sparse vector
				// for each triple
					// check object type
					// string -> add to property index (property->index)
					// numeric/date -> add to sparse vectors
					// uri -> add to sparse vectors (boolean value)
		
		// compute indexes (word2vec or tf-idf)
		// for each property
			// ...
		
		// normalize values
		
	}
	
	public OWLIndividual nextPositive() {
		return next(Type.POS);
	}

	public OWLIndividual nextNegative() {
		return next(Type.NEG);
	}

	private OWLIndividual next(Type type) {
		// TODO Auto-generated method stub
		Collection<OWLIndividual> ex = examples.get(type);
		return null;
	}

}
