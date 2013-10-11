/**
 * 
 */
package org.dllearner.algorithms.isle.wsd;

import java.util.Set;

/**
 * @author Lorenz Buehmann
 *
 */
public interface ContextExtractor {

	Set<String> extractContext(String token, String document);
}
