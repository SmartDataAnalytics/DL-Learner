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
	private String stringValue;
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
	
	public void add(Double value) {
		String name = String.valueOf(count++);
		subfeatures.put(name, new R2VSubfeature(this, name, value));
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

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	
}

enum R2VFeatureType {
	URI, STRING, NUMERICAL;
}
