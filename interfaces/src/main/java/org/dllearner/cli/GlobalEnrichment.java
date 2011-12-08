/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
package org.dllearner.cli;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Enriches all of the LOD cloud.
 * 
 * @author Jens Lehmann
 * 
 */
public class GlobalEnrichment {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		// get all SPARQL endpoints and their graphs
		List<SparqlEndpoint> endpoints = new LinkedList<SparqlEndpoint>();
		
		String query = "";
		query += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";
		query += "PREFIX void: <http://rdfs.org/ns/void#> ";
		query += "PREFIX dcterms: <http://purl.org/dc/terms/> ";
		query += "SELECT ?endpoint ";
		query += "WHERE { ";
		query += "?item rdf:type void:Dataset . ";
		query += "?item dcterms:isPartOf <http://ckan.net/group/lodcloud> . ";
		query += "?item void:sparqlEndpoint ?endpoint . ";
		query += "}"; 
//		query += "LIMIT 20";
		
		// LATC DSI/MDS
		SparqlEndpoint dsi = new SparqlEndpoint(new URL("http://api.talis.com/stores/latc-mds/services/sparql"));
		SparqlQuery sq = new SparqlQuery(query, dsi);
		ResultSet rs = sq.send();
		while(rs.hasNext()) {
			QuerySolution qs = rs.next();
			String endpoint = qs.get("endpoint").toString();
//			String graph = qs.getLiteral("graph").getString();
			System.out.println(endpoint);
		}
	}

}
