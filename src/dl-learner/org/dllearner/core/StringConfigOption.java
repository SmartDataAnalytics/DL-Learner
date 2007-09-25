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
package org.dllearner.core;

import java.util.Set;
import java.util.TreeSet;

/**
 * A configuration option, which allows values of type String. Optionally a set
 * of allowed strings can be set. By default all strings are allowed.
 * 
 * @author Jens Lehmann
 *
 */
public class StringConfigOption extends ConfigOption<String> {

	private Set<String> allowedValues;
	
	public StringConfigOption(String name) {
		super(name);
		allowedValues = new TreeSet<String>();
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.ConfigOption#isValidValue(java.lang.Object)
	 */
	@Override
	public boolean isValidValue(String value) {
		if(allowedValues.size() == 0 || allowedValues.contains(value))
			return true;
		else
			return false;
	}	
	
	/**
	 * @return the allowedValues
	 */
	public Set<String> getAllowedValues() {
		return allowedValues;
	}

	/**
	 * @param allowedValues the allowedValues to set
	 */
	public void setAllowedValues(Set<String> allowedValues) {
		this.allowedValues = allowedValues;
	}

}
