package org.dllearner.kb.sparql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Set;



public class SparqlHTTPRequest {
	
	
	private SpecificSparqlEndpoint SparqlEndpoint;
	
	
	
	public SparqlHTTPRequest(SpecificSparqlEndpoint SparqlEndpoint){
		this.SparqlEndpoint=SparqlEndpoint;		
		
		
	}
	
	
	public String  sendAndReceiveSPARQL( String sparql){
		String ret= "";
		try{
		//System.out.println(sparql);
		
		//System.out.println(content);
		
		ret=this.sendAndReceive(sparql);
		//System.out.println(ret);
		
		//this.sendAndReceiveSPARQL("SELECT * WHERE {?a ?b ?c} LIMIT 10");
		}catch (Exception e) {e.printStackTrace();}
		return ret;
	
		
	}//down
	
	
		
	
	
	
	private String sendAndReceive(String sparql) throws IOException{
		StringBuilder answer = new StringBuilder();	
		
		// String an Sparql-Endpoint schicken
		HttpURLConnection connection;
			
		connection = (HttpURLConnection) this.SparqlEndpoint.getURL().openConnection();
		connection.setDoOutput(true);
							
		connection.addRequestProperty("Host", this.SparqlEndpoint.getHost());
		connection.addRequestProperty("Connection","close");
		connection.addRequestProperty("Accept","text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		connection.addRequestProperty("Accept-Language","de-de,de;q=0.8,en-us;q=0.5,en;q=0.3");
		connection.addRequestProperty("Accept-Charset","utf-8;q=1.0");
		connection.addRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.1.4) Gecko/20070515 Firefox/2.0.0.4 Web-Sniffer/1.0.24");
				
		OutputStream os = connection.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os);
		
		Set<String> s =SparqlEndpoint.getParameters().keySet();
		Iterator<String> it=s.iterator();
		String FullURI="";
		while (it.hasNext()) {
			String element = (String) it.next();
			FullURI+=""+URLEncoder.encode(element, "UTF-8")+"="+
					URLEncoder.encode(SparqlEndpoint.getParameters().get(element), "UTF-8")+"&";
		}
		//System.out.println(FullURI);
		FullURI+=""+SparqlEndpoint.getHasQueryParameter()+"="+URLEncoder.encode(sparql, "UTF-8");
		
		
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

