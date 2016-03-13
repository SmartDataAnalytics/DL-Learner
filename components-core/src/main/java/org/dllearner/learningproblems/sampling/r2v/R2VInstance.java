package org.dllearner.learningproblems.sampling.r2v;

import java.util.HashMap;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class R2VInstance {
	
	private String uri;
	private R2VModel model;
	private HashMap<R2VProperty, R2VFeature> features;

	public R2VInstance(R2VModel model, String uri) {
		super();
		this.model = model;
		this.uri = uri;
		this.features = new HashMap<>();
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
		HashMap<String, Double> vector = new HashMap<>();
		for(R2VFeature feature : features.values())
			for(R2VSubfeature subfeature : feature.getSubfeatures().values())
				vector.put(feature.getProperty().getUri() + "^^" + subfeature.getName(), subfeature.getValue());
		return vector;
	}

	@Override
	public String toString() {
		return "R2VInstance [uri=" + uri + ", features=" + features + "]";
	}
	
}
