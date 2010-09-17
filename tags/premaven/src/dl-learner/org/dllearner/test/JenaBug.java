package org.dllearner.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.dllearner.scripts.SparqlEndpointFinder;

import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class JenaBug {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {
		String query = "SELECT * WHERE {?s ?p ?o} LIMIT 1";
		for(String endpoint : new SparqlEndpointFinder().find()){
			System.out.println("ENDPOINT: " + endpoint);
			try {
				QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint, query);
				queryExecution.execSelect();
				System.out.println("WORKS");
			} catch (Exception e) {
				System.out.println("DOESN'T WORK");
			}
		}
		
//		JenaBug.sendGetRequest(endpoint + "?query=" + URLencodeUTF8.encode(query));

	}
	
	 public static String sendGetRequest(String urlStr)
	{
		String result = null;
		

		try
		{
			URL url = new URL(urlStr);
			System.out.println(urlStr);
			URLConnection conn = url.openConnection ();

			//	 Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line="";
			System.out.println("aa"+line);
			while ((line = rd.readLine()) != null)
			{
				System.out.println("aa"+line);
				sb.append(line);
			}
			rd.close();
			result = sb.toString();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
	
		return result;
	}


}
