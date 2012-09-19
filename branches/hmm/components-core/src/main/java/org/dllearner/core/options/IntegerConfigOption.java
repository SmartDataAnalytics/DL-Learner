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
 * A configuration option, which allows values of type integer. A minimum and
 * maximum value of the argument can optionally be specified.
 * 
 * @author Jens Lehmann
 * 
 */
public class IntegerConfigOption extends ConfigOption<Integer> {

	private int lowerLimit = Integer.MIN_VALUE;
	private int upperLimit = Integer.MAX_VALUE;

	

	public IntegerConfigOption(String name, String description, Integer defaultValue, boolean mandatory, boolean requiresInit) {
		super(name, description, defaultValue, mandatory, requiresInit);
		
	}

	public IntegerConfigOption(String name, String description, Integer defaultValue) {
		super(name, description, defaultValue);
		
	}

	public IntegerConfigOption(String name, String description) {
		super(name, description);
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.config.ConfigOption#getValueTypeAsJavaString()
	 */
	@Override
	public String getValueTypeAsJavaString(){
		return "int";
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.ConfigOption#isValidValue(java.lang.Object)
	 */
	@Override
	public boolean isValidValue(Integer value) {
		if (value >= lowerLimit && value <= upperLimit)
			return true;
		else
			return false;
	}

	/**
	 * @return the The lowest possible value for this configuration option.
	 */
	public int getLowerLimit() {
		return lowerLimit;
	}

	/**
	 * @param lowerLimit
	 *            The lowest possible value for this configuration option.
	 */
	public void setLowerLimit(int lowerLimit) {
		this.lowerLimit = lowerLimit;
	}

	/**
	 * @return the The highest possible value for this configuration option.
	 */
	public int getUpperLimit() {
		return upperLimit;
	}

	/**
	 * @param upperLimit
	 *            The highest possible value for this configuration option.
	 */
	public void setUpperLimit(int upperLimit) {
		this.upperLimit = upperLimit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.ConfigOption#checkType(java.lang.Object)
	 */
	@Override
	public boolean checkType(Object object) {
		return (object instanceof Integer);
	}

	@Override
	public String getAllowedValuesDescription() {
		String str = getValueTypeAsJavaString()+" ";
		if (lowerLimit != Integer.MIN_VALUE)
			str += " min " + lowerLimit;
		if (upperLimit != Integer.MAX_VALUE)
			str += " max " + upperLimit;
		return str;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.config.ConfigOption#getValueFormatting(java.lang.Object)
	 */
	@Override
	public String getValueFormatting(Integer value) {
		if (value != null)
			return value.toString() + ";";
		else
			return null;
	}
}
