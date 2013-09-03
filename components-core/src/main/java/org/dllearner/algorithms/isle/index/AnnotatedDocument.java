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
public interface AnnotatedDocument extends Document {
	
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

    /**
     * Returns the number of occurrences of the given entity in this document.
     *
     * @param entity the entity to get frequency for
     * @return number of occurrences of given entity in this document
     */
    int getEntityFrequency(Entity entity);
}
