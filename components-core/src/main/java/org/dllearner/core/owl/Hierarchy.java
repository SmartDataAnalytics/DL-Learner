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