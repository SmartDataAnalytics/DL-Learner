/**
 * 
 */
package org.dllearner.utilities.split;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * A splitter for numeric values which simply returns a fixed number of split
 * values.
 * Given k is the maximal number of values
 * used for the refinement of a data property, the assertion
 * values of the data property are split into k equal parts and
 * the middle values of split parts are used for refinement (they
 * are rounded for the integer datatype).
 * Supposed we have a set of integer values
 * {1, 2, 3, 4, 5, 6, 10, 12, 16, 20, 28, 30} and use 4 splits, then
 * we would get 5 split values [1, 3, 8, 18, 30].
 * 
 * @author Lorenz Buehmann
 *
 */
public class DefaultNumericValuesSplitter extends AbstractNumericValuesSplitter {

	private int maxNrOfSplits = 10;

	public DefaultNumericValuesSplitter(AbstractReasonerComponent reasoner, OWLDataFactory dataFactory) {
		super(reasoner, dataFactory);
	}

	/**
	 * @param maxNrOfSplits the maximal number of splits
	 */
	public void setMaxNrOfSplits(int maxNrOfSplits) {
		this.maxNrOfSplits = maxNrOfSplits;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.utilities.split.AbstractValuesSplitter#computeSplits(org.semanticweb.owlapi.model.OWLDataProperty)
	 */
	@Override
	public List<OWLLiteral> computeSplits(OWLDataProperty dp) {
		List<OWLLiteral> splitLiterals = new ArrayList<>();
		
		List<? extends Number> splitValues = computeSplitValues(dp);
		
		for (Number value : splitValues) {
			OWLLiteral literal = dataFactory.getOWLLiteral(value.toString(), reasoner.getDatatype(dp));
			splitLiterals.add(literal);
		}
		
		return splitLiterals;
	}
	
	private <T extends Number & Comparable<T>> List<T> computeSplitValues(OWLDataProperty dp) {
		Set<T> valuesSet = new TreeSet<T>();

		Map<OWLIndividual, SortedSet<T>> ind2Values = reasoner.getNumericDatatypeMembers(dp);

		// add all values to the set
		for(Entry<OWLIndividual, SortedSet<T>> e : ind2Values.entrySet()){
			valuesSet.addAll(e.getValue());
		}

		// convert set to a list where values are sorted
		List<T> values = new LinkedList<T>(valuesSet);
		Collections.sort(values);

		int nrOfValues = values.size();
		
		// create split set
		List<T> splitsDP = new LinkedList<T>();
		for (int splitNr = 0; splitNr < Math.min(maxNrOfSplits, nrOfValues - 1); splitNr++) {
			int index;
			if (nrOfValues <= maxNrOfSplits) {
				index = splitNr;
			} else {
				index = (int) Math.floor(splitNr * (double) nrOfValues / (maxNrOfSplits + 1));
			}
			index = Math.max(0, (int) Math.floor(splitNr * (double) nrOfValues / (maxNrOfSplits) - 1));
			
			T number1 = values.get(index);
			T number2 = values.get(index + 1);
			
//			System.out.println("Index:" + index + " v1=" + number1 + " v2=" + number2);

			T avg = avg(number1, number2);

			splitsDP.add(avg);
		}
		
		// add the last element
		splitsDP.add(values.get(nrOfValues - 1));
		
		return splitsDP;
	}
	
	private <T extends Number & Comparable<T>> T avg(T number1, T number2){
		T avg = null;
		if (number1 instanceof Integer && number2 instanceof Integer){
			avg = (T) Integer.valueOf((number1.intValue() + number2.intValue()) / 2);
		} else if (number1 instanceof Short && number2 instanceof Short){
			avg = (T) Short.valueOf((short) ((number1.shortValue() + number2.shortValue()) / 2));
		} else if (number1 instanceof Byte && number2 instanceof Byte){
			avg = (T) Byte.valueOf((byte) ((number1.byteValue() + number2.byteValue()) / 2));
		} else if (number1 instanceof Long && number2 instanceof Long){
			avg = (T) Long.valueOf((long) ((number1.longValue() + number2.longValue()) / 2));
		} else if (number1 instanceof Double && number2 instanceof Double) {
			avg = (T) Double.valueOf((BigDecimal.valueOf(number1.doubleValue()).add(
					BigDecimal.valueOf(number2.doubleValue())).divide(BigDecimal.valueOf(2))).doubleValue());
		} else if(number1 instanceof Float && number2 instanceof Float) {
			avg = (T) Float.valueOf(
					(BigDecimal.valueOf(number1.floatValue()).
			add(BigDecimal.valueOf(number2.floatValue())).divide(
					BigDecimal.valueOf(2d))).floatValue());
		}
		return avg;
	}
}
