package org.dllearner.algorithm.qtl.cache;

import java.util.HashMap;
import java.util.Map;

import org.dllearner.algorithm.qtl.util.ModelGenerator;
import org.dllearner.algorithm.qtl.util.ModelGenerator.Strategy;

import com.hp.hpl.jena.rdf.model.Model;

public class ModelCache {
	
	private Map<String, Model> cache;
	private ModelGenerator modelGen;
	
	private int recursionDepth = 2;
	
	
	public ModelCache(ModelGenerator modelGen){
		this.modelGen = modelGen;
		
		cache = new HashMap<String, Model>();
	}
	
	public Model getModel(String uri){
		Model model = cache.get(uri);
		if(model == null){
			model = modelGen.createModel(uri, Strategy.CHUNKS, recursionDepth);
			cache.put(uri, model);
		}
		return cache.get(uri);
	}
	
	public void setRecursionDepth(int recursionDepth){
		this.recursionDepth = recursionDepth;
	}
	
	public void clear(){
		cache.clear();
	}
	
	public void dispose(){
		cache = null;
	}

}
