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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.dllearner.algorithms.refexamples.ExampleBasedNode;
import org.dllearner.algorithms.refexamples.NodeComparatorStable;

/**
 * A tree model used for displaying example based nodes. A search tree
 * can become very large, so lazy loading should be used, i.e. never
 * load the full search tree but only the nodes selected by the user.
 * Note that the search tree changes over time.
 * 
 * @author Jens Lehmann
 *
 */
public class EBNodeTreeModel implements TreeModel {

	// root of the search tree
	private ExampleBasedNode rootNode;
	
	// a mapping from nodes to their children;
	// the main problem is that example based nodes use sets instead
	// of lists, so we need to convert these sets to lists and store 
	// them here
	private Map<ExampleBasedNode,List<ExampleBasedNode>> childrenMap = new TreeMap<ExampleBasedNode,List<ExampleBasedNode>>(new NodeComparatorStable());
	
	// listeners for this model
    private List<TreeModelListener> treeModelListeners =
        new LinkedList<TreeModelListener>();	
	
	public EBNodeTreeModel(ExampleBasedNode rootNode) {
		this.rootNode = rootNode;
	}
	
	public void addTreeModelListener(TreeModelListener l) {
		treeModelListeners.add(l);
	}

	public Object getChild(Object parent, int index) {
		return getChildren((ExampleBasedNode)parent).get(index);
	}

	public int getChildCount(Object parent) {
		return ((ExampleBasedNode)parent).getChildren().size();
	}

	public int getIndexOfChild(Object parent, Object child) {
		return getChildren((ExampleBasedNode)parent).indexOf(child);
	}

	public Object getRoot() {
		return rootNode;
	}

	public boolean isLeaf(Object node) {
		return (getChildCount(node)==0);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.remove(l);
	}

	public void valueForPathChanged(TreePath path, Object newValue) {

	}

    private void fireTreeStructureChanged(ExampleBasedNode node) {
        TreeModelEvent e = new TreeModelEvent(this, 
                                              new Object[] {node});
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeStructureChanged(e);
        }
    }	
	
	// convert the set of children to a list and store it in this model
	private List<ExampleBasedNode> getChildren(ExampleBasedNode node) {
		List<ExampleBasedNode> children = childrenMap.get(node);
		// if the children have not been cached or the list is outdated
		// (node has more children now) we do an update
		if(children == null || children.size() != node.getChildren().size()) {
			children = new LinkedList<ExampleBasedNode>(node.getChildren());
			childrenMap.put(node, children);
			fireTreeStructureChanged(node);
		}
		return children;
	}
	
}
