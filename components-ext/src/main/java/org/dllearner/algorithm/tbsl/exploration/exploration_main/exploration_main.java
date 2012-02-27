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
				    int anzahl_query_with_answers=0;
				    int yago_querys=0;
					for(queryInformation s : list_of_structs){
						anzahl=anzahl+1;
				    	System.out.println("");
				    	if(s.getId()==""||s.getId()==null)System.out.println("NO");
						System.out.println("ID: "+s.getId());
						System.out.println("Query: "+s.getQuery());
						System.out.println("Type: "+s.getType());
						System.out.println("XMLType: "+s.getXMLtype());
						//queryInformation tmpquery;
						//only question, which are not yago files
						if(s.isYago()==true)yago_querys=yago_querys+1;
						//if(s.isYago()==false){
							queryInformation tmpquery=sparql.create_Sparql_query(s);
							if(!tmpquery.getResult().isEmpty()) {
								list_of_resultstructs.add(sparql.create_Sparql_query(s));
								anzahl_query_with_answers=anzahl_query_with_answers+1;
							}
						//}
					}
					
				    
				/*    //Print to Console
					System.out.println("\n#############\n Result:");
					for(queryInformation s : list_of_resultstructs){
						System.out.println(s.getResult());
					}*/
					String systemid="";
					systemid=createXML(list_of_resultstructs);
					writeQueryInformation(list_of_structs,systemid);
					writeTime(list_of_structs,systemid);
					
					//now create File with systemid for time and a file, which lists alle propertys and so on
					
				    long timeNow = System.currentTimeMillis();
				    long diff = timeNow-startTime;
				    String string1="Time for "+anzahl+" questions = "+diff+" ms.";
				    System.out.println(string1);
				    String string2="From "+anzahl_query_with_answers+" questions I got an answer";
				    String string3=yago_querys+ " Yago Questions were skiped";
				    System.out.println(string2);
				    System.out.println(string3);
				    String string4 ="Average time for one question : "+(diff/anzahl/1000)+"sek";
				    File file;
					FileWriter writer;
					file = new File("../../generalInformation"+systemid+".txt");
				     try {
				       writer = new FileWriter(file ,true);    
				       writer.write(string1+"\n"+string2+"\n"+string3+"\n"+string4);
				       writer.flush();
				       

				       writer.close();
				    } catch (IOException e) {
				      e.printStackTrace();
				    }
				     
				    System.out.println("Finished test");
				    System.exit(0);
				     
				}
				
				else if(schleife==true && doing ==true){
					long startTime = System.currentTimeMillis();
					queryInformation newQuery = new queryInformation(line,"0","",false,false,false,"non",false);
					queryInformation result = new queryInformation(line,"0","",false,false,false,"non",false);
					result= sparql.create_Sparql_query(newQuery);
					ArrayList<String> ergebnis = result.getResult();
					//get eacht result only once!
					Set<String> setString = new HashSet<String>();
					for(String i: ergebnis){
						setString.add(i);
						//System.out.println(i);
					}
					for(String z: setString){
						System.out.println(z);
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

	private static void writeQueryInformation(ArrayList<queryInformation> list, String systemid){
		String Document="";
		for (queryInformation s : list){
			ArrayList<ArrayList<String>> tmp = s.getQueryInformation();
			Document+= "Question "+s.getQuery()+" and ID "+s.getId()+"\n";
			for(ArrayList<String> z : tmp){
				String bla="";
				for(String p : z ){
					bla+=p+" ";
				}
				Document+=bla+"\n";
			}
			Document+="#########################\n";
		}
		
		File file;
		FileWriter writer;
		file = new File("../../queryInfromation"+systemid+".txt");
	     try {
	       writer = new FileWriter(file ,true);    
	       writer.write(Document);
	       writer.flush();
	       

	       writer.close();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	     
		
	}
	
	private static void writeTime(ArrayList<queryInformation> list, String systemid){
		String Document="";
		for (queryInformation s : list){
			Document+= "Question "+s.getQuery()+" and ID "+s.getId()+"\n"+"Gesamtzeit: "+s.getTimeGesamt()+"ParserZeit: "+s.getTimeParser() + "Iteration Zeit: "+s.getTimeWithoutParser()+"\n";
			
			Document+="#########################\n";
		}
		
		File file;
		FileWriter writer;
		file = new File("../../time"+systemid+".txt");
	     try {
	       writer = new FileWriter(file ,true);    
	       writer.write(Document);
	       writer.flush();
	       

	       writer.close();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	     
		
	}
	
	
	private static String createXML(ArrayList<queryInformation> list){
		
		java.util.Date now = new java.util.Date();

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
		String systemid = sdf.format(now);

		
		String xmlDocument="";
		int counter=0;
		for (queryInformation s : list){
			String tmp;
			if(counter==0){
				counter=counter+1;
				xmlDocument="<?xml version=\"1.0\" ?><dataset id=\""+s.getXMLtype()+"\">";
			}
			tmp="<question id=\""+s.getId()+"\"><string>"+s.getQuery()+"</string>\n<answers>";
			
			//to get all answers only once!
			Set<String> setString = new HashSet<String>();
			for(String z: s.getResult()){
				setString.add(z);
			}
			for(String i : setString){
				String input="";
				if(i.contains("http")) input="<uri>"+i+"</uri>\n";
				else if (i.contains("true")||i.contains("false")) input="<boolean>"+i+"</boolean>\n";
				else if(i.matches("[0-9]*"))input="<number>"+i+"</number>\n";
				else input="<string>"+i+"</string>\n";
				tmp+="<answer>"+input+"</answer>\n";
			}
			tmp+="</answers></question>\n";
			xmlDocument+=tmp;
			
		}
		xmlDocument+="</dataset>";
		File file;
		FileWriter writer;
		file = new File("../../result"+systemid+".xml");
	     try {
	       writer = new FileWriter(file ,true);    
	       writer.write(xmlDocument);
	       writer.flush();
	       

	       writer.close();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	     
	   return systemid;
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