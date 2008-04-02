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

/**
 * This class represents a configuration option (without a value for the
 * option).
 * 
 * Note: Currently, handling the type of a configuration option is not
 * straightforward to implement, because Java Generics information is erased at
 * runtime. This will be fixed in Java 7, in particular JSR 308, which is due at
 * approx. the end of 2008.
 * 
 * @author Jens Lehmann
 * 
 */
public abstract class ConfigOption<T> {

	protected String name;

	protected String description;

	protected T defaultValue;

	public ConfigOption(String name, String description) {
		this(name, description, null);
	}

	public ConfigOption(String name, String description, T defaultValue) {
		this.name = name;
		this.description = description;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * @return the defaultValue
	 */
	public T getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Checks whether the object has the correct type to be used as a value for
	 * this option (this method is necessary, because generic information is
	 * erased at runtime in Java).
	 * 
	 * @param object
	 *            The object to check.
	 * @return
	 */
	public abstract boolean checkType(Object object);

	public abstract boolean isValidValue(T value);

	//TODO maybe change the function getClass in the options to get simpleName
	public String getAllowedValuesDescription() {
		return getClass().toString();
	}

	
	@Override
	public String toString() {
		return "option name: " + name + "\ndescription: " + description + "\nvalues: "
				+ getAllowedValuesDescription() + "\ndefault value: " + defaultValue + "\n";
	}

	/**
	 * Get a formatted value to put into configuration file.
	 * 
	 * @param value
	 * @param special
	 *            0 for normal output. 
	 *            1 for positiveExamples. 
	 *            2 for negativeExamples.
	 * 
	 * @return a string to put into a file
	 */
	public abstract String getValueFormatting(T value, Integer special);

}
