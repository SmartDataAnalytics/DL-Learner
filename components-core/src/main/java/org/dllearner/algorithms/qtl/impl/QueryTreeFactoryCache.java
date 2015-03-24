/**
 * Copyright (C) 2007-2010, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.algorithms.qtl.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.util.NamespaceDropStatementFilter;
import org.dllearner.algorithms.qtl.util.PredicateDropStatementFilter;
import org.dllearner.algorithms.qtl.util.StopURIsDBpedia;
import org.dllearner.algorithms.qtl.util.StopURIsOWL;
import org.dllearner.algorithms.qtl.util.StopURIsRDFS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class QueryTreeFactoryCache implements QueryTreeFactory {

	private Set<Filter<Statement>> dropFilters = new HashSet<Filter<Statement>>();
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
