package org.dllearner.autosparql.server.cache;

import com.hp.hpl.jena.rdf.model.Model;

public interface DBModelCache {
	
	Model getModel(String uri);
	
	Model getModel(String uri, int recursionDepth);

}
