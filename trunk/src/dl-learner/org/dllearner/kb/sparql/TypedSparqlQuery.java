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

import org.dllearner.kb.sparql.configuration.Configuration;
import org.dllearner.kb.sparql.configuration.SparqlEndpoint;
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
public class TypedSparqlQuery implements TypedSparqlQueryInterface{
	boolean print_flag=false;
	boolean debug_no_cache=false;// true means no cahce is used
	private Configuration configuration;
	// private SparqlHTTPRequest SparqlHTTPRequest;
	private SparqlQueryMaker sparqlQueryMaker;
	private SparqlQuery sparqlQuery;
	private CachedSparqlQuery cachedSparqlQuery;
	Cache cache;

	public TypedSparqlQuery(Configuration Configuration) {
		this.configuration = Configuration;
		// this.SparqlHTTPRequest = new
		// SparqlHTTPRequest(Configuration.getSparqlEndpoint());
		this.sparqlQueryMaker = new SparqlQueryMaker(Configuration.getSparqlQueryType());
		this.sparqlQuery=new SparqlQuery(configuration.getSparqlEndpoint());
		this.cache = new Cache("cache");
		this.cachedSparqlQuery=new CachedSparqlQuery(this.sparqlQuery,this.cache);
	}
	// standard query get a tupels (p,o) for subject s
	public Set<StringTuple> query(URI u) {

		// getQuery
		String sparql = sparqlQueryMaker.makeSubjectQueryUsingFilters(u.toString());
		return cachedSparql(u, sparql, "predicate", "object");

	}

	// query get a tupels (s,o) for role p
	public Set<StringTuple> getTupelsForRole(URI u) {

		// getQuery
		String sparql = sparqlQueryMaker.makeRoleQueryUsingFilters(u.toString());

		Set<StringTuple> s = cachedSparql(u, sparql, "subject", "object");
		// System.out.println(s);
		return s;

	}
	public Set<StringTuple> getTupelsForRole(URI u,boolean domain) {

		// getQuery
		String sparql = sparqlQueryMaker.makeRoleQueryUsingFilters(u.toString(),domain);

		Set<StringTuple> s = cachedSparql(u, sparql, "subject", "object");
		// System.out.println(s);
		return s;

	}

	
	// uses a cache 
	private Set<StringTuple> cachedSparql(URI u, String sparql, String a, String b) {
		// check cache
		String xml=this.cachedSparqlQuery.getAsXMLString(u, sparql);

		// System.out.println(sparql);
		// System.out.println(xml);
		// process XML
		Set<StringTuple> s = processResult(xml, a, b);
		try {
			//System.out.println("retrieved " + s.size() + " tupels\n");
		} catch (Exception e) {
		}
		return s;
	}

	public Set<StringTuple> processResult(String xml, String a, String b) {

		Set<StringTuple> ret = new HashSet<StringTuple>();
		// TODO if result is empty, catch exceptions
		String resEnd="</result>";
		String one = "binding name=\"" + a + "\"";
		String two = "binding name=\"" + b + "\"";
		String endbinding= "binding";
		String uri="uri";
		//String uridel = "<uri>";
		String bnode = "<bnode>";
		//String uriend = "</uri>";
		String predtmp = "";
		String objtmp = "";
		//System.out.println(getNextResult(xml));
		String nextResult="";
		while ((nextResult=getNextResult( xml))!=null){
			//System.out.println(xml.indexOf(resEnd));
			//System.out.println(xml);
			if(nextResult.indexOf(bnode)!=-1)
				{xml=xml.substring(xml.indexOf(resEnd)+resEnd.length());continue;}
			// get pred
			//predtmp = nextResult.substring(nextResult.indexOf(one) + one.length());
			predtmp=getinTag(nextResult, one,endbinding);
			predtmp=getinTag(predtmp, uri,uri);
			//System.out.println(predtmp);
			
			// getobj
			objtmp=getinTag(nextResult, two,endbinding);
			objtmp=getinTag(objtmp, uri,uri);
			//System.out.println(objtmp);
			
			StringTuple st=new StringTuple(predtmp, objtmp);
			//System.out.println(st);
			ret.add(st);
			xml=xml.substring(xml.indexOf(resEnd)+resEnd.length());
		
		}
		/*while (xml.indexOf(one) != -1) {
			
			

		
			// System.out.println(new Tupel(predtmp,objtmp));
		}*/

		return ret;

	}
	
	private String getNextResult(String xml){
		String res1="<result>";
		String res2="</result>";
		if(xml.indexOf(res1)==-1)return null;
		xml = xml.substring(xml.indexOf(res1) + res1.length());
		xml = xml.substring(0,xml.indexOf(res2) );
		//System.out.println(xml);
		return xml;
	}
	private String getinTag(String xml, String starttag, String endtag){
		String res1="<"+starttag+">";
		//System.out.println(res1);
		String res2="</"+endtag+">";
		if(xml.indexOf(res1)==-1)return null;
		xml = xml.substring(xml.indexOf(res1) + res1.length());
		//System.out.println(xml);
		xml = xml.substring(0,xml.indexOf(res2) );
		//System.out.println(xml);
		
		return xml;
	}

	public String sendAndReceiveSPARQL(String queryString){
		//SparqlQuery sq=new SparqlQuery(configuration.getSparqlEndpoint());
		return sparqlQuery.getAsXMLString(queryString);
	}
	
	public void p(String str){
		if(print_flag){
			System.out.println(str);
		}
	}
	
	

}
