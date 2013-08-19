package org.dllearner.algorithms.isle.index;

/**
 * Interface for classes representing documents.
 *
 * @author Daniel Fleischhacker
 */
public interface Document {
    /**
     * Returns the cleaned content of this document represented as a string. This returns the cleaned content,
     * thus markup and other structure is removed. The raw content can be retrieved using {@link #getRawContent}.
     * Methods for retrieving more specialized content formats might be implemented by the actual implementations.
     *
     * @return this document's text content
     */
    public String getContent();

    /**
     * Returns the uncleaned content, i.e., as originally retrieved, of this document represented as string.
     *
     * @return uncleaned content of this document
     */
    public String getRawContent();
}
