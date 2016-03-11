package org.dllearner.learningproblems.sampling.r2v;

//import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class R2VFeature {
	
	private R2VFeatureType type;
	
	private R2VModel model;
	private R2VInstance instance;
	private R2VProperty property;
	private HashMap<String, R2VSubfeature> subfeatures;
	private int count = 0;
//	private ArrayList<R2VSubfeature> subfeatures;
	
	public R2VFeature(R2VModel model, R2VInstance instance, R2VProperty property) {
		super();
		this.model = model;
		this.instance = instance;
		this.property = property;
		subfeatures = new HashMap<>();
//		subfeatures = new ArrayList<>();
	}

	public R2VInstance getInstance() {
		return instance;
	}

	public R2VProperty getProperty() {
		return property;
	}
	
	public void add(R2VSubfeature sub) {
		subfeatures.put(String.valueOf(count++), sub);
	}

	public HashMap<String, R2VSubfeature> getSubfeatures() {
		return subfeatures;
	}
	
//	public ArrayList<R2VSubfeature> getSubfeatures() {
//		return subfeatures;
//	}
	
	public R2VModel getModel() {
		return model;
	}

	public R2VFeatureType getType() {
		return type;
	}

	public void setType(R2VFeatureType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "R2VFeature [type=" + type + ", subfeatures=" + subfeatures + "]";
	}
	
}

enum R2VFeatureType {
	URI, STRING, NUMERICAL;
}
