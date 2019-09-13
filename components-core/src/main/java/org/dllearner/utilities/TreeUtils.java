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
package org.dllearner.utilities;

import org.dllearner.algorithms.ocel.ExampleBasedNode;
import org.dllearner.core.AbstractSearchTreeNode;
import org.dllearner.core.Heuristic;
import org.dllearner.utilities.datastructures.AbstractSearchTree;

import javax.annotation.Nonnull;

public class TreeUtils {

	public static <T extends AbstractSearchTreeNode> String toTreeString(
			AbstractSearchTree<T> tree) {
		return TreeUtils.<T>toTreeString(tree.getRoot(), tree.getHeuristic());
	}
	public static  <T extends AbstractSearchTreeNode> String toTreeString(T node, Heuristic<T> heuristic) {
		return TreeUtils.toTreeString(node, heuristic, 0).toString();
	}
	public static <T extends AbstractSearchTreeNode> String getRefinementChainString(T node) {
		if(node.getParent()!=null) {
			String ret = getRefinementChainString(node.getParent());
			ret += " => " + node.getExpression().toString();
			return ret;
		} else {
			return node.getExpression().toString();
		}
	}

	private static <T extends AbstractSearchTreeNode> StringBuilder toTreeString(T node,
	                                          Heuristic<T> heuristic, int depth) {
		StringBuilder treeString = new StringBuilder();
		for(int i=0; i<depth-1; i++)
			treeString.append("  ");
		if(depth!=0)
			treeString.append("|--> ");
		treeString.append(node.toString())
				.append(addNodeScore(node, heuristic))
				.append("\n");
		for (Object child :
		     node.getChildren()) {
			treeString.append(TreeUtils.<T>toTreeString((T) child, heuristic,depth+1));
		}
		return treeString;
	}

	@Nonnull
	private static <T extends AbstractSearchTreeNode> String addNodeScore(T node, Heuristic<T> heuristic) {
		if (heuristic == null)
			return "";

		if (node instanceof ExampleBasedNode) {
			if (!((ExampleBasedNode) node).isQualityEvaluated()) {
				return "";
			}
			if (((ExampleBasedNode) node).isTooWeak()) {
				return "[score:too weak]";
			}
		}

		return "[score: " + heuristic.getNodeScore(node) + "]";
	}

}
