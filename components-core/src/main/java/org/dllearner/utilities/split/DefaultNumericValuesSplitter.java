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

	public DefaultNumericValuesSplitter(AbstractReasonerComponent reasoner, OWLDataFactory dataFactory, int maxNrOfSplits) {
		super(reasoner, dataFactory);
		this.maxNrOfSplits = maxNrOfSplits;
	}

	public DefaultNumericValuesSplitter(AbstractReasonerComponent reasoner,
			OWLDataFactory dataFactory) {
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
		Set<T> valuesSet = new TreeSet<>();

		Map<OWLIndividual, SortedSet<T>> ind2Values = reasoner.getNumericDatatypeMembers(dp);
		// add all values to the set
		for(Entry<OWLIndividual, SortedSet<T>> e : ind2Values.entrySet()){
			try {
				valuesSet.addAll(e.getValue());
			} catch(ClassCastException ce) {
				System.err.println("Mixed datatypes in "+dp.toStringID());
				throw ce;
			}
		}

		return simpleListSplitter(valuesSet, maxNrOfSplits);
	}
}
