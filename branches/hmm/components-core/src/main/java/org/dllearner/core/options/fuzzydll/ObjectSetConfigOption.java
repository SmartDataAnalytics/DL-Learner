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

package org.dllearner.core.options.fuzzydll;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.options.ConfigOption;

/**
 * A set of strings.
 * 
 * @author Josue Iglesias
 * 
 */
public class ObjectSetConfigOption extends ConfigOption<Set<Object>> {

	public ObjectSetConfigOption(String name, String description) {
		super(name, description);
		
	}	
	
	public ObjectSetConfigOption(String name, String description, Set<Object> defaultValue) {
		super(name, description, defaultValue);
		
	}

	public ObjectSetConfigOption(String name, String description, Set<Object> defaultValue, boolean mandatory, boolean requiresInit) {
		super(name, description, defaultValue, mandatory, requiresInit);	
	}	

	/* (non-Javadoc)
	 * @see org.dllearner.core.config.ConfigOption#getValueTypeAsJavaString()
	 */
	@Override
	public String getValueTypeAsJavaString(){
		return "Set<Object>";
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
	public boolean isValidValue(Set<Object> value) {
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
			if (!(element instanceof Object))
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
	//
	// TODO this method has been copied from StringSetConfigOption class; it should be addapted to FuzzyExample
	//
	public String getValueFormatting(Set<Object> value) {
		String back = "";
		if (value != null && !name.equals("positiveExamples") && !name.equals("negativeExamples")) {
			Integer count = 0;
			back = "{";
			for (Object i : value) {
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
			for (Object i : value) {
				back += "\n+\"" + i + "\"";
			}
			return back + "\n";
		}
		// negative examples
		if (value != null && name.equals("negativeExamples")) {
			int count = 0;
			for (Object i : value) {
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
