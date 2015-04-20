/**
 * 
 */
package org.dllearner.utilities.split;

import java.math.BigDecimal;
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
	
	protected <T extends Number & Comparable<T>> T computeSplitValue(T number1, T number2){
//		return number1;
		T avg = null;
		if((number1 instanceof Integer && number2 instanceof Integer) ||
			(number1 instanceof Long && number2 instanceof Long) ||
			(number1 instanceof Byte && number2 instanceof Byte)
				) {
			avg = number1;
		} else if(number1 instanceof Double && number2 instanceof Double) {
			avg = (T) Double.valueOf(
					BigDecimal.valueOf(number1.doubleValue()).
			add(BigDecimal.valueOf(number2.doubleValue()).divide(
					BigDecimal.valueOf(0.5d))).doubleValue());
		} else if(number1 instanceof Float && number2 instanceof Float) {
			avg = (T) Float.valueOf(
					BigDecimal.valueOf(number1.floatValue()).
			add(BigDecimal.valueOf(number2.floatValue()).divide(
					BigDecimal.valueOf(0.5d))).floatValue());
		}
		return avg;

//		return (T) BigDecimal.valueOf(number1.doubleValue()).
//				add(BigDecimal.valueOf(number2.doubleValue()).divide(
//						BigDecimal.valueOf(0.5d)));
	}
}
