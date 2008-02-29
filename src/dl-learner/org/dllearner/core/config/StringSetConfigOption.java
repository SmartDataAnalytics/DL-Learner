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
package org.dllearner.core.config;

import java.util.Set;

/**
 * A set of strings.
 * 
 * @author Jens Lehmann
 * 
 */
public class StringSetConfigOption extends ConfigOption<Set<String>> {

	public StringSetConfigOption(String name, String description) {
		super(name, description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.ConfigOption#isValidValue(java.lang.Object)
	 */
	@Override
	public boolean isValidValue(Set<String> value) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.ConfigOption#checkType(java.lang.Object)
	 */
	@Override
	public boolean checkType(Object object) {
		if (!(object instanceof Set))
			return false;

		Set<?> set = (Set<?>) object;
		for (Object element : set) {
			if (!(element instanceof String))
				return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.config.ConfigOption#getValueFormatting(java.lang.Object)
	 */
	@Override
	public String getValueFormatting(Set<String> value, Integer special) {
		String back = "";
		if (value != null && special == 0) {
			Integer count = 0;
			back = "{";
			for (String i : value) {
				if (count > 0)
					back += ",";
				back += "\n\"" + i + "\"";
				count++;
			}
			back += "};";
			return back;
		}
		// positive examples
		if (value != null && special == 1) {
			for (String i : value) {
				back += "\n+\"" + i + "\"";
			}
			return back + "\n";
		}
		// negative examples
		if (value != null && special == 2) {
			Integer count = 0;
			for (String i : value) {
				count++;
				if (count == 1)
					back += "-\"" + i + "\"";
				else
					back += "\n-\"" + i + "\"";
			}
			return back + "\n";
		}
		return null;
	}
}
