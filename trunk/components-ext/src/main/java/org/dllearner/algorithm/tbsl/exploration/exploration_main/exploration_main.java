package org.dllearner.algorithm.tbsl.exploration.exploration_main;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.didion.jwnl.JWNLException;

import org.dllearner.algorithm.tbsl.exploration.Sparql.SparqlObject;
import org.dllearner.algorithm.tbsl.exploration.Sparql.queryInformation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * 
 * As you need more than 512 MB Ram, increase usable RAM for Java
 * in Eclipse Run -> RunConfigurations -> Arguments -> VM Arguments -> -Xmx1024m
 */


public class exploration_main {

	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws JWNLException 
	 * @throws InterruptedException 
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws IOException, JWNLException, InterruptedException, ClassNotFoundException, SQLException {
		
		/**
		 * Do the starting initializing stuff
		 */
		long startInitTime = System.currentTimeMillis();

		/*
		 * Create Sparql Object
		 */
		SparqlObject sparql = new SparqlObject();

		long stopInitTime = System.currentTimeMillis();
		System.out.println("Time for Initialising "+(stopInitTime-startInitTime)+" ms");
		
		boolean schleife=true;
		boolean doing = true;
		while(schleife==true){
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line;
			doing = true;
			try {
				System.out.println("\n\n");
				System.out.println("Please enter a Question:");
				line = in.readLine();
				if(line.contains(":q")){
					schleife=false;
					System.out.println("Bye!");
					System.exit(0);
				}
				if(line.contains(":setIterationdepth")){
					String[] tmp=line.split(" ");
					int i_zahl = new Integer(tmp[1]).intValue();
					if(tmp.length>=2) sparql.setIterationdepth(i_zahl);
					doing = false;
				}
				if(line.contains(":getIterationdepth")){
					System.out.println(sparql.getIterationdepth());
					doing = false;
				}
				if(line.contains(":setExplorationdepthwordnet")){
					String[] tmp=line.split(" ");
					int i_zahl = new Integer(tmp[1]).intValue();
					if(tmp.length>=2) sparql.setExplorationdepthwordnet(i_zahl);
					doing = false;
				}
				if(line.contains(":getExplorationdepthwordnet")){
					System.out.println(sparql.getExplorationdepthwordnet());
					doing = false;
				}
				if(line.contains(":setNumberofanswer")){
					String[] tmp=line.split(" ");
					int i_zahl = new Integer(tmp[1]).intValue();
					if(tmp.length>=2) sparql.setNumberofanswers(i_zahl);
					doing = false;
				}
				if(line.contains(":getNumberofanswer")){
					System.out.println(sparql.getNumberofanswers());
					doing = false;
				}

				if(line.contains(":textfile")&& schleife==true){
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
				    		//String query1, String id1, String type1, boolean fusion1, boolean aggregation1, boolean yago1, String XMLtype1
				    		queryInformation newQuery = new queryInformation(s,"0","",false,false,false,"non",false);
				    		queryInformation result = new queryInformation(s,"0","",false,false,false,"non",false);
				    		result=sparql.create_Sparql_query(newQuery);
				    		ArrayList<String> ergebnis = result.getResult();
							for(String i: ergebnis){
								System.out.println(i);
							}
				    }
				    long timeNow = System.currentTimeMillis();
				    long diff = timeNow-startTime;
				              
				    System.out.println("Time for "+anzahl+" questions = "+diff+" ms.");
				     
				}
				if(line.contains(":xml")&& schleife==true){
					TimeZone.setDefault(TimeZone.getTimeZone("GMT"));


					System.out.println("Please enter Path of xml File:");
					line=in.readLine();
					
					//create Structs
					ArrayList<queryInformation> list_of_structs = new ArrayList<queryInformation>();
					ArrayList<queryInformation> list_of_resultstructs = new ArrayList<queryInformation>();
					//if you dont want to use the hints in the questions, use false
					list_of_structs=generateStruct(line,true);
					//Start Time measuring
					long startTime = System.currentTimeMillis();
				    
				    int anzahl=0;
					for(queryInformation s : list_of_structs){
						anzahl=anzahl+1;
				    	System.out.println("");
				    	if(s.getId()==""||s.getId()==null)System.out.println("NO");
						System.out.println("ID: "+s.getId());
						System.out.println("Query: "+s.getQuery());
						System.out.println("Type: "+s.getType());
						System.out.println("XMLType: "+s.getXMLtype());
						list_of_resultstructs.add(sparql.create_Sparql_query(s));
					}
					
				    
				    //Print to Console
					System.out.println("\n#############\n Result:");
					for(queryInformation s : list_of_resultstructs){
						System.out.println(s.getResult());
					}
					createXML(list_of_resultstructs);
				    long timeNow = System.currentTimeMillis();
				    long diff = timeNow-startTime;
				              
				    System.out.println("Time for "+anzahl+" questions = "+diff+" ms.");
				     
				}
				
				else if(schleife==true && doing ==true){
					long startTime = System.currentTimeMillis();
					queryInformation newQuery = new queryInformation(line,"0","",false,false,false,"non",false);
					queryInformation result = new queryInformation(line,"0","",false,false,false,"non",false);
					result= sparql.create_Sparql_query(newQuery);
					ArrayList<String> ergebnis = result.getResult();
					for(String i: ergebnis){
						System.out.println(i);
					}
					long endTime= System.currentTimeMillis();
					System.out.println("\n The complete answering of the Question took "+(endTime-startTime)+" ms");
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	private static void createXML(ArrayList<queryInformation> list){
		
		
		String xmlDocument="";
		int counter=0;
		for (queryInformation s : list){
			String tmp;
			if(counter==0){
				counter=counter+1;
				xmlDocument="<?xml version=\"1.0\" ?><dataset id=\""+s.getXMLtype()+"\">";
			}
			tmp="<question id=\""+s.getId()+"\"><string>"+s.getQuery()+"</string><query></query><ANSWERS>";
			for(String i : s.getResult())tmp+="<answer>"+i+"</answer>";
			tmp+="</ANSWERS></question>";
			xmlDocument+=tmp;
			
		}
		xmlDocument+="</dataset>";
		File file;
		FileWriter writer;
		file = new File("/home/swalter/result.xml");
	     try {
	       writer = new FileWriter(file ,false);    
	       writer.write(xmlDocument);
	       writer.flush();
	       

	       writer.close();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	     
	}
	
	private static ArrayList<queryInformation> generateStruct(String filename, boolean hint) {
		
		String XMLType=null;
		
		BufferedReader in = null;
		
	    String tmp="";
		// Lies Textzeilen aus der Datei in einen Vector:
	    try {
	      in = new BufferedReader(
	                          new InputStreamReader(
	                          new FileInputStream(filename) ) );
	      String s;
		while( null != (s = in.readLine()) ) {
	        tmp=tmp+s;
	        //System.out.println(tmp);
	      }
	    } catch( FileNotFoundException ex ) {
	    } catch( Exception ex ) {
	      System.out.println( ex );
	    } finally {
	      if( in != null )
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }	
	    
		String string=tmp;
	    Pattern p = Pattern.compile (".*\\<question(.*)\\</question\\>.*");
	    Matcher m = p.matcher (string);
	    
	    
	    if(string.contains("id=\"dbpedia-train\"><question")){
	    	string=string.replace("id=\"dbpedia-train\"><question", "");
	    	XMLType="dbpedia-train";
	    	System.out.println("dbpedia-train");
	    }
	    if(string.contains("id=\"dbpedia-test\"><question")){
	    	string=string.replace("id=\"dbpedia-test\"><question", "");
	    	XMLType="dbpedia-test";
	    	//System.out.println("dbpedia-test");
	    }
	    ArrayList<queryInformation> querylist = new ArrayList<queryInformation>();
	    String [] bla = string.split("</question><question");
	    for(String s : bla){
	    	String query="";
	    	String type="";
	   	 	boolean fusion=false;
	   	 	boolean aggregation=false;
	   	 	boolean yago=false;
	   	 	String id="";
	   	 
	    	Pattern p1= Pattern.compile("(id.*)\\</string\\>\\<query\\>.*");
	    	Matcher m1 = p1.matcher(s);
	    	//System.out.println("");
	    	while(m1.find()){
	    		//System.out.println(m1.group(1));
	    		Pattern p2= Pattern.compile(".*><string>(.*)");
		    	Matcher m2 = p2.matcher(m1.group(1));
		    	while(m2.find()){
		    		//System.out.println("Query: "+ m2.group(1));
		    		query=m2.group(1);
		    	}
		    	Pattern p3= Pattern.compile("id=\"(.*)\" answer.*");
		    	Matcher m3 = p3.matcher(m1.group(1));
		    	while(m3.find()){
		    		//System.out.println("Id: "+ m3.group(1));
		    		id=m3.group(1);
		    	}
		    	
		    	Pattern p4= Pattern.compile(".*answertype=\"(.*)\" fusion.*");
		    	Matcher m4 = p4.matcher(m1.group(1));
		    	while(m4.find()){
		    		//System.out.println("answertype: "+ m4.group(1));
		    		type=m4.group(1);
		    	}
		    	
		    	Pattern p5= Pattern.compile(".*fusion=\"(.*)\" aggregation.*");
		    	Matcher m5 = p5.matcher(m1.group(1));
		    	while(m5.find()){
		    		//System.out.println("fusion: "+ m5.group(1));
		    		if(m5.group(1).contains("true"))fusion=true;
		    		else fusion=false;
		    	}
		    	
		    	Pattern p6= Pattern.compile(".*aggregation=\"(.*)\" yago.*");
		    	Matcher m6 = p6.matcher(m1.group(1));
		    	while(m6.find()){
		    		//System.out.println("aggregation: "+ m6.group(1));
		    		if(m6.group(1).contains("true"))aggregation=true;
		    		else aggregation=false;
		    	}
		    	
		    	Pattern p7= Pattern.compile(".*yago=\"(.*)\" ><string>.*");
		    	Matcher m7 = p7.matcher(m1.group(1));
		    	while(m7.find()){
		    		//System.out.println("yago: "+ m7.group(1));
		    		if(m7.group(1).contains("true"))yago=true;
		    		else yago=false;
		    	}
		    	
		    	
		    	
	    	}
	    	queryInformation blaquery=new queryInformation(query, id,type,fusion,aggregation,yago,XMLType,hint);
	    	if(id!=""&&id!=null) querylist.add(blaquery);
	    }
	   /* for(queryInformation s : querylist){
	    	System.out.println("");
	    	if(s.getId()==""||s.getId()==null)System.out.println("NO");
			System.out.println("ID: "+s.getId());
			System.out.println("Query: "+s.getQuery());
			System.out.println("Type: "+s.getType());
			System.out.println("XMLType: "+s.getXMLtype());
		}*/
	    return querylist;
	}

	

}
