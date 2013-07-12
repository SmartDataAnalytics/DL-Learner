/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.util.Map;

import org.dllearner.core.owl.Entity;

/**
 * @author Lorenz Buehmann
 *
 */
public interface EntityExtraction {
	
	/**
	 * Extracts all entities contained in the working text with some confidence value.
	 * @return
	 */
	Map<Entity, Double> extractEntities();
	
	/**
	 * Extracts all entities of the given <code>type</code> contained in the working text with some confidence value.
	 * @return
	 */
	Map<Entity, Double> extractEntities(Entity.Type type);

}
