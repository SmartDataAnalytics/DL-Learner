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

package org.dllearner.core.options;


/**
 * @author Jens Lehmann
 *
 */
public class InvalidConfigOptionValueException extends Exception {

	private static final long serialVersionUID = 3286110428258072698L;
	
	public InvalidConfigOptionValueException(ConfigOption<?> option, Object value) {
		super("The value " + value + " is not valid for the configuration option " + option + ".");
	}
	
	public InvalidConfigOptionValueException(ConfigOption<?> option, Object value, String reason) {
		super("The value " + value + " is not valid for the configuration option " + option + ". Reason: " + reason + ".");
	}
	
}
