/**
 * 
 */
package org.dllearner.utilities.split;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.owlapi.model.OWLDataProperty;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class AbstractValuesSplitter extends AbstractComponent implements ValuesSplitter {
	
	protected AbstractReasonerComponent reasoner;
	
	protected Set<OWLDataProperty> numericDataProperties;

	public AbstractValuesSplitter(AbstractReasonerComponent reasoner) {
		this.reasoner = reasoner;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		numericDataProperties = reasoner.getNumericDataProperties();
	}
	
	@Override
	public <T extends Number & Comparable<T>> Map<OWLDataProperty, List<T>> computeSplits() {
		Map<OWLDataProperty, List<T>> result = new HashMap<OWLDataProperty, List<T>>();
		
		for (OWLDataProperty dp : numericDataProperties) {
			List<T> splitValues = computeSplits(dp);
			result.put(dp, splitValues);
		}
		
		return result;
	}
}
