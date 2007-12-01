package org.dllearner.kb.extraction.sparql;

import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Set;

import org.dllearner.kb.extraction.SparqlEndpoint;



public class SparqlHTTPRequest {
	static final char value[]={13,10};
	static final String cut=new String(value);
	
	private SparqlEndpoint SparqlEndpoint;
	private SimpleHTTPRequest SimpleHTTPRequest;
	
	
	public SparqlHTTPRequest(SparqlEndpoint SparqlEndpoint){
		this.SparqlEndpoint=SparqlEndpoint;		
		InetAddress ia=null;
		try{
		ia=InetAddress.getByName(SparqlEndpoint.getHost());
		}catch (Exception e) {e.printStackTrace();}
		this.SimpleHTTPRequest=new SimpleHTTPRequest(ia,SparqlEndpoint.getPort());
		
	}
	
	
	public String  sendAndReceiveSPARQL( String sparql){
		
		//System.out.println(sparql);
		String content=makeContent(sparql );
		//System.out.println(content);
		String ret= this.SimpleHTTPRequest.sendAndReceive(content);
		//System.out.println(ret);
		
		//this.sendAndReceiveSPARQL("SELECT * WHERE {?a ?b ?c} LIMIT 10");
		
		return ret;
	
		
	}//down
	
	
		
	
	public String makeContent(String query){
		
		
	String RequestHeader="";
	try{
		
	RequestHeader="GET ";
	RequestHeader+=SparqlEndpoint.getHasAfterGET()+"?";
	// parameters
	Set<String> s =SparqlEndpoint.getParameters().keySet();
	Iterator<String> it=s.iterator();
	while (it.hasNext()) {
		String element = (String) it.next();
		RequestHeader+=""+URLEncoder.encode(element, "UTF-8")+"="+
				URLEncoder.encode(SparqlEndpoint.getParameters().get(element), "UTF-8")+"&";
	}
	RequestHeader+=""+SparqlEndpoint.getHasQueryParameter()+"="+URLEncoder.encode(query, "UTF-8");
	RequestHeader+=" HTTP/1.1"+cut;
	RequestHeader+="Host: "+SparqlEndpoint.getHost()+cut;
	
	RequestHeader+=
	"Connection: close"+cut+
    //"Accept-Encoding: gzip"+cut+
    "Accept: text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"+cut+
    "Accept-Language: de-de,de;q=0.8,en-us;q=0.5,en;q=0.3"+cut+
    "Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7"+cut+
    "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.1.4) Gecko/20070515 Firefox/2.0.0.4 Web-Sniffer/1.0.24"+cut+
    cut;
	}catch (Exception e) {e.printStackTrace();}
	return RequestHeader;
		
	}
	
}
