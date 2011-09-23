package org.dllearner.algorithm.tbsl.exploration.exploration_main;
import java.io.BufferedReader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

import net.didion.jwnl.JWNLException;

import org.dllearner.algorithm.tbsl.exploration.Sparql.SparqlObject;
import org.dllearner.algorithm.tbsl.exploration.sax.ParseXmlHtml;

/*
 * 
 * As you need more than 512 MB Ram, increase usable RAM for Java
 * in Eclipse Run -> RunConfigurations -> Arguments -> VM Arguments -> -Xmx1024m
 */

// Sax example from http://www.bennyn.de/programmierung/java/java-xml-sax-parser.html

/*
 * 
 * eins:http://greententacle.techfak.uni-bielefeld.de:5171/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Fx+%3Fl++WHERE+{%0D%0A++%3Fx+rdf%3Atype+%3Fc+.%0D%0A++%3Fx+rdfs%3Alabel+%3Fl+.%0D%0A++FILTER+%28lang%28%3Fl%29+%3D+%27en%27%29%0D%0A}&format=text%2Fhtml&debug=on&timeout=
 * zwei:http://greententacle.techfak.uni-bielefeld.de:5171/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Fc+%3Fl++WHERE+{%0D%0A++%3Fx+rdf%3Atype+%3Fc+.%0D%0A++%3Fc+rdfs%3Alabel+%3Fl+.%0D%0A++FILTER+%28lang%28%3Fl%29+%3D+%27en%27%29%0D%0A}&format=text%2Fhtml&debug=on&timeout=
 * 
 */
public class exploration_main {

	private static HashMap<String, String> hm = new HashMap<String, String>();
	private static String qaldEntity2="http://greententacle.techfak.uni-bielefeld.de:5171/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Fc+%3Fl++WHERE+{%0D%0A++%3Fx+rdf%3Atype+%3Fc+.%0D%0A++%3Fc+rdfs%3Alabel+%3Fl+.%0D%0A++FILTER+%28lang%28%3Fl%29+%3D+%27en%27%29%0D%0A}&format=text%2Fhtml&debug=on&timeout=";
	private static String qaldEntity1="http://greententacle.techfak.uni-bielefeld.de:5171/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Fx+%3Fl++WHERE+{%0D%0A++%3Fx+rdf%3Atype+%3Fc+.%0D%0A++%3Fx+rdfs%3Alabel+%3Fl+.%0D%0A++FILTER+%28lang%28%3Fl%29+%3D+%27en%27%29%0D%0A}&format=text%2Fhtml&debug=on&timeout=";
	/**
	 * @param args
	 * @throws IOException 
	 * @throws JWNLException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, JWNLException, InterruptedException {
		
		/**
		 * Do the starting initializing stuff
		 */
		long startInitTime = System.currentTimeMillis();

    	System.out.println("Start Indexing");
    	
    	 //For testing!
		hm=ParseXmlHtml.parse_xml("/home/swalter/workspace/ressource/sparql_zwei",hm);
		hm=ParseXmlHtml.parse_xml("/home/swalter/workspace/ressource/sparql_eins",hm);
		
    	
    	/*
    	 * For real use!
    	 */
	/*	hm=ParseXmlHtml.parse_xml((getEntity(qaldEntity2,"/tmp/qaldEntity2")),hm);
		System.out.println("Entity2 done");
		hm=ParseXmlHtml.parse_xml((getEntity(qaldEntity1,"/tmp/qaldEntity1")),hm);
		System.out.println("Entity1 done");*/
		System.out.println("Done with indexing\n");
		System.out.println("Start generating Wordnet Dictionary");
		SparqlObject sparql = new SparqlObject();
		System.out.println("Generating Wordnet Dictionary Done");
		long stopInitTime = System.currentTimeMillis();
		System.out.println("Time for Initialising "+(stopInitTime-startInitTime)+" ms");
		
		boolean schleife=true;
		while(schleife==true){
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line;
			try {
				System.out.println("\n\n");
				System.out.println("Please enter a Question:");
				line = in.readLine();
				if(line.contains("quit")){
					schleife=false;
					System.out.println("Bye!");
				}
				if(line.contains("text")&& schleife==true){
					TimeZone.setDefault(TimeZone.getTimeZone("GMT"));


					System.out.println("Please enter Path of txt. File:");
					line=in.readLine();
					
					//Start Time measuring
					long startTime = System.currentTimeMillis();
					String s="";
				    BufferedReader in_file = new BufferedReader(new InputStreamReader(new FileInputStream(line)));
				    int anzahl=0;
				    while( null != (s = in_file.readLine()) ) {
				    		System.out.println(s);
				    		anzahl++;
				    		//get each line and send it to the parser
				    		s=s.replace("?","");
				    		sparql.create_Sparql_query(s.toLowerCase(),hm);
				    }
				    long timeNow = System.currentTimeMillis();
				    long diff = timeNow-startTime;
				              
				    System.out.println("Time for "+anzahl+" questions = "+diff+" ms.");
				     
				}
				else if(schleife==true){
					long startTime = System.currentTimeMillis();
					line=line.replace("?","");
	            /*	Set<BasicQueryTemplate> querytemps = btemplator.buildBasicQueries(line);
	            	for (BasicQueryTemplate temp : querytemps) {
	            		System.out.println(temp.toString());
	            	}*/
					sparql.create_Sparql_query(line.toLowerCase(),hm);
					long endTime= System.currentTimeMillis();
					System.out.println("\n The complete answering of the Question took "+(endTime-startTime)+" ms");
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	
	private static String getEntity(String query, String name) throws IOException, InterruptedException{
		
	   // String query_complete="wget "+"\""+query+"\""+" -O "+"\""+name+"\"";
	    URL url = new URL(query);
	    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
	    //System.out.println(rbc.toString());
	    FileOutputStream fos = new FileOutputStream(name);
	    //max 200MB = 209715200 Byte
	    fos.getChannel().transferFrom(rbc, 0, 209715200 );

	    
	    return name;
	}

}
