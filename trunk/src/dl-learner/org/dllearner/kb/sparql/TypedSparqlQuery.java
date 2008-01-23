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

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.kb.sparql.configuration.Configuration;
import org.dllearner.kb.sparql.query.Cache;
import org.dllearner.kb.sparql.query.CachedSparqlQuery;
import org.dllearner.kb.sparql.query.SparqlQuery;
import org.dllearner.utilities.StringTuple;

/**
 * Can execute different queries.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class TypedSparqlQuery implements TypedSparqlQueryInterface {
	boolean print_flag = false;
	protected Configuration configuration;
	private SparqlQueryMaker sparqlQueryMaker;
	Cache cache;

	// boolean debug_no_cache = false;// true means no cache is used
	// private SparqlHTTPRequest SparqlHTTPRequest;
	// private SparqlQuery sparqlQuery;
	// private CachedSparqlQuery cachedSparqlQuery;

	public TypedSparqlQuery(Configuration Configuration) {
		this.configuration = Configuration;
		this.sparqlQueryMaker = new SparqlQueryMaker(Configuration
				.getSparqlQueryType());
		this.cache = new Cache("cache");
		// this.sparqlQuery=new SparqlQuery(configuration.getSparqlEndpoint());
		// this.cachedSparqlQuery=new
		// CachedSparqlQuery(this.sparqlQuery,this.cache);
	}

	// standard query get a tupels (p,o) for subject s
	/**
	 * uses a cache and gets the result tuples for a resource u
	 * 
	 * @param uri
	 *            the resource
	 * @param sparqlQueryString
	 * @param a
	 *            the name of the first bound variable for xml parsing, normally
	 *            predicate
	 * @param b
	 *            the name of the second bound variable for xml parsing,
	 *            normally object
	 * @return
	 */

	public Set<StringTuple> getTupelForResource(URI uri) {
		// TODO remove
		String a = "predicate";
		String b = "object";
		// getQuery
		String sparqlQueryString = sparqlQueryMaker
				.makeSubjectQueryUsingFilters(uri.toString());

		CachedSparqlQuery csq = new CachedSparqlQuery(configuration
				.getSparqlEndpoint(), cache, uri.toString(), sparqlQueryString);

		String xml = csq.getAsXMLString();
		// TODO needs to be changed to new format
		Set<StringTuple> s = processResult(xml, a, b);
		try {
			// System.out.println("retrieved " + s.size() + " tupels\n");
		} catch (Exception e) {
		}
		return s;
		// return cachedSparql(u, sparql, "predicate", "object");

	}

	@Deprecated
	private Set<StringTuple> cachedSparql(URI uri, String sparqlQueryString,
			String a, String b) {
		return null;
		/*
		 * OLD CODE FOLLOWING keep until Jena is working String FromCache =
		 * cache.get(u.toString(), sparqlQueryString); if (debug_no_cache) {
		 * //FromCache = null; } String xml = null; // if not in cache get it
		 * from EndPoint if (FromCache == null) {
		 * configuration.increaseNumberOfuncachedSparqlQueries(); // try { xml =
		 * sendAndReceiveSPARQL(sparqlQueryString);
		 * 
		 * //} catch (IOException e) {e.printStackTrace();}
		 * 
		 * p(sparqlQueryString); // System.out.println(xml); if
		 * (!debug_no_cache) { cache.put(uri.toString(), sparqlQueryString,
		 * xml); } // System.out.print("\n"); } else {
		 * configuration.increaseNumberOfCachedSparqlQueries(); xml = FromCache; //
		 * System.out.println("FROM CACHE"); }
		 */
		// System.out.println(sparql);
		// System.out.println(xml);
		// process XML
	}

	/**
	 * TODO old XML processing, can be removed, once Jena is done
	 * 
	 * @param xml
	 * @param a
	 * @param b
	 * @return a Set of Tuples <a|b>
	 */
	@Deprecated
	public Set<StringTuple> processResult(String xml, String a, String b) {

		Set<StringTuple> ret = new HashSet<StringTuple>();
		// TODO if result is empty, catch exceptions
		String resEnd = "</result>";
		String one = "binding name=\"" + a + "\"";
		String two = "binding name=\"" + b + "\"";
		String endbinding = "binding";
		String uri = "uri";
		// String uridel = "<uri>";
		String bnode = "<bnode>";
		// String uriend = "</uri>";
		String predtmp = "";
		String objtmp = "";
		// System.out.println(getNextResult(xml));
		String nextResult = "";
		while ((nextResult = getNextResult(xml)) != null) {
			// System.out.println(xml.indexOf(resEnd));
			// System.out.println(xml);
			if (nextResult.indexOf(bnode) != -1) {
				xml = xml.substring(xml.indexOf(resEnd) + resEnd.length());
				continue;
			}
			// get pred
			// predtmp = nextResult.substring(nextResult.indexOf(one) +
			// one.length());
			predtmp = getinTag(nextResult, one, endbinding);
			predtmp = getinTag(predtmp, uri, uri);
			// System.out.println(predtmp);

			// getobj
			objtmp = getinTag(nextResult, two, endbinding);
			objtmp = getinTag(objtmp, uri, uri);
			// System.out.println(objtmp);

			StringTuple st = new StringTuple(predtmp, objtmp);
			// System.out.println(st);
			ret.add(st);
			xml = xml.substring(xml.indexOf(resEnd) + resEnd.length());

		}
		/*
		 * while (xml.indexOf(one) != -1) {
		 * 
		 * 
		 *  // System.out.println(new Tupel(predtmp,objtmp)); }
		 */

		return ret;

	}

	/**
	 * TODO used by old XML processing, can be removed once Jena is done
	 * 
	 * @param xml
	 * @return
	 */
	@Deprecated
	private String getNextResult(String xml) {
		String res1 = "<result>";
		String res2 = "</result>";
		if (xml.indexOf(res1) == -1)
			return null;
		xml = xml.substring(xml.indexOf(res1) + res1.length());
		xml = xml.substring(0, xml.indexOf(res2));
		// System.out.println(xml);
		return xml;
	}

	/**
	 * TODO used by old XML processing, can be removed once Jena is done
	 * 
	 * @param xml
	 * @param starttag
	 * @param endtag
	 * @return
	 */
	@Deprecated
	private String getinTag(String xml, String starttag, String endtag) {
		String res1 = "<" + starttag + ">";
		// System.out.println(res1);
		String res2 = "</" + endtag + ">";
		if (xml.indexOf(res1) == -1)
			return null;
		xml = xml.substring(xml.indexOf(res1) + res1.length());
		// System.out.println(xml);
		xml = xml.substring(0, xml.indexOf(res2));
		// System.out.println(xml);

		return xml;
	}

	@Deprecated
	public String sendAndReceiveSPARQL(String queryString) {
		// SparqlQuery sq=new SparqlQuery(configuration.getSparqlEndpoint());
		return new SparqlQuery(queryString, configuration.getSparqlEndpoint())
				.getAsXMLString();
	}

	public void p(String str) {
		if (print_flag) {
			System.out.println(str);
		}
	}

}
