package org.dllearner.algorithm.tbsl.exploration.Sparql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dllearner.algorithm.tbsl.exploration.sax.MySaxParser;

public class GetRessourcePropertys {
	
	public HashMap<String,String> getPropertys(String element, String side) throws IOException{
			
		return sendServerPropertyRequest(element,side);
		
		
			
	}
	
	/**
	 * Get an uri and saves the properties of this resource
	 * @param vergleich
	 * @return 
	 * @throws IOException
	 */
	private HashMap<String,String> sendServerPropertyRequest(String vergleich, String side) throws IOException{
		/*
		 * 
		 * For the second Iteration, I can just add the sparql property here.
		 */
		
		/*
		 * 
		 * SELECT DISTINCT ?p WHERE {<http://dbpedia.org/resource/Berlin> ?y ?p.} für Berlin links der Property
		 * PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?s ?p WHERE {<http://dbpedia.org/resource/Berlin> ?p ?y. ?p rdfs:label ?s.}
		 * 
		 * SELECT DISTINCT ?p WHERE {?y ?p <http://dbpedia.org/resource/Berlin>.} für Berlin rechts der Property
		 * PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?s ?p WHERE {?y ?p <http://dbpedia.org/resource/Berlin>. ?p rdfs:label ?s.}
		 * http://greententacle.techfak.uni-bielefeld.de:5171/sparql?default-graph-uri=&query=PREFIX+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E+SELECT+DISTINCT+%3Fs+%3Fp+WHERE+{%3Fy+%3Fp+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FBerlin%3E.+%3Fp+rdfs%3Alabel+%3Fs.}&format=text%2Fhtml&debug=on&timeout=
		 */
		
		String vergleichorig = vergleich;
		
		
		String tmp_left="http://greententacle.techfak.uni-bielefeld.de:5171/sparql?default-graph-uri=&query="+createServerRequest("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?s ?p WHERE {?y ?p <"+vergleichorig+">. ?p rdfs:label ?s.}")+"%0D%0A&format=text%2Fhtml&debug=on&timeout=";
		//System.out.println("property right!!! : " +tmp_right);
		String tmp_right="http://greententacle.techfak.uni-bielefeld.de:5171/sparql?default-graph-uri=&query="+createServerRequest("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?s ?p WHERE {<"+vergleichorig+"> ?p ?y. ?p rdfs:label ?s.}")+"%0D%0A&format=text%2Fhtml&debug=on&timeout=";

		String verarbeitungsstring=null;
		if(side.contains("RIGHT")) verarbeitungsstring=tmp_right;
		if(side.contains("LEFT")) verarbeitungsstring=tmp_left;
		
		//just in case.....
		if(!side.contains("LEFT") && !side.contains("RIGHT")) verarbeitungsstring=tmp_left;

		//String verarbeitungsstring="http://greententacle.techfak.uni-bielefeld.de:5171/sparql?default-graph-uri=&query=PREFIX+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%0D%0APREFIX+res%3A+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2F%3E%0D%0A%0D%0ASELECT+DISTINCT+%3Fp+%3Fl+WHERE++{%0D%0A+{+res%3A"+vergleich+"+%3Fp+%3Fo+.+}%0D%0A+UNION%0D%0A+{+%3Fs+%3Fp+res%3A"+vergleich+"+.+}%0D%0A+{+%3Fp+rdfs%3Alabel+%3Fl+.+}%0D%0A}%0D%0A&format=text%2Fhtml&debug=on&timeout=";
		URL url;
	    InputStream is;
	    InputStreamReader isr;
	    BufferedReader r;
	    String str;
	    String result="";

	    try {
	      url = new URL(verarbeitungsstring);
	      is = url.openStream();
	      isr = new InputStreamReader(is);
	      r = new BufferedReader(isr);
	      do {
	        str = r.readLine();
	        if (str != null)
	          result=result+str;
	      } while (str != null);
	    } catch (MalformedURLException e) {
	      System.out.println("Must enter a valid URL");
	    } catch (IOException e) {
	      System.out.println("Can not connect");
	    }
	    
	 /*   FileWriter w = new FileWriter("answer_property");
	    w.write(result);
	    w.close();
	    */
	    
	    HashMap<String,String> hm = new HashMap();
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
	    	//System.out.println(tmp_array[i-1].toLowerCase() + "  " +tmp_array[i]);
	    }
	    
	    return hm;
	}
	
	
	  private static ArrayList<String> do_parsing(String datei)
	  {
	    ArrayList<String> indexObject = null;
	    
	    File file = new File(datei);
	    try
	    {
	      MySaxParser parser = new MySaxParser(file);
	      parser.parse();
	      indexObject = parser.getIndexObject();
	    }
	    catch (Exception ex)
	    {
	      System.out.println("Another exciting error occured: " + ex.getLocalizedMessage());
	    }
	    
	    return indexObject;
	  }
	  
	  
		
		
		private String createServerRequest(String query){
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
		
		private String removeSpecialKeys(String query){
			query=query.replace("\\","");
		    //query=query.replaceAll("\a","");
		    query=query.replace("\b","");
		    query=query.replace("\f","");
		    query=query.replace("\r","");
		    query=query.replace("\t","");
		   // query=query.replaceAll("\v","");
		    return query;
		}
	  
}
