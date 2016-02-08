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

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Lorenz Buehmann
 *
 */
public class SearchTree<S, T extends SearchTreeNode<S>> {
	
	private SortedSet<T> tree;
	
	public SearchTree(Comparator<T> comparator) {
		tree = new TreeSet<>(comparator);
	}
	
	public boolean addNode(T node) {
		return tree.add(node);
	}
	
	public boolean removeNode(T node) {
		return tree.remove(node);
	}
	
	public SortedSet<T> getNodes() {
		return tree;
	}

}
