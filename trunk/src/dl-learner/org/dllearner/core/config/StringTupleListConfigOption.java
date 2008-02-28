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

import java.util.List;

import org.dllearner.utilities.StringTuple;

/**
 * A list if string tuples, for instance for specifying several parameters or
 * replacement rules.
 * 
 * @author Jens Lehmann
 */
public class StringTupleListConfigOption extends ConfigOption<List<StringTuple>> {

	public StringTupleListConfigOption(String name, String description) {
		this(name, description, null);
	}

	public StringTupleListConfigOption(String name, String description,
			List<StringTuple> defaultValue) {
		super(name, description, defaultValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.config.ConfigOption#checkType(java.lang.Object)
	 */
	@Override
	public boolean checkType(Object object) {
		if (!(object instanceof List))
			return false;

		List<?> set = (List<?>) object;
		for (Object element : set) {
			if (!(element instanceof StringTuple))
				return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.config.ConfigOption#isValidValue(java.lang.Object)
	 */
	@Override
	public boolean isValidValue(List<StringTuple> value) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.config.ConfigOption#getValueFormatting(java.lang.Object)
	 */
	@Override
	public String getValueFormatting(List<StringTuple> value, Integer special) {
		Integer count = 0;
		if (value != null) {
			String back = "[";
			for (StringTuple i : value) {
				if (count > 0)
					back += ",";
				back += "\n(\"" + i.a + "\",\"" + i.b + "\")";
				count++;
			}
			back += "];";
			return back;
		} else
			return null;
	}

}
