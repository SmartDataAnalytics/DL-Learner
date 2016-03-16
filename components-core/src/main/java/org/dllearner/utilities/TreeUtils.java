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

import org.dllearner.core.AbstractSearchTreeNode;
import org.dllearner.utilities.datastructures.AbstractSearchTree;

public class TreeUtils {

	public static String toTreeString(
			AbstractSearchTree<? extends AbstractSearchTreeNode<? extends AbstractSearchTreeNode>> tree) {
		return TreeUtils.toTreeString(tree.getRoot());
	}
	public static String toTreeString(AbstractSearchTreeNode<? extends AbstractSearchTreeNode> node) {
		return TreeUtils.toTreeString(node, 0).toString();
	}
	public static String getRefinementChainString(AbstractSearchTreeNode<? extends AbstractSearchTreeNode> node) {
		if(node.getParent()!=null) {
			String ret = getRefinementChainString(node.getParent());
			ret += " => " + node.getExpression().toString();
			return ret;
		} else {
			return node.getExpression().toString();
		}
	}

	private static StringBuilder toTreeString(AbstractSearchTreeNode<? extends AbstractSearchTreeNode> node,
			int depth) {
		StringBuilder treeString = new StringBuilder();
		for(int i=0; i<depth-1; i++)
			treeString.append("  ");
		if(depth!=0)
			treeString.append("|--> ");
		treeString.append(node.toString()).append("\n");
		for(AbstractSearchTreeNode child : node.getChildren()) {
			treeString.append(TreeUtils.toTreeString(child, depth+1));
		}
		return treeString;
	}

}
