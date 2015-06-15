/**
 * 
 */
package org.dllearner.core.ref;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lorenz Buehmann
 *
 */
public class SearchTreeNodeSimple<T> implements SearchTreeNode<T>{
	
	protected T data;
	protected SearchTreeNode<T> parent;
	protected List<SearchTreeNode<T>> children;

	public SearchTreeNodeSimple(T data, SearchTreeNode<T> parent, List<SearchTreeNode<T>> children) {
		this.data = data;
		this.parent = parent;
		this.children = children;
	}
	
	public SearchTreeNodeSimple(T data, SearchTreeNode<T> parent) {
		this(data, parent, new ArrayList<SearchTreeNode<T>>());
	}
	
	/**
	 * @return the data
	 */
	public T getData() {
		return data;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.ref.SearchTreeNode#getParent()
	 */
	@Override
	public SearchTreeNode<T> getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.ref.SearchTreeNode#getChildren()
	 */
	@Override
	public List<SearchTreeNode<T>> getChildren() {
		return children;
	}
	
	public boolean addChild(SearchTreeNode<T> child) {
		return children.add(child);
	}

}
