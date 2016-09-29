/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 * <p>
 * This file is part of DL-Learner.
 * <p>
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.qtl.operations.lgg;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import java.util.List;

/**
 * A generator of the Least General Generalization (LGG) for RDF query trees.
 *
 * @author Lorenz Bühmann
 */
public interface LGGGenerator {

	/**
	 * Returns the Least General Generalization of two RDF resource trees.
	 *
	 * @param tree1 the first tree
	 * @param tree2 the second tree
	 * @return the Least General Generalization
	 */
	default RDFResourceTree getLGG(RDFResourceTree tree1, RDFResourceTree tree2) {
		return getLGG(tree1, tree2, false);
	}

	/**
	 * Returns the Least General Generalization of two RDF resource trees. It can be forced to learn filters
	 * on literal values.
	 *
	 * @param tree1        the first tree
	 * @param tree2        the second tree
	 * @param learnFilters whether to learn filters on literal values
	 * @return the Least General Generalization
	 */
	RDFResourceTree getLGG(RDFResourceTree tree1, RDFResourceTree tree2, boolean learnFilters);

	/**
	 * Returns the Least General Generalization of a list of RDF resource trees.
	 *
	 * @param trees the trees
	 * @return the Least General Generalization
	 */
	default RDFResourceTree getLGG(List<RDFResourceTree> trees) {
		return getLGG(trees, false);
	}

	/**
	 * Returns the Least General Generalization of a list of RDF resource trees. It can be forced to learn filters
	 * on literal values.
	 *
	 * @param trees        the trees
	 * @param learnFilters whether to learn filters on literal values
	 * @return the Least General Generalization
	 */
	default RDFResourceTree getLGG(List<RDFResourceTree> trees, boolean learnFilters) {
		if(trees.isEmpty()) {
			throw new RuntimeException("LGG computation for empty set of trees.");
		}
		// if there is only 1 tree return it
		if (trees.size() == 1) {
			return trees.get(0);
		}

		RDFResourceTree lgg = trees.get(0);
		for (int i = 1; i < trees.size(); i++) {
			lgg = getLGG(lgg, trees.get(i), learnFilters);
		}
		return lgg;
	}
}
