/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import org.dllearner.core.owl.Entity;

import java.io.Serializable;
import java.util.Set;

/**
 * @author Lorenz Buehmann
 *
 */
public interface AnnotatedDocument extends Document, Serializable{
	
	/**
	 * Returns a set of entities which are contained in the document.
	 * @return
	 */
	Set<Entity> getContainedEntities();
	
	/**
	 * Returns all annotations of the document.
	 * @return
	 */
	Set<SemanticAnnotation> getAnnotations();
	
    /**
     * Returns the number of occurrences of the given entity in this document.
     *
     * @param entity the entity to get frequency for
     * @return number of occurrences of given entity in this document
     */
    int getEntityFrequency(Entity entity);
}
