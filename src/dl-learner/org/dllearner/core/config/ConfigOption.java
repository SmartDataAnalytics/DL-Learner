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

import java.util.SortedSet;
import java.util.TreeSet;

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
	
	public enum Tags {NORMAL, MANDATORY, REINIT}
	
	protected boolean mandatory = false;
	protected boolean reinitNecessary = false;

	public ConfigOption(String name, String description) {
		this(name, description, null);
	}
	
	public ConfigOption(String name, String description,  T defaultValue, Tags ...tags ) {
		this.name = name;
		this.description = description;
		this.defaultValue = defaultValue;
		boolean normal = false;
		for(Tags t:tags){
			if (t.equals(Tags.NORMAL)){
				normal =true;
			} 
		}
		for(Tags t:tags){
			if(normal){
				;//DO Nothing
			}
			else if (t.equals(Tags.MANDATORY)){
				this.mandatory = true;
			} else  if (t.equals(Tags.REINIT)){
				this.reinitNecessary = true;
			}
		}
		
	}
	
	public ConfigOption(String name, String description, T defaultValue ) {
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
	 * @return the defaultValue
	 */
	public String getDefaultValueInJava() {
		return defaultValue+"";
	}
	
	/**
	 * says, if this option is mandatory for the component
	 * @return 
	 */
	public boolean isMandatory() {
		return mandatory;
	}
	
	/**
	 * says, if this option requires that the componnent is reinitialized with init() 
	 * @return 
	 */
	public boolean isReinitNecessary() {
		return reinitNecessary;
	}
	
	/**
	 * gets java imports
	 * @return 
	 */
	public SortedSet<String> getJavaImports() {
		return new TreeSet<String>();
	}

	/**
	 * Checks whether the object has the correct type to be used as a value for
	 * this option (this method is necessary, because generic information is
	 * erased at runtime in Java).
	 * 
	 * @param object
	 *            The object to check.
	 * @return True of the type is correct, false otherwise.
	 */
	public abstract boolean checkType(Object object);

	public abstract boolean isValidValue(T value);
	
	public abstract String getValueTypeAsJavaString();

	//TODO maybe change the function getClass in the options to get simpleName
	public String getAllowedValuesDescription() {
		return getClass().toString();
	}

	
	@Override
	public String toString() {
		return "option name: " + name + "\ndescription: " + description + "\nvalues: "
				+ getAllowedValuesDescription() + "\ndefault value: " + defaultValue + "\n";
	}
	
	public String getJavaDocString() {
		String line = "* option name: " + name + "\n";
		line += "* " + description + "\n";
		//line += "* allowed values: "+ getAllowedValuesDescription() + "\n";
		line += "* default value: " + defaultValue + "\n";
		return line;
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
