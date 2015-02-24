/**
 * 
 */
package org.dllearner.core.owl;

import java.util.SortedSet;

import org.semanticweb.owlapi.model.OWLObject;

/**
 * @author Lorenz Buehmann
 *
 */
public interface Hierarchy<T extends OWLObject> {

	public abstract SortedSet<T> getChildren(T entity);

	public abstract SortedSet<T> getChildren(T entity, boolean direct);
	
	public abstract SortedSet<T> getParents(T entity);

	public abstract SortedSet<T> getParents(T entity, boolean direct);

	public abstract SortedSet<T> getSiblings(T entity);

	public abstract boolean isChildOf(T entity1, T entity2);

	public abstract boolean isParentOf(T entity1, T entity2);

	public abstract SortedSet<T> getRoots();

	/**
	 * Checks whether the entity is contained in the hierarchy.
	 * @param entity
	 * @return
	 */
	public abstract boolean contains(T entity);

}