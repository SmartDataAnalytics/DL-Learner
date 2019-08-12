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
package org.dllearner.utilities.datastructures;

import org.dllearner.core.AbstractSearchTreeNode;
import org.dllearner.core.Heuristic;

import java.util.Collection;

public class SearchTreeNonWeakPartialSet<T extends AbstractSearchTreeNode & WeakSearchTreeNode>
	extends SearchTreeNonWeak<T> implements SearchTreePartialSet<T> {

	public SearchTreeNonWeakPartialSet(Heuristic<T> heuristic) {
		super(heuristic);
	}

	@Override
	public void retainAll(Collection<T> promisingNodes) {
		this.nodes.retainAll(promisingNodes);
	}

}
