package org.dllearner.core.ref;

import java.util.List;

/**
 * @author Lorenz Buehmann
 *
 */
public interface SearchTreeNode<T>{
	
	T getData();
	
	SearchTreeNode<T> getParent();
	
	List<SearchTreeNode<T>> getChildren();

}
