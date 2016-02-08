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
package org.dllearner.core.owl;

import java.util.SortedSet;

import org.semanticweb.owlapi.model.OWLObject;

/**
 * @author Lorenz Buehmann
 *
 */
public interface Hierarchy<T extends OWLObject> {

	SortedSet<T> getChildren(T entity);

	SortedSet<T> getChildren(T entity, boolean direct);
	
	SortedSet<T> getParents(T entity);

	SortedSet<T> getParents(T entity, boolean direct);

	SortedSet<T> getSiblings(T entity);

	boolean isChildOf(T entity1, T entity2);

	boolean isParentOf(T entity1, T entity2);

	SortedSet<T> getRoots();

	/**
	 * Checks whether the entity is contained in the hierarchy.
	 * @param entity the entity
	 * @return whether the entity is contained in the hierarchy or not
	 */
	boolean contains(T entity);

}