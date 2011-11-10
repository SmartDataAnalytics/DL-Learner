package org.dllearner.algorithm.tbsl.exploration.exploration_main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.dllearner.algorithm.tbsl.exploration.Sparql.Levenshtein;
import org.dllearner.algorithm.tbsl.exploration.sax.MySaxParser;


/*
 * 
 * wget "http://greententacle.techfak.uni-bielefeld.de:5171/sparql?default-graph-uri=&query=PREFIX+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%0D%0APREFIX+res%3A+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2F%3E%0D%0A%0D%0ASELECT+DISTINCT+%3Fp+%3Fl+WHERE++{%0D%0A+{+res%3ABerlin+%3Fp+%3Fo+.+}%0D%0A+UNION%0D%0A+{+%3Fs+%3Fp+res%3ABerlin+.+}%0D%0A+{+%3Fp+rdfs%3Alabel+%3Fl+.+}%0D%0A}&format=text%2Fhtml&debug=on&timeout=" -O bla.txt

 */
public class test_vergleich {

	public String DoVergleich(String suchbegriff, String vergleich) throws IOException{
		String ergebnis_string="";
		//sendServerRequest(vergleich);
		sendServerRequest_new(vergleich);
		ergebnis_string=do_parsing("answer",suchbegriff);
		
		return ergebnis_string;
		
			
	}
	
	private void sendServerRequest(String vergleich) throws IOException{
		String tmp="wget -O answer \"http://greententacle.techfak.uni-bielefeld.de:5171/sparql?default-graph-uri=&query=PREFIX+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%0D%0APREFIX+res%3A+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2F%3E%0D%0A%0D%0ASELECT+DISTINCT+%3Fp+%3Fl+WHERE++{%0D%0A+{+res%3ABerlin+%3Fp+%3Fo+.+}%0D%0A+UNION%0D%0A+{+%3Fs+%3Fp+res%3A"+vergleich+"+.+}%0D%0A+{+%3Fp+rdfs%3Alabel+%3Fl+.+}%0D%0A}%0D%0A&format=text%2Fhtml&debug=on&timeout=\"";
	    //System.out.println(tmp);
		Process p = Runtime.getRuntime().exec(tmp);
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendServerRequest_new(String vergleich) throws IOException{
		String tmp="http://greententacle.techfak.uni-bielefeld.de:5171/sparql?default-graph-uri=&query=PREFIX+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%0D%0APREFIX+res%3A+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2F%3E%0D%0A%0D%0ASELECT+DISTINCT+%3Fp+%3Fl+WHERE++{%0D%0A+{+res%3ABerlin+%3Fp+%3Fo+.+}%0D%0A+UNION%0D%0A+{+%3Fs+%3Fp+res%3A"+vergleich+"+.+}%0D%0A+{+%3Fp+rdfs%3Alabel+%3Fl+.+}%0D%0A}%0D%0A&format=text%2Fhtml&debug=on&timeout=";
		URL url;
	    InputStream is;
	    InputStreamReader isr;
	    BufferedReader r;
	    String str;
	    String result="";

	    try {
	      url = new URL(tmp);
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
	    
	    FileWriter w = new FileWriter("answer");
	    w.write(result);
	    w.close();
	}
	
	
	  private static String do_parsing(String datei, String suchbergriff)
	  {
	    ArrayList<String> indexObject = null;
	    String ergebnis_uri="";
	    double zwischenwert=0;
	    double tmp=0;
	    Levenshtein levenshtein = new Levenshtein();
	    
	    File file = new File(datei);
	    try
	    {
	      MySaxParser parser = new MySaxParser(file);
	      parser.parse();
	      indexObject = parser.getIndexObject();
	      for (int i = 1; i < indexObject.size(); i=i+2)
	      {
	    	System.out.println((indexObject.get(i)).toLowerCase());
			tmp = levenshtein.nld(suchbergriff.toLowerCase(), (indexObject.get(i)).toLowerCase());
			System.out.println(tmp);
			System.out.println("######");
			
			String ergebnis_string;
			if(tmp==1.0){
				zwischenwert=tmp;
				System.out.println(tmp);
				System.out.println("YEAH!!!!");
				ergebnis_string=indexObject.get(i);
				
				ergebnis_uri=indexObject.get(i-1);
				System.out.println(ergebnis_uri);
				i=indexObject.size();
				break;
			}
			if(tmp>zwischenwert){
				zwischenwert=tmp;
				System.out.println(tmp);
				ergebnis_string=indexObject.get(i);
				
				ergebnis_uri=indexObject.get(i-1);
				System.out.println(ergebnis_uri);
			}
	      }
	      indexObject.clear();

	    }
	    catch (Exception ex)
	    {
	      System.out.println("Another exciting error occured: " + ex.getLocalizedMessage());
	    }
	    
	    return ergebnis_uri;
	  }
	  
	  
	
}
