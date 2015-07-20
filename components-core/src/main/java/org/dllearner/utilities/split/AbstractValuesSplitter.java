/**
 * 
 */
package org.dllearner.utilities.split;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;

import com.google.common.collect.Lists;

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
	
	public AbstractValuesSplitter(AbstractReasonerComponent reasoner, OWLDataFactory dataFactory, int maxNrOfSplits) {
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
	@Override
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

	protected <T> T mixTwoValues(T value1, T value2) { return null; }

	protected <T extends Comparable<? super T>> List<T> simpleListSplitter(
			Collection<T> allValues, int maxNrOfSplits) {
		// convert set to a list where values are sorted
		List<T> values = new LinkedList<T>(allValues);
		Collections.sort(values);

		int nrOfValues = values.size();
		int nrOfSplits = Math.min(maxNrOfSplits, nrOfValues + 1);

		// create split set
		Set<T> splitsDP = new TreeSet<T>();

		// add the first element
		if (nrOfValues > 0) {
			splitsDP.add(values.get(0));
		}

		for (int splitNr = 1; splitNr < nrOfSplits; splitNr++) {
			int index = (int) ((splitNr * (double) (nrOfValues)/(nrOfSplits-1))-1);

			T number1 = values.get(index);
			T number2 = values.get(Math.min(nrOfValues - 1, index + 1));

			//			System.out.println("Index:" + index + " v1=" + number1 + " v2=" + number2);

			T avg = mixTwoValues(number1, number2);

			splitsDP.add(avg);
		}

		// add the last element
		if(nrOfValues > 1)
			splitsDP.add(values.get(nrOfValues - 1));

		return Lists.newLinkedList(splitsDP);
	}
}
