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

import com.google.common.collect.Sets;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.OWLAPIUtils;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

/**
 * @author Lorenz Buehmann
 *
 */
public class OptimizedNumericValuesSplitter extends AbstractNumericValuesSplitter{

	private PosNegLP lp;
//	private Class<T> clazz;

	public OptimizedNumericValuesSplitter(AbstractReasonerComponent reasoner, OWLDataFactory dataFactory, PosNegLP lp) {
		super(reasoner, dataFactory);
		this.lp = lp;
		
//		clazz = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), OptimizedValuesSplitter.class);
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

	public <T extends Number & Comparable<T>> List<T> computeSplitValues(OWLDataProperty dp) {
		List<T> splitsDP = new LinkedList<>();
		NumberFormat numberFormat = NumberFormat.getInstance(Locale.ROOT);
		
		SortedSet<T> posRelatedValues = new TreeSet<>();
		
		for (OWLIndividual ex : lp.getPositiveExamples()) {
			Set<OWLLiteral> relatedValues = reasoner.getRelatedValues(ex, dp);
			for (OWLLiteral lit : relatedValues) {
				if(OWLAPIUtils.isIntegerDatatype(lit)) {
					posRelatedValues.add((T) Integer.valueOf(lit.parseInteger()));
				} else {
					try {
						Number number = numberFormat.parse(lit.getLiteral());
						if(number instanceof Long) {
							number = Double.valueOf(number.toString());
						}
						posRelatedValues.add((T) (number) );
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				
			}
		}
		
		SortedSet<T> negRelatedValues = new TreeSet<>();
		
		for (OWLIndividual ex : lp.getNegativeExamples()) {
			Set<OWLLiteral> relatedValues = reasoner.getRelatedValues(ex, dp);
			for (OWLLiteral lit : relatedValues) {
				if(OWLAPIUtils.isIntegerDatatype(lit)) {
					negRelatedValues.add((T) Integer.valueOf(lit.parseInteger()));
				} else {
					try {
						Number number = numberFormat.parse(lit.getLiteral());
						if(number instanceof Long) {
							number = Double.valueOf(number.toString());
						}
						negRelatedValues.add((T) (number) );
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				
			}
		}
		
		SortedSet<T> allRelatedValues = Sets.newTreeSet(posRelatedValues);
		allRelatedValues.addAll(negRelatedValues);
		
		boolean posBefore = false;
		boolean negBefore = false;
		for (T val : allRelatedValues) {
			boolean pos = posRelatedValues.contains(val);
			boolean neg = negRelatedValues.contains(val);
			
			if(pos && !posBefore) {
				splitsDP.add(val);
			}
			
			if(neg && !negBefore) {
				splitsDP.add(val);
			}
			
			if(pos && neg) {
				splitsDP.add(val);
			}
			
			posBefore = pos;
			negBefore = neg;
		}
		
		return splitsDP;
	}

}
