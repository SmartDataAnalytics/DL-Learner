package org.dllearner.learningproblems.sampling.r2v;

import java.util.HashMap;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class R2VFeature {
	
	private Double value;
	private R2VModel model;
	private R2VInstance instance;
	private R2VProperty property;
	private HashMap<String, R2VSubfeature> subfeatures;
	
	public R2VFeature(R2VModel model, R2VInstance instance, R2VProperty property) {
		super();
		this.model = model;
		this.instance = instance;
		this.property = property;
	}

	public Double getValue() {
		return value;
	}
	
	public void setValue(Double value) {
		this.value = value;
	}

	public R2VInstance getInstance() {
		return instance;
	}

	public R2VProperty getProperty() {
		return property;
	}

	public HashMap<String, R2VSubfeature> getSubfeatures() {
		return subfeatures;
	}

	public R2VModel getModel() {
		return model;
	}
	
}
