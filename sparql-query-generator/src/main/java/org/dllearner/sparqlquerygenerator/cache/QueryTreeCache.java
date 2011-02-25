package org.dllearner.sparqlquerygenerator.cache;

import java.util.HashMap;
import java.util.Map;

import org.dllearner.sparqlquerygenerator.QueryTreeFactory;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.impl.QueryTreeFactoryImpl;
import org.dllearner.sparqlquerygenerator.util.Filter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;

public class QueryTreeCache {

	private Map<Model, QueryTree<String>> cache;
	private QueryTreeFactory<String> factory;
	
	public QueryTreeCache(){
		cache = new HashMap<Model, QueryTree<String>>();
		factory = new QueryTreeFactoryImpl();
	}
	
	public QueryTree<String> getQueryTree(String root, Model model){
		QueryTree<String> tree = cache.get(model);
		if(tree == null){
			tree = factory.getQueryTree(root, model);
		}
		return tree;
	}
	
	public QueryTree<String> getQueryTree(Resource root, Model model){
		QueryTree<String> tree = cache.get(model);
		if(tree == null){
			tree = factory.getQueryTree(root, model);
		}
		return tree;
	}
	
	public void setPredicateFilter(Filter filter){
		factory.setPredicateFilter(filter);
	}
	
	public void setObjectFilter(Filter filter){
		factory.setObjectFilter(filter);
	}
	
	public void setStatementFilter(Selector filter){
		factory.setStatementFilter(filter);
	}
	
	public void clear(){
		cache.clear();
	}
	
	public void dispose(){
		cache = null;
	}
}
