package org.dllearner.algorithm.tbsl.exploration.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class ServerUtil {

	
	//String static server_Prefix="http://greententacle.techfak.uni-bielefeld.de:5171/sparql";
	//private static String server_Prefix="http://dbpedia.org/sparql";
	private static String server_Prefix="http://greententacle.techfak.uni-bielefeld.de:5171/sparql";
	//private static String server_Prefix="http://purpurtentacle.techfak.uni-bielefeld.de:8890/sparql";
	
	private static int timeToTimeoutOnServer=30000;
	
		
	public static String createServerRequest(String query){
		String anfrage=null;
		anfrage=removeSpecialKeys(query);
	    anfrage=anfrage.replace("&lt;","<");
	    anfrage=anfrage.replace("%gt;",">");
	    anfrage=anfrage.replace("&amp;","&");
	    //anfrage=anfrage.replaceAll("#>","%23%3E%0D%0A%");
	    anfrage=anfrage.replace("#","%23");
	    anfrage=anfrage.replace(" ","+");
	    anfrage=anfrage.replace("/","%2F");
	    anfrage=anfrage.replace(":","%3A");
	    anfrage=anfrage.replace("?","%3F");
	    anfrage=anfrage.replace("$","%24");
	    //anfrage=anfrage.replaceAll("F&gt;+","F%3E%0D%0A");
	    anfrage=anfrage.replace(">","%3E");
	    anfrage=anfrage.replace("<","%3C");
	    anfrage=anfrage.replace("\"","%22");
	    anfrage=anfrage.replace("\n","%0D%0A%09");
	    anfrage=anfrage.replace("%%0D%0A%09","%09");
	    anfrage=anfrage.replace("=","%3D");
	    anfrage=anfrage.replace("@","%40");
	    anfrage=anfrage.replace("&","%26");
	    anfrage=anfrage.replace("(","%28");
	    anfrage=anfrage.replace(")","%29");
	    anfrage=anfrage.replace("%3E%0D%0A%25","%3E");
	    //anfrage=anfrage.replaceAll("\n",".%0D%0A%09");
		return anfrage;
	}
	
	private static String removeSpecialKeys(String query){
		query=query.replace("\\","");
	    //query=query.replaceAll("\a","");
	    query=query.replace("\b","");
	    query=query.replace("\f","");
	    query=query.replace("\r","");
	    query=query.replace("\t","");
	   // query=query.replaceAll("\v","");
	    return query;
	}

	public static String getServer_Prefix() {
		return server_Prefix;
	}

	public static int getTimeToTimeoutOnServer() {
		return timeToTimeoutOnServer;
	}

	public static void setTimeToTimeoutOnServer(int timeToTimeoutOnServer) {
		ServerUtil.timeToTimeoutOnServer = timeToTimeoutOnServer;
	}
	
	
	/**
	 * Get an uri and saves the properties of this resource
	 * @param vergleich
	 * @return 
	 * @throws IOException
	 */
	public static HashMap<String,String> sendServerPropertyRequest(String vergleich, String side) throws IOException{
		
		String vergleichorig = vergleich;
		
		String tmp_left=ServerUtil.getServer_Prefix()+"?default-graph-uri=&query="+ServerUtil.createServerRequest("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?s ?p WHERE {?y ?p <"+vergleichorig+">. ?p rdfs:label ?s.}")+"%0D%0A&format=text%2Fhtml&debug=on&timeout=";

		String tmp_right=ServerUtil.getServer_Prefix()+"?default-graph-uri=&query="+ServerUtil.createServerRequest("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?s ?p WHERE {<"+vergleichorig+"> ?p ?y. ?p rdfs:label ?s.}")+"%0D%0A&format=text%2Fhtml&debug=on&timeout=";

		String tmp_both=ServerUtil.getServer_Prefix()+"?default-graph-uri=&query="+ServerUtil.createServerRequest("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?s ?p WHERE {{?y ?p <"+vergleichorig+">. ?p rdfs:label ?s.} UNION {<"+vergleichorig+"> ?p ?y. ?p rdfs:label ?s.}}")+"%0D%0A&format=text%2Fhtml&debug=on&timeout=";
		String verarbeitungsurl=null;
		
		/*Original*/
		if(side.contains("RIGHT")) verarbeitungsurl=tmp_right;
		if(side.contains("LEFT")) verarbeitungsurl=tmp_left;
		if(side.contains("BOTH")) verarbeitungsurl=tmp_both;
		 
		if(!side.contains("LEFT") && !side.contains("RIGHT")) verarbeitungsurl=tmp_left;

	    String result="";
		HttpURLConnection connection = null;
	      BufferedReader rd  = null;
	      StringBuilder sb = null;
	      String line = null;
	    
	      URL serverAddress = null;
	    
	      try {
	          serverAddress = new URL(verarbeitungsurl);
	          //set up out communications stuff
	          connection = null;
	        
	          //Set up the initial connection
	          connection = (HttpURLConnection)serverAddress.openConnection();
	          connection.setRequestMethod("GET");
	          connection.setDoOutput(true);
	          connection.setReadTimeout(getTimeToTimeoutOnServer());
	                    
	          connection.connect();
	          rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	          sb = new StringBuilder();
	        
	          while ((line = rd.readLine()) != null)
	          {
	              sb.append(line + '\n');
	          }
	        
	          result=sb.toString();
	                    
	      } catch (MalformedURLException e) {
		      System.out.println("Must enter a valid URL");
		    } catch (IOException e) {
		      System.out.println("Can not connect or timeout");
		    }
	      finally
	      {
	          //close the connection, set all objects to null
	          connection.disconnect();
	          rd = null;
	          sb = null;
	          connection = null;
	      }
	    
	    HashMap<String,String> hm = new HashMap<String,String>();
	    result=result.replace("<th>s</th>","");
	    result=result.replace("<th>p</th>","");
	    result=result.replace("<table class=\"sparql\" border=\"1\">","");
	    result=result.replace("<tr>","");
	    result=result.replace("</tr>","");
	    result=result.replace("\n", "");
	    result=result.replace(" ", "");
	    result=result.replaceFirst("<td>", "");
	    
	    
	    String[] tmp_array=result.split("</td><td>");
	    
	    for(int i =1; i<=tmp_array.length-2;i=i+2) {
	    	hm.put(tmp_array[i-1].toLowerCase(), tmp_array[i]);
	    }
	    
	    return hm;
	}

}
