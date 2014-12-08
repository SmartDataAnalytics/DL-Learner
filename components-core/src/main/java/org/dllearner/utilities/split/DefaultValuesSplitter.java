/**
 * 
 */
package org.dllearner.utilities.split;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

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

	/* (non-Javadoc)
	 * @see org.dllearner.utilities.split.ValuesSplitter#computeSplits()
	 */
	@Override
	public Map<OWLDataProperty, List<Double>> computeSplits() {
		Map<OWLDataProperty, List<Double>> result = new HashMap<OWLDataProperty, List<Double>>();
		
		for (OWLDataProperty dp : numericDataProperties) {
			// get all possible values for the data property
			List<Double> values = new ArrayList<Double>();
			Map<OWLIndividual, SortedSet<Double>> datatypeMembers = reasoner.getDoubleDatatypeMembers(dp);
			for (SortedSet<Double> currentValues : datatypeMembers.values()) {
				values.addAll(currentValues);
			}
			
			// sort
			Collections.sort(values);
			
			int nrOfValues = values.size();
			
			// create split set
			List<Double> splitsDP = new ArrayList<Double>();
			for (int splitNr = 0; splitNr < Math.min(maxNrOfSplits, nrOfValues - 1); splitNr++) {
				int index;
				
				if (nrOfValues <= maxNrOfSplits) {
					index = splitNr;
				} else {
					index = (int) Math.floor(splitNr * (double) nrOfValues / (maxNrOfSplits + 1));
				}
				
				double val1  = values.get(index);
				double val2 =  values.get(index + 1);
				
				double value = 0.5 * (val1 + val2);
				
				splitsDP.add(value);
			}
			result.put(dp, splitsDP);
		}
		
		return result;
	}
	
	/**
	 * @param maxNrOfSplits the maxNrOfSplits to set
	 */
	public void setMaxNrOfSplits(int maxNrOfSplits) {
		this.maxNrOfSplits = maxNrOfSplits;
	}

}
