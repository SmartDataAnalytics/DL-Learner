/**
 * 
 */
package org.dllearner.utilities.split;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.learningproblems.PosNegLP;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * @author Lorenz Buehmann
 *
 */
public class OptimizedValuesSplitter extends AbstractValuesSplitter{

	private PosNegLP lp;

	/**
	 * @param reasoner
	 */
	public OptimizedValuesSplitter(AbstractReasonerComponent reasoner, PosNegLP lp) {
		super(reasoner);
		this.lp = lp;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.utilities.split.ValuesSplitter#computeSplits()
	 */
	@Override
	public Map<OWLDataProperty, List<Double>> computeSplits() {
		Map<OWLDataProperty, List<Double>> result = new HashMap<OWLDataProperty, List<Double>>();
		
		Map<OWLDataProperty, Set<OWLLiteral>> relations = new HashMap<OWLDataProperty, Set<OWLLiteral>>();
		
		// generate relations for positive examples
		for (OWLIndividual ind : lp.getPositiveExamples()) {
			Map<OWLDataProperty, Set<OWLLiteral>> individualRelations = reasoner.getDataPropertyRelationships(ind);

			for (OWLDataProperty dp : individualRelations.keySet()) {
				if (relations.keySet().contains(dp)) {
					relations.get(dp).addAll(individualRelations.get(dp));
				} else {
					relations.put(dp, individualRelations.get(dp));
				}
			}
		}

		// generate relation for negative examples
		for (OWLIndividual ind : lp.getNegativeExamples()) {
			Map<OWLDataProperty, Set<OWLLiteral>> individualRelations = reasoner.getDataPropertyRelationships(ind);

			for (OWLDataProperty dp : individualRelations.keySet()) {
				if (relations.keySet().contains(dp)) {
					relations.get(dp).addAll(individualRelations.get(dp));
				} else {
					relations.put(dp, individualRelations.get(dp));
				}
			}
		}
		
		
		// -------------------------------------------------
		// calculate the splits for each data property
		// -------------------------------------------------

		Map<OWLDataProperty, List<Double>> splits = new HashMap<OWLDataProperty, List<Double>>();

//		for (OWLDataProperty dp : relations.keySet()) {
//
//			if (!relations.get(dp).isEmpty()) {
//				List<Double> values = new ArrayList<Double>();
//				Set<OWLLiteral> propertyValues = relations.get(dp);
//
//				int priorType = propertyValues.first().getType();
//				double priorValue = propertyValues.first().getValue();
//
//				Iterator<OWLLiteral> iterator = propertyValues.iterator();
//				while (iterator.hasNext()) {
//					ValueCount currentValueCount = iterator.next();
//					int currentType = currentValueCount.getType();
//					double currentValue = currentValueCount.getValue();
//
//					// check if a new value should be generated: when the type changes or the
//					// current value belongs to both pos. and neg.
//					if ((currentType == 3) || (currentType != priorType)) {
//						//calculate the middle/avg. value
//						//TODO: how to identify the splitting strategy here? For examples: time,... 
//						values.add((priorValue + currentValue) / 2.0);
//
//						//Double newValue = new Double(new TimeSplitter().calculateSplit((int)priorValue, (int)currentValue));
//						//if (!values.contains(newValue))
//						//	values.add(newValue);
//
//						//values.add((priorValue + currentValue) / 2.0);
//					}
//
//					// update the prior type and value after process the current element
//					priorType = currentValueCount.getType();
//					priorValue = currentValueCount.getValue();
//
//				}
//
//				// add processed property into the result set (splits)
//				splits.put(dp, values);
//			}
//		}
		
		return result;
	}

}
