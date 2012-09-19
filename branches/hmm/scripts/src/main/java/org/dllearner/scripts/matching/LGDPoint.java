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
 * A LinkedGeoData point.
 * 
 * @author Jens Lehmann
 *
 */
public class LGDPoint extends Point {

	private double name;
	
	public LGDPoint(URI uri, double geoLat, double geoLong) {
		super(uri, null, geoLat, geoLong);
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
	
	public static String getSPARQLRestriction(POIClass poiClass, String variable) {
		switch(poiClass) {
		case CITY : return "{ " + variable + " <http://linkedgeodata.org/vocabulary#place> \"city\" } UNION {" + variable + " <http://linkedgeodata.org/vocabulary#place> \"village\" } UNION {" + variable + " <http://linkedgeodata.org/vocabulary#place> \"town\" } UNION {" + variable + " <http://linkedgeodata.org/vocabulary#place> \"suburb\" }";
		case UNIVERSITY : return variable + " <http://linkedgeodata.org/vocabulary#amenity> \"university\" . ";
		case SCHOOL : return variable + " <http://linkedgeodata.org/vocabulary#amenity> \"school\" . ";
		case AIRPORT : return variable + " <http://linkedgeodata.org/vocabulary#aeroway> \"aerodrome\" . ";
		case LAKE : return variable + " <http://linkedgeodata.org/vocabulary#natural> \"water\" . ";
		case COUNTRY : return variable + " <http://linkedgeodata.org/vocabulary#place> \"country\" . ";
		case RAILWAY_STATION : return variable + " <http://linkedgeodata.org/vocabulary#railway> \"station\" . ";
		case ISLAND : return variable + " <http://linkedgeodata.org/vocabulary#place> \"island\" . ";
		case STADIUM : return variable + " <http://linkedgeodata.org/vocabulary#leisure> \"stadium\" . ";
		case RIVER : return variable + " <http://linkedgeodata.org/vocabulary#waterway> ?something . ";
		case BRIDGE : return variable + " <http://linkedgeodata.org/vocabulary#bridge> ?something . ";				
		case MOUNTAIN : return variable + " <http://linkedgeodata.org/vocabulary#natural> \"peak\" . ";				
		case RADIO_STATION : return variable + " <http://linkedgeodata.org/vocabulary#amenity> \"studio\" . ";				
		case LIGHT_HOUSE : return variable + " <http://linkedgeodata.org/vocabulary#man_made> \"lighthouse\" . ";				
		default: throw new Error("Cannot restrict.");
		}
	}
}
