/**
 *
 */
package org.dllearner.algorithms.isle.index;

import java.util.Set;

/**
 * Interface for a syntactic index, e.g., a basic string-based inverted index.
 *
 * @author Lorenz Buehmann
 * @author Daniel Fleischhacker
 */
public interface SyntacticIndex {

    /**
     * Returns a set of documents based on how the underlying index is processing the given
     * search string.
     *
     * @param searchString query specifying the documents to retrieve
     * @return set of documents retrieved based on the given query string
     */
    Set<Document> getDocuments(String searchString);

    /**
     * Returns the number of documents based on how the underlying index is processing the
     * given search string.
     *
     * @param searchString query specifying the documents to include in the number of documents
     * @return number of documents retrieved based on the given query string
     */
    int count(String searchString);

    /**
     * Returns the total number of documents contained in the index.
     *
     * @return the total number of documents contained in the index
     */
    int getSize();

}
