package org.dllearner.learningproblems.sampling.r2v;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class R2VProperty {
	
	private String uri;
	private R2VModel model;
	
	private TfidfIndex textIndex;

	public R2VProperty(R2VModel model, String uri) {
		super();
		this.model = model;
		this.uri = uri;
		this.textIndex = new TfidfIndex(this);
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

	public TfidfIndex getTextIndex() {
		return textIndex;
	}


}
