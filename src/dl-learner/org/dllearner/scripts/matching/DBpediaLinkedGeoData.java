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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

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

	// chose between nt and csv
	private static String dbpediaFileFormat = "csv";
	private static File dbpediaFile =  new File("log/DBpedia_POIs." + dbpediaFileFormat);	
	private static boolean regenerateFile = false;
	
	private static File matchingFile = new File("log/DBpedia_GeoData_Links.nt");
	
	private static SparqlEndpoint dbpediaEndpoint = SparqlEndpoint.getEndpointLOCALDBpedia();
	private static SparqlEndpoint geoDataEndpoint = SparqlEndpoint.getEndpointLOCALGeoData();
	
	public static void main(String[] args) throws IOException {
		
		// download all objects having geo-coordinates from DBpedia if necessary
		if(!dbpediaFile.exists() || regenerateFile) {
			createDBpediaFile();
		}
		
		FileOutputStream fos = new FileOutputStream(matchingFile, true);
		// read file point by point
		BufferedReader br = new BufferedReader(new FileReader(dbpediaFile));
		String line;
		int counter = 0;
		int matches = 0;
		while ((line = br.readLine()) != null) {
			
			// read line and convert it into an object
			String[] parts = line.split(",");
			URI uri = URI.create(parts[0]);
			String label = parts[1];
			double geoLat = new Double(parts[2]);
			double geoLong = new Double(parts[3]);
			DBpediaPoint dp = new DBpediaPoint(uri, label, geoLat, geoLong);
			
			// find match (we assume there is exactly one match)
			URI matchURI = findGeoDataMatch(dp);
			if(matchURI != null) {
				String matchStr = "<" + uri + "> <http://www.w3.org/2002/07/owl#sameAs> <" + matchURI + "> .\n";
				fos.write(matchStr.getBytes());	
				matches++;
			}
			counter++;
			
			if(counter % 1000 == 0) {
				System.out.println(counter + " points processed. " + matches + " matches found.");
			}
		}
		br.close();
		fos.close();
	}
	
	// downloads information about DBpedia into a separate file
	private static void createDBpediaFile() throws IOException {
		
		// use this to set the "chunk size" for getting DBpedia points
		int limit = 1000;
		int offset = 0;
		
		int counter = 0;
		FileOutputStream fos = new FileOutputStream(dbpediaFile, true);
		
		do {
			counter = 0;
			
			// query DBpedia for all objects having geo-coordinates
			String queryStr = "SELECT ?object, ?lat, ?long, ?label  WHERE {"; 
			queryStr += "?object <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat .";
			queryStr += "?object <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long .";
			queryStr += "?object rdfs:label ?label . }";
			queryStr += "LIMIT " + limit + " OFFSET " + offset;
			
			SparqlQuery query = new SparqlQuery(queryStr, dbpediaEndpoint);
			ResultSet rs = query.send();
				
			while(rs.hasNext()) {
				QuerySolution qs = rs.nextSolution();
				
				String object = qs.get("object").toString();
				String geoLat = qs.getLiteral("lat").getString();
				String geoLong = qs.getLiteral("long").getString();
				String label = qs.getLiteral("label").getString();
				
				String content = "";
				if(dbpediaFileFormat.equals("nt")) {
					content += "<" + object + ">" + " <http://www.w3.org/2000/01/rdf-schema#label> \"" + label + "\" .\n";
					content += "<" + object + ">" + " <http://www.w3.org/2003/01/geo/wgs84_pos#lat> \"" + geoLat + "\"^^<http://www.w3.org/2001/XMLSchema#float> .\n";
					content += "<" + object + ">" + " <http://www.w3.org/2003/01/geo/wgs84_pos#long> \"" + geoLong + "\"^^<http://www.w3.org/2001/XMLSchema#float> .\n";					
				} else {
					content += object + ",\"" + label + "\"," + geoLat + "," + geoLong + "\n"; 
				}

				fos.write(content.getBytes());
			
				counter++;
			}
			
			offset += limit;
			System.out.println(offset + " points queried.");
			
		} while(counter == limit);
			
		fos.close();		
	}
	
	private static URI findGeoDataMatch(DBpediaPoint dbpediaPoint) {
		
		// get all GeoData points close to the given point
//		SparqlQuery query = new SparqlQuery("", geoDataEndpoint);
		
		/*
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
				
		*/
		return null;
	}
}
