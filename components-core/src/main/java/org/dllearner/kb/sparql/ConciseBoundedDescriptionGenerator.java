package org.dllearner.kb.sparql;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;

public interface ConciseBoundedDescriptionGenerator {

	public Model getConciseBoundedDescription(String resourceURI);
	
	public Model getConciseBoundedDescription(String resourceURI, int depth);
	
	/**
	 * Returns the CBD of depth depth with additional information about the types of the leafs.
	 * @param resourceURI
	 * @param depth
	 * @param withTypesForLeafs
	 * @return
	 */
	public Model getConciseBoundedDescription(String resourceURI, int depth, boolean withTypesForLeafs);
	
	public void setRestrictToNamespaces(List<String> namespaces);
	
	public void setRecursionDepth(int maxRecursionDepth);
	
}
