package org.dllearner.algorithm.tbsl.exploration.Sparql;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class mySQLDictionary {
	private Connection conn;

	public mySQLDictionary() throws ClassNotFoundException, SQLException {
		// TODO Auto-generated constructor stub
		Class.forName( "org.sqlite.JDBC" );
		conn = DriverManager.getConnection("jdbc:sqlite::memory:");
		createIndexPropertys();
		createIndexResource();
		
		//optional!!
		//createIndexWikipedia();
	
	}

private String createSimpleHashKey(String string){
	string = string.replace("!","");
	string = string.replace(":","");
	string = string.replace("/","");
	string = string.replace("\\","");
	string = string.replace("?","");
	string = string.replace(":","");

	string = string.replace("a","1");
	string = string.replace("b","2");
	string = string.replace("c","3");
	string = string.replace("d","4");
	string = string.replace("e","5");
	string = string.replace("f","6");
	string = string.replace("g","7");
	string = string.replace("h","8");
	string = string.replace("i","9");
	string = string.replace("j","10");
	string = string.replace("k","11");
	string = string.replace("l","12");
	string = string.replace("m","13");
	string = string.replace("n","14");
	string = string.replace("o","15");
	string = string.replace("p","16");
	string = string.replace("q","17");
	string = string.replace("r","18");
	string = string.replace("s","19");
	string = string.replace("t","20");
	string = string.replace("u","21");
	string = string.replace("v","22");
	string = string.replace("w","23");
	string = string.replace("x","24");
	string = string.replace("y","25");
	string = string.replace("z","26");
	string = string.replace("ä","0");
	string = string.replace("ö","0");
	string = string.replace("ü","0");
	string = string.replace("?","0");
	string = string.replace(" ","0");
	return string;

	
}

	public String getResourceURI(String string) throws SQLException{
		  Statement stat = conn.createStatement();
		  ResultSet rs;
		try {
			rs = stat.executeQuery("select uri from resource where name='"+string.toLowerCase()+"';");
			return rs.getString("uri");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
		  
	  }

	public String getPropertyURI(String string) throws SQLException{
		  Statement stat = conn.createStatement();
		  ResultSet rs;
		try {
			rs = stat.executeQuery("select uri from property where name='"+string.toLowerCase()+"';");
			return rs.getString("uri");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
	
		  
	  }
	
	public String getWikipediaURI(String string) throws SQLException{
		  Statement stat = conn.createStatement();
		  ResultSet rs;
		try {
			rs = stat.executeQuery("select uri from wikiindex where name='"+string.toLowerCase()+"';");
			return rs.getString("uri");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
	
		  
	  }
	
	
	  private void createIndexWikipedia() throws ClassNotFoundException, SQLException{
			/*System.out.println("Start SQL test");
			Class.forName( "org.sqlite.JDBC" );
			conn = DriverManager.getConnection("jdbc:sqlite::memory:");*/
		    Statement stat = conn.createStatement();
		    stat.executeUpdate("drop table if exists wikiindex;");
		    stat.executeUpdate("create table wikiindex (name, uri);");
		    PreparedStatement prep = conn.prepareStatement("insert into wikiindex values (?, ?);");
		    BufferedReader in=null;
		   // conn.setAutoCommit(false);
		    int zaehler=0;
			try {
			      in = new BufferedReader(
			                          new InputStreamReader(
			                          new FileInputStream( "/home/swalter/workspace/URIsFromWikipedia" ) ) );
			      String s;
				while( null != (s = in.readLine()) ) {
			        String[] tmp_array =s.split("::");
			        if(tmp_array.length>=2){
			        	prep.setString(1, tmp_array[0]);
			    	    prep.setString(2, tmp_array[1]);
			    	    prep.addBatch();
			    	    zaehler=zaehler+1;
			    	    //if(zaehler%100000==0) System.out.println(zaehler);
			    	    if(zaehler%1000000==0){
			    	    	conn.setAutoCommit(false);
			    		    prep.executeBatch();
			    		    conn.setAutoCommit(false);
			    		    System.out.println("done");
			    	    }

			        }
			      }
			    } catch( FileNotFoundException ex ) {
			    } catch( Exception ex ) {
			      System.out.println( ex );
			    } finally {
			      if( in != null )
					try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
		 
		    conn.setAutoCommit(false);
		    prep.executeBatch();
		    conn.setAutoCommit(true);
		    System.out.println("Done");
		    //Statement stat = conn.createStatement();
		/*    long start = System.currentTimeMillis();
		    // zu messender Code
		    
		    ResultSet rs = stat.executeQuery("select * from people where name='kornyval';");
		    while (rs.next())
		    {
		      System.out.println("name = " + rs.getString("name"));
		      System.out.println("job = " + rs.getString("occupation"));
		    }
		    System.out.println("Duration in ms: " + (System.currentTimeMillis() - start));
		    
		    start = System.currentTimeMillis();
		    // zu messender Code
		    
		    rs = stat.executeQuery("select * from people where name='barack obama';");
		    while (rs.next())
		    {
		      System.out.println("name = " + rs.getString("name"));
		      System.out.println("job = " + rs.getString("occupation"));
		    }
		    System.out.println("Duration in ms: " + (System.currentTimeMillis() - start));
		    
		    rs = stat.executeQuery("select * from people where name='kornyval';");
		    while (rs.next())
		    {
		      System.out.println("name = " + rs.getString("name"));
		      System.out.println("job = " + rs.getString("occupation"));
		    }
		    System.out.println("Duration in ms: " + (System.currentTimeMillis() - start));
		    
		    
		    rs.close();*/
		   // conn.close();
		  }
private void createIndexPropertys() throws ClassNotFoundException, SQLException{
			/*System.out.println("Start SQL test");
			Class.forName( "org.sqlite.JDBC" );
			conn = DriverManager.getConnection("jdbc:sqlite::memory:");*/
			System.out.println("start indexing Properties");
		    Statement stat = conn.createStatement();
		    stat.executeUpdate("drop table if exists property;");
		    stat.executeUpdate("create table property (name, uri);");
		    PreparedStatement prep = conn.prepareStatement("insert into property values (?, ?);");
		    BufferedReader in=null;
		   // conn.setAutoCommit(false);
		    int zaehler=0;
			try {
			      in = new BufferedReader(
			                          new InputStreamReader(
			                          new FileInputStream( "/home/swalter/workspace/property" ) ) );
			      String s;
				while( null != (s = in.readLine()) ) {
			        String[] tmp_array =s.split(":::");
			        if(tmp_array.length>=2){
			        	prep.setString(1, tmp_array[1]);
			    	    prep.setString(2, tmp_array[0]);
			    	    prep.addBatch();
			    	    zaehler=zaehler+1;
			    	    //if(zaehler%10000==0) System.out.println(zaehler);
			    	    if(zaehler%1000000==0){
			    	    	conn.setAutoCommit(false);
			    		    prep.executeBatch();
			    		    conn.setAutoCommit(false);
			    		    System.out.println("done");
			    	    }

			        }
			      }
			    } catch( FileNotFoundException ex ) {
			    } catch( Exception ex ) {
			      System.out.println( ex );
			    } finally {
			      if( in != null )
					try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
		 
		    conn.setAutoCommit(false);
		    prep.executeBatch();
		    conn.setAutoCommit(true);
		    System.out.println("Done");
		    
		  }
private void createIndexResource() throws ClassNotFoundException, SQLException{
		/*System.out.println("Start SQL test");*/
			System.out.println("start indexing Resources");
		    Statement stat = conn.createStatement();
		    stat.executeUpdate("drop table if exists resource;");
		    stat.executeUpdate("create table resource (name, uri);");
		    PreparedStatement prep = conn.prepareStatement("insert into resource values (?, ?);");
		    BufferedReader in=null;
		   // conn.setAutoCommit(false);
		    int zaehler=0;
			try {
			      in = new BufferedReader(
			                          new InputStreamReader(
			                          new FileInputStream( "/home/swalter/workspace/resource" ) ) );
			      String s;
				while( null != (s = in.readLine()) ) {
			        String[] tmp_array =s.split(":::");
			        if(tmp_array.length>=2){
			        	prep.setString(1, tmp_array[1]);
			    	    prep.setString(2, tmp_array[0]);
			    	    prep.addBatch();
			    	    zaehler=zaehler+1;
			    	  //  if(zaehler%10000==0) System.out.println(zaehler);
			    	    if(zaehler%1000000==0){
			    	    	conn.setAutoCommit(false);
			    		    prep.executeBatch();
			    		    conn.setAutoCommit(false);
			    		    System.out.println("done");
			    	    }

			        }
			      }
			    } catch( FileNotFoundException ex ) {
			    } catch( Exception ex ) {
			      System.out.println( ex );
			    } finally {
			      if( in != null )
					try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
		 
		    conn.setAutoCommit(false);
		    prep.executeBatch();
		    conn.setAutoCommit(true);
		    System.out.println("Done");
		    
		  }

}