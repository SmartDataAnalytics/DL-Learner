package org.dllearner.kb.repository.bioportal;

import java.util.ArrayList;
import java.util.List;

public class Data {
	List<OntologyBean> list = new ArrayList<OntologyBean>();

	public void setOntologyBeans(List<OntologyBean> list) {
		this.list = list;
	}

	public List<OntologyBean> getOntologyBeans() {
		return list;
	}
}
