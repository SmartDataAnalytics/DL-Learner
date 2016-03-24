package org.dllearner.learningproblems.sampling.r2v;

import java.util.HashMap;

import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class R2VInstance {
	
	private OWLNamedIndividual ind;
	private String uri;
	private R2VModel model;
	private HashMap<R2VProperty, R2VFeature> features;
	
	private HashMap<String, Double> flatSparseVector;

	public R2VInstance(R2VModel model, OWLNamedIndividual ind) {
		super();
		this.model = model;
		this.ind = ind;
		this.uri = ind.toString();
		this.features = new HashMap<>();
	}

	public OWLNamedIndividual getIndividual() {
		return ind;
	}

	public String getUri() {
		return uri;
	}

	public HashMap<R2VProperty, R2VFeature> getFeatures() {
		return features;
	}

	public R2VModel getModel() {
		return model;
	}
	
	/**
	 * Flat sparse vector considers all subfeatures at the same level.
	 * 
	 * @return
	 */
	public HashMap<String, Double> getFlatSparseVector() {
		// return cached vector
		if(flatSparseVector != null)
			return flatSparseVector;
		// else, compute it
		flatSparseVector = new HashMap<>();
		for(R2VFeature feature : features.values())
			for(R2VSubfeature subfeature : feature.getSubfeatures().values())
				flatSparseVector.put(feature.getProperty().getUri() + "^^" + subfeature.getName(), subfeature.getNormValue());
		return flatSparseVector;
	}

	@Override
	public String toString() {
		return "R2VInstance [uri=" + uri + ", features=" + features + "]";
	}
	
}
