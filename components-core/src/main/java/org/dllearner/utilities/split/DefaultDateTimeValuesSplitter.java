/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.utilities.split;

import java.util.ArrayList;
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

	public DefaultDateTimeValuesSplitter(AbstractReasonerComponent reasoner, OWLDataFactory dataFactory, int maxNrOfSplits) {
		super(reasoner, dataFactory);
		this.maxNrOfSplits = maxNrOfSplits;
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
		
		DateTimeFormatter parser = OWLAPIUtils.dateTimeParsers.get(datatype);
		DateTimeFormatter formatter = OWLAPIUtils.dateTimeFormatters.get(datatype);
		
		List<DateTime> values = new LinkedList<>();
		for (Entry<OWLIndividual, SortedSet<OWLLiteral>> entry : ind2Values.entrySet()) {
			
			for (OWLLiteral value : entry.getValue()) {
				DateTime dateTime = parser.parseDateTime(value.getLiteral());
				values.add(dateTime);
			}
			
		}
		
		List<DateTime> splitValues = simpleListSplitter(values, maxNrOfSplits);
		
		for (DateTime value : splitValues) {
			OWLLiteral literal = dataFactory.getOWLLiteral(value.toString(formatter), reasoner.getDatatype(dp));
			splitLiterals.add(literal);
		}
		
		return splitLiterals;
	}
	
	private DateTime computeSplitValue(DateTime value1, DateTime value2){
		return value1;
	}

	@Override
	protected <T> T mixTwoValues(T value1, T value2) {
		return (T)computeSplitValue((DateTime)value1, (DateTime)value2);
	}
}
