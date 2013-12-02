/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import java.util.Set;

/**
 * Interface for generating (non-semantic) annotations for documents.
 * @author Lorenz Buehmann
 */
public interface LinguisticAnnotator {
    /**
     * Returns the set of annotation for the given document.
     * @param document the document to get annotation for
     * @return set of annotations for the given document
     */
	Set<Annotation> annotate(TextDocument document);

}
