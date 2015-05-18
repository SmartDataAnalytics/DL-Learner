/**
 * 
 */
package org.dllearner.core.ref;

import java.util.SortedSet;

/**
 * @author Lorenz Buehmann
 *
 */
public interface RefinementOperator<T> {
	
	SortedSet<T> refineNode(T node);

}
