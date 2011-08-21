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
 * Represents datatype quantor restrictions. For instance,
 * \exists salary < 100.000 can be used to specify
 * those objects (persons) which have a salary of below 100.000. 
 * 
 * @author Jens Lehmann
 *
 */
public abstract class DatatypeQuantorRestriction extends QuantorRestriction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7230621629431369625L;

	/**
	 * Creates a <code>DatatypeQuantorRestriction</code> along the 
	 * given property.
	 * @param datatypeProperty The datatype property along which this restriction acts.
	 */
	public DatatypeQuantorRestriction(DatatypeProperty datatypeProperty) {
		super(datatypeProperty);
	}

}
