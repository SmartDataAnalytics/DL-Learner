/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.utilities.owl;

import java.util.Comparator;
import java.util.Set;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.reasoning.OWLPunningDetector;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNaryBooleanClassExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

/**
 * Comparator for evaluated descriptions, which orders them by
 * accuracy as first criterion, length as second criterion, and
 * syntactic structure as third criterion.
 * 
 * @author Jens Lehmann
 *
 */
public class EvaluatedDescriptionComparator implements Comparator<EvaluatedDescription> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(EvaluatedDescription ed1, EvaluatedDescription ed2) {
		double acc1 = ed1.getAccuracy();
		double acc2 = ed2.getAccuracy();
		if(acc1 > acc2)
			return 1;
		else if(acc1 < acc2)
			return -1;
		else {
			int length1 = 
					getLength(ed1);
//			ed1.getDescriptionLength();
			int length2 = 
					getLength(ed2);
//			ed2.getDescriptionLength();
			if(length1 < length2)
				return 1;
			else if(length1 > length2)
				return -1;
			else
				return ed1.getDescription().compareTo(ed2.getDescription());
		}
	}
	
	private int getLength(EvaluatedDescription ed){
		int length = 0;
		OWLClassExpression ce = ed.getDescription();
		if(ce instanceof OWLNaryBooleanClassExpression){
			Set<OWLClassExpression> operands = ((OWLNaryBooleanClassExpression) ce).getOperands();
			for (OWLClassExpression child : operands) {
				if(child instanceof OWLObjectSomeValuesFrom && ((OWLObjectSomeValuesFrom) child).getProperty() == OWLPunningDetector.punningProperty){
					length += OWLClassExpressionUtils.getLength(((OWLObjectSomeValuesFrom)child).getFiller());
				} else {
					length += OWLClassExpressionUtils.getLength(child);
				}
			}
			length += operands.size() - 1;
		} else {
			length = OWLClassExpressionUtils.getLength(ce);
		}
		return length;
	}

}