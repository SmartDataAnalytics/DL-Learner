/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 */

package org.dllearner.kb.sparql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.JamonMonitorLogger;

import com.jamonapi.Monitor;

/**
 * SPARQL query cache to avoid possibly expensive multiple queries. The queries
 * and their results are written to files. A cache has an associated cache
 * directory where all files are written.
 * 
 * Each SPARQL query and its result is written to one file. The name of this
 * file is a hash of the query. The result of the query is written as JSON
 * serialisation of the SPARQL XML result, see
 * http://www.w3.org/TR/rdf-sparql-json-res/.
 * 
 * Apart from the query and its result, a timestamp of the query is stored.
 * After a configurable amount of time, query results are considered outdated.
 * If a cached result of a SPARQL query exists, but is too old, the cache
 * behaves as if the cached result would not exist.
 * 
 * TODO: We are doing md5 hashing at the moment, so in rare cases different
 * SPARQL queries can be mapped to the same file. Support for such scenarios
 * needs to be included.
 * 
 * @author Sebastian Hellmann
 * @author Sebastian Knappe
 * @author Jens Lehmann
 */
public class Cache implements Serializable {

	private static Logger logger = Logger.getLogger(Cache.class);
	
	// true = H2 embedded database is used; false = stored in files
	private boolean useDatabase = false;
	private ExtractionDBCache h2;

	private static final long serialVersionUID = 843308736471742205L;

	// maps hash of a SPARQL queries to JSON representation
	// of its results; this
	// private HashMap<String, String> hm;

	private transient String cacheDir = "";
	private transient String fileEnding = ".cache";
	// private long timestamp;

	// specifies after how many seconds a cached result becomes invalid
	private long freshnessSeconds = 15 * 24 * 60 * 60;

	/**
	 *  same ad Cache(String) default is "cache"
	 */
	/*public Cache() {
		this("cache");
	} */
	
	/**
	 * A Persistant cache is stored in the folder cachePersistant.
	 * It has longer freshness 365 days and is mainly usefull for developing
	 * @return a Cache onject
	 */
	public static Cache getPersistentCache(){
		Cache c = new Cache(getPersistantCacheDir()); 
		c.setFreshnessInDays(365);
		return c;
	}
	
	/**
	 * @return the default cache object
	 */
	public static Cache getDefaultCache(){
		Cache c = new Cache( getDefaultCacheDir()); 
		return c;
	}
	
	/**
	 * the default cachedir normally is "cache".
	 * @return Default Cache Dir
	 */
	public static String getDefaultCacheDir(){
		return "cache";
	}
	
	/**
	 * a more persistant cache used for example generation."cachePersistant"
	 * @return persistant Cache Dir
	 */
	public static String getPersistantCacheDir(){
		return "cachePersistant";
	}
	
	/**
	 * Constructor for the cache itself.
	 * 
	 * @param cacheDir
	 *            Where the base path to the cache is .
	 */
	public Cache(String cacheDir) {
		this(cacheDir, false);
	}

	public Cache(String cacheDir, boolean useDatabase) {
		this.cacheDir = cacheDir + File.separator;
		this.useDatabase = useDatabase;
		if (!new File(cacheDir).exists()) {
			Files.mkdir(cacheDir);
			logger.info("Created directory: " + cacheDir + ".");
		}
		
		if(this.useDatabase) {
			h2 = new ExtractionDBCache(cacheDir);
		}		
	}
	
