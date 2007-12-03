/**
 * Copyright (C) 2007, Sebastian Hellmann
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
package org.dllearner.kb.sparql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// can execute different queries
public class TypedSparqlQuery {
	private Configuration configuration;
	// private SparqlHTTPRequest SparqlHTTPRequest;
	private SparqlQueryMaker sparqlQueryMaker;
	Cache cache;

	public TypedSparqlQuery(Configuration Configuration) {
		this.configuration = Configuration;
		// this.SparqlHTTPRequest = new
		// SparqlHTTPRequest(Configuration.getSparqlEndpoint());
		this.sparqlQueryMaker = new SparqlQueryMaker(Configuration.getSparqlQueryType());
		this.cache = new Cache("cache");
	}

	public Set<Tupel> query(URI u) {

		// getQuery
		String sparql = sparqlQueryMaker.makeSubjectQueryUsingFilters(u.toString());
		return cachedSparql(u, sparql, "predicate", "object");

	}

	public Set<Tupel> getTupelsForRole(URI u) {

		// getQuery
		String sparql = sparqlQueryMaker.makeRoleQueryUsingFilters(u.toString());

		Set<Tupel> s = cachedSparql(u, sparql, "subject", "object");
		// System.out.println(s);
		return s;

	}

	private Set<Tupel> cachedSparql(URI u, String sparql, String a, String b) {
		// check cache
		String FromCache = cache.get(u.toString(), sparql);

		String xml = null;
		// if not in cache get it from EndPoint
		if (FromCache == null) {
			try {
				xml = sendAndReceiveSPARQL(sparql);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(sparql);
			// System.out.println(xml);
			cache.put(u.toString(), sparql, xml);
			//System.out.print("\n");
		} else {
			xml = FromCache;
			System.out.println("FROM CACHE");
		}

		// System.out.println(sparql);
		// System.out.println(xml);
		// process XML
		Set<Tupel> s = processResult(xml, a, b);
		try {
			System.out.println("retrieved " + s.size() + " tupels\n");
		} catch (Exception e) {
		}
		return s;
	}

	public Set<Tupel> processResult(String xml, String a, String b) {

		Set<Tupel> ret = new HashSet<Tupel>();
		// TODO if result is empty, catch exceptions
		String one = "<binding name=\"" + a + "\">";
		String two = "<binding name=\"" + b + "\">";
		String uridel = "<uri>";
		String end = "</uri>";
		String predtmp = "";
		String objtmp = "";

		while (xml.indexOf(one) != -1) {
			// get pred
			xml = xml.substring(xml.indexOf(one) + one.length());
			xml = xml.substring(xml.indexOf(uridel) + uridel.length());
			predtmp = xml.substring(0, xml.indexOf(end));

			// getobj
			xml = xml.substring(xml.indexOf(two) + two.length());
			xml = xml.substring(xml.indexOf(uridel) + uridel.length());
			objtmp = xml.substring(0, xml.indexOf(end));
			ret.add(new Tupel(predtmp, objtmp));
			// System.out.println(new Tupel(predtmp,objtmp));
		}

		return ret;

	}

	private String sendAndReceiveSPARQL(String sparql) throws IOException {
		StringBuilder answer = new StringBuilder();

		// String an Sparql-Endpoint schicken
		HttpURLConnection connection;
		SpecificSparqlEndpoint se = configuration.getSparqlEndpoint();

		connection = (HttpURLConnection) se.getURL().openConnection();
		connection.setDoOutput(true);

		connection.addRequestProperty("Host", se.getHost());
		connection.addRequestProperty("Connection", "close");
		connection
				.addRequestProperty(
						"Accept",
						"text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		connection.addRequestProperty("Accept-Language", "de-de,de;q=0.8,en-us;q=0.5,en;q=0.3");
		connection.addRequestProperty("Accept-Charset", "utf-8;q=1.0");
		connection
				.addRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.1.4) Gecko/20070515 Firefox/2.0.0.4 Web-Sniffer/1.0.24");

		OutputStream os = connection.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os);

		Set<String> s = se.getParameters().keySet();
		Iterator<String> it = s.iterator();
		String FullURI = "";
		while (it.hasNext()) {
			String element = it.next();
			FullURI += "" + URLEncoder.encode(element, "UTF-8") + "="
					+ URLEncoder.encode(se.getParameters().get(element), "UTF-8") + "&";
		}
		// System.out.println(FullURI);
		FullURI += "" + se.getHasQueryParameter() + "=" + URLEncoder.encode(sparql, "UTF-8");

		osw.write(FullURI);
		osw.close();

		// receive answer
		InputStream is = connection.getInputStream();
		InputStreamReader isr = new InputStreamReader(is, "UTF-8");
		BufferedReader br = new BufferedReader(isr);

		String line;
		do {
			line = br.readLine();
			if (line != null)
				answer.append(line);
		} while (line != null);

		br.close();

		return answer.toString();
	}

}
