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

	@Override
	public String toString() {
		return "R2VInstance [uri=" + uri + ", features=" + features + "]";
	}

	
}
