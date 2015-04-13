/**
 * 
 */
package org.dllearner.utilities.split;

import java.util.List;
import java.util.Map;

import org.dllearner.core.Component;
import org.semanticweb.owlapi.model.OWLDataProperty;

/**
 * @author Lorenz Buehmann
 *
 */
public interface ValuesSplitter extends Component{
	
	/**
     * Compute splits for all comparable numeric data properties in the ontology
     * 
     * @return a map of data properties and their splitting values
     */
	<T extends Number & Comparable<T>> Map<OWLDataProperty, List<T>> computeSplits();

	/**
	 * Compute splits values for the given data property
	 * 
	 * @param dp the data property
	 * @return a list of split values
	 */
	<T extends Number & Comparable<T>> List<T> computeSplits(OWLDataProperty dp);

}
