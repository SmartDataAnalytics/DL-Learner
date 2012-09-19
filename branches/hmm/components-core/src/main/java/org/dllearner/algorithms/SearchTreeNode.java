/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.algorithms;

import java.util.Collection;

import org.dllearner.core.owl.Description;

/**
 * Interface for search tree nodes, which are used in various algorithms.
 * 
 * @author Jens Lehmann
 *
 */
public interface SearchTreeNode {

	/**
	 * Gets the OWL 2 class expression at this search tree node.
	 * @return The expression at this node.
	 */
	public Description getExpression();
	
	/**
	 * The children of this node.
	 * @return The children of this node.
	 */
	public Collection<? extends SearchTreeNode> getChildren();
}
