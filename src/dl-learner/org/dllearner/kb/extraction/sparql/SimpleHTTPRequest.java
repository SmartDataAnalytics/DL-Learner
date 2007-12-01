package org.dllearner.kb.extraction.sparql;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;



public class SimpleHTTPRequest {
	static final char value[]={13,10};
	static final String cut=new String(value);
	private InetAddress ia;
	private int port;
	
	
	
	public SimpleHTTPRequest(InetAddress ia, int port) {
		super();
		this.ia = ia;
		this.port = port;
		
	}

	public String  sendAndReceive( String content){
		String retval="";
		//
		
		byte resp[]=null;
		
		try{
			Socket SparqlServer=new Socket(this.ia,this.port);
			//String request=makeHeader(content);
			// send request
			(SparqlServer.getOutputStream()).write(content.getBytes());

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
	

	private String sendAndReceive2(String sparql,URL url) throws IOException{
		StringBuilder answer = new StringBuilder();	
		
		// String an Sparql-Endpoint schicken
		HttpURLConnection connection;
			
		connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
							
		connection.addRequestProperty("Host", "dbpedia.openlinksw.com");
		connection.addRequestProperty("Connection","close");
		connection.addRequestProperty("Accept","text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		connection.addRequestProperty("Accept-Language","de-de,de;q=0.8,en-us;q=0.5,en;q=0.3");
		connection.addRequestProperty("Accept-Charset","utf-8;q=1.0");
		connection.addRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.1.4) Gecko/20070515 Firefox/2.0.0.4 Web-Sniffer/1.0.24");
				
		OutputStream os = connection.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os);
		osw.write("default-graph-uri=http%3A%2F%2Fdbpedia.org&query=" +
			URLEncoder.encode(sparql, "UTF-8")+
			"&format=application%2Fsparql-results%2Bxml");
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
