/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import java.util.Set;

/**
 * @author Lorenz Buehmann
 *
 */
public interface LinguisticAnnotator {
	
	Set<Annotation> annotate(Document document);

}
