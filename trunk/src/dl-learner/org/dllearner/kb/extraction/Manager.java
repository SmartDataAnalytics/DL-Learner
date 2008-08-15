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
package org.dllearner.kb.extraction;

import java.net.URI;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * An object of this class encapsulates everything.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class Manager {

	private Configuration configuration;
	private ExtractionAlgorithm extractionAlgorithm;
	
	private static Logger logger = Logger
		.getLogger(Manager.class);
	
	
	public void useConfiguration(Configuration configuration) {

		this.configuration = configuration;
		//System.out.println(this.configuration);
		
		this.extractionAlgorithm = new ExtractionAlgorithm(configuration);

	}

	public String extract(URI uri) {
		// this.TypedSparqlQuery.query(uri);
		// System.out.println(ExtractionAlgorithm.getFirstNode(uri));
		System.out.println("Start extracting");

		Node n = extractionAlgorithm.expandNode(uri, configuration.getTupelAquisitor());
		SortedSet<String> s = n.toNTriple();
		StringBuffer nt = new StringBuffer(33000);
		for (String str : s) {
			nt.append(str + "\n");
		}
		System.out.println("sizeofStringBuffer"+nt.length());
		return nt.toString();
	}

	public String extract(Set<String> instances) {
		// this.TypedSparqlQuery.query(uri);
		// System.out.println(ExtractionAlgorithm.getFirstNode(uri));
		logger.info("Start extracting");
		SortedSet<String> tripleCollector = new TreeSet<String>();
		int progress=0;
		for (String one : instances) {
			progress++;
			//if(progress % 10 == 0) {
				logger.info("Progress: "+progress+" of "+instances.size()+" finished: "+one);
			//}
			try {
				Node n = extractionAlgorithm.expandNode(new URI(one),
						configuration.getTupelAquisitor());
				tripleCollector.addAll(n.toNTriple());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.info("Finished extracting, start conversion");
		StringBuffer nt = new StringBuffer(100000);
		Object[] arr = tripleCollector.toArray();
		for (int i = 0; i < arr.length; i++) {
			nt.append((String) arr[i] + "\n");
			if (i % 1000 == 0)
				logger.info(i + " of  " + arr.length + " triples done");
		}
		logger.info(arr.length + " of  " + arr.length + " triples done");
		/*
		 * String tmp=""; while ( ret.size() > 0) { tmp=ret.first(); nt+=tmp;
		 * ret.remove(tmp); System.out.println(ret.size()); } /*for (String str :
		 * ret) { nt += str + "\n"; }
		 */
		logger.info("Ontology String size = " + nt.length());
		return nt.toString();
	}


	public Configuration getConfiguration() {
		return configuration;
	}

}