/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import java.util.Set;

import org.dllearner.core.owl.Entity;

/**
 * @author Lorenz Buehmann
 *
 */
public interface AnnotatedDocument {
	
	/**
	 * Returns a set of entities which are contained in the document.
	 * @return
	 */
	Set<Entity> getContainedEntities();
	
	/**
	 * Returns all annotations of the document.
	 * @return
	 */
	Set<Annotation> getAnnotations();
	
	/**
	 * Returns the annotation at the given position(offset) of given length.
	 * @param offset
	 * @param length
	 * @return
	 */
	Annotation getAnnotation(int offset, int length);

}
