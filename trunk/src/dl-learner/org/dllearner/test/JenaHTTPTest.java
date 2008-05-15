package org.dllearner.test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;

import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class JenaHTTPTest {
	public static void main(String[] args) throws Exception{
		
		String query="SELECT DISTINCT ?object\n"+
					"FROM <http://dbpedia.org>\n"+
					"WHERE { <http://dbpedia.org/resource/Leipzig> <http://www.w3.org/2000/01/rdf-schema#label> ?object}\n";
		
		double time=0;
		/*for (int i=0; i<101; i++)
		{
			if (i!=0) time+=JenaHTTPTest.httpQuery(query);
		}
		time=time/100;
		System.out.println("Durchschnittliche Zeit f�r eine Anfrage per Http-Methode: "+time);
		*/
		time=0;
		for (int i=0; i<101; i++)
		{
			if (i!=0) time+=JenaHTTPTest.jenaQuery(query);
		}
		time=time/100;
		System.out.println("Durchschnittliche Zeit f�r eine Anfrage DBpedia: "+time);
		
		time=0;
		for (int i=0; i<101; i++)
		{
			if (i!=0) time+=JenaHTTPTest.jenaLocalQuery(query);
		}
		time=time/100;
		System.out.println("Durchschnittliche Zeit f�r eine Anfrage per DBpedia LOCAL: "+time);
	}
	
	private static double jenaQuery(String query)
	{
		double start=System.currentTimeMillis();
		QueryEngineHTTP queryExecution=new QueryEngineHTTP("http://dbpedia.openlinksw.com:8890/sparql",query);
		queryExecution.addDefaultGraph("http://dbpedia.org");
		// Jena access to DBpedia SPARQL endpoint
		// ResultSet rs = 
		queryExecution.execSelect();
		double end=System.currentTimeMillis();
		return ((end-start)/1000);
	}
	
	private static double jenaLocalQuery(String query)
	{
		double start=System.currentTimeMillis();
		QueryEngineHTTP queryExecution=new QueryEngineHTTP("http://139.18.2.37:8890/sparql",query);
		queryExecution.addDefaultGraph("http://dbpedia.org");
		// Jena access to DBpedia SPARQL endpoint
		// ResultSet rs = 
		queryExecution.execSelect();
		double end=System.currentTimeMillis();
		return ((end-start)/1000);
	}
	
	private static double httpQuery(String query) throws Exception
	{
		char value[]={13,10};
		String cut=new String(value);
		String test="GET /sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=" +
		URLEncoder.encode(query, "UTF-8")+
		"&format=application%2Fsparql-results%2Bxml HTTP/1.1"+cut+
		"Host: localhost"+cut+
	    "Connection: close"+cut+
	    "Accept: text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"+cut+
	    "Accept-Language: de-de,de;q=0.8,en-us;q=0.5,en;q=0.3"+cut+
	    "Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7"+cut+
	    "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.1.4) Gecko/20070515 Firefox/2.0.0.4 Web-Sniffer/1.0.24"+cut+
	    cut;
		String retval="";
		//
		double start=System.currentTimeMillis();
		byte resp[]=null;
		
		try{
			Socket SparqlServer=new Socket(InetAddress.getByName("localhost"),8890);
			String request=test;
			// send request
			(SparqlServer.getOutputStream()).write(request.getBytes());
	
			//get Response
			resp=readBuffer(new BufferedInputStream(SparqlServer.getInputStream()));
			retval=new String(resp);
			retval=retval.substring(retval.indexOf(cut+""+cut)+4);
					
			SparqlServer.close();
		} catch (Exception e){}
		double end=System.currentTimeMillis();
		return (end-start)/1000;
	}
	
	private static byte[] readBuffer(InputStream IS)
	throws IOException{
		byte  buffer[] = new byte[0xffff];
		int nbytes=0;
		byte resp[]=new byte[0];
		while ((nbytes=IS.read(buffer))!=-1)	{
			byte tmp[]=new byte[resp.length+nbytes];
			int i=0;
			for (;i<resp.length;i++){
				tmp[i]=resp[i];
			}
			for(int a=0;a<nbytes;a++,i++){
				tmp[i]=buffer[a];
			}
			resp=tmp;
		}
		return resp;
	}
}
