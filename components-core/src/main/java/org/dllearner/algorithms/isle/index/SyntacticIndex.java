/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import java.util.Set;

/**
 * @author Lorenz Buehmann
 *
 */
public interface SyntacticIndex {

	/**
	 * This method returns a set of documents based on how the underlying index is processing the given search string.
	 * @param searchString
	 * @return
	 */
	Set<String> getDocuments(String searchString);
	/**
	 * This method returns the number of documents based on how the underlying index is processing the given search string.
	 * @param searchString
	 * @return
	 */
	int count(String searchString);
	/**
	 * This methods returns the total number of documents contained in the index.
	 * @return the total number of documents contained in the index
	 */
	int getSize();
	
}
