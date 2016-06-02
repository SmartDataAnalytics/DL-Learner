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

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.Filter;

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