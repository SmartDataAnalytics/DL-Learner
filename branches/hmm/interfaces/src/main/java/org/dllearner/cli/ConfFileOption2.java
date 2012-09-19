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


import org.dllearner.configuration.IConfigurationProperty;

/**
 * Programmatic representation of an option setting in a conf file:
 * bean.property = value;
 * 
 * @author Jens Lehmann
 *
 */
public class ConfFileOption2 implements IConfigurationProperty{

	// a boolean flag which indicates whether it is a reference to a bean (or set/list of beans)
	private boolean isBeanRef;

    private boolean isBeanReferenceCollection;
	
	private String beanName;
	
	private String propertyName;
	
	private String propertyValue;
	
	private Class<?> propertyType;
	
	// the object should be either a primitive, a Collection<String> or a Map<String,String>,
	// the actual mapping from Strings to datatypes is later done e.g. by property editors
	private Object valueObject;
	
	public ConfFileOption2() {
		
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

	public Object getValueObject() {
		return valueObject;
	}

	public void setValueObject(Object valueObject) {
		this.valueObject = valueObject;
	}

	public boolean isBeanRef() {
		return isBeanRef;
	}

	public void setBeanRef(boolean isBeanRef) {
		this.isBeanRef = isBeanRef;
	}

    @Override
    public String getName() {
        return getPropertyName();
    }

    @Override
    public Object getValue() {
        return getValueObject();
    }

    @Override
    public boolean isBeanReference() {
        return isBeanRef();
    }

    @Override
    public boolean isBeanReferenceCollection() {
        return isBeanReferenceCollection;
    }

    public void setBeanReferenceCollection(boolean beanReferenceCollection) {
        isBeanReferenceCollection = beanReferenceCollection;
    }

}
