/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import java.io.Serializable;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;

/**
 * @author Lorenz Buehmann
 *
 */
public interface AnnotatedDocument extends Document, Serializable{
	
	/**
	 * Returns a set of entities which are contained in the document.
	 * @return
	 */
	Set<OWLEntity> getContainedEntities();
	
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
    int getEntityFrequency(OWLEntity entity);
}
