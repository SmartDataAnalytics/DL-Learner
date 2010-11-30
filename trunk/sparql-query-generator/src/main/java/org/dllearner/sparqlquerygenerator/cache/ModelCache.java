package org.dllearner.sparqlquerygenerator.cache;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;

public class ModelCache {
	
	private Map<String, Model> cache;
	
	public ModelCache(){
		cache = new HashMap<String, Model>();
	}
	
	public Model getModel(String uri){
		return cache.get(uri);
	}
	
	public void clear(){
		cache.clear();
	}

}
