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

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class represents a configuration option (without a value for the
 * option).
 * 
 * Note: Currently, handling the type of a configuration option is not
 * straightforward to implement, because Java Generics information is erased at
 * runtime. This will be fixed in Java 7, in particular JSR 308, which is due at
 * approx. the beginning of 2009.
 * 
 * @param <T> The type of the config option, e.g. Integer, String etc.
 * @author Jens Lehmann
 * 
 */
public abstract class ConfigOption<T> {

	/**
	 * Name of this option.
	 */
	protected String name;

	/**
	 * A short description explaining the effect of this option.
	 */
	protected String description;

	/**
	 * The default value of this option.
	 */
	protected T defaultValue;
	
	/**
	 * Specifies whether this option is mandatory for a component,
	 * e.g. if a value other than the default value needs to be given.
	 */
	protected boolean mandatory = false;
	
	/**
	 * Specifies whether a change of the value of the option requires
	 * running the init method of the component again. For some options
	 * (e.g. url of an OWL file component) a new run of init is needed,
	 * while others can be changed without the need to re-init the 
	 * component.
	 */
	protected boolean requiresInit = false;

	/**
	 * Calls this(name, description, null, false, true).
	 * @param name Name of config option.
	 * @param description Explanation of option.
	 */
	public ConfigOption(String name, String description) {
		this(name, description, null, false, true);
	}
	
	/**
	 * Calls this(name, description, defaultValue, false, true).
	 * @param name Name of config option.
	 * @param description Explanation of option.
	 * @param defaultValue Standard value of option.
	 */
	public ConfigOption(String name, String description, T defaultValue) {
		this(name, description, defaultValue, false, true);
	}	
	
	/**
	 * Constructs a component configuration option.
	 * @param name Name of config option.
	 * @param description Explanation of option.
	 * @param defaultValue Standard value of option.
	 * @param mandatory Specifies whether assigning a value to the option is required.
	 * @param requiresInit Says whether init() has to be called again when the option is changed.
	 */
	public ConfigOption(String name, String description,  T defaultValue, boolean mandatory, boolean requiresInit) {
		this.name = name;
		this.description = description;
		this.defaultValue = defaultValue;
		this.mandatory = mandatory;
		this.requiresInit = requiresInit;		
	}
	
	/**
	 * 
	 * @return The name of this option.
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return The textual description of this option.
	 */
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
	 * Specifies whether this option is mandatory for the component.
	 * (Mandatory means that this component cannot be initialised 
	 * without setting a value for this option.)
	 * @return True if option is mandatory for component, false otherwise.
	 */
	public boolean isMandatory() {
		return mandatory;
	}
	
	/**
	 * Specifies whether setting this option requires that the 
	 * component or any components depending on this component
	 * need to be (re-)initialised. 
	 * @return True if option requires init, false otherwise.
	 */
	public boolean requiresInit() {
		return requiresInit;
	}
	
	/**
	 * TODO Method should be removed and a mapping table in ConfigJavaGenerator 
	 * created instead.
	 * gets java imports
	 * @return Java imports.
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

	/**
	 * Checks whether the value is valid, e.g. passing 1985 as
	 * integer value for an option, which requires values between
	 * 0 and 1, is not valid.
	 * @param value A value for the option.
	 * @return True if the value is valid and false otherwise.
	 */
	public abstract boolean isValidValue(T value);
	
	public abstract String getValueTypeAsJavaString();

	//TODO maybe change the function getClass in the options to get simpleName
	public String getAllowedValuesDescription() {
		return getValueTypeAsJavaString();
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(T defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @param mandatory the mandatory to set
	 */
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}	
	
	/**
	 * @param requiresInit the requiresInit to set
	 */
	public void setRequiresInit(boolean requiresInit) {
		this.requiresInit = requiresInit;
	}

	@Override
	public String toString() {
		String mand = (isMandatory())?" (mandatory)":"";
		String defval = (defaultValue==null)?"not set":defaultValue+"";
		return  "option name: " + name + "\n" +
				"description: " + description + "\n" +
				"allowed values: " + getAllowedValuesDescription() + "\n" +
				//"values: " + getValueTypeAsJavaString() + "\n" +
				"default value: " + defval + mand+ "\n";
	}
	
	public String getJavaDocString() {
		String line = "* @param " + name + " " + description + ".\n";
		line += "* mandatory: "+isMandatory()+"| reinit necessary: "+requiresInit()+"\n";
		line += "* default value: " + defaultValue + "\n";
		return line;
	}

	/**
	 * Get a formatted value to put into configuration file.
	 * 
	 * @param value Option value.
	 * 
	 * @return A string to put into a conf file.
	 */
	public abstract String getValueFormatting(T value);

}
