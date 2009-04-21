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
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.LinkedList;

import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.utilities.Files;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.wcohen.ss.Jaro;
import com.wcohen.ss.api.StringDistance;

/**
 * Computes owl:sameAs links between DBpedia and LinkedGeoData
 * (or Wikipedia and OSM).
 * 
 * @author Jens Lehmann
 *
 */
public class DBpediaLinkedGeoData {

	// chose between nt and dat
	private static String dbpediaFileFormat = "dat";
	private static File dbpediaFile =  new File("log/DBpedia_POIs." + dbpediaFileFormat);	
	private static boolean regenerateFile = false;
	
	private static File matchingFile = new File("log/DBpedia_GeoData_Links.nt");
	private static File missesFile = new File("log/DBpedia_GeoData_Misses.dat");
	private static double scoreThreshold = 0.8;
	private static StringDistance distance = new Jaro();
	
	private static SparqlEndpoint dbpediaEndpoint = SparqlEndpoint.getEndpointLOCALDBpedia();
	private static SparqlEndpoint geoDataEndpoint = SparqlEndpoint.getEndpointLOCALGeoData();
	
	// read in DBpedia ontology such that we perform taxonomy reasoning
//	private static ReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.DBPEDIA_OWL);
//	private static ClassHierarchy hierarchy = reasoner.getClassHierarchy();
	
	// true = SPARQL is used for retrieving close points;
	// false = Triplify spatial extension is used
	private static boolean useSparqlForGettingNearbyPoints = false;
	
