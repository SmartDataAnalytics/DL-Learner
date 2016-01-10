package org.dllearner.algorithms.qtl.impl;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class QueryTreeFactoryCache implements QueryTreeFactory {
	
//	private static final long size = 0;
//
//	LoadingCache<String, RDFResourceTree> cache = CacheBuilder.newBuilder().
//			maximumSize(size)
//			.build(
//			new CacheLoader<String, RDFResourceTree>() {
//				@Override
//				public RDFResourceTree load(String resource) throws Exception {
//					return delegatee.getQueryTree(resource, model);
//				}
//	});

	private QueryTreeFactory delegatee;

	public QueryTreeFactoryCache(QueryTreeFactory delegatee) {
		this.delegatee = delegatee;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.impl.QueryTreeFactory#setMaxDepth(int)
	 */
	@Override
	public void setMaxDepth(int maxDepth) {
		delegatee.setMaxDepth(maxDepth);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.impl.QueryTreeFactory#getQueryTree(java.lang.String, com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	public RDFResourceTree getQueryTree(String example, Model model) {
		return delegatee.getQueryTree(example, model);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.impl.QueryTreeFactory#getQueryTree(com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	public RDFResourceTree getQueryTree(Resource resource, Model model) {
		return delegatee.getQueryTree(resource, model);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.impl.QueryTreeFactory#getQueryTree(java.lang.String, com.hp.hpl.jena.rdf.model.Model, int)
	 */
	@Override
	public RDFResourceTree getQueryTree(String example, Model model, int maxDepth) {
		return delegatee.getQueryTree(model.getResource(example), model, maxDepth);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.impl.QueryTreeFactory#getQueryTree(com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.rdf.model.Model, int)
	 */
	@Override
	public RDFResourceTree getQueryTree(Resource resource, Model model, int maxDepth) {
		return delegatee.getQueryTree(resource, model, maxDepth);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.impl.QueryTreeFactory#addDropFilters(com.hp.hpl.jena.util.iterator.Filter)
	 */
	@Override
	public void addDropFilters(Filter<Statement>... dropFilters) {
		delegatee.addDropFilters(dropFilters);
	}
}
