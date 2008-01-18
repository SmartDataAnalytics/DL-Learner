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
package org.dllearner.kb.sparql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * SPARQL query cache to avoid possibly expensive multiple queries. An object of
 * this class can be the cache itself or a cache object(one entry), We could
 * split that in two classes, but one entry o object only has contains data and
 * one additional function and would just be a data class
 * 
 * it writes the files according to one resource in the basedir and saves the
 * cache object in it.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class Cache implements Serializable {

	/**
	 * This maps sparql query to sparql result
	 */
	protected HashMap<String, String> hm;

	final static long serialVersionUID = 104;
	transient String basedir = "";
	transient String fileending = ".cache";
	transient boolean debug_print_flag = false;
	long timestamp;
	/**
	 * After how many days cache entries get invalid
	 */
	protected long daysoffreshness = 15;
	protected long multiplier = 24 * 60 * 60 * 1000;// h m s ms

	// private HashMap<String, String> inmem_cache;

	// 
	/**
	 * constructor for the cache itself
	 * 
	 * @param path
	 *            where the cache files will be
	 */
	public Cache(String path) {
		this.basedir = path + File.separator;
		if (!new File(path).exists()) {
			System.out.println("created directory: " + path + " : "
					+ new File(path).mkdir());

		}
	}

	// constructor for single cache object(one entry)
	/**
	 * @param sparql
	 *            query
	 * @param content
	 *            that is the sparql query result as xml
	 */
	protected Cache(String sparql, String content) {
		// this.content = c;
		// this.sparqlquery = sparql;
		this.timestamp = System.currentTimeMillis();
		this.hm = new HashMap<String, String>();
		hm.put(sparql, content);
	}

	/**
	 * gets a chached sparqlquery for a resource(key) and returns the
	 * sparqlXMLResult or null, if none is found.
	 * 
	 * @param key
	 *            is the resource, the identifier
	 * @param sparqlquery
	 *            is a special sparql query about that resource
	 * @return sparqlXMLResult
	 */
	public String get(String key, String sparqlquery) {
		// System.out.println("get From "+key);
		String ret = null;
		try {
			Cache c = readFromFile(makeFilename(key));
			if (c == null)
				return null;
			// System.out.println(" file found");
			if (!c.checkFreshness())
				return null;
			// System.out.println("fresh");
			String xml = "";
			try {
				xml = c.hm.get(sparqlquery);
			} catch (Exception e) {
				return null;
			}
			return xml;
			// System.out.println("valid");
			// ret = c.content;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * @param key
	 *            is the resource, the identifier
	 * @param sparqlquery
	 *            is the query used as another identifier
	 * @param content
	 *            is the result of the query
	 */
	public void put(String key, String sparqlquery, String content) {
		// System.out.println("put into "+key);
		Cache c = readFromFile(makeFilename(key));
		if (c == null) {
			c = new Cache(sparqlquery, content);
			putIntoFile(makeFilename(key), c);
		} else {
			c.hm.put(sparqlquery, content);
			putIntoFile(makeFilename(key), c);
		}

	}

	/**
	 * this function takes a resource string and then URIencodes it and makes a
	 * filename out of it for the use in the hashmap
	 * 
	 * @param key
	 * @return the complete key for filename in the hashmap
	 */
	protected String makeFilename(String key) {
		String ret = "";
		try {
			ret = basedir + URLEncoder.encode(key, "UTF-8") + fileending;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
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
	 * @param Filename
	 * @param c
	 */
	protected void putIntoFile(String Filename, Cache c) {
		try {
			// FileWriter fw=new FileWriter(new File(Filename),true);
			FileOutputStream fos = new FileOutputStream(Filename, false);
			ObjectOutputStream o = new ObjectOutputStream(fos);
			o.writeObject(c);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			System.out.println("Not in cache creating: " + Filename);
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
			// FileReader fr=new FileReader(new File(Filename,"r"));
			// BufferedReader br=new BufferedReader(fr);
		} catch (Exception e) {
		}
		return content;

	}

	protected boolean checkFreshness() {
		if ((System.currentTimeMillis() - this.timestamp) <= (daysoffreshness * multiplier))
			// fresh
			return true;
		else
			return false;
	}

}
