/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
package org.dllearner.gui;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.dllearner.algorithms.SearchTreeNode;
import org.dllearner.algorithms.ocel.ExampleBasedNode;

/**
 * A tree model used for displaying example based nodes. A search tree can
 * become very large, so lazy loading should be used, i.e. never load the full
 * search tree but only the nodes selected by the user. Note that the search
 * tree changes over time.
 * 
 * @author Jens Lehmann
 * 
 */
public class EBNodeTreeModel implements TreeModel {

	// root of the search tree
	private SearchTreeNode rootNode;

	// a mapping from nodes to their children;
	// the main problem is that example based nodes use sets instead
	// of lists, so we need to convert these sets to lists and store
	// them here
	private Map<SearchTreeNode, List<SearchTreeNode>> childrenMap;
	// = new TreeMap<SearchTreeNode, List<SearchTreeNode>>(
	//		new NodeComparatorStable());

	private Comparator<SearchTreeNode> nodeComparator;
	
	// listeners for this model
	private List<TreeModelListener> treeModelListeners = new LinkedList<TreeModelListener>();

	public EBNodeTreeModel(SearchTreeNode rootNode, Comparator<SearchTreeNode> comparator) {
		this.rootNode = rootNode;
		this.nodeComparator = comparator;
		childrenMap = new TreeMap<SearchTreeNode, List<SearchTreeNode>>(comparator);
				// new NodeComparatorStable());
	}

	public void addTreeModelListener(TreeModelListener l) {
		treeModelListeners.add(l);
	}

	public Object getChild(Object parent, int index) {
		return getChildren((SearchTreeNode) parent).get(index);
	}

	public int getChildCount(Object parent) {
		return ((SearchTreeNode) parent).getChildren().size();
	}

	public int getIndexOfChild(Object parent, Object child) {
		return getChildren((SearchTreeNode) parent).indexOf(child);
	}

	public Object getRoot() {
		return rootNode;
	}

	public boolean isLeaf(Object node) {
		return (getChildCount(node) == 0);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.remove(l);
	}

	public void valueForPathChanged(TreePath path, Object newValue) {

	}

	@SuppressWarnings("unused")
	private void fireTreeStructureChanged(ExampleBasedNode node) {
		TreeModelEvent e = new TreeModelEvent(this, new Object[] { node });
		for (TreeModelListener tml : treeModelListeners) {
			tml.treeStructureChanged(e);
		}
	}

	// convert the set of children to a list and store it in this model
	private List<SearchTreeNode> getChildren(SearchTreeNode node) {
		// System.out.println("asking for children of " + node);

		List<SearchTreeNode> children = childrenMap.get(node);
		// if the children have not been cached or the list is outdated
		// (node has more children now) we do an update
		if (children == null || children.size() != node.getChildren().size()) {
			SortedSet<SearchTreeNode> childrenSet = new TreeSet<SearchTreeNode>(nodeComparator);
			childrenSet.addAll(node.getChildren()); 
			children = new LinkedList<SearchTreeNode>(childrenSet);

			// we need to ensure that the children are sorted correctly
			// children = new LinkedList<ExampleBasedNode>();
			// for(ExampleBasedNode child : childrenSet) {
			// children.add(child);
			// }

			childrenMap.put(node, children);

			// fireTreeStructureChanged(node);
			// System.out.println("updating children of " + node);
		}
		return children;
	}

}
