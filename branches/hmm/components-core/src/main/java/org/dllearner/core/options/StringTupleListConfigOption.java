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

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.utilities.datastructures.StringTuple;

/**
 * A list if string tuples, for instance for specifying several parameters or
 * replacement rules.
 * 
 * @author Jens Lehmann
 */
public class StringTupleListConfigOption extends ConfigOption<List<StringTuple>> {

	
	public StringTupleListConfigOption(String name, String description, List<StringTuple> defaultValue, boolean mandatory, boolean requiresInit) {
		super(name, description, defaultValue, mandatory, requiresInit);
		
	}

	public StringTupleListConfigOption(String name, String description, List<StringTuple> defaultValue) {
		super(name, description, defaultValue);
		
	}

	public StringTupleListConfigOption(String name, String description) {
		super(name, description);
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.config.ConfigOption#getValueTypeAsJavaString()
	 */
	@Override
	public String getValueTypeAsJavaString(){
		return "List<StringTuple>";
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.config.ConfigOption#getJavaImports()
	 */
	@Override
	public SortedSet<String> getJavaImports() {
		SortedSet<String> ret = new TreeSet<String>();
		ret.add("java.util.List");
		ret.add("org.dllearner.utilities.datastructures.StringTuple");
		return ret;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.config.ConfigOption#checkType(java.lang.Object)
	 */
	@Override
	public boolean checkType(Object object) {
		if (!(object instanceof List<?>))
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
	public String getValueFormatting(List<StringTuple> value) {
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