	// compute md5-hash
	private String getHash(String string) {
		Monitor hashTime = JamonMonitorLogger.getTimeMonitor(Cache.class, "HashTime").start();
		// calculate md5 hash of the string (code is somewhat
		// difficult to read, but there doesn't seem to be a
		// single function call in Java for md5 hashing)
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		md5.reset();
		md5.update(string.getBytes());
		byte[] result = md5.digest();

		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			hexString.append(Integer.toHexString(0xFF & result[i]));
		}
		String str = hexString.toString();
		hashTime.stop();
		return str;
	}

	// return filename where the query result should be saved
	private String getFilename(String sparqlQuery) {
		return cacheDir + getHash(sparqlQuery) + fileEnding;
	}

	/**
	 * Gets a result for a query if it is in the cache.
	 * 
	 * @param sparqlQuery
	 *            SPARQL query to check.
	 * @return Query result as JSON or null if no result has been found or it is
	 *         outdated.
	 */
	@SuppressWarnings({"unchecked"})
	private String getCacheEntry(String sparqlQuery) {
		
		String filename = getFilename(sparqlQuery);
		File file = new File(filename);
		
		// return null (indicating no result) if file does not exist
		if(!file.exists()) {
			return null;
		}
			
		
		LinkedList<Object> entry = null;
		try {
			FileInputStream fos = new FileInputStream(filename);
			ObjectInputStream o = new ObjectInputStream(fos);
			entry = (LinkedList<Object>) o.readObject();
			o.close();
		} catch (IOException e) {
			e.printStackTrace();
			if(Files.debug){System.exit(0);}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			if(Files.debug){System.exit(0);}
		}
		
		// TODO: we need to check whether the query is correct
		// (may not always be the case due to md5 hashing)
		
		// determine whether query is outdated
		long timestamp = (Long) entry.get(0);
		boolean fresh = checkFreshness(timestamp);
		
		if(!fresh) {
			// delete file
			file.delete();
			// return null indicating no result
			return null;
		}
		
		return (String) entry.get(2);
	}
	
	

	/**
	 * Adds an entry to the cache.
	 * 
	 * @param sparqlQuery
	 *            The SPARQL query.
	 * @param result
	 *            Result of the SPARQL query.
	 */
	private void addToCache(String sparqlQuery, String result) {
		String filename = getFilename(sparqlQuery);
		long timestamp = System.currentTimeMillis();

		// create the object which will be serialised
		LinkedList<Object> list = new LinkedList<Object>();
		list.add(timestamp);
		list.add(sparqlQuery);
		list.add(result);

		// create the file we want to use
		//File file = new File(filename);
		FileOutputStream fos = null;
		ObjectOutputStream o = null;
		try {
			//file.createNewFile();
			fos = new FileOutputStream(filename, false);
			o = new ObjectOutputStream(fos);
			o.writeObject(list);
			fos.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try{
				fos.close();
				o.close();
			}catch (Exception e2) {
				 e2.printStackTrace();
			}
		}
	}

	// check whether the given timestamp is fresh
	private boolean checkFreshness(long timestamp) {
		return ((System.currentTimeMillis() - timestamp) <= (freshnessSeconds * 1000));
	}

	/**
	 * Takes a SPARQL query (which has not been evaluated yet) as argument and
	 * returns a JSON result set. The result set is taken from this cache if the
	 * query is stored here. Otherwise the query is send and its result added to
	 * the cache and returned. Convenience method.
	 * 
	 * @param query
	 *            The SPARQL query.
	 * @return Jena result set in JSON format
	 */
	public String executeSparqlQuery(SparqlQuery query) {
		if(useDatabase) {
			return h2.executeSelectQuery(query.getSparqlEndpoint(), query.getSparqlQueryString());
		}
		
		Monitor totaltime =JamonMonitorLogger.getTimeMonitor(Cache.class, "TotalTimeExecuteSparqlQuery").start();
		JamonMonitorLogger.increaseCount(Cache.class, "TotalQueries");
	
		Monitor readTime = JamonMonitorLogger.getTimeMonitor(Cache.class, "ReadTime").start();
		String result = getCacheEntry(query.getSparqlQueryString());
		readTime.stop();
		
		if (result != null) {
//			query.setJson(result);
//			
//		    query.setRunning(false);
//			SparqlQuery.writeToSparqlLog("***********\nJSON retrieved from cache");
//			SparqlQuery.writeToSparqlLog("wget -S -O - '\n"+query.getSparqlEndpoint().getHTTPRequest());
//			SparqlQuery.writeToSparqlLog(query.getSparqlQueryString());
			
			//SparqlQuery.writeToSparqlLog("JSON: "+result);
			JamonMonitorLogger.increaseCount(Cache.class, "SuccessfulHits");
			
		} else {
			
			//ResultSet rs= query.send();
		    	query.send();
			String json = query.getJson();
			if (json!=null){
				addToCache(query.getSparqlQueryString(), json);
//				SparqlQuery.writeToSparqlLog("result added to cache: "+json);
				logger.debug("result added to SPARQL cache: "+json);
				result=json;
				//query.setJson(result);
			} else {
				json="";
				result="";
				logger.warn(Cache.class.getSimpleName()+"empty result: "+query.getSparqlQueryString());
				
			}
			
			//return json;
		}
		totaltime.stop();
		return result;
	}
		
	public boolean executeSparqlAskQuery(SparqlQuery query) {
		String str = getCacheEntry(query.getSparqlQueryString());
		JamonMonitorLogger.increaseCount(Cache.class, "TotalQueries");
		if(str != null) {
			JamonMonitorLogger.increaseCount(Cache.class, "SuccessfulHits");
			return Boolean.parseBoolean(str);
		} else {
			Boolean result = query.sendAsk();
			addToCache(query.getSparqlQueryString(), result.toString());
			return result;
		}
	}
	
	/**
	 * deletes all Files in the cacheDir, does not delete the cacheDir itself, 
	 * and can thus still be used without creating a new Cache Object
	 */
	public void clearCache() {
		
			File f = new File(cacheDir);
		    String[] files = f.list();
		    for (int i = 0; i < files.length; i++) {
		    	Files.deleteFile(new File(cacheDir+"/"+files[i]));
		    }     
	}
	
	/**
	 * Changes how long cached results will stay fresh (default 15 days).
	 * @param days number of days
	 */
	public void setFreshnessInDays(int days){
		freshnessSeconds = days * 24 * 60 * 60;
	}

}
