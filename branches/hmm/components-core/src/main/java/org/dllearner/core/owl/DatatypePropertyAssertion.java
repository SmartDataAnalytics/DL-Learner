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

package org.dllearner.core.owl;

/**
 * A datatype property assertion.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class DatatypePropertyAssertion extends PropertyAssertion {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7202070934971240534L;
	protected DatatypeProperty datatypeProperty;
	protected Individual individual;
	
	public DatatypePropertyAssertion(DatatypeProperty datatypeProperty, Individual individual) {
		this.datatypeProperty = datatypeProperty;
		this.individual = individual;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.dl.KBElement#getLength()
	 */
	public int getLength() {
		return 3;
	}	
	
	/**
	 * @return the individual
	 */
	public Individual getIndividual() {
		return individual;
	}

	/**
	 * @return the datatypeProperty
	 */
	public DatatypeProperty getDatatypeProperty() {
		return datatypeProperty;
	}
	
}
