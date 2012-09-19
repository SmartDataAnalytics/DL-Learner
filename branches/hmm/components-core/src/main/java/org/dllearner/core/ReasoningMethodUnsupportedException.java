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

package org.dllearner.core;

/**
 * Exception indicating that a reasoner implementation cannot support
 * the requested operation. Either the operation itself is not implemented
 * or does not support certain features, e.g. a reasoner could support
 * instance checks but not if the class description contains datatype
 * constructs.
 * 
 * @author Jens Lehmann
 *
 */
public class ReasoningMethodUnsupportedException extends Exception {

	private static final long serialVersionUID = -7045236443032695475L;
	
	public ReasoningMethodUnsupportedException() {
		super();
	}	
	
	public ReasoningMethodUnsupportedException(String message) {
		super(message);
	}

}
