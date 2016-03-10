package org.dllearner.learningproblems.sampling;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class CELOEPlusSampling {
	
	final static Logger logger = Logger.getLogger(CELOEPlusSampling.class);
	
	/**
	 * Namespace of the individuals to be considered, discarding sameAs
	 * instances.
	 */
	private static final String NAMESPACE = "http://dbpedia.org/resource/";

	
	private static enum Type {
		POS, NEG;
	}
	
	private String className;
	private HashMap<Type, Collection<OWLIndividual>> examples = new HashMap<>();
	
	private static CELOEPlusSampling instance;
	
	protected CELOEPlusSampling() {
		super();
	}
	
	public static CELOEPlusSampling getInstance() {
		return (instance == null) ? instance = new CELOEPlusSampling() : instance;
	}
	
	public void sample(String className, Collection<OWLIndividual> pos, Collection<OWLIndividual> neg) {
		
		this.className = className;
		this.examples.put(Type.POS, pos);
		this.examples.put(Type.NEG, neg);
		
		logger.info("CELOE+ sampling started on class "+className);
		logger.warn("Namespace is hard-coded! Selection is limited to "+NAMESPACE);
		
		Collection<OWLIndividual> posF = nsFilter(pos);
		logger.info("|P| = " + posF.size() + "\t\tP = " + posF);
		Collection<OWLIndividual> negF = nsFilter(neg);
		logger.info("|N| = " + negF.size() + "\t\tN = " + negF);

		// TODO
		
		// declare sparse vectors by instance
		

		// for each class (pos/neg)
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
	
	/**
	 * Filter out all owl:sameAs instances and consider only the ones belonging to the namespace.
	 * 
	 * @param instances
	 * @return
	 */
	private Collection<OWLIndividual> nsFilter(Collection<OWLIndividual> instances) {
		
		Set<OWLIndividual> ind = new TreeSet<>();
		for (OWLIndividual i : instances) {
			if (i.isAnonymous())
				continue;
			if (i.asOWLNamedIndividual().getIRI().toString().startsWith(NAMESPACE))
				ind.add(i);
		}
		return ind;
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
