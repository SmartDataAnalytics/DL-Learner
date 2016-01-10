package org.dllearner.algorithms.qtl.impl;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * @author Lorenz Buehmann
 *
 */
public interface QueryTreeFactory {

	/**
	 * @param maxDepth the maximum depth of the generated query trees.
	 */
	void setMaxDepth(int maxDepth);

	RDFResourceTree getQueryTree(String example, Model model);

	RDFResourceTree getQueryTree(Resource resource, Model model);

	RDFResourceTree getQueryTree(String example, Model model, int maxDepth);

	RDFResourceTree getQueryTree(Resource resource, Model model, int maxDepth);

	/**
	 * @param dropFilters the dropFilters to set
	 */
	void addDropFilters(Filter<Statement>... dropFilters);

}