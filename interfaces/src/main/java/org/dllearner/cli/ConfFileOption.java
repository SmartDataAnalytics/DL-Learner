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

package org.dllearner.cli;

import java.util.List;
import java.util.Set;

import org.dllearner.utilities.datastructures.StringTuple;

/**
 * Represents one configuration option in a conf file, e.g.
 * refinement.horizontalExpansionFactor = 0.6.
 * 
 * @author Jens Lehmann
 *
 */
public class ConfFileOption {

	private boolean containsSubOption = true;
	private boolean isIntegerOption = false;
	private boolean isDoubleOption = false;
	private boolean isStringOption = false;
	private boolean isSetOption = false;
	private boolean isListOption = false;
	private String option;
	private String subOption;
	private String stringValue;
	private int intValue;
	private double doubleValue;
	private Set<String> setValues;
	private List<StringTuple> listTuples;
	
	public ConfFileOption(String option, String value) {
		this(option, null, value);
		containsSubOption = false;
	}
	
	public ConfFileOption(String option, String subOption, String value) {
		this.option = option;
		this.subOption = subOption;
		stringValue = value;
		isStringOption = true;
	}
	
	public ConfFileOption(String option, int value) {
		this(option, null, value);
		containsSubOption = false;
	}
	
	public ConfFileOption(String option, String subOption, int value) {
		this.option = option;
		this.subOption = subOption;
		intValue = value;
		isIntegerOption = true;
	}

	public ConfFileOption(String option, double value) {
		this(option, null, value);
		containsSubOption = false;
	}
	
	public ConfFileOption(String option, String subOption, double value) {
		this.option = option;
		this.subOption = subOption;
		doubleValue = value;
		isDoubleOption = true;
	}
	
	public ConfFileOption(String option, Set<String> values) {
		this(option, null, values);
		containsSubOption = false;
	}
	
	public ConfFileOption(String option, String subOption, Set<String> values) {
		this.option = option;
		this.subOption = subOption;
		isSetOption = true;
		setValues = values;
	}
	
	public ConfFileOption(String option, List<StringTuple> tuples) {
		this(option, null, tuples);
		containsSubOption = false;
	}
	
	public ConfFileOption(String option, String subOption, List<StringTuple> tuples) {
		this.option = option;
		this.subOption = subOption;
		isListOption = true;
		listTuples = tuples;
	}	
	
	public boolean containsSubOption() {
		return containsSubOption;
	}

	public int getIntValue() {
		return intValue;
	}
	
	public String getOption() {
		return option;
	}

	public String getStringValue() {
		return stringValue;
	}

	public String getSubOption() {
		return subOption;
	}
	
	public double getDoubleValue() {
		return doubleValue;
	}

	public Set<String> getSetValues() {
		return setValues;
	}	
	
	public List<StringTuple> getListTuples() {
		return listTuples;
	}
	
	public Object getValue() {
		if(isIntegerOption)
			return intValue;
		else if(isDoubleOption)
			return doubleValue;
		else if(isStringOption)
			return stringValue;
		else if(isSetOption)
			return setValues;
		else
			return listTuples;
	}
		
	/**
	 * 
	 * @return The class of the value of the conf file option;
	 */
	public Class<?> getType() {
		if(isIntegerOption)
			return Integer.class;
		else if(isDoubleOption)
			return Double.class;
		else if(isStringOption)
			return String.class;
		else
			return Set.class;		
	}
	
	public boolean isIntegerOption() {
		return isIntegerOption;
	}

	public boolean isDoubleOption() {
		return isDoubleOption;
	}
	
	public boolean isNumeric() {
		return (isIntegerOption || isDoubleOption);
	}
	
	public boolean isStringOption() {
		return isStringOption;
	}	
	
	public boolean isSetOption() {
		return isSetOption;
	}	
	
	public boolean isListOption() {
		return isListOption;
	}	
	
	@Override
	public String toString() {
		String completeOption = "Configuration Option: ";
		if(containsSubOption)
			completeOption += option + "." + subOption;
		else
			completeOption += option;
		if(isNumeric())
			if(isIntegerOption)
				return completeOption + "=" + intValue;
			else
				return completeOption + "=" + doubleValue;
		else
			if(isListOption)
				return completeOption + "=" + listTuples;
			else if(isSetOption)
				return completeOption + "=" + setValues;
			else
				return completeOption + "=" + stringValue;
	}
	
	public String getFullName() {
		if(containsSubOption)
			return option + "." + subOption;
		else
			return option;
	}

}
