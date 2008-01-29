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
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.ResultSet;

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
 * @author Sebastian Hellmann
 * @author Sebastian Knappe
 * @author Jens Lehmann
 */
public class Cache implements Serializable {

	private static Logger logger = Logger.getLogger(Cache.class);

	private static final long serialVersionUID = 843308736471742205L;

	// maps hash of a SPARQL queries to JSON representation
	// of its results; this 
	private HashMap<String, String> hm;

	private transient String cacheDir = "";
	private transient String fileEnding = ".cache";
	private long timestamp;

	// specifies after how many seconds a cached result becomes invalid
	private long freshnessSeconds = 15 * 24 * 60 * 60;

	/**
	 * Constructor for the cache itself.
	 * 
	 * @param cacheDir
	 *            Where the base path to the cache is .
	 */
	public Cache(String cacheDir) {
		this.cacheDir = cacheDir + File.separator;
		if (!new File(cacheDir).exists()) {
			logger
					.info("Created directory: " + cacheDir + " : " + new File(cacheDir).mkdir()
							+ ".");
		}
	}

	/**
	 * constructor for single cache object(one entry)
	 * 
	 * @param sparqlQuery
	 *            query
	 * @param content
	 *            that is the sparql query result as xml
	 */
	private Cache(String sparqlQuery, String content) {
		// this.content = c;
		// this.sparqlquery = sparql;
		this.timestamp = System.currentTimeMillis();
		this.hm = new HashMap<String, String>();
		hm.put(sparqlQuery, content);
	}

	private String getHash(String string) {
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
		return hexString.toString();
	}

	private String getFilename(String sparqlQuery) {
		return getHash(sparqlQuery) + fileEnding;
	}
	
	/**
	 * Gets the query result for a SPARQL query.
	 * 
	 * @param sparqlQuery
	 *            SPARQL query to check.
	 * @return Query result or null if no result has been found or it is
	 *         outdated.
	 */
	public String get(String sparqlQuery) {
		Cache c = readFromFile(getFilename(sparqlQuery));
		if (c == null)
			return null;
		// System.out.println(" file found");
		if (!c.checkFreshness())
			return null;
		// System.out.println("fresh");
		String xml = "";
		try {
			xml = c.hm.get(sparqlQuery);
		} catch (Exception e) {
			return null;
		}
		return xml;
	}

	/**
	 * @param key
	 *            is the resource, the identifier
	 * @param sparqlquery
	 *            is the query used as another identifier
	 * @param content
	 *            is the result of the query
	 */
	public void put(String sparqlQuery, String content) {
		String hash = getHash(sparqlQuery);
		Cache c = readFromFile(hash);
		if (c == null) {
			c = new Cache(sparqlQuery, content);
			putIntoFile(hash, c);
		} else {
			c.hm.put(sparqlQuery, content);
			putIntoFile(hash, c);
		}

	}

	public void checkFile(String Filename) {
		if (!new File(Filename).exists()) {
			try {
				new File(Filename).createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * puts a cache entry in a file
	 * 
	 * @param filename
	 * @param c
	 */
	protected void putIntoFile(String filename, Cache c) {
		try {
			// FileWriter fw=new FileWriter(new File(Filename),true);
			FileOutputStream fos = new FileOutputStream(filename, false);
			ObjectOutputStream o = new ObjectOutputStream(fos);
			o.writeObject(c);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			System.out.println("Not in cache creating: " + filename);
		}
	}

	/**
	 * reads a cache entry from a file
	 * 
	 * @param Filename
	 * @return cache entry
	 */
	protected Cache readFromFile(String Filename) {
		Cache content = null;
		try {
			FileInputStream fos = new FileInputStream(Filename);
			ObjectInputStream o = new ObjectInputStream(fos);
			content = (Cache) o.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return content;
	}

	private boolean checkFreshness() {
		if ((System.currentTimeMillis() - this.timestamp) <= (freshnessSeconds * 1000))
			// fresh
			return true;
		else
			return false;
	}

	/**
	 * Takes a SPARQL query (which has not been evaluated yet) as argument and
	 * returns a result set. The result set is taken from this cache if the
	 * query is stored here. Otherwise the query is send and its result added to
	 * the cache and returned. Convenience method.
	 * 
	 * @param query
	 *            The SPARQL query.
	 * @return Jena result set.
	 */
	public ResultSet executeSparqlQuery(SparqlQuery query) {
		if (hm.containsKey(query.getQueryString())) {
			String result = hm.get(query.getQueryString());
			return SparqlQuery.JSONtoResultSet(result);
		} else {
			query.send();
			return query.getResultSet();
		}
	}

}
