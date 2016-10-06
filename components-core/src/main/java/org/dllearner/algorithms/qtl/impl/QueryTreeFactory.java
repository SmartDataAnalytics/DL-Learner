/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
 */
package org.dllearner.algorithms.qtl.impl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import java.util.function.Predicate;

/**
 * @author Lorenz Buehmann
 *
 */
public interface QueryTreeFactory {

	/**
	 * Generates a query tree with the given resource as root and the edges based on the data contained in the model.
	 *
	 * @param resource the resource URI which is supposed to be the root of the query tree
	 * @param model the data
	 * @return the query tree
	 */
	default RDFResourceTree getQueryTree(String resource, Model model) {
		return getQueryTree(model.getResource(resource), model);
	}

	/**
	 * Generates a query tree with the given resource as root and the edges based on the data contained in the model.
	 *
	 * @param resource the resource which is supposed to be the root of the query tree
	 * @param model the data
	 * @return the query tree
	 */
	default RDFResourceTree getQueryTree(Resource resource, Model model) {
		return getQueryTree(resource, model, maxDepth());
	}

	/**
	 * Generates a query tree with the given resource as root and the edges based on the data contained in the model.
	 *
	 * @param resource the resource URI which is supposed to be the root of the query tree
	 * @param model the data
	 * @param maxDepth the maximum depth of the query tree
	 * @return the query tree
	 */
	default RDFResourceTree getQueryTree(String resource, Model model, int maxDepth) {
		return getQueryTree(model.getResource(resource), model, maxDepth);
	}

	/**
	 * Generates a query tree with the given resource as root and the edges based on the data contained in the model.
	 *
	 * @param resource the resource which is supposed to be the root of the query tree
	 * @param model the data
	 * @param maxDepth the maximum depth of the query tree
	 * @return the query tree
	 */
	RDFResourceTree getQueryTree(Resource resource, Model model, int maxDepth);

	/**
	 * @return the maximum depth of the generated query trees (Default: 3)
	 */
	default int maxDepth() {
		return 3;
	}

	/**
	 * @param maxDepth the maximum depth of the generated query trees
	 */
	void setMaxDepth(int maxDepth);

	/**
	 * Adds a set of filters that will be applied on the model before the creation of the query tree.
	 *
	 * @param dropFilters the filters
	 */
	@SuppressWarnings("unchecked")
	void addDropFilters(Predicate<Statement>... dropFilters);

}