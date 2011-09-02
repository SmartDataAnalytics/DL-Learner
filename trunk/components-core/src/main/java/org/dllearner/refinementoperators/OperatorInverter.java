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

package org.dllearner.refinementoperators;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Negation;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.ConceptTransformation;

/**
 * The class uses an existing refinement operator and inverts it, i.e. a 
 * downward refinement operator is turned into an upward refinement operator
 * and vice versa.
 * 
 * @author Jens Lehmann
 *
 */
public class OperatorInverter implements RefinementOperator {

	private RefinementOperator operator;
	private ConceptComparator cc = new ConceptComparator();
	private boolean useNegationNormalForm = true;
	private boolean guaranteeLength = true;
	
	public OperatorInverter(RefinementOperator operator) {
		this.operator = operator;
	}
	
	@Override
	public Set<Description> refine(Description description) {
		Set<Description> refinements = operator.refine(getNegation(description));
		TreeSet<Description> results = new TreeSet<Description>(cc);
		for(Description d : refinements) {
			results.add(getNegation(d));
		}
		return results;
	}

	@Override
	public Set<Description> refine(Description description, int maxLength) {
		Description negatedDescription = getNegation(description);
//		System.out.println("negated description: " + negatedDescription);
		// concept length can change because of the conversion process; as a heuristic
		// we increase maxLength by the length difference of negated and original concept
		int lengthDiff = Math.max(0, negatedDescription.getLength() - description.getLength());
		Set<Description> refinements = operator.refine(negatedDescription, maxLength+lengthDiff+1);
//		System.out.println("refinv: " + refinements);
		TreeSet<Description> results = new TreeSet<Description>(cc);
		for(Description d : refinements) {
			Description dNeg = getNegation(d);
//			System.out.println("dNeg: " + dNeg);
			// to satisfy the guarantee that the method does not return longer
			// concepts, we perform an additional check
			if(!guaranteeLength || dNeg.getLength() <= maxLength) {
				results.add(dNeg);
			}
		}
		return results;	
	}

	@Override
	public Set<Description> refine(Description description, int maxLength,
			List<Description> knownRefinements) {
		throw new Error("Method not implemented.");
	}

	private Description getNegation(Description description) {
		Description negatedDescription = new Negation(description);
		if(useNegationNormalForm) {
			negatedDescription = ConceptTransformation.transformToNegationNormalForm(negatedDescription);
		}
		return negatedDescription;
	}

	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub
		
	}
	
}
