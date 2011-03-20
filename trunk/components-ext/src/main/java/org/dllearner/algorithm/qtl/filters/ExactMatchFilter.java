package org.dllearner.algorithm.qtl.filters;

import java.util.Set;

public class ExactMatchFilter implements Filter{
	
	private Set<String> urisToFilter;
	
	public ExactMatchFilter(Set<String> urisToFilter){
		this.urisToFilter = urisToFilter;
	}

	@Override
	public boolean isRelevantResource(String uri) {
		return urisToFilter.contains(uri);
	}

}
