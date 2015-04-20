/**
 * 
 */
package org.dllearner.utilities.split;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.OWLAPIUtils;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.springframework.core.GenericTypeResolver;

import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class OptimizedValuesSplitter<T extends Number & Comparable<T>> extends AbstractValuesSplitter{

	private PosNegLP lp;
	private Class<T> clazz;

	/**
	 * @param reasoner
	 */
	public OptimizedValuesSplitter(AbstractReasonerComponent reasoner, PosNegLP lp) {
		super(reasoner);
		this.lp = lp;
		
		clazz = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), OptimizedValuesSplitter.class);
	}

//	/* (non-Javadoc)
//	 * @see org.dllearner.utilities.split.ValuesSplitter#computeSplits()
//	 */
//	@Override
//	public Map<OWLDataProperty, List<T>> computeSplits() {
//		Map<OWLDataProperty, List<T>> result = new HashMap<OWLDataProperty, List<T>>();
//		
//		Map<OWLDataProperty, SortedSet<T>> relations = new HashMap<OWLDataProperty, SortedSet<T>>();
//		
//		// get the used values for each positive and negative example
//		for (OWLDataProperty dp : numericDataProperties) {
//			Map<OWLIndividual, SortedSet<T>> numericDatatypeMembers = reasoner.getNumericDatatypeMembers(dp, clazz);
//			
//			// generate relations for positive examples
//			for (OWLIndividual ind : lp.getPositiveExamples()) {
//				if (relations.keySet().contains(dp)) {
//					relations.get(dp).addAll(relations.get(dp));
//				} else {
//					relations.put(dp, relations.get(dp));
//				}
//			}
//			// generate relation for negative examples
//			for (OWLIndividual ind : lp.getNegativeExamples()) {
//				if (relations.keySet().contains(dp)) {
//					relations.get(dp).addAll(relations.get(dp));
//				} else {
//					relations.put(dp, relations.get(dp));
//				}
//			}
//		}
//		
//		// -------------------------------------------------
//		// calculate the splits for each data property
//		// -------------------------------------------------
//
//		Map<OWLDataProperty, List<T>> splits = new HashMap<OWLDataProperty, List<T>>();
//
////		for (Entry<OWLDataProperty, SortedSet<T>> entry : relations.entrySet()) {
////			OWLDataProperty dp = entry.getKey();
////			SortedSet<T> propertyValues = entry.getValue();
////			
////			if (!propertyValues.isEmpty()) {
////				List<T> splitValues = new ArrayList<T>();
////				
////				Iterator<T> iterator = propertyValues.iterator();
////
////				T first = iterator.next();
////				
////				int priorType = first.getType();
////				T priorValue = first.getValue();
////				
////				while (iterator.hasNext()) {
////					ValueCount currentValueCount = iterator.next();
////					int currentType = currentValueCount.getType();
////					T currentValue = currentValueCount.getValue();
////
////					// check if a new value should be generated: when the type changes or the
////					// current value belongs to both pos. and neg.
////					if ((currentType == 3) || (currentType != priorType)) {
////						//calculate the middle/avg. value
////						T splitValue = computeSplitValue(priorValue, currentValue);
////						
////						splitValues.add(splitValue);
////					}
////
////					// update the prior type and value after process the current element
////					priorType = currentValueCount.getType();
////					priorValue = currentValueCount.getValue();
////
////				}
////
////				// add processed property into the result set (splits)
////				splits.put(dp, splitValues);
////			}
////		}
//		
//		return result;
//	}

	/* (non-Javadoc)
	 * @see org.dllearner.utilities.split.ValuesSplitter#computeSplits(org.semanticweb.owlapi.model.OWLDataProperty)
	 */
	@Override
	public <T extends Number & Comparable<T>> List<T> computeSplits(OWLDataProperty dp) {
		List<T> splitsDP = new LinkedList<T>();		
		NumberFormat numberFormat = NumberFormat.getInstance();
		
		SortedSet<T> posRelatedValues = new TreeSet<T>();
		
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
		
		SortedSet<T> negRelatedValues = new TreeSet<T>();
		
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
