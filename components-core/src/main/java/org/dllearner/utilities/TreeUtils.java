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

import java.util.Map;

import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.utilities.datastructures.AbstractSearchTree;

public class TreeUtils {

	public static String toTreeString(AbstractSearchTree<OENode> tree,
			String baseURI, Map<String, String> prefixes) {
		return TreeUtils.toTreeString(tree.getRoot(), baseURI, prefixes);
	}
	public static String toTreeString(OENode node,
			String baseURI, Map<String, String> prefixes) {
		return TreeUtils.toTreeString(node, 0, baseURI, prefixes).toString();
	}

	private static StringBuilder toTreeString(OENode node,
			int depth, String baseURI, Map<String, String> prefixes) {
		StringBuilder treeString = new StringBuilder();
		for(int i=0; i<depth-1; i++)
			treeString.append("  ");
		if(depth!=0)
			treeString.append("|--> ");
		treeString.append(node.getShortDescription(baseURI, prefixes)).append("\n");
		for(OENode child : node.getChildren()) {
			treeString.append(TreeUtils.toTreeString(child, depth+1, baseURI, prefixes));
		}
		return treeString;
	}

}
