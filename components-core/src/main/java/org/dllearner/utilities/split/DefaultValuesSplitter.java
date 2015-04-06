/**
 * 
 */
package org.dllearner.utilities.split;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * @author Lorenz Buehmann
 *
 */
public class DefaultValuesSplitter extends AbstractValuesSplitter{

	private int maxNrOfSplits = 10;

	/**
	 * @param reasoner
	 */
	public DefaultValuesSplitter(AbstractReasonerComponent reasoner) {
		super(reasoner);
	}

	/**
	 * @param maxNrOfSplits the maxNrOfSplits to set
	 */
	public void setMaxNrOfSplits(int maxNrOfSplits) {
		this.maxNrOfSplits = maxNrOfSplits;
	}
	
	@Override
	public <T extends Number & Comparable<T>> List<T> computeSplits(OWLDataProperty dp) {
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
			T number1 = values.get(index);
			T number2 = values.get(index + 1);

			T avg = computeSplitValue(number1, number2);

			splitsDP.add(avg);
		}
		return splitsDP;
	}
}
