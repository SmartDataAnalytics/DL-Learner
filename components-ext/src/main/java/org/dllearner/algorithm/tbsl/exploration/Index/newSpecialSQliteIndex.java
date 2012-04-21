package org.dllearner.algorithm.tbsl.exploration.Index;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class newSpecialSQliteIndex {
	private Connection conn;

	public ArrayList<String> getListOfUriSpecialIndex(String string){
		string = string.toLowerCase();
		String[] temp_list = string.split(" ");
		ArrayList<String> first_result=new ArrayList<String>();
		ArrayList<String> result=new ArrayList<String>();
		ArrayList<ArrayList<String>> tmp_result = new ArrayList<ArrayList<String>>();
		try {
			for(String s : temp_list)first_result.add(getNumberForWordInIndex(s));
			
			
			for(String s : first_result){
				ArrayList<String> second_result=new ArrayList<String>();
				if(s!=null){
					String[] tmp = s.split(":");
					
					for(String z : tmp) second_result.add(z);
						
						
					
				
					tmp_result.add(second_result);
				}
				
			}
		
			for(int i=1;i<tmp_result.size();i++){
				tmp_result.get(0).retainAll(tmp_result.get(i));
			}
			
			
			if(tmp_result.get(0).size()!=0){
				for(String s : tmp_result.get(0)){
					result.add(getUriForIndex(s));
				}
			}
			
			
			
			
			
			
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
		
		
		return result;
	}
	
	
	public newSpecialSQliteIndex() throws ClassNotFoundException, SQLException {
		// TODO Auto-generated constructor stub
		System.out.println("start");
		Class.forName( "org.sqlite.JDBC" );
		conn = DriverManager.getConnection("jdbc:sqlite::memory:");
		System.out.println("start IndexNumber");
		createNewSpecialIndexNumber();
		System.out.println("start SpecialIndex");
		createNewSpecialIndex();
		
		System.out.println("done");
	
	}

	
	public String getUriForIndex(String string) throws SQLException{
		/*  while(rs.next())
	      {*/
		  Statement stat = conn.createStatement();
		  ResultSet rs;
		try {
			rs = stat.executeQuery("select uri from newSpecialIndexNumber where name='"+string.toLowerCase()+"';");
			return rs.getString("uri");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
		  
	  }
	
	
	public String getNumberForWordInIndex(String string) throws SQLException{
		/*  while(rs.next())
	      {*/
		  Statement stat = conn.createStatement();
		  ResultSet rs;
		try {
			rs = stat.executeQuery("select uri from newSpecialIndex where name='"+string.toLowerCase()+"';");
			return rs.getString("uri");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
		  
	  }
private void createNewSpecialIndex() throws ClassNotFoundException, SQLException{
			System.out.println("start indexing Properties");
		    Statement stat = conn.createStatement();
		    stat.executeUpdate("drop table if exists newSpecialIndex;");
		    stat.executeUpdate("create table newSpecialIndex (name, uri);");
		    PreparedStatement prep = conn.prepareStatement("insert into newSpecialIndex values (?, ?);");
		    BufferedReader in=null;
		    int zaehler=0;
			try {
			      in = new BufferedReader(
			                          new InputStreamReader(
			                          new FileInputStream( "/home/swalter/workspace/SpecialIndex/Index" ) ) );
			      String s;
				while( null != (s = in.readLine()) ) {
			        String[] tmp_array =s.split(":::");
			        tmp_array[1]=tmp_array[1].replace("\n", "");
			        tmp_array[0]=tmp_array[0].replace("\n", "");
			        if(tmp_array.length>=2){
			        	prep.setString(1, tmp_array[0]);
			    	    prep.setString(2, tmp_array[1]);
			    	    prep.addBatch();
			    	    zaehler=zaehler+1;
			    	    if(zaehler%1000000==0){
			    	    	conn.setAutoCommit(false);
			    		    prep.executeBatch();
			    		    conn.setAutoCommit(false);
			    		   // System.out.println(zaehler+" done");
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
		    System.out.println("Number of newSpecialIndex: "+zaehler);
		    System.out.println("Done");
		    
		  }


private void createNewSpecialIndexNumber() throws ClassNotFoundException, SQLException{
	System.out.println("start indexing newSpecialIndexNumber");
    Statement stat = conn.createStatement();
    stat.executeUpdate("drop table if exists newSpecialIndexNumber;");
    stat.executeUpdate("create table newSpecialIndexNumber (name, uri);");
    PreparedStatement prep = conn.prepareStatement("insert into newSpecialIndexNumber values (?, ?);");
    BufferedReader in=null;
    int zaehler=0;
	try {
	      in = new BufferedReader(
	                          new InputStreamReader(
	                          new FileInputStream( "/home/swalter/workspace/SpecialIndex/IndexNumberUri" ) ) );
	      String s;
		while( null != (s = in.readLine()) ) {
	        String[] tmp_array =s.split(":::");
	       
	        tmp_array[1]=tmp_array[1].replace("\n", "");
	        tmp_array[0]=tmp_array[0].replace("\n", "");
	        /*System.out.println(tmp_array[0]);
	        System.out.println(tmp_array[1]);*/
	        if(tmp_array.length>=2){
	        	prep.setString(1, tmp_array[1]);
	    	    prep.setString(2, tmp_array[0]);
	    	    prep.addBatch();
	    	    zaehler=zaehler+1;
	    	    if(zaehler%1000000==0){
	    	    	conn.setAutoCommit(false);
	    		    prep.executeBatch();
	    		    conn.setAutoCommit(false);
	    		   // System.out.println(zaehler+" done");
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
    System.out.println("Number of IndexNumberUri: "+zaehler);
    System.out.println("Done");
    
  }



}