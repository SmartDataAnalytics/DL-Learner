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
 * A geo location.
 * 
 * @author Jens Lehmann
 *
 */
public class Point {
	
	protected double geoLat;
	
	protected double geoLong;
	
	protected URI uri;
	
	protected POIClass poiClass;
	
	public Point(URI uri, POIClass poiClass, double geoLat, double geoLong) {
		this.uri = uri;
		this.poiClass = poiClass;
		this.geoLat = geoLat;
		this.geoLong = geoLong;
	}

	public double getGeoLat() {
		return geoLat;
	}

	public double getGeoLong() {
		return geoLong;
	}
	
	public URI getUri() {
		return uri;
	}	
	
	public POIClass getPoiClass() {
		return poiClass;
	}	
	
}
