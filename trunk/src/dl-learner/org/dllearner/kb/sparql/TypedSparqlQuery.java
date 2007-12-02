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

public class TypedSparqlQuery {
	private Configuration Configuration;
	// private SparqlHTTPRequest SparqlHTTPRequest;
	private SparqlQueryMaker SparqlQueryMaker;
	Cache Cache;

	public TypedSparqlQuery(Configuration Configuration) {
		this.Configuration = Configuration;
		// this.SparqlHTTPRequest = new SparqlHTTPRequest(Configuration.getSparqlEndpoint());
		this.SparqlQueryMaker = new SparqlQueryMaker(Configuration.getSparqlQueryType());
		this.Cache = new Cache("cache");
	}

	public Set<Tupel> query(URI u) {

		// getQuery
		String sparql = SparqlQueryMaker.makeQueryUsingFilters(u.toString());

		// check cache
		String FromCache = this.Cache.get(u.toString(), sparql);
		
		String xml = null;
		// if not in cache get it from EndPoint
		if (FromCache == null) {
			try {
				xml = sendAndReceiveSPARQL(sparql);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println(sparql);
			//System.out.println(xml);
			 this.Cache.put(u.toString(), xml, sparql);
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

	private String sendAndReceiveSPARQL(String sparql) throws IOException{
		StringBuilder answer = new StringBuilder();	
		
		// String an Sparql-Endpoint schicken
		HttpURLConnection connection;
		SpecificSparqlEndpoint se = Configuration.getSparqlEndpoint();	
		
		connection = (HttpURLConnection) se.getURL().openConnection();
		connection.setDoOutput(true);
							
		connection.addRequestProperty("Host", se.getHost());
		connection.addRequestProperty("Connection","close");
		connection.addRequestProperty("Accept","text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		connection.addRequestProperty("Accept-Language","de-de,de;q=0.8,en-us;q=0.5,en;q=0.3");
		connection.addRequestProperty("Accept-Charset","utf-8;q=1.0");
		connection.addRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.1.4) Gecko/20070515 Firefox/2.0.0.4 Web-Sniffer/1.0.24");
				
		OutputStream os = connection.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os);
		
		Set<String> s =se.getParameters().keySet();
		Iterator<String> it=s.iterator();
		String FullURI="";
		while (it.hasNext()) {
			String element = (String) it.next();
			FullURI+=""+URLEncoder.encode(element, "UTF-8")+"="+
					URLEncoder.encode(se.getParameters().get(element), "UTF-8")+"&";
		}
		//System.out.println(FullURI);
		FullURI+=""+se.getHasQueryParameter()+"="+URLEncoder.encode(sparql, "UTF-8");
		
		
		osw.write(FullURI);
		osw.close();
				
		// receive answer
		InputStream is = connection.getInputStream();
		InputStreamReader isr = new InputStreamReader(is,"UTF-8");
		BufferedReader br = new BufferedReader(isr);
			
		String line;
		do {
			line = br.readLine();
			if(line!=null)
				answer.append(line);
		} while (line != null);
			
		br.close();
				
		return answer.toString();
	}	
	
}