	public static void main(String[] args) throws IOException {
		
		// download all objects having geo-coordinates from DBpedia if necessary
		if(!dbpediaFile.exists() || regenerateFile) {
			createDBpediaFile();
		}
		
		Files.clearFile(matchingFile);
		Files.clearFile(missesFile);
		FileOutputStream fos = new FileOutputStream(matchingFile, true);
		FileOutputStream fosMiss = new FileOutputStream(missesFile, true);
		// read file point by point
		BufferedReader br = new BufferedReader(new FileReader(dbpediaFile));
		String line;
		int counter = 0;
		int matches = 0;
		
		// temporary variables needed while reading in file
		int itemCount = 0;
		URI uri = null;
		String label = null;
		String[] classes = null;
		int decimalCount = 0;
		Double geoLat = null;
		Double geoLong = null;
		
		while ((line = br.readLine()) != null) {
			
			if(line.isEmpty()) {
				DBpediaPoint dp = new DBpediaPoint(uri, label, classes, geoLat, geoLong, decimalCount);
				
				// find match (we assume there is exactly one match)
				URI matchURI = findGeoDataMatch(dp);
				if(matchURI == null) {
					String missStr = dp.toString() + "\n";
					fosMiss.write(missStr.getBytes());
				} else {
					String matchStr = "<" + dp.getUri() + "> <http://www.w3.org/2002/07/owl#sameAs> <" + matchURI + "> .\n";
					fos.write(matchStr.getBytes());	
					matches++;
				}
				counter++;
				
				if(counter % 1000 == 0) {
					System.out.println(counter + " points processed. " + matches + " matches found.");
				}				
				
				itemCount = 0;
			} else {
				switch(itemCount) {
				case 0 : uri = URI.create(line); break;
				case 1 : label = line; break;
				case 2 : classes = line.substring(1, line.length()).split(","); break;
				case 3 : 
					geoLat = new Double(line);
					// we avoid "computerized scientific notation" e.g. 9.722222457639873E-4
					// since it causes problems in the REST interface
					if(geoLat.toString().contains("E")) {
						geoLat = 0.0;
					}
					decimalCount = 0; 
					String[] tmp = line.split(".");
					if(tmp.length == 2) {
						decimalCount = tmp[1].length();
					}
					break;
				case 4: geoLong = new Double(line); 
				if(geoLong.toString().contains("E")) {
					geoLong = 0.0;
				}
				}
							
				itemCount++;
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
		int points = 0;
		FileOutputStream fos = new FileOutputStream(dbpediaFile, true);
		
		do {
			counter = 0;
			
			// query DBpedia for all objects having geo-coordinates
			String queryStr = "SELECT ?object, ?lat, ?long, ?label, ?type  WHERE {"; 
			queryStr += "?object <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat .";
			queryStr += "?object <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long .";
			queryStr += "?object rdfs:label ?label . ";
			queryStr += "OPTIONAL { ?object rdf:type ?type . ";
			queryStr += "FILTER (!(?type LIKE <http://dbpedia.org/ontology/Resource>)) .";
			queryStr += "FILTER (?type LIKE <http://dbpedia.org/ontology/%>) .";
			queryStr += "} }";
			queryStr += "LIMIT " + limit + " OFFSET " + offset;
			
			SparqlQuery query = new SparqlQuery(queryStr, dbpediaEndpoint);
			ResultSet rs = query.send();
			String previousObject = null;
			String geoLat = "";
			String geoLong = "";
			String label = "";
			Collection<String> types = new LinkedList<String>();
				
			while(rs.hasNext()) {
				QuerySolution qs = rs.nextSolution();
				
				String object = qs.get("object").toString();
				
				if(object.equals(previousObject)) {
					// only type has changed compared to previous row
					types.add(qs.get("type").toString());
					
					// we are only interested in the most special DBpedia class
//					NamedClass nc = new NamedClass(typeTmp);
//					if(hierarchy.getSubClasses(nc).size()==1) {
						// usually there is just one type assigned in the DBpedia ontology
//						if(!type.equals("unknown")) {
//							throw new Error("two different types for " + object + ": " + type + " and " + typeTmp);
//						}
//						type = typeTmp;
//					}						
				} else {
					if(previousObject != null) {
						// we have new a new point => write previous point to file
						String content = "";
						if(dbpediaFileFormat.equals("nt")) {
							content += "<" + previousObject + ">" + " <http://www.w3.org/2000/01/rdf-schema#label> \"" + label + "\" .\n";
							for(String type : types) {
								content += "<" + previousObject + ">" + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> \"" + type + "\" .\n";
							}
							content += "<" + previousObject + ">" + " <http://www.w3.org/2003/01/geo/wgs84_pos#lat> \"" + geoLat + "\"^^<http://www.w3.org/2001/XMLSchema#float> .\n";
							content += "<" + previousObject + ">" + " <http://www.w3.org/2003/01/geo/wgs84_pos#long> \"" + geoLong + "\"^^<http://www.w3.org/2001/XMLSchema#float> .\n";					
						} else {
							content += previousObject + "\n" + label + "\n" + types.toString().replace(" ", "") + "\n" + geoLat + "\n" + geoLong + "\n\n"; 
						}
						
						fos.write(content.getBytes());
						
					}
					
					// reset default values
					types.clear();
					
					// get new data
					geoLat = qs.getLiteral("lat").getString();
					geoLong = qs.getLiteral("long").getString();
					label = qs.getLiteral("label").getString();
					if(qs.contains("type")) {
						types.add(qs.get("type").toString());
						
						// we are only interested in the most special DBpedia class
//						NamedClass nc = new NamedClass(typeTmp);
//						if(hierarchy.getSubClasses(nc).size()==1) {
							// usually there is just one type assigned in the DBpedia ontology
//							if(!type.equals("unknown")) {
//								throw new Error("two different types for " + object + ": " + type + " and " + typeTmp);
//							}
//							type = typeTmp;
//						}							
					}
					
					previousObject = object;					
					points++;
				}
				
				counter++;
			}
			
			offset += limit;
			System.out.println(points + " points queried.");
			
		} while(counter == limit);
			
		fos.close();		
	}
	
	private static URI findGeoDataMatch(DBpediaPoint dbpediaPoint) throws IOException {
		
		// 1 degree is about 111 km (depending on the specific point)
		int distanceThresholdMeters = 1000;
		boolean quiet = true;
		
		if(useSparqlForGettingNearbyPoints) {
			// TODO: convert from meters to lat/long
			double distanceThresholdLat = 0.3;
			double distanceThresholdLong = 0.3;
			
			// create a box around the point
			double minLat = dbpediaPoint.getGeoLat() - distanceThresholdLat;
			double maxLat = dbpediaPoint.getGeoLat() + distanceThresholdLat;
			double minLong = dbpediaPoint.getGeoLong() - distanceThresholdLong;
			double maxLong = dbpediaPoint.getGeoLong() + distanceThresholdLong;		
			
			// query all points in the box
			String queryStr = "select ?point ?lat ?long ?name where { ";
			queryStr += "?point <http://linkedgeodata.org/vocabulary/latitude> ?lat .";
			queryStr += "FILTER (xsd:float(?lat) > " + minLat + ") .";
			queryStr += "FILTER (xsd:float(?lat) < " + maxLat + ") .";		
			queryStr += "?point <http://linkedgeodata.org/vocabulary/longitude> ?long .";
			queryStr += "FILTER (xsd:float(?long) > " + minLong + ") .";
			queryStr += "FILTER (xsd:float(?long) < " + maxLong + ") .";
			queryStr += "?point <http://linkedgeodata.org/vocabulary/name> ?name .";		
			queryStr += "}";
			
			SparqlQuery query = new SparqlQuery(queryStr, geoDataEndpoint);
			ResultSet rs = query.send();
			
			while(rs.hasNext()) {
//				QuerySolution qs = rs.nextSolution();
				
				// measure string similarity and proximity
				// TODO: incomplete
			}		
			return null;
		// use Tripliy spatial extension
		} else {
			
			if(!quiet)
				System.out.println(dbpediaPoint.getLabel());
			
			URL linkedGeoDataURL = new URL("http://linkedgeodata.org/triplify/nearhacked/"+dbpediaPoint.getGeoLat()+","+dbpediaPoint.getGeoLong()+"/"+distanceThresholdMeters);
			
			double highestScore = 0;
			String bestURI = null;
			String bestLabel = null;
			URLConnection conn = linkedGeoDataURL.openConnection();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//			StringBuffer sb = new StringBuffer();
			String line="";
			while ((line = rd.readLine()) != null)
			{	
				if(line.contains("<http://linkedgeodata.org/vocabulary#name>") || line.contains("<http://linkedgeodata.org/vocabulary/#name%25en>")) {
					int first = line.indexOf("\"") + 1;
					int last = line.lastIndexOf("\"");
					String label = line.substring(first, last);
					
					// perform string similarity
					// (we can use a variety of string matching heuristics)
					double score = distance.score(label, dbpediaPoint.getLabel());
					if(score > highestScore) {
						highestScore = score;
						bestURI = line.substring(1, line.indexOf(" ")-1);
						bestLabel = label;
					}
				}
//				sb.append(line);
			}
			rd.close();	
			
			if(!quiet) {
				System.out.println("  " + linkedGeoDataURL);
				System.out.println("  " + highestScore);
				System.out.println("  " + bestURI);
				System.out.println("  " + bestLabel);				
			}
			
			if(highestScore > scoreThreshold) {
//				System.out.println("  match");
				return URI.create(bestURI);
			} else {
//				System.out.println("  no match");
				return null;
			}
		}
	}
}
