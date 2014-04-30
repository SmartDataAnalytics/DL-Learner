package org.dllearner.algorithms.qtl.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dllearner.algorithms.qtl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.filters.Filter;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryImpl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.Statement;

public class QueryTreeCache {

	private Map<Model, QueryTree<String>> cache;
	private QueryTreeFactory<String> factory;
	private Model model;
	
	public QueryTreeCache(){
		cache = new HashMap<Model, QueryTree<String>>();
		factory = new QueryTreeFactoryImpl();
	}
	
	public QueryTreeCache(Model model){
		this.model = model;
		cache = new HashMap<Model, QueryTree<String>>();
		factory = new QueryTreeFactoryImpl();
	}
	
	public QueryTree<String> getQueryTree(String root){
		QueryTree<String> tree = cache.get(model);
		if(tree == null){
			tree = factory.getQueryTree(root, model);
		}
		return tree;
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
	
	public void setStatementFilter(com.hp.hpl.jena.util.iterator.Filter<Statement> filter){
		factory.setStatementFilter(filter);
	}
	
	public void setStatementSelector(Selector selector){
		factory.setStatementSelector(selector);
	}
	
	public void clear(){
		cache.clear();
	}
	
	public void dispose(){
		cache = null;
	}
	
	public void addAllowedNamespaces(Set<String> allowedNamespaces) {
		factory.addAllowedNamespaces(allowedNamespaces);
	}
}
