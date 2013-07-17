/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import java.util.Set;

import org.dllearner.core.owl.Entity;

/**
 * This class 
 * @author Lorenz Buehmann
 *
 */
public interface SemanticIndex {

	/**
	 * This method returns a set of documents for the given entity.
	 * @param entity
	 * @return
	 */
	Set<String> getDocuments(Entity entity);
	/**
	 * This method returns the number of documents for the given entity.
	 * @param entity
	 * @return
	 */
	int count(Entity entity);
	/**
	 * This methods returns the total number of documents contained in the index.
	 * @return the total number of documents contained in the index
	 */
	int getSize();

}
