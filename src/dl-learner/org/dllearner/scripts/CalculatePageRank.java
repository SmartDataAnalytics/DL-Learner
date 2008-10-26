/**
 * Copyright (C) 2007-2008, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.ini4j.IniFile;

/**
 * Fills that database needed for running DBpedia Navigator.
 * First move the mentioned DBpedia files to the specified
 * directory, then execute this script. Database settings are
 * taken from the settings.ini file of DBpedia Navigator.
 * 
 * @author Sebastian Knappe
 * @author Jens Lehmann
 *
 */
public class CalculatePageRank {
	
	private static String datasetDir;
	private static String dbServer;
	private static String dbName;
	private static String dbUser;
	private static String dbPass;
	
	private final String wikilinks = datasetDir + "pagelinks_en.nt";
	private final String labels = datasetDir + "articles_label_en.nt";
	private final String categories = datasetDir + "yago_en.nt";
	private final String categoriesNewOntology = datasetDir + "dbpedia-ontology-schema.nt";
	private final String categoriesNewOntology2 = datasetDir + "dbpedia-ontology-types.nt";
	
	private static Connection con;
	
	public CalculatePageRank() throws BackingStoreException
	{
		// reading values from ini file
		String iniFile = "src/dbpedia-navigator/settings.ini";
		Preferences prefs = new IniFile(new File(iniFile));
		dbServer = prefs.node("database").get("name", null);
		dbName = prefs.node("database").get("name", null);
		dbUser = prefs.node("database").get("user", null);
		dbPass = prefs.node("database").get("pass", null);
		datasetDir = prefs.node("database").get("datasetDir", null);
	}
	
