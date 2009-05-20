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
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * A geo location in DBpedia.
 * 
 * @author Jens Lehmann
 *
 */
public class DBpediaPoint extends Point {
	
	private String label;
	
	private String[] classes;

	// decimal count in latitude value => indicator for size of object (no or low
	// number of decimals indicates a large object)
	private int decimalCount;
	
	Pattern pattern = Pattern.compile("\\w+");
	
	/**
	 * Constructs a DBpedia point using SPARQL.
	 * @param uri URI of DBpedia resource.
	 */
	public DBpediaPoint(URI uri) throws Exception {
		super(uri, null, 0,0);
		this.uri = uri;
		
		// construct DBpedia query
		String queryStr = "SELECT ?lat, ?long, ?label, ?type  WHERE {"; 
		queryStr += "<"+uri+"> <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat .";
		queryStr += "<"+uri+"> <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long .";
		queryStr += "<"+uri+"> rdfs:label ?label . ";
		queryStr += "OPTIONAL { <"+uri+"> rdf:type ?type . ";
		queryStr += "FILTER (!(?type LIKE <http://dbpedia.org/ontology/Resource>)) .";
		queryStr += "FILTER (?type LIKE <http://dbpedia.org/ontology/%>) .";
		queryStr += "} }";
		
		SparqlQuery query = new SparqlQuery(queryStr, DBpediaLinkedGeoData.dbpediaEndpoint);
		ResultSet rs = query.send();
		classes = new String[] { };
		List<String> classList = new LinkedList<String>();
		
		if(!rs.hasNext()) {
			throw new Exception("cannot construct point for " + uri + " (latitude/longitude missing?)");
		}
		
		while(rs.hasNext()) {
			QuerySolution qs = rs.nextSolution();
			geoLat = qs.getLiteral("lat").getDouble();
			if(((Double)geoLat).toString().contains("E")) {
				geoLat = 0.0;
			}
			geoLong = qs.getLiteral("long").getDouble();
			if(((Double)geoLong).toString().contains("E")) {
				geoLong = 0.0;
			}	
			label = qs.getLiteral("label").getString();
			if(qs.contains("type")) {
				classList.add(qs.get("type").toString());
			}
		}
		
		classes = classList.toArray(classes);
		poiClass = getPOIClass(classes);
	}
	
	public DBpediaPoint(URI uri, String label, String[] classes, double geoLat, double geoLong, int decimalCount) {
		super(uri, null, geoLat,geoLong);
		this.label = label;
		this.classes = classes;
		this.decimalCount = decimalCount;
		poiClass = getPOIClass(classes);
	}
	
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * 
	 * @return Returns only first characters until a special symbol occurs, i.e. instead
	 * of "Stretton, Derbyshire" it returns "Stretton". 
	 */
	public String getPlainLabel() {
		Matcher matcher = pattern.matcher(label);
		matcher.find();
		return label.substring(0, matcher.end());
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
	
	private POIClass getPOIClass(String[] classes) {
		for(String clazz : classes) {
			if(clazz.equals("http://dbpedia.org/ontology/City")) {
				return POIClass.CITY;
			}
		}
		return null;
	}
}
