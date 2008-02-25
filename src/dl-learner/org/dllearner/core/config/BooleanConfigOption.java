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
 * @author Jens Lehmann
 *
 */
public class BooleanConfigOption extends ConfigOption<Boolean> {

	public BooleanConfigOption(String name, String description) {
		super(name, description);
	}

	public BooleanConfigOption(String name, String description, boolean defaultValue) {
		super(name, description, defaultValue);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.ConfigOption#checkType(java.lang.Object)
	 */
	@Override
	public boolean checkType(Object object) {
		return (object instanceof Boolean);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.ConfigOption#isValidValue(java.lang.Object)
	 */
	@Override
	public boolean isValidValue(Boolean value) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.config.ConfigOption#getValueFormatting(java.lang.Object)
	 */
	@Override
	public String getValueFormatting(Boolean value) {
		return value.toString();
	}

}
