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
 * Contains all types of points of interests (POIs) we are
 * interested in.
 * 
 * @author Jens Lehmann
 *
 */
public enum POIClass {

	// 50 km box
	CITY (50000), 
	
	// 10 km box
	AIRPORT (10000),
	
	// 10 km box
	UNIVERSITY (10000),
	
	// 10 km box - usage unclear
//	MUNICIPALITY (10000),
	
	SCHOOL (10000),
	
	RAILWAY_STATION (10000),
	
	// 200 km box (largest lake is the Caspian Sea with 1200 km length)
	LAKE (200000),
	
	// 1 km box
	BRIDGE (1000),
	
	// 10 km box
	MOUNTAIN (10000),
	
	// 1000 km box (continents are not counted as islands in UMBEL and DBpedia ontology)
	ISLAND (1000000),
	
	// 2 km box
	STADIUM (2000),
	
	// 1000 km box
	RIVER (1000000),
	
	// 1 km box
	RADIO_STATION (1000),
	
	// 1 km box
	LIGHT_HOUSE (1000),	
	
	// 2000 km box (Russia has radius 4000 km)
	COUNTRY (2000000);
	
	private double maxBox;
	
	POIClass(double maxBox) {
		this.maxBox = maxBox;
	}
	
	/**
	 * Maximum distance coordinates and actual position of
	 * this POI type can differ in meters. Retrieving POIs within
	 * the box specified by this distance should always contain
	 * the POI itself.
	 *  
	 * @return The distance in meters different typical POIs for this
	 * type can differ.
	 */
	public double getMaxBox() {
		return maxBox;
	}
}
