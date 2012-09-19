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
 */

package org.dllearner.test;

import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;


public class JenaQueryToResultSpeedTest {
	static boolean print_flag=false;
	
	public static void main(String[] args) {

		String queryString = "PREFIX dbpedia2: <http://dbpedia.org/property/> "
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "SELECT ?episode ?chalkboard_gag WHERE {   ?episode skos:subject"
				+ "    <http://dbpedia.org/resource/Category:The_Simpsons_episodes%2C_season_12>."
				+ "  ?episode dbpedia2:blackboard ?chalkboard_gag }";

		int howOften=20;
		testJenaAsXML(howOften, queryString);
		testJenaAsList(howOften, queryString);
		testJenaAsJSON(howOften, queryString);
		testJenaAsJSONandBack(howOften, queryString);

		

		// compareResults( queryString);

	}

	
	public static void testJenaAsXML(int howOften, String queryString){
		SparqlEndpoint sse = SparqlEndpoint.getEndpointDBpedia();
		SparqlQuery sqJena = new SparqlQuery(queryString, sse);
		// first query is not counted
		sqJena.send();
		sqJena.getXMLString();
		long now = System.currentTimeMillis();
		long tmp = now;
		for (int i = 0; i < howOften; i++) {

			sqJena.send();
			sqJena.getXMLString();		
			p("Jena as XML needed: "
					+ (System.currentTimeMillis() - tmp));
			tmp = System.currentTimeMillis();
		}
		long total=System.currentTimeMillis() - now;
		System.out.println("Jena as XML total: " + total +
				" ms , average: "+ (total/howOften) );
		
	}
	
	public static void testJenaAsList(int howOften, String queryString){
		SparqlEndpoint sse = SparqlEndpoint.getEndpointDBpedia();
		SparqlQuery sqJena = new SparqlQuery(queryString, sse);
		// first query is not counted
		//sqJena.getAsList();
		sqJena.send();
		long now = System.currentTimeMillis();
		long tmp = now;
		for (int i = 0; i < howOften; i++) {

			// sqJena.getAsList();
			ResultSet rs = sqJena.send();
			ResultSetFormatter.toList(rs);
			p("Jena as List needed: "
					+ (System.currentTimeMillis() - tmp));
			tmp = System.currentTimeMillis();

		}
		long total=System.currentTimeMillis() - now;
		System.out.println("Jena as List total: " + total +
				" ms , average: "+ (total/howOften) );
		
	}
	
	public static void testJenaAsJSON(int howOften, String queryString){
		SparqlEndpoint sse = SparqlEndpoint.getEndpointDBpedia();
		SparqlQuery sqJena = new SparqlQuery(queryString, sse);
		// first query is not counted
		sqJena.send();
		sqJena.getJson();
		long now = System.currentTimeMillis();
		long tmp = now;
		for (int i = 0; i < howOften; i++) {

		    	sqJena.send();
			sqJena.getJson();
			p("Jena as JSON needed: "
					+ (System.currentTimeMillis() - tmp));
			tmp = System.currentTimeMillis();

		}
		long total=System.currentTimeMillis() - now;
		System.out.println("Jena as JSON total: " + total +
				" ms , average: "+ (total/howOften) );
		
	}
	
	public static void testJenaAsJSONandBack(int howOften, String queryString){
		SparqlEndpoint sse = SparqlEndpoint.getEndpointDBpedia();
		SparqlQuery sqJena = new SparqlQuery(queryString, sse);
		// first query is not counted
		sqJena.send();
		sqJena.getJson();		
		long now = System.currentTimeMillis();
		long tmp = now;
		for (int i = 0; i < howOften; i++) {

		//	System.out.println(sqJena.getAsJSON());
		    	sqJena.send();
			String json = sqJena.getJson();
			SparqlQuery.convertJSONtoResultSet(json);
			p("Jena as JSON and back needed: "
					+ (System.currentTimeMillis() - tmp));
			tmp = System.currentTimeMillis();

		}
		long total=System.currentTimeMillis() - now;
		System.out.println("Jena as JSON and back total: " + total +
				" ms , average: "+ (total/howOften) );
		
	}
	

	public static void compareResults(String queryString) {
		SparqlEndpoint sse = SparqlEndpoint.getEndpointDBpedia();
		SparqlQuery sqJena = new SparqlQuery(queryString, sse);
		// SparqlQueryConventional sqConv=new SparqlQueryConventional(sse);

		sqJena.send();
		System.out.println(sqJena.getJson());
		// System.out.println(sqConv.getAsXMLString(""));

	}
	
	static void p(String s) {
		if (print_flag)
			System.out.println(s);
	}
}
