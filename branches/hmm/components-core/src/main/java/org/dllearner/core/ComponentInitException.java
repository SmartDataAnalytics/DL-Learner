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
 * Exception which is thrown when a component cannot be intialised,
 * e.g. due to bad configuration parameters, or unforeseen 
 * circumstances, e.g. unreachable web files. It can encapsulate arbitrary
 * exceptions occurring during initialisation.
 * 
 * @author Jens Lehmann
 *
 */
public class ComponentInitException extends Exception {
	         
	private static final long serialVersionUID = -3550079897929658317L;

	/**
	 * Creates a <code>ComponentInitException</code> with the specified message.
	 * @param message The specified detail message.
	 */
	public ComponentInitException(String message) {
		super(message);
	}
	
	/**
	 * Creates a <code>ComponentInitException</code> with the
	 * specified cause.
	 * @param cause The cause of this exception.
	 */
	public ComponentInitException(Throwable cause) {
		super(cause);
	}	
	
	/**
	 * Creates a <code>ComponentInitException</code> with the
	 * specified message and cause.
	 * @param message The specified detail message.
	 * @param cause The cause of this exception.
	 */
	public ComponentInitException(String message, Throwable cause) {
		super(message, cause);
	}

}
