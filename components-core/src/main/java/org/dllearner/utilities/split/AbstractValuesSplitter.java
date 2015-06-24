/**
 * 
 */
package org.dllearner.utilities.split;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Abstract class for values splitting implementation.
 * @author Lorenz Buehmann
 *
 */
public abstract class AbstractValuesSplitter implements ValuesSplitter{
	
	protected AbstractReasonerComponent reasoner;
	protected OWLDataFactory dataFactory;
	
	public AbstractValuesSplitter(AbstractReasonerComponent reasoner, OWLDataFactory dataFactory) {
		this.reasoner = reasoner;
		this.dataFactory = dataFactory;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
	}
	
	/**
	 * Computes a sorted list of split values for each appropriate data property. 
	 * @return a map of data properties and its sorted list of split values
	 */
	public Map<OWLDataProperty, List<OWLLiteral>> computeSplits() {
		Map<OWLDataProperty, List<OWLLiteral>> result = new HashMap<>();
		
		for (OWLDataProperty dp : getDataProperties()) {
			List<OWLLiteral> splitValues = computeSplits(dp);
			result.put(dp, splitValues);
		}
		
		return result;
	}
	
	/**
	 * @return all applicable data properties.
	 */
	protected abstract Set<OWLDataProperty> getDataProperties();
}
