package org.dllearner.kb.sparql;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class TypedSparqlQuery {
	private Configuration Configuration;
	private SparqlHTTPRequest SparqlHTTPRequest;
	private SparqlQueryMaker SparqlQueryMaker;
	Cache Cache;

	public TypedSparqlQuery(Configuration Configuration) {
		this.Configuration = Configuration;
		this.SparqlHTTPRequest = new SparqlHTTPRequest(Configuration.getSparqlEndpoint());
		this.SparqlQueryMaker = new SparqlQueryMaker(Configuration.getSparqlQueryType());
		this.Cache = new Cache("cache");
	}

	public Set<Tupel> query(URI u) {

		// getQuery
		String sparql = SparqlQueryMaker.makeQueryUsingFilters(u.toString());

		// check cache
		String FromCache = this.Cache.get(u.toString(), sparql);
		FromCache = null;
		String xml;
		// if not in cache get it from EndPoint
		if (FromCache == null) {
			xml = this.SparqlHTTPRequest.sendAndReceiveSPARQL(sparql);
			// this.Cache.put(u.toString(), xml, sparql);
			System.out.print("\n");
		} else {
			xml = FromCache;
			System.out.println("FROM CACHE");
		}

		// System.out.println(xml);
		// process XML
		Set<Tupel> s = this.processResult(xml);
		try {
			System.out.println("retrieved " + s.size() + " tupels");
		} catch (Exception e) {
		}
		return s;
	}

	public Set<Tupel> processResult(String xml) {

		Set<Tupel> ret = new HashSet<Tupel>();
		// TODO if result is empty, catch exceptions
		String one = "<binding name=\"predicate\">";
		String two = "<binding name=\"object\">";
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

}
