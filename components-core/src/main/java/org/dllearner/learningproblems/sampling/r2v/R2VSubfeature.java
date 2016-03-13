package org.dllearner.learningproblems.sampling.r2v;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class R2VSubfeature {
	
	private Double value;
	private String name;
	private R2VFeature feature;
	
	public R2VSubfeature(R2VFeature feature, String name, Double value) {
		super();
		this.feature = feature;
		this.name = name;
		this.value = value;
	}
	
	public Double getValue() {
		return value;
	}
	
	public R2VFeature getFeature() {
		return feature;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	public String getName() {
		return name;
	}
	
}
