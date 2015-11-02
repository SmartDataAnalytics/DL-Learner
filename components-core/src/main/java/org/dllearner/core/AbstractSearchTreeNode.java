package org.dllearner.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dllearner.utilities.datastructures.AbstractSearchTree;
import org.dllearner.utilities.datastructures.SearchTreeNode;
import org.semanticweb.owlapi.model.OWLClassExpression;

public abstract class AbstractSearchTreeNode <T extends AbstractSearchTreeNode> implements SearchTreeNode {

	protected Set< AbstractSearchTree<T> > trees = new HashSet<>();
	protected T parent;
	protected List<T> children = new LinkedList<>();

	@Override
	public abstract OWLClassExpression getExpression();

	/**
	 * add a child node to this node
	 * @param node the child node
	 */
	public void addChild(T node) {
		node.setParent(this);
		children.add(node);
		node.notifyTrees(this.trees);
	}
	
	/**
	 * set the parent of this node
	 * @param node parent node
	 */
	protected void setParent(T node) {
		if (this.parent == node) {
			return;
		} else if (this.parent != null) {
			throw new Error("Parent already set on node");
		}
		this.parent = node;
	}

	/**
	 * internally used by the tree<->node contract to add this node to a set of trees
	 * @param trees the set of owning trees
	 */
	public void notifyTrees( Collection<? extends AbstractSearchTree<T>> trees ) {
		updatePrepareTree();
		this.trees.addAll(trees);
		notifyTree();
	}

	public void notifyTree( AbstractSearchTree<T> tree ) {
		updatePrepareTree();
		this.trees.add(tree);
		notifyTree();
	}
	
	private void notifyTree() {
		for(AbstractSearchTree<T> tree : trees) {
			tree.notifyNode((T)this);
		}
	}
	
	private void updatePrepareTree() {
		for(AbstractSearchTree<T> tree : trees) {
			tree.updatePrepare((T)this);
		}
	}

	/**
	 * @return the parent
	 */
	public T getParent() {
		return parent;
	}

	/**
	 * @return the children
	 */
	@Override
	public Collection<T> getChildren() {
		return children;
	}
}
