package org.dllearner;

import java.util.Set;

/**
 * Repraesentiert eine Konfigurationsoption. Es werden verschiedene Optionen angeboten.
 * 
 * @author jl
 *
 */
public class ConfigurationOption {

	private boolean containsSubOption = true;
	private boolean isIntegerOption = false;
	private boolean isNumeric = false;
	private boolean isSetOption = false;
	private String option;
	private String subOption;
	private String strValue;
	private int intValue;
	private double doubleValue;
	private Set<String> setValues;
	
	public ConfigurationOption(String option, String value) {
		this(option, null, value);
		containsSubOption = false;
	}
	
	public ConfigurationOption(String option, String subOption, String value) {
		this.option = option;
		this.subOption = subOption;
		strValue = value;
	}
	
	public ConfigurationOption(String option, int value) {
		this(option, null, value);
		containsSubOption = false;
	}
	
	public ConfigurationOption(String option, String subOption, int value) {
		this.option = option;
		this.subOption = subOption;
		intValue = value;
		isIntegerOption = true;
		isNumeric = true;
	}

	public ConfigurationOption(String option, double value) {
		this(option, null, value);
		containsSubOption = false;
	}
	
	public ConfigurationOption(String option, String subOption, double value) {
		this.option = option;
		this.subOption = subOption;
		doubleValue = value;
		// isIntegerOption = false;
		isNumeric = true;
	}
	
	public ConfigurationOption(String option, Set<String> values) {
		this(option, null, values);
		containsSubOption = false;
	}
	
	public ConfigurationOption(String option, String subOption, Set<String> values) {
		this.option = option;
		this.subOption = subOption;
		isSetOption = true;
		setValues = values;
	}
	
	public boolean containsSubOption() {
		return containsSubOption;
	}

	public int getIntValue() {
		return intValue;
	}

	public boolean isIntegerOption() {
		return isIntegerOption;
	}

	public String getOption() {
		return option;
	}

	public String getStrValue() {
		return strValue;
	}

	public String getSubOption() {
		return subOption;
	}
	
	public double getDoubleValue() {
		return doubleValue;
	}

	public boolean isNumeric() {
		return isNumeric;
	}	
	
	public boolean isSetOption() {
		return isSetOption;
	}	
	
	@Override
	public String toString() {
		String completeOption = "Configuration Option: ";
		if(containsSubOption)
			completeOption += option + "." + subOption;
		else
			completeOption += option;
		if(isNumeric)
			if(isIntegerOption)
				return completeOption + "=" + intValue;
			else
				return completeOption + "=" + doubleValue;
		else
			return completeOption + "=" + strValue;
	}

	public Set<String> getSetValues() {
		return setValues;
	}

}
