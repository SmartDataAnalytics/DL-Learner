package org.dllearner.algorithm.tbsl.exploration.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerUtil {

	
	//String static server_Prefix="http://greententacle.techfak.uni-bielefeld.de:5171/sparql";
	//private static String server_Prefix="http://dbpedia.org/sparql";
	private static String server_Prefix="http://greententacle.techfak.uni-bielefeld.de:5171/sparql";
	//private static String server_Prefix="http://purpurtentacle.techfak.uni-bielefeld.de:8890/sparql";
	
	private static int timeToTimeoutOnServer=30000;
	
	public static HashMap<String, String> generatesQueryForOutsideClasses(String query){
		String working_query= ServerUtil.getServer_Prefix()+"?default-graph-uri=&query="+ServerUtil.createServerRequest(query)+"%0D%0A&format=text%2Fhtml&debug=on&timeout=";
	    
	    return generateList(getListOfElements(working_query));
	    
	}
		
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
	 * Uses an URI to get the properties of this resource
	 * @param uri
	 * @return 
	 * @throws IOException
	 */
	public static HashMap<String,String> getPropertiesForGivenResource(String uri, String side) throws IOException{
		
		
		String query_property_left=ServerUtil.getServer_Prefix()+"?default-graph-uri=&query="+ServerUtil.createServerRequest("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?s ?p WHERE {?y ?p <"+uri+">. ?p rdfs:label ?s. FILTER (lang(?s) = 'en') }")+"%0D%0A&format=text%2Fhtml&debug=on&timeout=";

		String query_property_right=ServerUtil.getServer_Prefix()+"?default-graph-uri=&query="+ServerUtil.createServerRequest("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?s ?p WHERE {<"+uri+"> ?p ?y. ?p rdfs:label ?s. FILTER (lang(?s) = 'en') }")+"%0D%0A&format=text%2Fhtml&debug=on&timeout=";

		String query_property_leftANDright=ServerUtil.getServer_Prefix()+"?default-graph-uri=&query="+ServerUtil.createServerRequest("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?s ?p WHERE {{?y ?p <"+uri+">. ?p rdfs:label ?s. FILTER (lang(?s) = 'en') } UNION {<"+uri+"> ?p ?y. ?p rdfs:label ?s. FILTER (lang(?s) = 'en') }}")+"%0D%0A&format=text%2Fhtml&debug=on&timeout=";
		String verarbeitungsurl=null;
		
		/*Original*/
		if(side.contains("RIGHT")) verarbeitungsurl=query_property_right;
		if(side.contains("LEFT")) verarbeitungsurl=query_property_left;
		if(side.contains("BOTH")) verarbeitungsurl=query_property_leftANDright;
		 
		if(!side.contains("LEFT") && !side.contains("RIGHT")) verarbeitungsurl=query_property_left;

	    String result="";
	    /*System.out.println(verarbeitungsurl);
	    System.out.println("side: "+ side);*/
		result = getListOfElements(verarbeitungsurl);
	    
	    return generateList(result);
	}

	/**
	 * Uses an URI of a Class to get the Elements of the Class and the related URIs
	 * @param classUri
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String,String> getElementsForGivenClass(String classUri) throws IOException{
		
		/*
		PREFIX dbo: <http://dbpedia.org/ontology/>
SELECT DISTINCT ?p WHERE {
 { ?x ?p ?y . } UNION { ?y ?p ?x . }
    { 
      SELECT ?x {
        ?x rdf:type dbo:Mountain . 
      }
      LIMIT 10
    }
}
ORDER BY ?x 


		TODO:Try with different Limits
		 */
		
		/*
		 * TODO: Still a not "valid" url
		 */
		String query="SELECT DISTINCT ?s ?p WHERE {{?x ?p ?y. ?p rdfs:label ?s. FILTER (lang(?s) = 'en').} UNION {?y ?p ?x. ?p rdfs:label ?s. FILTER (lang(?s) = 'en').} { SELECT ?x { ?x rdf:type <"+classUri+">.}LIMIT 10}}";
		//System.out.println(query);
		//DebugMode.waitForButton();
		String query_final=ServerUtil.getServer_Prefix()+"?default-graph-uri=&query="+ServerUtil.createServerRequest(query)+"%0D%0A&format=text%2Fhtml&debug=on&timeout=";
	    String result="";
		result = getListOfElements(query_final);
	    
	    return generateList(result);
	}

	
	
	
	
	
	private static HashMap<String, String> generateList(String result) {
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
	
	

	private static String getListOfElements(String verarbeitungsurl) {
		
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
		      //System.out.println("Must enter a valid URL");
	    	  System.err.println("ATTENTION\n URL not valid : "+verarbeitungsurl+"\n");
		    } catch (IOException e) {
		    	System.err.println("Can not connect or timeout");
		    }
	      finally
	      {
	          //close the connection, set all objects to null
	          try{
	        	  connection.disconnect();
	          }
	          catch (Exception e){
	        	  System.err.println("ATTENTION\n Error in disconecting Connection to Server in getListOfElements\n ");
	          }
	          rd = null;
	          sb = null;
	          connection = null;
	      }
		return result;
	}
	
	public static ArrayList<String> requestAnswerFromServer(String query){
		String query_url=server_Prefix+"?default-graph-uri=&query="+createServerRequest(query)+"&format=text%2Fhtml&debug=on&timeout=";

		//System.out.println(tmp);
		String result="";
		HttpURLConnection connection = null;
	      OutputStreamWriter wr = null;
	      BufferedReader rd  = null;
	      StringBuilder sb = null;
	      String line = null;
	    
	      URL serverAddress = null;
	    
	      try {
	          serverAddress = new URL(query_url);
	          //set up out communications stuff
	          connection = null;
	        
	          //Set up the initial connection
	          connection = (HttpURLConnection)serverAddress.openConnection();
	          connection.setRequestMethod("GET");
	          connection.setDoOutput(true);
	          connection.setReadTimeout(timeToTimeoutOnServer);
	                    
	          connection.connect();
	          rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	          sb = new StringBuilder();
	        
	          while ((line = rd.readLine()) != null)
	          {
	              sb.append(line + '\n');
	          }
	        
	          //System.out.println(sb.toString());
	          result=sb.toString();
	                    
	      } catch (MalformedURLException e) {
	    	  System.err.println("ATTENTION\n URL not valid : "+query_url+"\n");
		    } catch (IOException e) {
		      System.out.println("Can not connect or timeout");
		    }
	      finally
	      {
	          //close the connection, set all objects to null
	          connection.disconnect();
	          rd = null;
	          sb = null;
	          wr = null;
	          connection = null;
	      }
	    
	    
	    
		return createAnswerArray(result);
	}
	
	private static ArrayList<String> createAnswerArray(String string){

		Pattern p = Pattern.compile (".*\\<td\\>(.*)\\</td\\>.*");
		string = string.replace("<table class=\"sparql\" border=\"1\">", "").replace("<tr>","").replace("</tr>", "").replace("</table>", "");
	    Matcher m = p.matcher (string);
	    String[] bla = string.split("   ");
	    
	    ArrayList<String> result= new ArrayList<String>();
	  	for(String s: bla){
	  		m=p.matcher(s);
	  		while (m.find()) {
	  			String temp = m.group(1);
	  			temp = temp.replace("\"@en","");
	  			temp = temp.replace("\"","");
	  			temp = temp.replace("^^<http://www.w3.org/2001/XMLSchema#date>","");
	  			temp = temp.replace("^^<http://www.w3.org/2001/XMLSchema#int>","");
	  			temp = temp.replace("^^<http://www.w3.org/2001/XMLSchema#number>","");
	  			temp = temp.replace("\"","");
	  			//result.add(m.group(1));
	  			result.add(temp);
	  			  		
	  		}
	    }
	  	
	  		
		//if (result.length()==0) result="EmtyAnswer";
	  	if(string.matches("true")|| string.matches("false")) result.add(string);
		return result;

	}
	

}
