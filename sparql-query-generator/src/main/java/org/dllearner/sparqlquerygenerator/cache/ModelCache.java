package org.dllearner.sparqlquerygenerator.cache;

import java.util.HashMap;
import java.util.Map;

import org.dllearner.sparqlquerygenerator.util.ModelGenerator;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator.Strategy;

import com.hp.hpl.jena.rdf.model.Model;

public class ModelCache {
	
	private Map<String, Model> cache;
	private ModelGenerator modelGen;
	
	
	public ModelCache(ModelGenerator modelGen){
		this.modelGen = modelGen;
		
		cache = new HashMap<String, Model>();
	}
	
	public Model getModel(String uri){
		Model model = cache.get(uri);
		if(model == null){
			model = modelGen.createModel(uri, Strategy.CHUNKS, 2);
			cache.put(uri, model);
		}
		return cache.get(uri);
	}
	
	public void clear(){
		cache.clear();
	}
	
	public void dispose(){
		cache = null;
	}

}
