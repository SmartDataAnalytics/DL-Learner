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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.kb.sparql.configuration.SparqlEndpoint;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.ResultBinding;

public class TestResultSet {

	public static void main(String[] args) {

		String queryString = "PREFIX dbpedia2: <http://dbpedia.org/property/> "
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "SELECT ?episode  WHERE {   ?episode skos:subject"
				+ "    <http://dbpedia.org/resource/Category:The_Simpsons_episodes%2C_season_12>."
				+ "  ?episode dbpedia2:blackboard ?chalkboard_gag }";
		// ?chalkboard_gag
		SparqlEndpoint sse = SparqlEndpoint.dbpediaEndpoint();
		SparqlQuery sqJena = new SparqlQuery(queryString, sse);

		ResultSet rs = sqJena.send();
		String json = SparqlQuery.getAsJSON(rs);
		System.out.println(json);
		
		
		//List<ResultBinding> l = sqJena.getAsList();
		//System.out.println(l.getClass());
		//testSaving(new LinkedList<ResultBinding>(l));
		/*
		 * for (ResultBinding o : l) { System.out.println(o); // Iterator
		 * it=o.varNames(); while (it.hasNext()){ String tmp=(String)it.next();
		 * //System.out.println(); getVar(tmp,o); //Vector v;
		 *  } //System.out.println(o.getBinding().);
		 *  }
		 */
	}

	public static String getVar(String s, ResultBinding rb) {
		System.out.println(rb.get(s));
		return "";
	}

	@SuppressWarnings({"unchecked"})
	public static void testSaving(List<ResultBinding> l) {
		System.out.println(l + "\n****************************");
		try {
			// FileWriter fw=new FileWriter(new File(Filename),true);
			FileOutputStream fos = new FileOutputStream("test.txt", false);
			ObjectOutputStream o = new ObjectOutputStream(fos);
			o.writeObject(l);
			fos.flush();
			fos.close();
			FileInputStream fis = new FileInputStream("test.txt");
			ObjectInputStream i = new ObjectInputStream(fis);
			List<ResultBinding> in = (List<ResultBinding>) i.readObject();
			System.out.println((in) + "\n****************************");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// sqJena.asList();

		// compareResults( queryString);

	}

	public static void testTime(int howOften, String queryString) {
		SparqlEndpoint sse = SparqlEndpoint.dbpediaEndpoint();

		SparqlQuery sqJena = new SparqlQuery(queryString, sse);
		//SparqlQueryConventional sqConv = new SparqlQueryConventional(sse);
		
		// first query is not counted
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
		ResultSet rs = sqJena.send();
		SparqlQuery.getAsXMLString(rs);
		now = System.currentTimeMillis();
		tmp = now;
		for (int i = 0; i < howOften; i++) {

			rs = sqJena.send();
			SparqlQuery.getAsXMLString(rs);			
			System.out.println("Jena needed: "
					+ (System.currentTimeMillis() - tmp));
			tmp = System.currentTimeMillis();

		}
		System.out.println("Jena total: " + (System.currentTimeMillis() - now));

		// first query is not counted
		// sqJena.();
		now = System.currentTimeMillis();
		tmp = now;
		for (int i = 0; i < howOften; i++) {

			// sqJena.asJenaModel();
			System.out.println("JenaModel needed: "
					+ (System.currentTimeMillis() - tmp));
			tmp = System.currentTimeMillis();

		}
		System.out.println("Jena total: " + (System.currentTimeMillis() - now));
	}

	public static void compareResults(String queryString) {
		SparqlEndpoint sse = SparqlEndpoint.dbpediaEndpoint();
		SparqlQuery sqJena = new SparqlQuery(queryString, sse);
	//	SparqlQueryConventional sqConv = new SparqlQueryConventional(sse);

		ResultSet rs = sqJena.send();
		System.out.println(SparqlQuery.getAsXMLString(rs));
		//System.out.println(sqConv.getAsXMLString(queryString));

	}
}
