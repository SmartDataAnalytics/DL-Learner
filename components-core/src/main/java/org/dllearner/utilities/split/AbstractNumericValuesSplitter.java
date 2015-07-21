/**
 * 
 */
package org.dllearner.utilities.split;

import java.math.BigDecimal;
import java.util.Set;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.utilities.OWLAPIUtils;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class AbstractNumericValuesSplitter extends AbstractValuesSplitter{

	public AbstractNumericValuesSplitter(AbstractReasonerComponent reasoner, OWLDataFactory dataFactory) {
		super(reasoner, dataFactory);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.utilities.split.AbstractValuesSplitter#getDataProperties()
	 */
	@Override
	public Set<OWLDataProperty> getDataProperties() {
		return reasoner.getNumericDataProperties();
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.utilities.split.AbstractValuesSplitter#getDatatypes()
	 */
	@Override
	public Set<OWLDatatype> getDatatypes() {
		return OWLAPIUtils.numericDatatypes;
	}

	@Override
	protected <T> T mixTwoValues(T value1, T value2) {
		return avg(value1, value2);
	}

	private <T> T avg(T number1, T number2) {
		T avg = null;
		if (number1 instanceof Integer && number2 instanceof Integer){
			avg = (T) Integer.valueOf((((Integer)number1).intValue() + ((Integer)number2).intValue()) / 2);
		} else if (number1 instanceof Short && number2 instanceof Short){
			avg = (T) Short.valueOf((short) ((((Short)number1).shortValue() + ((Short)number2).shortValue()) / 2));
		} else if (number1 instanceof Byte && number2 instanceof Byte){
			avg = (T) Byte.valueOf((byte) ((((Byte)number1).byteValue() + ((Byte)number2).byteValue()) / 2));
		} else if (number1 instanceof Long && number2 instanceof Long){
			avg = (T) Long.valueOf((((Long)number1).longValue() + ((Long)number2).longValue()) / 2);
		} else if (number1 instanceof Double && number2 instanceof Double) {
			avg = (T) Double.valueOf((BigDecimal.valueOf(((Double)number1).doubleValue()).add(
					BigDecimal.valueOf(((Double)number2).doubleValue())).divide(BigDecimal.valueOf(2))).doubleValue());
		} else if(number1 instanceof Float && number2 instanceof Float) {
			avg = (T) Float.valueOf(
					(BigDecimal.valueOf(((Float)number1).floatValue()).
			add(BigDecimal.valueOf(((Float)number2).floatValue())).divide(
					BigDecimal.valueOf(2d))).floatValue());
		}
		return avg;
	}

}
