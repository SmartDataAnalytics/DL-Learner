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
package org.dllearner.kb.sparql.test;

import org.dllearner.kb.sparql.configuration.SparqlEndpoint;
import org.dllearner.kb.sparql.old.SparqlQueryConventional;
import org.dllearner.kb.sparql.query.SparqlQuery;

/**
 * Represents a SPARQL query. It includes support for stopping the SPARQL query
 * (which may be necessary if a timeout is reached).
 * 
 * TODO: It is probably good to change all SPARQL query calls to use only this
 * class.
 * 
 * TODO: Could we use Jena as a solid foundation here? (com.hp.jena.query)
 * 
 * @author Jens Lehmann
 * 
 */
public class TestSparqlQuery {

	// this is a working Jena script
	// TODO: query runtime seems to be much too high (compared to running it in
	// http://dbpedia.org/sparql)
	// verify whether our SPARQL query implementation is faster and why;
	// TODO: check whether Jena works with the other endpoints in
	// PredefinedEndpoint; if not
	// check whether it can be configured to run with these
	public static void main(String[] args) {

		String queryString = "PREFIX dbpedia2: <http://dbpedia.org/property/> "
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "SELECT ?episode ?chalkboard_gag WHERE {   ?episode skos:subject"
				+ "    <http://dbpedia.org/resource/Category:The_Simpsons_episodes%2C_season_12>."
				+ "  ?episode dbpedia2:blackboard ?chalkboard_gag }";

		testTime(20, queryString);

		// compareResults( queryString);

	}

	public static void testTime(int howOften, String queryString) {
		SparqlEndpoint sse = SparqlEndpoint.dbpediaEndpoint();
		SparqlQuery sqJena = new SparqlQuery(queryString, sse);
		SparqlQueryConventional sqConv = new SparqlQueryConventional(sse);

		// first query is not counted
		sqJena.getAsList();
		long now = System.currentTimeMillis();
		long tmp = now;
		for (int i = 0; i < howOften; i++) {
			// sqConv.getAsXMLString(queryString);
			sqJena.getAsList();
			System.out.println("Conv needed: "
					+ (System.currentTimeMillis() - tmp));
			tmp = System.currentTimeMillis();

		}
		System.out.println("Conv total: " + (System.currentTimeMillis() - now));
		// first query is not counted
		sqJena.getAsXMLString();
		now = System.currentTimeMillis();
		tmp = now;
		for (int i = 0; i < howOften; i++) {

			sqJena.getAsXMLString();
			System.out.println("Jena needed: "
					+ (System.currentTimeMillis() - tmp));
			tmp = System.currentTimeMillis();

		}
		System.out.println("Jena total: " + (System.currentTimeMillis() - now));
	}

	public static void compareResults(String queryString) {
		SparqlEndpoint sse = SparqlEndpoint.dbpediaEndpoint();
		SparqlQuery sqJena = new SparqlQuery(queryString, sse);
		// SparqlQueryConventional sqConv=new SparqlQueryConventional(sse);

		System.out.println(sqJena.getAsXMLString());
		// System.out.println(sqConv.getAsXMLString(""));

	}
}
