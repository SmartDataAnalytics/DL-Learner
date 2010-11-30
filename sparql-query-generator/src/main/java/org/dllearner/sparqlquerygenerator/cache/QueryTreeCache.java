package org.dllearner.sparqlquerygenerator.cache;

import java.util.HashMap;
import java.util.Map;

import org.dllearner.sparqlquerygenerator.QueryTreeFactory;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.impl.QueryTreeFactoryImpl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class QueryTreeCache {

	private Map<Model, QueryTree<?>> cache;
	private QueryTreeFactory<?> factory;
	
	public QueryTreeCache(){
		cache = new HashMap<Model, QueryTree<?>>();
		factory = new QueryTreeFactoryImpl();
	}
	
	public QueryTree<?> getQueryTree(String root, Model model){
		QueryTree<?> tree = cache.get(model);
		if(tree == null){
			tree = factory.getQueryTree(root, model);
		}
		return tree;
	}
	
	public QueryTree<?> getQueryTree(Resource root, Model model){
		QueryTree<?> tree = cache.get(model);
		if(tree == null){
			tree = factory.getQueryTree(root, model);
		}
		return tree;
	}
	
	public void clear(){
		cache.clear();
	}
	
	public void dispose(){
		cache = null;
	}
}
