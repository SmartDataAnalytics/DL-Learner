package org.dllearner.kb.sparql;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;

public interface ConciseBoundedDescriptionGenerator {

	public Model getConciseBoundedDescription(String resourceURI);
	
	public Model getConciseBoundedDescription(String resourceURI, int depth);
	
	public void setRestrictToNamespaces(List<String> namespaces);
	
	public void setRecursionDepth(int maxRecursionDepth);
	
}
