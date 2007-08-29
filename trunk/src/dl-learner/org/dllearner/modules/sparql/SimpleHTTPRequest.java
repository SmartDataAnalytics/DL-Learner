package org.dllearner.modules.sparql;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;



public class SimpleHTTPRequest {
	static final char value[]={13,10};
	static final String cut=new String(value);
	
	
	
	
	public String  sendAndReceive(InetAddress ia, int port, String sparql){
		String retval="";
		//
		
		byte resp[]=null;
		
		try{
			Socket SparqlServer=new Socket(ia,port);
			String request=makeHeader(sparql);
			// send request
			(SparqlServer.getOutputStream()).write(request.getBytes());

			//get Response
			resp=readBuffer(new BufferedInputStream(SparqlServer.getInputStream()));
			retval=new String(resp);
			retval=subtractResponseHeader(retval);
			//retval="||"+retval;
			
			SparqlServer.close();

			

		}
		catch(Exception e){e.printStackTrace();}
		//System.out.println("got it");
		return retval;
		
	}//down
	
	public static byte[] readBuffer(InputStream IS)
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
	
	public String subtractResponseHeader(String in){
		//System.out.println(in.indexOf(cut+""+cut));
		return in.substring(in.indexOf(cut+""+cut)+4);
		
		
	}
	
	public String makeHeader(String query){
	
		
		String RequestHeader="";
	try{
		
	RequestHeader="GET /sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=" +
	//"SELECT%20%2A%20WHERE%20%7B%20%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FAristotle%3E%20%3Fa%20%3Fb%20%7D%20" +
	URLEncoder.encode(query, "UTF-8")+
	//query+// URLencode
	"&format=application%2Fsparql-results%2Bxml HTTP/1.1"+cut+
	"Host: dbpedia.openlinksw.com"+cut+
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
