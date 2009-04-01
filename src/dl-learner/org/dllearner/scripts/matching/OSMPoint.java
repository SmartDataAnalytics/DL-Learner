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

/**
 * @author Jens Lehmann
 *
 */
public class OSMPoint {

	private long id;
	
	private double geoLat;
	
	private double geoLong;
	
	private double name;
	
	public OSMPoint(long id) {
		this.id = id;
	}

	/**
	 * @return the geoLat
	 */
	public double getGeoLat() {
		return geoLat;
	}

	/**
	 * @param geoLat the geoLat to set
	 */
	public void setGeoLat(double geoLat) {
		this.geoLat = geoLat;
	}

	/**
	 * @return the geoLong
	 */
	public double getGeoLong() {
		return geoLong;
	}

	/**
	 * @param geoLong the geoLong to set
	 */
	public void setGeoLong(double geoLong) {
		this.geoLong = geoLong;
	}

	/**
	 * @return the name
	 */
	public double getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(double name) {
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	
}
