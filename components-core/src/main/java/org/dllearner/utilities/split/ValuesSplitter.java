/**
 * 
 */
package org.dllearner.utilities.split;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.core.Component;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * @author Lorenz Buehmann
 *
 */
public interface ValuesSplitter extends Component{
	
	/**
     * Computes split literals for all applicable data properties in the ontology
     * 
     * @return a map of data properties and their splitting values
     */
	Map<OWLDataProperty, List<OWLLiteral>> computeSplits();

	/**
	 * Computes split literals for the given data property
	 * 
	 * @param dp the data property
	 * @return a list of split literals
	 */
	List<OWLLiteral> computeSplits(OWLDataProperty dp);
	
	/**
	 * @return the supported datatypes
	 */
	Set<OWLDatatype> getDatatypes();

}
