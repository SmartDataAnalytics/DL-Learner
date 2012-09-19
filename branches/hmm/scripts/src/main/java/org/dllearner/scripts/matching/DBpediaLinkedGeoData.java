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
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
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

	private static Logger logger = Logger.getLogger(DBpediaLinkedGeoData.class);
	
	// chose between nt and dat
	private static String dbpediaFileFormat = "dat";
	static File dbpediaFile =  new File("log/DBpedia_POIs." + dbpediaFileFormat);
	private static boolean regenerateFile = false;
	
	private static File matchingFile = new File("log/DBpedia_GeoData_Links.nt");
	@SuppressWarnings("unused")
	private static File matchingFileMySQL =  new File("log/DBpedia_POIs.csv");	
	private static File missesFile = new File("log/DBpedia_GeoData_Misses.dat");
	private static double scoreThreshold = 0.85;
	private static StringDistance distance = new Jaro();
	
	private static String usedDatatype = "xsd:decimal";
	
//	public static SparqlEndpoint dbpediaEndpoint = SparqlEndpoint.getEndpointDBpedia();
	public static SparqlEndpoint dbpediaEndpoint = SparqlEndpoint.getEndpointLOCALDBpedia();
	private static SPARQLTasks dbpedia = new SPARQLTasks(new Cache("cache/dbpedia_file/"), dbpediaEndpoint);	
	private static SparqlEndpoint geoDataEndpoint = SparqlEndpoint.getEndpointLOCALGeoData();
//	private static SPARQLTasks lgd = new SPARQLTasks(new Cache("cache/lgd/"), geoDataEndpoint);
	private static SPARQLTasks lgd = new SPARQLTasks(geoDataEndpoint);	
	
	private static Map<POIClass, Integer> noMatchPerClass = new HashMap<POIClass, Integer>();
	private static Map<POIClass, Integer> matchPerClass = new HashMap<POIClass, Integer>();
	
	private static DecimalFormat df = new DecimalFormat();
	private static int skipCount = 0;
	private static int counter = 0;
	private static int matches = 0;
	private static Date startDate;
	
	private static final int totalPOICount = 328232;
	
	// read in DBpedia ontology such that we perform taxonomy reasoning
