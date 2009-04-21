/**
 * Copyright (C) 2007-2009, Jens Lehmann
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
package org.dllearner.scripts.matching;

import java.net.URI;

/**
 * A geo location in DBpedia.
 * 
 * @author Jens Lehmann
 *
 */
public class DBpediaPoint extends Point {

	private URI uri;
	
	private String label;
	
	private String[] classes;

	// decimal count in latitude value => indicator for size of object (no or low
	// number of decimals indicates a large object)
	private int decimalCount;
	
	public DBpediaPoint(URI uri, String label, String[] classes, double geoLat, double geoLong, int decimalCount) {
		super(geoLat,geoLong);
		this.uri = uri;
		this.label = label;
		this.classes = classes;
		this.decimalCount = decimalCount;
	}
	
	/**
	 * @return the uri
	 */
	public URI getUri() {
		return uri;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	
	public String[] getClasses() {
		return classes;
	}

	/**
	 * @return the decimalCount
	 */
	public int getDecimalCount() {
		return decimalCount;
	}	
	
	@Override
	public String toString() {
		String str = uri + ", \"" + label + "\", " + geoLat + ", " + geoLong + " (classes: ";
		for(String clazz : classes) {
			str += clazz + " ";
		}
		return str + ")";
	}
}
