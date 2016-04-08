package org.dllearner.learningproblems.sampling;

import org.dllearner.core.Reasoner;
import org.dllearner.learningproblems.sampling.r2v.R2VModel;
import org.dllearner.learningproblems.sampling.strategy.TfidfFEXStrategy;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

//import org.dllearner.learningproblems.OntologyEngineering;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class CELOEPlusSampling {
	
	private final static Logger logger = LoggerFactory.getLogger(CELOEPlusSampling.class);
	
	/**
	 * Namespace of the individuals to be considered, discarding sameAs
	 * instances.
	 */
	private static final String NAMESPACE = 
//			"http://dbpedia.org/resource/";
			"http://www.biopax.org/release/";

	private static CELOEPlusSampling instance;
	
	private static enum Type {
		POS, NEG;
	}
	
	private HashMap<Type, Collection<OWLNamedIndividual>> examples = new HashMap<>();
	private HashMap<Type, Collection<OWLNamedIndividual>> cache = new HashMap<>();
	private HashMap<Type, OWLNamedIndividual> currents = new HashMap<>();
	
	private String className;
	private OWLOntology ont;
	private R2VModel model;
	
	protected CELOEPlusSampling() {
		super();
	}
	
	public String getClassName() {
		return className;
	}

	public static CELOEPlusSampling getInstance() {
		return (instance == null) ? instance = new CELOEPlusSampling() : instance;
	}
	
	public boolean sample(Reasoner rsnr, String className, Collection<OWLIndividual> pos, Collection<OWLIndividual> neg) {
		
		this.className = className;

		OWLAPIReasoner reasAPI = (OWLAPIReasoner) rsnr;
		ont = reasAPI.getOntology();
		
		
		logger.info("CELOE+ sampling started on class "+className);
//		logger.warn("Namespace is hard-coded! Selection is limited to "+NAMESPACE);
		
		Collection<OWLNamedIndividual> posF = nsFilter(pos);
		logger.info("|P| = " + posF.size() + "\t\tP = " + posF);
		Collection<OWLNamedIndividual> negF = nsFilter(neg);
		logger.info("|N| = " + negF.size() + "\t\tN = " + negF);
		this.examples.put(Type.POS, posF);
		this.examples.put(Type.NEG, negF);

		model = new R2VModel(ont, new TfidfFEXStrategy());
		
		// for each class (pos/neg)
		for(Type t : examples.keySet()) {
			logger.info("Processing type "+t.name());
			// add individuals to the model
			for(OWLNamedIndividual ind : examples.get(t))
				model.add(ind);
			// create caches
			cache.put(t, new TreeSet<>());
		}
		
		logger.info("Computing string features...");
		// compute string features according to FEX strategy
		model.stringFeatures();

		logger.info("Normalizing values...");
		// normalize values
		model.normalize();
		
		// print model info
		logger.info(model.info());
		
		if(posF.isEmpty() || negF.isEmpty())
			return false;
		
		return true;
	}
	
	/**
	 * Filter out all owl:sameAs instances and consider only the ones belonging to the namespace.
	 * 
	 * @param instances
	 * @return
	 */
	private Collection<OWLNamedIndividual> nsFilter(Collection<OWLIndividual> instances) {
		
		Set<OWLNamedIndividual> ind = new TreeSet<>();
		for (OWLIndividual i : instances) {
			if (i.isAnonymous())
				continue;
//			if (i.asOWLNamedIndividual().getIRI().toString().startsWith(NAMESPACE))
				ind.add(i.asOWLNamedIndividual());
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
		
		// method will return one element of this collection
		Collection<OWLNamedIndividual> points = examples.get(type);
		
		// get current element
		OWLNamedIndividual current = currents.get(type);
				
		logger.info("Current individual is "+current);
		
		// compute similarities and get farthest point
		OWLNamedIndividual farthest = null;
		Double max = Double.MIN_VALUE;
		for(OWLNamedIndividual ind : points) {
			if(ind != current && !cache.get(type).contains(ind)) {
				Double d;
				if(current != null)
					d = model.distance(current, ind);
				else
					d = model.distanceFromMeanPoint(ind);
				logger.trace("d("+current+", "+ind+") = "+d);
				if(d > max) {
					max = d;
					farthest = ind;
				}
			}
		}
		
		// update current
		currents.put(type, farthest);
		
		// add to cache, to avoid double visits
		if(farthest != null)
			cache.get(type).add(farthest);
		
		logger.info("Found farthest individual: "+farthest);
		
		return farthest;
	}
	

}
