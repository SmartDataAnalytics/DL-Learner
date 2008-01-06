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
 * SPARQL query cache to avoid possibly expensive multiple queries.
 * 
 * @author Sebastian Hellmann
 *
 */
public class Cache implements Serializable {
	// Object can be the cache itself
	// or a cache object(one entry)
	// it now uses a hashmap and can contain different queries at once
	private HashMap<String, String> hm;

	final static long serialVersionUID = 104;
	transient String basedir = "";
	transient String fileending = ".cache";
	long timestamp;
	long daysoffreshness = 15;
	long multiplier = 24 * 60 * 60 * 1000;// h m s ms
	// private HashMap<String, String> inmem_cache;

	// constructor for the cache itself
	public Cache(String path) {
		this.basedir = path + File.separator;
		if (!new File(path).exists()) {
			System.out.println(new File(path).mkdir());
			;
		}
	}

	// constructor for single cache object(one entry)
	public Cache(String sparql, String content) {
		// this.content = c;
		// this.sparqlquery = sparql;
		this.timestamp = System.currentTimeMillis();
		this.hm = new HashMap<String, String>();
		hm.put(sparql, content);
	}

	public String get(String key, String sparql) {
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
				xml = c.hm.get(sparql);
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
	};

	public void put(String key, String sparql, String content) {
		// System.out.println("put into "+key);
		Cache c = readFromFile(makeFilename(key));
		if (c == null) {
			c = new Cache(sparql, content);
			putIntoFile(makeFilename(key), c);
		} else {
			c.hm.put(sparql, content);
			putIntoFile(makeFilename(key), c);
		}

	}

	String makeFilename(String key) {
		String ret = "";
		try {
			ret = basedir + URLEncoder.encode(key, "UTF-8") + fileending;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	boolean checkFreshness() {
		if ((System.currentTimeMillis() - this.timestamp) <= (daysoffreshness * multiplier))
			// fresh
			return true;
		else
			return false;
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

	public void putIntoFile(String Filename, Cache content) {
		try {
			// FileWriter fw=new FileWriter(new File(Filename),true);
			FileOutputStream fos = new FileOutputStream(Filename, false);
			ObjectOutputStream o = new ObjectOutputStream(fos);
			o.writeObject(content);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			System.out.println("Not in cache creating: " + Filename);
		}
	}

	public Cache readFromFile(String Filename) {
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
	
}
