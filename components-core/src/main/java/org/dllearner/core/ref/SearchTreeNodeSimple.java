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
		this(data, parent, new ArrayList<>());
	}
	
	/**
	 * @return the data
	 */
	@Override
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
