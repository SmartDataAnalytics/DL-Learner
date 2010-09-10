/**
 * Copyright (C) 2007, Jens Lehmann
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
 *
 */
package org.dllearner.server;

/**
 * Exception for indicating that an operation was performed on 
 * a wrong type.
 * 
 * @author Jens Lehmann
 *
 */
public class ConfigOptionTypeException extends Exception {

	private static final long serialVersionUID = -6856243711006023178L;
	
	public ConfigOptionTypeException(String optionName, Class<?> correctType, Class<?> wrongType) {
		super(optionName + " is of type " + correctType.getName() + ", so you cannot use type " + wrongType.getName() + ".");
	}

}
