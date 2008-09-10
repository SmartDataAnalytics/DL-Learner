/**
 * Copyright (C) 2007-2008, Jens Lehmann
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

import java.net.URL;

/**
 * Option which has an URL as value.
 * 
 * @author Jens Lehmann
 *
 */
public class URLConfigOption extends ConfigOption<URL> {

	public URLConfigOption(String name, String description) {
		super(name, description);
	}	
	
	public URLConfigOption(String name, String description, URL defaultValue) {
		super(name, description, defaultValue);
	}	
	
	public URLConfigOption(String name, String description, URL defaultValue, boolean mandatory,
			boolean requiresInit) {
		super(name, description, defaultValue, mandatory, requiresInit);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.config.ConfigOption#checkType(java.lang.Object)
	 */
	@Override
	public boolean checkType(Object object) {
		return (object instanceof URL);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.config.ConfigOption#getValueFormatting(java.lang.Object, java.lang.Integer)
	 */
	@Override
	public String getValueFormatting(URL value) {
		return value.toString();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.config.ConfigOption#getValueTypeAsJavaString()
	 */
	@Override
	public String getValueTypeAsJavaString() {
		return "URL";
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.config.ConfigOption#isValidValue(java.lang.Object)
	 */
	@Override
	public boolean isValidValue(URL value) {
		return true;
	}



}
