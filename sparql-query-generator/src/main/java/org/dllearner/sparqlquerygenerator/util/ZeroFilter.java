package org.dllearner.sparqlquerygenerator.util;

public class ZeroFilter implements Filter{

	@Override
	public boolean isRelevantResource(String uri) {
		return true;
	}

}
