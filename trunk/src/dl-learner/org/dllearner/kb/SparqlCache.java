/**
 * Copyright (C) 2007, Sebastian Hellmann
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
package org.dllearner.kb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
/**
 * 
 * This is a primitive cache.
 * The objects of this class can be either the cache itself or just on entry in the cache
 * 
 * the cache remembers: a timestamp, the original sparql-query, the result
 * key is the subject http://dbpedia.org/resource/Angela_Merkel which is first urlencoded 
 * and so serves as the hash for the filename.
 * Cache validates if timestamp too old and Sparql-Query the same 
 * before returning the SPARQL xml-result
 * 
 * @author Sebastian Hellmann
 * @author Sebastian Knappe
 */
public class SparqlCache implements Serializable{
	
	final static long serialVersionUID=104;
	transient String basedir="";
	transient String fileending=".cache";
	long timestamp;
	String content="";
	long daysoffreshness=15;
	long multiplier=24*60*60*1000;//h m s ms
	String sparqlquery="";
	
	
	/**
	 * Constructor for the cache itself.
	 * Called once at the beginning
	 * 
	 * @param path Where the base path to the cache is 
	 */
	public SparqlCache(String path){
		this.basedir=path+File.separator;
		if(!new File(path).exists())
			{System.out.println(new File(path).mkdir());;}
	
	}
	
//	
	/**
	 * Constructor for single cache object(one entry)
	 * 
	 * @param content the sparql xml result
	 * @param sparql the sparql query
	 */
	public SparqlCache(String content, String sparql){
		this.content=content;
		this.sparqlquery=sparql;
		this.timestamp=System.currentTimeMillis();
	}
	
	

	/**
	 * use only on the cache object describing the cache itself
	 * 
	 * @param key the individual
	 * @param sparql the sparql query
	 * @return the cached sparql result or null
	 */
	public String get(String key, String sparql){
		String ret=null;
		try{
		SparqlCache c =readFromFile(makeFilename(key))	;
		if(c==null)return null;
		if(!c.checkFreshness())return null;
		if(!c.validate(sparql))return null;
		
		ret=c.content;
		}catch (Exception e) {e.printStackTrace();}
		return ret;
	}
	
	/**
	 * 
	 * constructor for single cache object(one entry)
	 * 
	 * @param key  the individual
	 * @param content the sparql result
	 * @param sparql the sparql query
	 */
	public void put(String key, String content, String sparql){
		SparqlCache c=new SparqlCache(content,sparql);
		putIntoFile(makeFilename(key), c);
	}
	
	
	/**
	 * to normalize the filenames
	 * 
	 * @param key
	 * @return
	 */
	String makeFilename(String key){
		String ret="";
		try{
		ret=basedir+URLEncoder.encode(key, "UTF-8")+fileending;
		}catch (Exception e) {e.printStackTrace();}
		return ret;
	}
	
	/**
	 *  how old is the result
	 * @return
	 */
	boolean checkFreshness(){
		if((System.currentTimeMillis()-this.timestamp)<=(daysoffreshness*multiplier))
			//fresh
			return true;
		else return false;
	}
	
	
	/**
	 * some sparql query
	 * @param sparql
	 * @return
	 */
	boolean validate(String sparql){
		if(this.sparqlquery.equals(sparql))
			//valid
			return true;
		else return false;
	}
	
	/**
	 * makes a new file if none exists
	 * @param Filename
	 */
	public void checkFile(String Filename){
		if(!new File(Filename).exists()){
			try{
				new File(Filename).createNewFile();
			}catch (Exception e) {e.printStackTrace();}
			
		}
		
	}
	
	/**
	 * internal saving function 
	 * puts a cache object into a file
	 * 
	 * @param Filename
	 * @param content
	 */
	public void putIntoFile(String Filename,SparqlCache content){
		try{
			FileOutputStream  fos = new FileOutputStream( Filename , false ); 
			ObjectOutputStream o = new ObjectOutputStream( fos ); 
			o.writeObject( content ); 
			fos.flush();
			fos.close();
		}catch (Exception e) {System.out.println("Not in cache creating: "+Filename);}
	}
	
	/**
	 * internal retrieval function
	 * 
	 * @param Filename
	 * @return one entry object
	 */
	public SparqlCache readFromFile(String Filename){
		SparqlCache content=null;
		try{
			FileInputStream  fos = new FileInputStream( Filename ); 
			ObjectInputStream o = new ObjectInputStream( fos ); 
			content=(SparqlCache)o.readObject();
		}catch (Exception e) {}
		return content;
		
	}
}