	private void calculateLinks()
	{
		try{
			Statement stmt;
			ResultSet rs;
			int number;

			stmt = con.createStatement();
			BufferedReader in = new BufferedReader(new FileReader(wikilinks));
			
			String line;
			String[] split;
			String name;
			int i=0;
			while ((line=in.readLine())!=null)
			{
				split=line.split(" ");
				name=split[2].substring(1, split[2].length()-1);
				rs=stmt.executeQuery("SELECT number FROM rank WHERE name='"+name+"'");
				if (rs.next()){
					number=rs.getInt(1);
					number++;
					stmt.executeUpdate("UPDATE rank SET number="+number+" WHERE name='"+name+"'");
				}
				else{
					try{
						stmt.executeUpdate("INSERT INTO rank (name,number) VALUES ('"+name+"',1)");
					}catch(Exception e)
					{}
				}
				if (i%100000==0) System.out.println(i);
				i++;
			}
			
			in.close();
		} catch (FileNotFoundException e)
		{
			System.out.println("File not found");
		} catch (IOException e)
		{
			System.out.println("IOException");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addLabels()
	{
		try{
			Statement stmt;
			ResultSet rs;
			
			stmt = con.createStatement();
			BufferedReader in = new BufferedReader(new FileReader(labels));
			
			String line;
			String[] split;
			String name;
			String label;
			int i=0;
			while ((line=in.readLine())!=null)
			{
				split=line.split(">");
				name=split[0].substring(1);
				label=split[2].substring(split[2].indexOf("\"")+1, split[2].lastIndexOf("\""));
				rs=stmt.executeQuery("SELECT number FROM rank WHERE name='"+name+"'");
				if (rs.next()){
					stmt.executeUpdate("UPDATE rank SET label=\""+label+"\" WHERE name='"+name+"'");
				}
				else{
					try{
						stmt.executeUpdate("INSERT INTO rank (name,label) VALUES ('"+name+"',\""+label+"\")");
					}catch(Exception e)
					{}
				}
				if (i%100000==0) System.out.println(i);
				i++;
			}
			
			in.close();
		} catch (FileNotFoundException e)
		{
			System.out.println("File not found");
		} catch (IOException e)
		{
			System.out.println("IOException");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void calculateCategories()
	{
		try{
			Statement stmt;
						
			stmt = con.createStatement();
			
			BufferedReader in = new BufferedReader(new FileReader(categories));
			
			String line;
			String[] split;
			String name;
			String label;
			String pred;
			int i=0;
			while ((line=in.readLine())!=null)
			{
				split=line.split(">");
				name=split[0].substring(1);
				pred=split[1].substring(2);
				if (pred.equals("http://www.w3.org/2000/01/rdf-schema#label"))
					label=split[2].substring(split[2].indexOf("\"")+1, split[2].lastIndexOf("\""));
				else
					label=split[2].substring(2);
				if (pred.equals("http://www.w3.org/2000/01/rdf-schema#label")){
					try{
						stmt.executeUpdate("INSERT INTO categories (category,label) VALUES (\""+name+"\",\""+label+"\")");
					}catch(Exception e)
					{}
				}
				else{
					if (name.startsWith("http://dbpedia.org/resource")){
						try{
							stmt.executeUpdate("INSERT INTO articlecategories (name,category) VALUES ('"+name+"','"+label+"')");
						}catch(Exception e)
						{}
					}else{
						try{
							stmt.executeUpdate("INSERT INTO classhierarchy (father,child) VALUES ('"+label+"','"+name+"')");
						}catch(Exception e)
						{}
					}
				}
				if (i%100000==0) System.out.println(i);
				i++;
			}
			
			in.close();
		} catch (FileNotFoundException e)
		{
			System.out.println("File not found");
		} catch (IOException e)
		{
			System.out.println("IOException");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void calculateCategoriesNewOntology()
	{
		try{
			Statement stmt;
						
			stmt = con.createStatement();
			
			BufferedReader in = new BufferedReader(new FileReader(categoriesNewOntology));
			
			String line;
			String[] split;
			String name;
			String label;
			String pred;
			int i=0;
			boolean isClassLabel;
			String className;
			while ((line=in.readLine())!=null)
			{
				split=line.split(">");
				name=split[0].substring(1);
				className=name.substring(name.lastIndexOf("/")+1,name.lastIndexOf("/")+2);
				if (className.toLowerCase().equals(className))
					isClassLabel=false;
				else
					isClassLabel=true;
				pred=split[1].substring(2);
				if (pred.equals("http://www.w3.org/2000/01/rdf-schema#label"))
					label=split[2].substring(split[2].indexOf("\"")+1, split[2].lastIndexOf("\""));
				else
					label=split[2].substring(2);
				if (pred.equals("http://www.w3.org/2000/01/rdf-schema#label")&&isClassLabel){
					try{
						stmt.executeUpdate("INSERT INTO categories (category,label) VALUES (\""+name+"\",\""+label+"\")");
					}catch(Exception e)
					{}
				}
				else{
					if (pred.equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")){
						try{
							stmt.executeUpdate("INSERT INTO classhierarchy (father,child) VALUES ('"+label+"','"+name+"')");
						}catch(Exception e)
						{}
					}
				}
				if (i%100000==0) System.out.println(i);
				i++;
			}
			
			in.close();
			
			in = new BufferedReader(new FileReader(categoriesNewOntology2));
			
			i=0;
			while ((line=in.readLine())!=null)
			{
				split=line.split(">");
				name=split[0].substring(1);
				label=split[2].substring(2);
				try{
					stmt.executeUpdate("INSERT INTO articlecategories (name,category) VALUES ('"+name+"','"+label+"')");
				}catch(Exception e)
				{}
				if (i%100000==0) System.out.println(i);
				i++;
			}
			
			in.close();
		} catch (FileNotFoundException e)
		{
			System.out.println("File not found");
		} catch (IOException e)
		{
			System.out.println("IOException");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void copyNumbers()
	{
		try{
			Statement stmt;
						
			stmt = con.createStatement();
			
			stmt.executeUpdate("UPDATE articlecategories SET number=(SELECT number FROM rank WHERE articlecategories.name=rank.name)");
				
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException,SQLException,BackingStoreException{
		Class.forName("com.mysql.jdbc.Driver");
		
		String url =
		            "jdbc:mysql://"+dbServer+":3306/"+dbName;
	
		con = DriverManager.getConnection(
		                                 url, dbUser, dbPass);
		
		CalculatePageRank cal=new CalculatePageRank();
		cal.calculateLinks();
		cal.addLabels();
		//cal.calculateCategories();
		cal.calculateCategoriesNewOntology();
		cal.copyNumbers();
		
		con.close();
	}
}