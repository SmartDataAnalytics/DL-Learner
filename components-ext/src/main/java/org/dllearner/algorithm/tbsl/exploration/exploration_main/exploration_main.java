package org.dllearner.algorithm.tbsl.exploration.exploration_main;
import java.io.BufferedReader;

import java.io.BufferedInputStream;
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

import org.dllearner.algorithm.tbsl.exploration.Index.SQLiteIndex;
import org.dllearner.algorithm.tbsl.exploration.Sparql.SparqlObject;
import org.dllearner.algorithm.tbsl.exploration.Sparql.queryInformation;
import org.dllearner.algorithm.tbsl.nlp.StanfordLemmatizer;
import org.dllearner.algorithm.tbsl.nlp.WordNet;
import org.dllearner.algorithm.tbsl.templator.BasicTemplator;
import org.ibex.nestedvm.util.Seekable.InputStream;

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
		
		System.out.println("Starting Main File");
		long startInitTime = System.currentTimeMillis();

		/*
		 * Initial Index and Templator
		 */
		BasicTemplator btemplator = new BasicTemplator();
    	btemplator.UNTAGGED_INPUT = false;
		
		SQLiteIndex myindex = new SQLiteIndex();
		WordNet wordnet = new WordNet();
		StanfordLemmatizer lemmatiser = new StanfordLemmatizer();
		

		long stopInitTime = System.currentTimeMillis();
		
		Setting.setWaitModus(false);
		Setting.setDebugModus(false);
		

		
		System.out.println("Time for Initialising "+(stopInitTime-startInitTime)+" ms");
		
		boolean schleife=true;
		boolean startQuestioning = true;
		while(schleife==true){
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line;
			startQuestioning = true;
			try {
				System.out.println("\n\n");
				System.out.println("Please enter a Question:");
				line = in.readLine();
				if(line.contains(":q")){
					schleife=false;
					System.out.println("Bye!");
					System.exit(0);
				}
				
				if(line.contains(":wait on")){
					Setting.setWaitModus(true);
					startQuestioning=false;
					if(Setting.isWaitModus()) System.out.println("WaitModus is now online");
					else System.out.println("Wait Modus is now offline");
				}
				if(line.contains(":wait off")){
					Setting.setWaitModus(false);
					startQuestioning=false;
					if(Setting.isWaitModus()) System.out.println("WaitModus is now online");
					else System.out.println("Wait Modus is now offline");
				}
				if(line.contains(":debug on")){
					Setting.setDebugModus(true);
					startQuestioning=false;
					if(Setting.isDebugModus()) System.out.println("DebugModus is now online");
					else System.out.println("DebugModus is now offline");
				}
				if(line.contains(":debug off")){
					Setting.setDebugModus(false);
					startQuestioning=false;
					if(Setting.isDebugModus()) System.out.println("DebugModus is now online");
					else System.out.println("DebugModus is now offline");
				}
				
				

				if(line.contains(":xml")&& schleife==true){
					TimeZone.setDefault(TimeZone.getTimeZone("GMT"));


					/*System.out.println("Please enter Path of xml File:");
					line=in.readLine();*/
					//line="/home/swalter/Dokumente/Auswertung/XMLDateien/dbpedia-train.xml";
					//line="/home/swalter/Dokumente/Auswertung/XMLDateien/dbpedia-train-withoutnonparsed.xml";
					//line="/home/swalter/Dokumente/Auswertung/XMLDateien/very_small.xml";
					//line="/home/swalter/Dokumente/Auswertung/XMLDateien/berlin.xml";
					//line="/home/swalter/Dokumente/Auswertung/XMLDateien/vortragfragen.xml";
					//line="/home/swalter/Dokumente/Auswertung/XMLDateien/iteration-test.xml";
					line="/home/swalter/Dokumente/Auswertung/XMLDateien/dbpedia-train-tagged.xml";
					//line="/home/swalter/Dokumente/Auswertung/XMLDateien/dbpedia-train-tagged-withoutNotparsed.xml";
					//line="/home/swalter/Dokumente/Auswertung/XMLDateien/dbpedia-test-questions.xml";
					
					//create Structs
					ArrayList<queryInformation> list_of_structs = new ArrayList<queryInformation>();
					
					list_of_structs=generateStruct(line,true);
					//Start Time measuring
					long startTime = System.currentTimeMillis();
				    
				    int anzahl=0;
				    int anzahl_query_with_answers=0;
				    int yago_querys=0;
					for(queryInformation qi : list_of_structs){
						anzahl=anzahl+1;
				    	System.out.println("");
				    	if(qi.getId()==""||qi.getId()==null)System.out.println("NO");
						/*System.out.println("ID: "+qi.getId());
						System.out.println("Query: "+qi.getQuery());
						System.out.println("Type: "+qi.getType());
						System.out.println("XMLType: "+qi.getXMLtype());*/
						String question = qi.getQuery();
						ArrayList<String> answers=MainInterface.startQuestioning(question,btemplator,myindex,wordnet,lemmatiser);
						qi.setResult(answers);
					}
					
				    
				/*    //Print to Console
					System.out.println("\n#############\n Result:");
					for(queryInformation s : list_of_resultstructs){
						System.out.println(s.getResult());
					}*/
					
					long stopTime = System.currentTimeMillis();
					System.out.println("For "+anzahl+" Questions the QA_System took "+ ((stopTime-startTime)/1000)+"sek");
					
					String systemid="";
					systemid=createXML(list_of_structs);
				    String filename_for_evaluation="/home/swalter/Dokumente/Auswertung/ResultXml/result"+systemid.replace(" ", "_")+".xml";
				    String execute = "python /home/swalter/Dokumente/Auswertung/Evaluation/Evaluation.py "+filename_for_evaluation+" 0";
				    
				    /*
				     * First only for training
				     */
				    
				    System.out.println("execute: "+execute);
				    Runtime r = Runtime.getRuntime();
				    Process p = r.exec(execute);
				    
				    String open_file="/home/swalter/Dokumente/Auswertung/Evaluation/upload/out"+systemid.replace(" ", "_")+".html";
				    execute ="firefox "+ open_file;
				    p = r.exec(execute);
				     
				}
				
				else if(schleife==true && startQuestioning ==true){
					long startTime = System.currentTimeMillis();
					queryInformation result = new queryInformation(line,"0","",false,false,false,"non",false);
					MainInterface.startQuestioning(line,btemplator,myindex,wordnet,lemmatiser);
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
		System.out.println("In createXML");
		
		String xmlDocument="";
		int counter=0;
		System.out.println("Anzahl queryInformations: "+list.size());
		for (queryInformation s : list){
			//why doing this? try that it doesnt matter if there is an answer or not....
			//if(!s.getResult().isEmpty()){
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
					//System.out.println("i: "+i);
					String input="";
					if(i.contains("http")) input="<uri>"+i+"</uri>\n";
					else if (i.contains("true")||i.contains("false")) input="<boolean>"+i+"</boolean>\n";
					else if(i.matches("[0-9]*"))input="<number>"+i+"</number>\n";
					else if(i.matches("[0-9]*-[0-9][0-9]-[0-9]*"))input="<date>"+i+"</date>\n";
					else input="<string>"+i+"</string>\n";
					tmp+="<answer>"+input+"</answer>\n";
				}
				tmp+="</answers></question>\n";
				xmlDocument+=tmp;
			//}
			
		}
		xmlDocument+="</dataset>";
		File file;
		FileWriter writer;
		file = new File("/home/swalter/Dokumente/Auswertung/ResultXml/result"+systemid.replace(" ", "_")+".xml");
	     try {
	       writer = new FileWriter(file ,true);    
	       writer.write(xmlDocument);
	       writer.flush();
	       

	       writer.close();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	     
	   System.out.println("In createXML - Done");
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
	    /* string= string.replace(" answertype=\"number\"", "");
	     string= string.replace(" answertype=\"string\"", "");
	     string= string.replace(" answertype=\"date\"", "");
	     string= string.replace(" answertype=\"resource\"", "");
	     string= string.replace(" answertype=\"boolean\"", "");
	     string = string.replace(" aggregation=\"true\"", "");
	     string = string.replace(" aggregation=\"false\"", "");
	     string = string.replace(" onlydbo=\"false\"", "");
	     string = string.replace(" onlydbo=\"true\"", "");*/
	    
	    if(string.contains("id=\"dbpedia-train\"><question")){
	    	string=string.replace("id=\"dbpedia-train\"><question", "");
	    	XMLType="dbpedia-train";
	    	System.out.println("dbpedia-train");
	    }
	    else if(string.contains("id=\"dbpedia-test\"><question")){
	    	string=string.replace("id=\"dbpedia-test\"><question", "");
	    	XMLType="dbpedia-test";
	    	//System.out.println("dbpedia-test");
	    }
	    
	    else XMLType="dbpedia-train";
	    ArrayList<queryInformation> querylist = new ArrayList<queryInformation>();
	    String [] bla = string.split("</question><question");
	    for(String s : bla){
	    	String query="";
	    	String type="";
	   	 	boolean fusion=false;
	   	 	boolean aggregation=false;
	   	 	boolean yago=false;
	   	 	String id="";
	   	 
	   	 	/*
	   	 	 * Pattern p1= Pattern.compile("(id.*)\\</string\\>\\<query\\>.*");
	   	 	 */
	    	Pattern p1= Pattern.compile("(id.*)\\</string\\>\\<keywords\\>.*");
	    	Matcher m1 = p1.matcher(s);
	    	//System.out.println("");
	    	while(m1.find()){
	    		//System.out.println(m1.group(1));
	    		Pattern p2= Pattern.compile(".*><string>(.*)");
		    	Matcher m2 = p2.matcher(m1.group(1));
		    	while(m2.find()){
		    		//System.out.println("Query: "+ m2.group(1));
		    		
		    		query=m2.group(1);
		    		query=query.replace("<![CDATA[", "");
		    		query=query.replace("]]>", "");
		    		query=query.replace("CDATA", "");
		    		query=query.replace("]", "");
		    		query=query.replace("!", "");
		    		query=query.replace(">", "");
		    		query=query.replace("<", "");
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
	    for(queryInformation s : querylist){
	    	System.out.println("");
	    	if(s.getId()==""||s.getId()==null)System.out.println("NO");
			System.out.println("ID: "+s.getId());
			System.out.println("Query: "+s.getQuery());
			System.out.println("Type: "+s.getType());
			System.out.println("XMLType: "+s.getXMLtype());
		}
	    return querylist;
	}

	

}
