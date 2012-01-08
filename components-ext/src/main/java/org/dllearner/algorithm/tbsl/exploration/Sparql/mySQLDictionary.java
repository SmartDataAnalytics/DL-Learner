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
import java.util.ArrayList;

import org.dllearner.algorithm.tbsl.nlp.StanfordLemmatizer;

public class mySQLDictionary {
	private Connection conn;
	StanfordLemmatizer lemma;

	public mySQLDictionary() throws ClassNotFoundException, SQLException {
		// TODO Auto-generated constructor stub
		Class.forName( "org.sqlite.JDBC" );
		conn = DriverManager.getConnection("jdbc:sqlite::memory:");
		createIndexPropertys();
		createIndexResource();
		createWordnetHelp();
		createIndexOntology();
		createIndexOntologyClass();
		createIndexofYago();
		lemma = new StanfordLemmatizer();
		
		//optional!!
		//createIndexWikipedia();
	
	}

	/*
	 * Next, we want to select the persons living in a city that contains the pattern "tav" from the "Persons" table.

We use the following SELECT statement:
SELECT * FROM Persons
WHERE City LIKE '%tav%'
	 */
	
	public String getResourceURI(String string) throws SQLException{
		/*  while(rs.next())
	      {*/
		  Statement stat = conn.createStatement();
		  ResultSet rs;
		try {
			rs = stat.executeQuery("select uri from resource where name='"+string.toLowerCase()+"';");
			/*while(rs.next()){
				System.out.println("Next: "+rs.getString("uri"));
			}*/
			return rs.getString("uri");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
		  
	  }
	
	public ArrayList<String> getResourceURILike(String string) throws SQLException{
		/*  while(rs.next())
	      {*/
		  Statement stat = conn.createStatement();
		  ResultSet rs;
		  ArrayList<String> result= new ArrayList<String>();
		try {
			rs = stat.executeQuery("select uri from resource where name like'"+string.toLowerCase()+"%';");
			while(rs.next()){
				System.out.println("Next: "+rs.getString("uri"));
				result.add(rs.getString("uri"));
			}
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
		  
	  }
	
	public ArrayList<String> getYagoURILike(String string) throws SQLException{
		/*  while(rs.next())
	      {*/
		  Statement stat = conn.createStatement();
		  ResultSet rs;
		  ArrayList<String> result= new ArrayList<String>();
		try {
			rs = stat.executeQuery("select uri from yago where name like'"+string.toLowerCase()+"%';");
			while(rs.next()){
				System.out.println("Next: "+rs.getString("uri"));
				result.add(rs.getString("uri"));
			}
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
		  
	  }
	
	
	public String getYagoURI(String string) throws SQLException{
		/*  while(rs.next())
	      {*/
		  Statement stat = conn.createStatement();
		  ResultSet rs;
		try {
			rs = stat.executeQuery("select uri from yago where name='"+string.toLowerCase()+"';");
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
	
	public String getontologyURI(String string) throws SQLException{
		  Statement stat = conn.createStatement();
		  ResultSet rs;
		try {
			rs = stat.executeQuery("select uri from ontology where name='"+string.toLowerCase()+"';");
			return rs.getString("uri");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
	
		  
	  }
	
	public String getontologyClassURI(String string) throws SQLException{
		  Statement stat = conn.createStatement();
		  ResultSet rs;
		try {
			rs = stat.executeQuery("select uri from ontologyClass where name='"+string.toLowerCase()+"';");
			return rs.getString("uri");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
	
		  
	  }
	public ArrayList<String> getontologyClassURILike(String string) throws SQLException{
		  Statement stat = conn.createStatement();
		  ResultSet rs;
		  ArrayList<String> result= new ArrayList<String>();
		try {
			rs = stat.executeQuery("select uri from ontologyClass where name like'"+string.toLowerCase()+"%';");
			while(rs.next()){
				System.out.println("Next: "+rs.getString("uri"));
				result.add(rs.getString("uri"));
			}
			return result;
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
	
	public String getWordnetHelp(String string) throws SQLException{
		  Statement stat = conn.createStatement();
		  ResultSet rs;
		try {
			rs = stat.executeQuery("select singular from wordnet where plural='"+string.toLowerCase()+"';");
			return rs.getString("singular");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
	
		  
	  }
	
	private void createWordnetHelp() throws SQLException{		/*System.out.println("Start SQL test");
		Class.forName( "org.sqlite.JDBC" );
		conn = DriverManager.getConnection("jdbc:sqlite::memory:");*/
		System.out.println("start generating Wordnet Help-Function");
	    Statement stat = conn.createStatement();
	    stat.executeUpdate("drop table if exists wordnet;");
	    stat.executeUpdate("create table wordnet (plural, singular);");
	    PreparedStatement prep = conn.prepareStatement("insert into wordnet values (?, ?);");
	    BufferedReader in=null;
	   // conn.setAutoCommit(false);
	    int zaehler=0;
		try {
		      in = new BufferedReader(
		                          new InputStreamReader(
		                          new FileInputStream( "/home/swalter/workspace/noun.exc" ) ) );
		      String s;
			while( null != (s = in.readLine()) ) {
		        String[] tmp_array =s.split(" ");
		        if(tmp_array.length>=2){
		        	prep.setString(1, tmp_array[0]);
		    	    prep.setString(2, tmp_array[1]);
		    	    String temp="";
		    	    if(tmp_array.length>2){
		    	    	for(int i =1;i<tmp_array.length;i++){
		    	    		temp=temp+tmp_array[i]+" ";
		    	    	}
		    	    	prep.setString(2, temp);
		    	    }
		    	    prep.addBatch();
		    	    zaehler=zaehler+1;
		    	    //if(zaehler%10000==0) System.out.println(zaehler);
		    	    if(zaehler%10000==0){
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
		  }
private void createIndexPropertys() throws ClassNotFoundException, SQLException{
			System.out.println("start indexing Properties");
		    Statement stat = conn.createStatement();
		    stat.executeUpdate("drop table if exists property;");
		    stat.executeUpdate("create table property (name, uri);");
		    PreparedStatement prep = conn.prepareStatement("insert into property values (?, ?);");
		    BufferedReader in=null;
		    int zaehler=0;
			try {
			      in = new BufferedReader(
			                          new InputStreamReader(
			                          new FileInputStream( "/home/swalter/workspace/property" ) ) );
			      String s;
				while( null != (s = in.readLine()) ) {
			        String[] tmp_array =s.split(":::");
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
		    System.out.println("Number of Property: "+zaehler);
		    System.out.println("Done");
		    
		  }
private void createIndexResource() throws ClassNotFoundException, SQLException{
			System.out.println("start indexing Resources");
		    Statement stat = conn.createStatement();
		    stat.executeUpdate("drop table if exists resource;");
		    stat.executeUpdate("create table resource (name, uri);");
		    PreparedStatement prep = conn.prepareStatement("insert into resource values (?, ?);");
		    BufferedReader in=null;
		    int zaehler=0;
			try {
			      in = new BufferedReader(
			                          new InputStreamReader(
			                          new FileInputStream( "/home/swalter/workspace/resource" ) ) );
			      String s;
				while( null != (s = in.readLine()) ) {
			        String[] tmp_array =s.split(":::");
			        if(tmp_array.length>=2){
			        	prep.setString(1, tmp_array[0]);
			    	    prep.setString(2, tmp_array[1]);
			    	    prep.addBatch();
			    	    zaehler=zaehler+1;
			    	    if(zaehler%1000000==0){
			    	    	conn.setAutoCommit(false);
			    		    prep.executeBatch();
			    		    conn.setAutoCommit(false);
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
		    System.out.println("Number of Resources: "+zaehler);
		    System.out.println("Done");

			
		    
		  }
private void createIndexOntology() throws ClassNotFoundException, SQLException{
	/*System.out.println("Start SQL test");*/
		System.out.println("start indexing Ontology");
	    Statement stat = conn.createStatement();
	    stat.executeUpdate("drop table if exists ontology;");
	    stat.executeUpdate("create table ontology (name, uri);");
	    PreparedStatement prep = conn.prepareStatement("insert into ontology values (?, ?);");
	    BufferedReader in=null;
	   // conn.setAutoCommit(false);
	    int zaehler=0;
		try {
		      in = new BufferedReader(
		                          new InputStreamReader(
		                          new FileInputStream( "/home/swalter/workspace/ontology" ) ) );
		      String s;
			while( null != (s = in.readLine()) ) {
		        String[] tmp_array =s.split(":::");
		        if(tmp_array.length>=2){
		        	prep.setString(1, tmp_array[0]);
		    	    prep.setString(2, tmp_array[1]);
		    	    prep.addBatch();
		    	    zaehler=zaehler+1;
		    	  //  if(zaehler%10000==0) System.out.println(zaehler);
		    	    if(zaehler%1000000==0){
		    	    	conn.setAutoCommit(false);
		    		    prep.executeBatch();
		    		    conn.setAutoCommit(false);
		    		    //System.out.println("done" + zaehler);
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
	    System.out.println("Number of Ontologys: "+zaehler);
	    System.out.println("Done");
	    
	  }

private void createIndexOntologyClass() throws ClassNotFoundException, SQLException{
	/*System.out.println("Start SQL test");*/
		System.out.println("start indexing ontologyClass");
	    Statement stat = conn.createStatement();
	    stat.executeUpdate("drop table if exists ontologyClass;");
	    stat.executeUpdate("create table ontologyClass (name, uri);");
	    PreparedStatement prep = conn.prepareStatement("insert into ontologyClass values (?, ?);");
	    BufferedReader in=null;
	   // conn.setAutoCommit(false);
	    int zaehler=0;
		try {
		      in = new BufferedReader(
		                          new InputStreamReader(
		                          new FileInputStream( "/home/swalter/workspace/ontologyClass" ) ) );
		      String s;
			while( null != (s = in.readLine()) ) {
		        String[] tmp_array =s.split(":::");
		        if(tmp_array.length>=2){
		        	prep.setString(1, tmp_array[0]);
		    	    prep.setString(2, tmp_array[1]);
		    	    prep.addBatch();
		    	    zaehler=zaehler+1;
		    	  //  if(zaehler%10000==0) System.out.println(zaehler);
		    	    if(zaehler%1000000==0){
		    	    	conn.setAutoCommit(false);
		    		    prep.executeBatch();
		    		    conn.setAutoCommit(false);
		    		    //System.out.println("done" + zaehler);
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
	    System.out.println("Number of OntologyClass: "+zaehler);
	    System.out.println("Done");
	    
	  }


private void createIndexofYago() throws ClassNotFoundException, SQLException{
	/*System.out.println("Start SQL test");*/
		System.out.println("start indexing yago");
	    Statement stat = conn.createStatement();
	    stat.executeUpdate("drop table if exists yago;");
	    stat.executeUpdate("create table yago (name, uri);");
	    PreparedStatement prep = conn.prepareStatement("insert into yago values (?, ?);");
	    BufferedReader in=null;
	   // conn.setAutoCommit(false);
	    int zaehler=0;
		try {
		      in = new BufferedReader(
		                          new InputStreamReader(
		                          new FileInputStream( "/home/swalter/workspace/yago" ) ) );
		      String s;
			while( null != (s = in.readLine()) ) {
		        String[] tmp_array =s.split(":::");
		        if(tmp_array.length>=2){
		        	prep.setString(1, tmp_array[0]);
		    	    prep.setString(2, tmp_array[1]);
		    	    prep.addBatch();
		    	    zaehler=zaehler+1;
		    	  //  if(zaehler%10000==0) System.out.println(zaehler);
		    	    if(zaehler%1000000==0){
		    	    	conn.setAutoCommit(false);
		    		    prep.executeBatch();
		    		    conn.setAutoCommit(false);
		    		    //System.out.println("done" + zaehler);
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
	    System.out.println("Number of Yago: "+zaehler);
	    System.out.println("Done");
	    
	  }



}