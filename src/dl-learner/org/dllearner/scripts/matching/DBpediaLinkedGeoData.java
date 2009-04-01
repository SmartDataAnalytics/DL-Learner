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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Computes owl:sameAs links between DBpedia and LinkedGeoData
 * (or Wikipedia and OSM).
 * 
 * @author Jens Lehmann
 *
 */
public class DBpediaLinkedGeoData {

	public static void main(String[] args) throws IOException {
		
		// we start from the DBpedia URI and try to find the corresponding 
		// OSM URI (assuming that each location having coordinates in Wikipedia also
		// exists in OSM)
		URI dbpediaURI = URI.create("http://dbpedia.org/resource/Auerbachs_Keller");
		
		int distanceThresholdMeters = 100;
		
		// use official DBpedia endpoint (switch to db0 later)
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		SPARQLTasks st = new SPARQLTasks(endpoint);
		
		// query latitude and longitude
		String query = "SELECT ?lat ?long WHERE { ";
		query += "<" + dbpediaURI + "> <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat .";
		query += "<" + dbpediaURI + "> <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long . } LIMIT 1";
		
		// perform query and read lat and long from results
		ResultSet results = st.queryAsResultSet(query);
		QuerySolution qs = results.nextSolution();
		String geoLat = qs.getLiteral("lat").getString();
		String geoLong = qs.getLiteral("long").getString();
		
		System.out.println("lat: " + geoLat + ", long: " + geoLong);
		
		URL linkedGeoDataURL = new URL("http://linkedgeodata.org/triplify/near/"+geoLat+","+geoLong+"/"+distanceThresholdMeters);
		
		// TODO: replace by SPARQL query
		
		URLConnection conn = linkedGeoDataURL.openConnection();
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuffer sb = new StringBuffer();
		String line="";
//		int pointID = 0;
		while ((line = rd.readLine()) != null)
		{	
			if(line.contains("Auerbach")) {
				System.out.println(line);
			}
			
			sb.append(line);
		}
		rd.close();
		
//		System.out.println(sb.toString());
		
	}
	
}