//	private static ReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.DBPEDIA_OWL);
//	private static ClassHierarchy hierarchy = reasoner.getClassHierarchy();
	
	// true = SPARQL is used for retrieving close points;
	// false = Triplify spatial extension is used
	private static boolean useSparqlForGettingNearbyPoints = true;
	
	public static void main(String[] args) throws IOException {
		
		Logger.getRootLogger().setLevel(Level.WARN);
		
		// download all objects having geo-coordinates from DBpedia if necessary
		if(!dbpediaFile.exists() || regenerateFile) {
			createDBpediaFile();
		}
		
		for(POIClass poiClass : POIClass.values()) {
			matchPerClass.put(poiClass, 0);
			noMatchPerClass.put(poiClass, 0);
		}
		
		Files.clearFile(matchingFile);
		Files.clearFile(missesFile);
		FileOutputStream fos = new FileOutputStream(matchingFile, true);
		FileOutputStream fosMySQL = new FileOutputStream(matchingFile, true);
		FileOutputStream fosMiss = new FileOutputStream(missesFile, true);
		// read file point by point
		BufferedReader br = new BufferedReader(new FileReader(dbpediaFile));
		String line;
		
		// temporary variables needed while reading in file
		int itemCount = 0;
		URI uri = null;
		String label = null;
		String[] classes = null;
		int decimalCount = 0;
		Double geoLat = null;
		Double geoLong = null;
		
		startDate = new Date();
		System.out.println("Start matching process at date " + startDate);
		while ((line = br.readLine()) != null) {
			
			if(line.isEmpty()) {
				DBpediaPoint dp = new DBpediaPoint(uri, label, classes, geoLat, geoLong, decimalCount);
				POIClass poiClass = dp.getPoiClass();
				
//				System.out.println("DBpedia Point: " + dp);
				
				if(poiClass != null) {
					// find match (we assume there is exactly one match)
					URI matchURI = findGeoDataMatch(dp);
					if(matchURI == null) {
						String missStr = dp.toString() + "\n";
						fosMiss.write(missStr.getBytes());
						noMatchPerClass.put(poiClass, noMatchPerClass.get(poiClass)+1);
					} else {
						String matchStr = "<" + dp.getUri() + "> <http://www.w3.org/2002/07/owl#sameAs> <" + matchURI + "> .\n";
						fos.write(matchStr.getBytes());	
						
						// strip off http://dbpedia.org/resource/
						String dpName = dp.getUri().toString().substring(28);
						String uriStr = matchURI.toString();
						String nodeWay = uriStr.contains("/node/") ? "node" : "way";
						String lgdID = uriStr.substring(uriStr.lastIndexOf("/"));
						String matchStrMySQL = dpName + "\t" + nodeWay + "\t" + lgdID + "\n";
						fosMySQL.write(matchStrMySQL.getBytes());
						
						System.out.println(matchStrMySQL);
						
						matches++;
						matchPerClass.put(poiClass, matchPerClass.get(poiClass)+1);
					}
//					System.out.println(poiClass);
					counter++;
					
					if(counter % 1000 == 0) {
//						System.out.println(new Date().toString() + ": " + counter + " points processed. " + matches + " matches found. " + skipCount + " POIs skipped.");
						printSummary();
					}
				} else {
//					System.out.println(dp.getUri() + " " + dp.getClasses());
					skipCount++;
				}
				
				itemCount = 0;
			} else {
				switch(itemCount) {
				case 0 : uri = URI.create(line); break;
				case 1 : label = line; break;
				case 2 : line = line.substring(1, line.length()-1); // strip brackets
					if(line.length()>1) {
						classes = line.split(",");
					} else {
						classes = new String[0];
					}
					break;
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
		
		printSummary();
		System.out.println("Finished Successfully.");
	}
	
	private static void printSummary() {
		Date currDate = new Date();
		System.out.println("Summary at date " + currDate.toString());
		
		for(POIClass poiClass : POIClass.values()) {
			int classTests = matchPerClass.get(poiClass)+noMatchPerClass.get(poiClass);
			double per = (classTests == 0) ? 0 : 100 * matchPerClass.get(poiClass)/(double)(classTests);
			System.out.println("POI class " + getFixedLengthString(poiClass,15) + ": " + getFixedLengthString(matchPerClass.get(poiClass),5) + " matches found from " + getFixedLengthString(classTests,5) + " POIs = " + df.format(per) + "% match rate" );
		}
		
//		System.out.println("");
		System.out.println("Overall:");
		int total = skipCount + counter;
		double skipFreq = 100*skipCount/(double)total;
		double countFreq = 100*counter/(double)total;
		double matchFreq = 100*matches/(double)total;
		double matchCountFreq = 100*matches/(double)counter;
		long diffMs = currDate.getTime() - startDate.getTime();
	    long diffHours = diffMs / (60 * 60 * 1000);
	    long diffMinutes = diffMs / (60 * 1000) - diffHours * 60;	    
	    double pointPercentage = 100 * total / (double) totalPOICount;
	    double pointsPerMs = total / (double) diffMs;
	    double pointsPerHour = 3600 * 1000 * pointsPerMs;
	    long estimatedMs = totalPOICount * diffMs / total;
	    Date estimatedDate = new Date(startDate.getTime() + estimatedMs);
	    System.out.println("algorithm runtime: " + diffHours + " hours " + diffMinutes + " minutes, estimated to finish at " + estimatedDate);
	    System.out.println(df.format(pointPercentage) + "% of points skipped or processed = " + df.format(pointsPerHour) + " points per hour");
		System.out.println(skipCount + " POIs skipped (cannot be assigned to a POI class) = " + df.format(skipFreq) + "%");
		System.out.println(counter + " POIs processed = " + df.format(countFreq) + "%");
		System.out.println(matches + " matches found = " + df.format(matchCountFreq) + "% of processed POIs, " + df.format(matchFreq) + "% of all POIs");
		System.out.println();
	}
	
	private static String getFixedLengthString(Object object, int length) {
		String str = object.toString();
		for(int i = str.length(); i < length; i++ ) {
			str = " " + str;
		}
		return str;
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
			queryStr += "FILTER (?type LIKE <http://dbpedia.org/ontology/%> || ?type LIKE <http://umbel.org/umbel/sc/%>) .";
			queryStr += "} }";
			queryStr += "LIMIT " + limit + " OFFSET " + offset;
			
//			SparqlQuery query = new SparqlQuery(queryStr, dbpediaEndpoint);
//			ResultSet rs = query.send();
			ResultSet rs = dbpedia.queryAsResultSet(queryStr);
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
	
	/**
	 * The main matching method. The matching is directed from DBpedia to LGD,
	 * i.e. given a POI in DBpedia, we try to find a match in LGD.
	 * 
	 * @param dbpediaPoint The DBpedia point.
	 * @return The URI of the matched LGD point or null if no match was found.
	 * @throws IOException Thrown if a query or linked data access does not work.
	 */
	public static URI findGeoDataMatch(DBpediaPoint dbpediaPoint) throws IOException {
		
		// 1 degree is about 111 km (depending on the specific point)
		double distanceThresholdMeters = dbpediaPoint.getPoiClass().getMaxBox();
		boolean quiet = true;
		
		if(useSparqlForGettingNearbyPoints) {
			// deprecated: direct specification of long/lat difference
//			double distanceThresholdLat = 0.5;
//			double distanceThresholdLong = 0.5;
			// create a box around the point
//			double minLat2 = dbpediaPoint.getGeoLat() - distanceThresholdLat;
//			double maxLat2 = dbpediaPoint.getGeoLat() + distanceThresholdLat;
//			double minLong2 = dbpediaPoint.getGeoLong() - distanceThresholdLong;
//			double maxLong2 = dbpediaPoint.getGeoLong() + distanceThresholdLong;	
 
			// Triplify: $1 = latitude, $2 = longitude, $3 = distance in meters
			// LGD uses integer for lat/long (standard values multiplied by 10000000)
			// $box='longitude between CEIL(($2-($3/1000)/abs(cos(radians($1))*111))*10000000) and CEIL(($2+($3/1000)/abs(cos(radians($1))*111))*10000000)
			//	AND latitude between CEIL(($1-($3/1000/111))*10000000) and CEIL(($1+($3/1000/111))*10000000)';
			
			double minLat =  dbpediaPoint.getGeoLat()-(distanceThresholdMeters/1000/111);
			double maxLat =  dbpediaPoint.getGeoLat()+(distanceThresholdMeters/1000/111);
			double minLong = dbpediaPoint.getGeoLong()-(distanceThresholdMeters/1000)/Math.abs(Math.cos(Math.toRadians(dbpediaPoint.getGeoLat()))*111);
			double maxLong = dbpediaPoint.getGeoLong()+(distanceThresholdMeters/1000)/Math.abs(Math.cos(Math.toRadians(dbpediaPoint.getGeoLat()))*111);
			
//			System.out.println("lat:  " + minLat + " < " + dbpediaPoint.getGeoLat() + " < " + maxLat);
//			System.out.println("long: " + minLong + " < " + dbpediaPoint.getGeoLong() + " < " + maxLong);
			
			// query all points in the box corresponding to this class
			// (we make sure that returned points are in the same POI class)
			String queryStr = "select ?point ?lat ?long ?name ?name_en ?name_int where { ";
			queryStr += LGDPoint.getSPARQLRestriction(dbpediaPoint.getPoiClass(), "?point");
			queryStr += "?point <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat .";
			queryStr += "FILTER ("+usedDatatype+"(?lat) > " + minLat + ") .";
			queryStr += "FILTER ("+usedDatatype+"(?lat) < " + maxLat + ") .";		
			queryStr += "?point <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long .";
			queryStr += "FILTER ("+usedDatatype+"(?long) > " + minLong + ") .";
			queryStr += "FILTER ("+usedDatatype+"(?long) < " + maxLong + ") .";
			queryStr += "?point <http://linkedgeodata.org/vocabulary#name> ?name .";
			queryStr += "OPTIONAL { ?point <http://linkedgeodata.org/vocabulary#name%25en> ?name_en } .";
			queryStr += "OPTIONAL { ?point <http://linkedgeodata.org/vocabulary#name_int> ?name_int } .";
			// filter out ways => we assume that it is always better to match a point and not a way
			// (if there is a way, there should also be a point but not vice versa)
			// => according to OSM data model, ways do not have longitude/latitude, so we should
			// always match nodes and not ways (TODO: discuss with Soeren)
//			queryStr += "FILTER (?point LIKE <http://linkedgeodata.org/triplify/node/%>) .";
			queryStr += "}";
			
//			SparqlQuery query = new SparqlQuery(queryStr, geoDataEndpoint);
//			ResultSet rs = query.send();
			ResultSet rs = lgd.queryAsResultSet(queryStr);
			
			double highestScore = 0;
			String bestURI = null;
			String bestLabel = null;
			while(rs.hasNext()) {
				QuerySolution qs = rs.nextSolution();
				String lgdURI = qs.getResource("point").toString();
				
				// step 1: string similarity
				double stringSimilarity;
				// from DBpedia we take the full label and an abbreviated version;
				// from LGD we take name, name%25en, name, int_name
				String dbpediaLabel1 = dbpediaPoint.getLabel();
				String dbpediaLabel2 = dbpediaPoint.getPlainLabel();
				
//				System.out.println("label 1: " + dbpediaLabel1);
//				System.out.println("label 2: " + dbpediaLabel2);
				
				String lgdLabel1 = qs.getLiteral("name").toString();
				stringSimilarity = distance.score(dbpediaLabel1, lgdLabel1);
				stringSimilarity = Math.max(distance.score(dbpediaLabel2, lgdLabel1), stringSimilarity);
				if(qs.contains("name_en")) {
					String lgdLabel2 = qs.getLiteral("name_en").toString();
					stringSimilarity = distance.score(dbpediaLabel1, lgdLabel2);
					stringSimilarity = Math.max(distance.score(dbpediaLabel2, lgdLabel2), stringSimilarity);
					System.out.println(qs.getResource("point").getURI());
					System.exit(0);
				}
				if(qs.contains("name_int")) {
					String lgdLabel3 = qs.getLiteral("name_int").toString();
					stringSimilarity = distance.score(dbpediaLabel1, lgdLabel3);
					stringSimilarity = Math.max(distance.score(dbpediaLabel2, lgdLabel3), stringSimilarity);					
				}				
				
				// step 2: spatial distance
				double lat = qs.getLiteral("lat").getDouble();
				double lon = qs.getLiteral("long").getDouble();
				double distance = spatialDistance(dbpediaPoint.getGeoLat(), dbpediaPoint.getGeoLong(), lat, lon);
				double frac = Math.min(1,distance / dbpediaPoint.getPoiClass().getMaxBox());
				double distanceScore = Math.pow(frac-1,2);
				
//				System.out.println(dbpediaPoint.getPoiClass().getMaxBox());
//				System.out.println(distance);
//				System.out.println(frac);
//				System.out.println(distanceScore);
//				System.out.println("===============");
				
				double score = 0.8 * stringSimilarity + 0.2 * distanceScore;
				// if there is a node and a way, we prefer the node (better representative) 
				if(lgdURI.contains("/way/")) {
					score -= 0.02;
				}
				
				if(score > highestScore) {
					highestScore = score;
					bestURI = lgdURI;
					bestLabel = lgdLabel1;
				}
				
			}
			
			if(highestScore > scoreThreshold) {
				logger.info("Match: " + highestScore + " " + bestLabel + " (" + dbpediaPoint.getUri() + " --> " + bestURI + ")");
				return URI.create(bestURI);
			} else {
				logger.info("No match: " + highestScore + " " + bestLabel + " (" + dbpediaPoint.getUri() + " --/-> " + bestURI + ")");
				return null;
			}
			
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
				if(line.contains("<http://linkedgeodata.org/vocabulary#name>") || line.contains("<http://linkedgeodata.org/vocabulary/#name%25en>") || line.contains("<http://linkedgeodata.org/vocabulary/#int_name>")) {
					int first = line.indexOf("\"") + 1;
					int last = line.lastIndexOf("\"");
					String label = line.substring(first, last);
					
					// perform string similarity
					// (we can use a variety of string matching heuristics)
//					System.out.println(label);
//					System.out.println(dbpediaPoint);
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
	
	// returns distance between two points in meters
	public static double spatialDistance(double lat1, double long1, double lat2, double long2) {
//		$distance='ROUND(1000*1.609 * 3956 * 2 * ASIN(SQRT(  POWER(SIN(($1 - latitude/10000000) * pi()/180 / 2), 2) +
//			COS($1 * pi()/180) *  COS(latitude/10000000 * pi()/180) *  POWER(SIN(($2 - longitude/10000000) * pi()
//			/180 / 2), 2) ) )) AS distance';
//		double distance = 1000 * 1.609 * 3956 * 2 * 
//			Math.asin(Math.sqrt(Math.pow(Math.sin((lat1 - lat2)/1000000, b)));
			
		// implementation according to http://www.movable-type.co.uk/scripts/latlong.html
		double r = 6371000; // meters
		double dLat = Math.toRadians(lat2-lat1);
		double dLon = Math.toRadians(long2-long1); 
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 
		        Math.sin(dLon/2) * Math.sin(dLon/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double distance = r * c;
		return distance;
	}
}
