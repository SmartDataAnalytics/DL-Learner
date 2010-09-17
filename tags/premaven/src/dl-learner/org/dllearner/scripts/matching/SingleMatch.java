package org.dllearner.scripts.matching;

import java.net.URI;

/**
 * 
 * Tests a single match (for debug purposes), i.e. given a DBpedia URI, it
 * tries to find a match in LinkedGeoData.
 * 
 * @author Jens Lehmann
 *
 */
public class SingleMatch {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		URI uri = URI.create("http://dbpedia.org/resource/Washington_DC");
		URI uri = URI.create("http://dbpedia.org/resource/Leipzig");
		System.out.println("Trying to find a match for " + uri);
		DBpediaPoint dp = new DBpediaPoint(uri);
		URI lgdURI = DBpediaLinkedGeoData.findGeoDataMatch(dp);
		System.out.println(lgdURI);
	}

}
