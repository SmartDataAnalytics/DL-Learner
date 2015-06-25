/**
 * 
 */
package org.dllearner.utilities.split;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.utilities.OWLAPIUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * A splitter for date time values which simply returns a fixed number of split
 * values.
 
 * @author Lorenz Buehmann
 *
 */
public class DefaultDateTimeValuesSplitter extends AbstractDateTimeValuesSplitter {
	
	private int maxNrOfSplits = 10;

	public DefaultDateTimeValuesSplitter(AbstractReasonerComponent reasoner, OWLDataFactory dataFactory) {
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
		
		Map<OWLIndividual, SortedSet<OWLLiteral>> ind2Values = reasoner.getDatatypeMembers(dp);
		
		OWLDatatype datatype = reasoner.getDatatype(dp);
		
		DateTimeFormatter formatter = OWLAPIUtils.dateTimeFormatters.get(datatype);
		
		List<DateTime> values = new LinkedList<DateTime>();
		for (Entry<OWLIndividual, SortedSet<OWLLiteral>> entry : ind2Values.entrySet()) {
			
			for (OWLLiteral value : entry.getValue()) {
				DateTime dateTime = formatter.parseDateTime(value.getLiteral());
				values.add(dateTime);
			}
			
		}
		Collections.sort(values);
		
		List<DateTime> splitValues = computeSplitValues(values);
		
		for (DateTime value : splitValues) {
			OWLLiteral literal = dataFactory.getOWLLiteral(value.toString(formatter), reasoner.getDatatype(dp));
			splitLiterals.add(literal);
		}
		
		return splitLiterals;
	}
	
	private List<DateTime> computeSplitValues(List<DateTime> values) {
		int nrOfValues = values.size();
		
		// create split set
		List<DateTime> splitValues = new LinkedList<DateTime>();
		for (int splitNr = 0; splitNr < Math.min(maxNrOfSplits, nrOfValues - 1); splitNr++) {
			int index;
			if (nrOfValues <= maxNrOfSplits) {
				index = splitNr;
			} else {
				index = (int) Math.floor(splitNr * (double) nrOfValues / (maxNrOfSplits + 1));
			}
			index = Math.max(0, (int) Math.floor(splitNr * (double) nrOfValues / (maxNrOfSplits) - 1));
			
			DateTime value1 = values.get(index);
			DateTime value2 = values.get(index + 1);
			
			DateTime splitValue = computeSplitValue(value1, value2);

			splitValues.add(splitValue);
		}
		
		// add the last element
		splitValues.add(values.get(nrOfValues - 1));
		
		return splitValues;
	}
	
	private DateTime computeSplitValue(DateTime value1, DateTime value2){
		return value1;
	}
}
