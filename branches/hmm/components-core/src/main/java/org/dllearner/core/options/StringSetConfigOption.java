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

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
	
	public StringSetConfigOption(String name, String description, Set<String> defaultValue) {
		super(name, description, defaultValue);
		
	}

	public StringSetConfigOption(String name, String description, Set<String> defaultValue, boolean mandatory, boolean requiresInit) {
		super(name, description, defaultValue, mandatory, requiresInit);	
	}	

	/* (non-Javadoc)
	 * @see org.dllearner.core.config.ConfigOption#getValueTypeAsJavaString()
	 */
	@Override
	public String getValueTypeAsJavaString(){
		return "Set<String>";
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.config.ConfigOption#getJavaImports()
	 */
	@Override
	public SortedSet<String> getJavaImports() {
		SortedSet<String> ret = new TreeSet<String>();
		ret.add("java.util.Set");
		return ret;
	
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
		if (!(object instanceof Set<?>))
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
	public String getValueFormatting(Set<String> value) {
		String back = "";
		if (value != null && !name.equals("positiveExamples") && !name.equals("negativeExamples")) {
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
		if (value != null && name.equals("positiveExamples")) {
			for (String i : value) {
				back += "\n+\"" + i + "\"";
			}
			return back + "\n";
		}
		// negative examples
		if (value != null && name.equals("negativeExamples")) {
			int count = 0;
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
