/**
 * 
 */
package org.dllearner.algorithms.isle.wsd;

import java.util.List;

import org.dllearner.algorithms.isle.index.Annotation;

/**
 * @author Lorenz Buehmann
 *
 */
public interface ContextExtractor {

	List<String> extractContext(Annotation annotation);
}
