/**
 * 
 */
package org.dllearner.utilities.split;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.learningproblems.PosNegLP;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.springframework.core.GenericTypeResolver;

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

	/* (non-Javadoc)
	 * @see org.dllearner.utilities.split.ValuesSplitter#computeSplits()
	 */
	@Override
	public Map<OWLDataProperty, List<T>> computeSplits() {
		Map<OWLDataProperty, List<T>> result = new HashMap<OWLDataProperty, List<T>>();
		
		Map<OWLDataProperty, SortedSet<T>> relations = new HashMap<OWLDataProperty, SortedSet<T>>();
		
		// get the used values for each positive and negative example
		for (OWLDataProperty dp : numericDataProperties) {
			Map<OWLIndividual, SortedSet<T>> numericDatatypeMembers = reasoner.getNumericDatatypeMembers(dp, clazz);
			
			// generate relations for positive examples
			for (OWLIndividual ind : lp.getPositiveExamples()) {
				if (relations.keySet().contains(dp)) {
					relations.get(dp).addAll(relations.get(dp));
				} else {
					relations.put(dp, relations.get(dp));
				}
			}
			// generate relation for negative examples
			for (OWLIndividual ind : lp.getNegativeExamples()) {
				if (relations.keySet().contains(dp)) {
					relations.get(dp).addAll(relations.get(dp));
				} else {
					relations.put(dp, relations.get(dp));
				}
			}
		}
		
		// -------------------------------------------------
		// calculate the splits for each data property
		// -------------------------------------------------

		Map<OWLDataProperty, List<T>> splits = new HashMap<OWLDataProperty, List<T>>();

//		for (Entry<OWLDataProperty, SortedSet<T>> entry : relations.entrySet()) {
//			OWLDataProperty dp = entry.getKey();
//			SortedSet<T> propertyValues = entry.getValue();
//			
//			if (!propertyValues.isEmpty()) {
//				List<T> splitValues = new ArrayList<T>();
//				
//				Iterator<T> iterator = propertyValues.iterator();
//
//				T first = iterator.next();
//				
//				int priorType = first.getType();
//				T priorValue = first.getValue();
//				
//				while (iterator.hasNext()) {
//					ValueCount currentValueCount = iterator.next();
//					int currentType = currentValueCount.getType();
//					T currentValue = currentValueCount.getValue();
//
//					// check if a new value should be generated: when the type changes or the
//					// current value belongs to both pos. and neg.
//					if ((currentType == 3) || (currentType != priorType)) {
//						//calculate the middle/avg. value
//						T splitValue = computeSplitValue(priorValue, currentValue);
//						
//						splitValues.add(splitValue);
//					}
//
//					// update the prior type and value after process the current element
//					priorType = currentValueCount.getType();
//					priorValue = currentValueCount.getValue();
//
//				}
//
//				// add processed property into the result set (splits)
//				splits.put(dp, splitValues);
//			}
//		}
		
		return result;
	}
	
	/**
	 * Compute a split value between 2 succeeding values.
	 * TODO: How to identify the splitting strategy here? For numbers we can use the avg, 
	 * but for time values?
	 */
//	private T computeSplitValue(T priorValue, T subsequentValue) {
//		if(clazz == Double.class){
//			return (T) Double.valueOf( ((Double) priorValue + (Double) subsequentValue) / 2.0);
//		} else if(clazz == Integer.class) {
//			return (T) Double.valueOf( ((Double) priorValue + (Double) subsequentValue) / 2.0);
//		}
//		throw new UnsupportedOperationException("Split of type " + clazz + " not implemented yet.");
//	}

}
