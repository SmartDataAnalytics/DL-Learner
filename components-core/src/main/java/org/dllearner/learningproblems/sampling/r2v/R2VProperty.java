package org.dllearner.learningproblems.sampling.r2v;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class R2VProperty {
	
	private String uri;
	private R2VModel model;

	public R2VProperty(R2VModel model, String uri) {
		super();
		this.model = model;
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	public R2VModel getModel() {
		return model;
	}

	@Override
	public String toString() {
		return "R2VProperty [uri=" + uri + "]";
	}

}
