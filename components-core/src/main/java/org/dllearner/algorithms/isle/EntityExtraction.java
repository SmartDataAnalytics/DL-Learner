/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * @author Lorenz Buehmann
 *
 */
public interface EntityExtraction {
	
	/**
	 * Extracts all entities contained in the working text with some confidence value.
	 * @return
	 */
	Map<OWLEntity, Set<String>> extractEntities();
	
	/**
	 * Extracts all entities of the given <code>type</code> contained in the working text with some confidence value.
	 * @return
	 */
	Map<OWLEntity, Double> extractEntities(EntityType<OWLEntity> type);

}
