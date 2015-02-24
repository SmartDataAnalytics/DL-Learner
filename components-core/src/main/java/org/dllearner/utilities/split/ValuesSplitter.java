/**
 * 
 */
package org.dllearner.utilities.split;

import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLDataProperty;

/**
 * @author Lorenz Buehmann
 *
 */
public interface ValuesSplitter {
	
	/**
     * Compute splits for all numeric data properties in the ontology
     * 
     * @return A map of data properties and their splitting values
     */
	<T extends Number & Comparable<T>> Map<OWLDataProperty, List<T>> computeSplits();

}
