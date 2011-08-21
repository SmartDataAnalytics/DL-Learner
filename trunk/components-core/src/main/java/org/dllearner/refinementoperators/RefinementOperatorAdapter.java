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

import org.dllearner.core.owl.Description;

/**
 * Adapter for {@link RefinementOperator} interface.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class RefinementOperatorAdapter implements RefinementOperator {

	/* (non-Javadoc)
	 * @see org.dllearner.refinementoperators.RefinementOperator#refine(org.dllearner.core.owl.Description)
	 */
	@Override
	public abstract Set<Description> refine(Description description);

	/* (non-Javadoc)
	 * @see org.dllearner.refinementoperators.RefinementOperator#refine(org.dllearner.core.owl.Description, int)
	 */
	@Override
	public Set<Description> refine(Description description, int maxLength) {
		throw new UnsupportedOperationException();
	}	
	
	/* (non-Javadoc)
	 * @see org.dllearner.refinementoperators.RefinementOperator#refine(org.dllearner.core.owl.Description, int, java.util.List)
	 */
	@Override
	public Set<Description> refine(Description description, int maxLength,
			List<Description> knownRefinements) {
		throw new UnsupportedOperationException();
	}

}
