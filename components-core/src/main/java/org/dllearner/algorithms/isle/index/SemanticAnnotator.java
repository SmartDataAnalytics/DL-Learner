package org.dllearner.algorithms.isle.index;

/**
 * Provides methods to annotate documents.
 */
public interface SemanticAnnotator {
    /**
     * Processes the given document and returns the annotated version of this document.
     *
     * @param document the document to annotate
     * @return the given document extended with annotations
     */
    AnnotatedDocument processDocument(Document document);
}
