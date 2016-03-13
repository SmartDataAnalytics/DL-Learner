package org.dllearner.learningproblems.sampling;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.Reasoner;
import org.dllearner.learningproblems.sampling.r2v.R2VModel;
import org.dllearner.learningproblems.sampling.strategy.TfidfFEXStrategy;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final String NAMESPACE = "http://dbpedia.org/resource/";
	
	private static enum Type {
		POS, NEG;
	}
	
	private String className;
	private OWLOntology ont;
	private HashMap<Type, Collection<OWLNamedIndividual>> examples = new HashMap<>();
	
	private static CELOEPlusSampling instance;
	
	protected CELOEPlusSampling() {
		super();
	}
	
	public String getClassName() {
		return className;
	}

	public static CELOEPlusSampling getInstance() {
		return (instance == null) ? instance = new CELOEPlusSampling() : instance;
	}
	
	public void sample(Reasoner rsnr, String className, Collection<OWLIndividual> pos, Collection<OWLIndividual> neg) {
		
		this.className = className;

		OWLAPIReasoner reasAPI = (OWLAPIReasoner) rsnr;
		ont = reasAPI.getOntology();
		
		
		logger.info("CELOE+ sampling started on class "+className);
		logger.warn("Namespace is hard-coded! Selection is limited to "+NAMESPACE);
		
		Collection<OWLNamedIndividual> posF = nsFilter(pos);
		logger.info("|P| = " + posF.size() + "\t\tP = " + posF);
		Collection<OWLNamedIndividual> negF = nsFilter(neg);
		logger.info("|N| = " + negF.size() + "\t\tN = " + negF);
		this.examples.put(Type.POS, posF);
		this.examples.put(Type.NEG, negF);

		// TODO
		
		R2VModel model = new R2VModel(ont, new TfidfFEXStrategy());
		
		// for each class (pos/neg)
		for(Type t : examples.keySet()) {
			
			logger.info("Processing type "+t.name());
			
			// for each individual
			for(OWLNamedIndividual ind : examples.get(t)) {
				model.add(ind);
			}
			
		}
		
		// compute string features according to FEX strategy
		model.stringFeatures();

		System.out.println(model);

		// normalize values
		model.normalize();
		
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
			if (i.asOWLNamedIndividual().getIRI().toString().startsWith(NAMESPACE))
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
//		Collection<OWLNamedIndividual> ex = examples.get(type);
		// TODO Auto-generated method stub
		// compute similarities and get farthest point
		return null;
	}

}
