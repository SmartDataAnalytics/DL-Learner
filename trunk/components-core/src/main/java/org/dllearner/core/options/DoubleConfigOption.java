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
 * Represents a configuration option with values of type value. Similar to the
 * integer option a minimum and a maximum value can specified.
 * 
 * @author Jens Lehmann
 * 
 */
public class DoubleConfigOption extends ConfigOption<Double> {

	private double lowerLimit = Double.MIN_VALUE;
	private double upperLimit = Double.MAX_VALUE;

	public DoubleConfigOption(String name, String description, Double defaultValue) {
		super(name, description, defaultValue);
		
	}

	public DoubleConfigOption(String name, String description) {
		super(name, description);
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.config.ConfigOption#getValueTypeAsJavaString()
	 */
	@Override
	public String getValueTypeAsJavaString(){
		return "double";
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.ConfigOption#isValidValue(java.lang.Object)
	 */
	@Override
	public boolean isValidValue(Double value) {
		double tolerance = 0.0001;
		return ((value >= lowerLimit-tolerance) && (value <= upperLimit+tolerance));
	}

	/**
	 * @return the The lowest possible value for this configuration option.
	 */
	public double getLowerLimit() {
		return lowerLimit;
	}

	/**
	 * @param lowerLimit
	 *            The lowest possible value for this configuration option.
	 */
	public void setLowerLimit(double lowerLimit) {
		this.lowerLimit = lowerLimit;
	}

	/**
	 * @return the The highest possible value for this configuration option.
	 */
	public double getUpperLimit() {
		return upperLimit;
	}

	/**
	 * @param upperLimit
	 *            The highest possible value for this configuration option.
	 */
	public void setUpperLimit(double upperLimit) {
		this.upperLimit = upperLimit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.ConfigOption#checkType(java.lang.Object)
	 */
	@Override
	public boolean checkType(Object object) {
		return (object instanceof Double);
	}

	@Override
	public String getAllowedValuesDescription() {
		String str = getValueTypeAsJavaString()+" ";//getClass().toString();
		if (lowerLimit != Double.MIN_VALUE)
			str += " min " + lowerLimit;
		if (upperLimit != Double.MAX_VALUE)
			str += " max " + upperLimit;
		return str;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.config.ConfigOption#getValueFormatting(java.lang.Object)
	 */
	@Override
	public String getValueFormatting(Double value) {
		if (value != null)
			return value.toString() + ";";
		else
			return null;
	}

}
