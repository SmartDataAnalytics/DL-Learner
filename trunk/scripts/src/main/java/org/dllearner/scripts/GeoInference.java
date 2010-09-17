/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
package org.dllearner.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;

/**
 * Gets additional geo information from DBpedia-Geonames
 * mappings.
 * 
 * @author Jens Lehmann
 *
 */
public class GeoInference {

	static String geoMappingFile = "src/dbpedia-navigator/data/links_geonames_en.nt";
	static String outputFile = "src/dbpedia-navigator/data/geo_inference.nt";
	
	public static void main(String[] args) throws IOException {
		// read in mapping file
		BufferedReader in = new BufferedReader(new FileReader(geoMappingFile));
		// output file
		BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
		
		// db.aksw.org SPARQL endpoint
		SparqlEndpoint endpoint = null;//SparqlEndpoint.getEndpointLOCALGeonames();
		SPARQLTasks st = new SPARQLTasks(endpoint);
		
		URI test = URI.create("http://sws.geonames.org/2959441/");
		List<URI> parents = new LinkedList<URI>();
		getParents(st, test, parents);
		System.out.println(parents);
		
		
//		String line;
//		while ((line=in.readLine())!=null)
//		{
//			st.query(sparqlQueryString)
//			out.write(line);
//		}
		
		in.close();
		out.close();
	}
	
	public static List<URI> getParents(SPARQLTasks st, URI geoURI, List<URI> parents) {
		String query = "SELECT ?x WHERE {<"+geoURI+"> <http://www.geonames.org/ontology#parentFeature> ?x}";
		System.out.println(query);
		SortedSet<String> res = st.queryAsSet(query, "x");
		if(res.size() == 0) {
			return parents;
		} else {
			URI parent = URI.create(res.first());
			parents.add(parent);
			// recurse
			return getParents(st, parent, parents);
		}
	}
	
}
