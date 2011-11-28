package org.dllearner.kb.sparql;

import com.hp.hpl.jena.rdf.model.Model;

public interface ConciseBoundedDescriptionGenerator {

	public Model getConciseBoundedDescription(String resourceURI);
	
	public Model getConciseBoundedDescription(String resourceURI, int depth);
}
