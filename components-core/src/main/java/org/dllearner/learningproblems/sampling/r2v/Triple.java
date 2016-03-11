package org.dllearner.learningproblems.sampling.r2v;

import org.semanticweb.owlapi.model.OWLDatatype;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
class Triple {

	private String subjURI;
	private String propURI;
	private OWLDatatype datatype;
	private String value;

	public String getSubjURI() {
		return subjURI;
	}

	public void setSubjURI(String subjURI) {
		this.subjURI = subjURI;
	}

	public String getPropURI() {
		return propURI;
	}

	public void setPropURI(String propURI) {
		this.propURI = propURI;
	}

	public OWLDatatype getDatatype() {
		return datatype;
	}

	public void setDatatype(OWLDatatype datatype) {
		this.datatype = datatype;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean hasObjectProperty() {
		return datatype == null;
	}

	@Override
	public String toString() {
		return "Triple [subjURI=" + subjURI + ", propURI=" + propURI
				+ ", value=" + value + "]";
	}
	
}