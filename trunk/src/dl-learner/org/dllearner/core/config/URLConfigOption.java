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
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Option which has an URL as value.
 * 
 * @author Jens Lehmann
 *
 */
public class URLConfigOption extends ConfigOption<URL> {

	private boolean refersToFile = false;
	
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

	/**
	 * Returns whether the URI can refer to a file or not, e.g. the
	 * URL of an OWL knowledge source does refer to a file whereas
	 * the URL of a SPARQL endpoint cannot refer to a file. The distinction
	 * can be useful in GUIs (e.g. they may offer to choose a local file). 
	 * @return the refersToFile
	 */
	public boolean refersToFile() {
		return refersToFile;
	}

	/**
	 * @param refersToFile Set whether this option can refer to a file.
	 */
	public void setRefersToFile(boolean refersToFile) {
		this.refersToFile = refersToFile;
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
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.config.ConfigOption#getJavaImports()
	 */
	@Override
	public SortedSet<String> getJavaImports() {
		SortedSet<String> ret = new TreeSet<String>();
		ret.add("java.net.URL");
		return ret;
	}



}
