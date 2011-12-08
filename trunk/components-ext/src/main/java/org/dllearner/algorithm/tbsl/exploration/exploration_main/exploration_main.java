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
				    		sparql.create_Sparql_query(s);
				    }
				    long timeNow = System.currentTimeMillis();
				    long diff = timeNow-startTime;
				              
				    System.out.println("Time for "+anzahl+" questions = "+diff+" ms.");
				     
				}
				else if(schleife==true && doing ==true){
					long startTime = System.currentTimeMillis();
					sparql.create_Sparql_query(line);
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
