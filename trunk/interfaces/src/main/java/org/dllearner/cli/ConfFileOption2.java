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


/**
 * Programmatic representation of an option setting in a conf file:
 * bean.property = value;
 * 
 * TODO: Class is not stable yet.
 * 
 * @author Jens Lehmann
 *
 */
public class ConfFileOption2 {

	// a boolean flag which indicates whether the value was in quotes
	// TODO: alternatively, we could use a flag "isBeanReferrence" - bean references are (currently) 
	// those options which do not have quotes, but have type "String" or "Set"
	private boolean inQuotes;
	
	private String beanName;
	
	private String propertyName;
	
	private String propertyValue;
	
	private Class<?> propertyType;
	
	// TODO: Do we want to store the actual value as object here or leave it up to
	// the corresponding PropertyEditor to create it?
	// WARNING: This feature does not work in conjunction with prefix post-processing yet!
	@Deprecated
	private Object valueObject;
	
	public ConfFileOption2() {
		
	}

	public boolean isInQuotes() {
		return inQuotes;
	}

	public void setInQuotes(boolean inQuotes) {
		this.inQuotes = inQuotes;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	public Class<?> getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(Class<?> propertyType) {
		this.propertyType = propertyType;
	}

	@Deprecated
	public Object getValueObject() {
		return valueObject;
	}

	@Deprecated
	public void setValueObject(Object valueObject) {
		this.valueObject = valueObject;
	}
	
}
