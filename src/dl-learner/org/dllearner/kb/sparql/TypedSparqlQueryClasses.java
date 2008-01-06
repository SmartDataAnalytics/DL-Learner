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

import org.dllearner.utilities.StringTuple;

/**
 * Can execute different queries.
 * 
 * @author Sebastian Hellmann
 *
 */
public class TypedSparqlQueryClasses implements TypedSparqlQueryInterface{
	boolean print_flag=false;
	boolean debug_no_cache=false;
	private Configuration configuration;
	// private SparqlHTTPRequest SparqlHTTPRequest;
	//private SparqlQueryMaker sparqlQueryMaker;
	Cache cache;

	
	public TypedSparqlQueryClasses(Configuration configuration) {
		this.configuration = configuration;
		this.cache = new Cache("cache");
	}
	
	// standard query get a tupels (p,o) for subject s
	public Set<StringTuple> query(URI u) {

		// getQuery
		String sparql = "SELECT ?predicate ?object " +
				"WHERE {" +
				"<"+u.toString()+"> ?predicate ?object;" +
						"a ?object . " +
		" FILTER (!regex(str(?object),'http://xmlns.com/foaf/0.1/'))"+
						"}";
			
		return cachedSparql(u, sparql, "predicate", "object");

	}

	
	
	// uses a cache 
	private Set<StringTuple> cachedSparql(URI u, String sparql, String a, String b) {
		// check cache
		String FromCache = cache.get(u.toString(), sparql);
		if(debug_no_cache) {
			FromCache=null;
			}
		String xml = null;
		// if not in cache get it from EndPoint
		if (FromCache == null) {
			try {
				xml = sendAndReceiveSPARQL(sparql);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			p(sparql);
			// System.out.println(xml);
			if(!debug_no_cache) {
				cache.put(u.toString(), sparql, xml);
			}
			//System.out.print("\n");
		} else {
			xml = FromCache;
			//System.out.println("FROM CACHE");
		}

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

	private String sendAndReceiveSPARQL(String sparql) throws IOException {
		p("sendAndReceiveSPARQL");
		StringBuilder answer = new StringBuilder();
		//sparql="SELECT * WHERE {?a ?b ?c}LIMIT 10";

		// String an Sparql-Endpoint schicken
		HttpURLConnection connection;
		SpecificSparqlEndpoint se = configuration.getSparqlEndpoint();
		p("URL: "+se.getURL());
		p("Host: "+se.getHost());
		
		connection = (HttpURLConnection) se.getURL().openConnection();
		connection.setDoOutput(true);

		//connection.addRequestProperty("Host", se.getHost());
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
		
		FullURI += "" + se.getHasQueryParameter() + "=" + URLEncoder.encode(sparql, "UTF-8");
		p(FullURI);
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
		p(answer.toString());
		return answer.toString();
	}
	public void p(String str){
		if(print_flag){
			System.out.println(str);
		}
	}

}
