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

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.utilities.StringTuple;

// an object of this class encapsulates everything
public class Manager {

	private Configuration configuration;
	private TypedSparqlQuery typedSparqlQuery;
	private ExtractionAlgorithm extractionAlgorithm;

	public void useConfiguration(SparqlQueryType SparqlQueryType,
			SpecificSparqlEndpoint SparqlEndpoint, Manipulator manipulator, int recursiondepth,
			boolean getAllBackground) {

		this.configuration = new Configuration(SparqlEndpoint, SparqlQueryType, manipulator,
				recursiondepth, getAllBackground);
		this.typedSparqlQuery = new TypedSparqlQuery(configuration);
		this.extractionAlgorithm = new ExtractionAlgorithm(configuration);
		
	}

	public Set<String> getDomainInstancesForRole(String role) {
		URI u = null;
		try {
			u = new URI(role);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Set<StringTuple> t = this.typedSparqlQuery.getTupelsForRole(u);
		Set<String> ret = new HashSet<String>();
		for (StringTuple one : t) {
			
			ret.add(one.a);
		}
		return ret;
	}

	public Set<String> getRangeInstancesForRole(String role) {
		URI u = null;
		try {
			u = new URI(role);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Set<StringTuple> t = this.typedSparqlQuery.getTupelsForRole(u);
		Set<String> ret = new HashSet<String>();
		for (StringTuple one : t) {
			
			ret.add(one.b);
		}
		return ret;
	}

	public String extract(URI uri) {
		// this.TypedSparqlQuery.query(uri);
		// System.out.println(ExtractionAlgorithm.getFirstNode(uri));
		System.out.println("Start extracting");
	
		Node n = extractionAlgorithm.expandNode(uri, typedSparqlQuery);
		Set<String> s = n.toNTriple();
		String nt = "";
		for (String str : s) {
			nt += str + "\n";
		}
		return nt;
	}

	public String extract(Set<String> instances) {
		// this.TypedSparqlQuery.query(uri);
		// System.out.println(ExtractionAlgorithm.getFirstNode(uri));
		System.out.println("Start extracting");
		SortedSet<String> ret = new TreeSet<String>();

		for (String one : instances) {
			try {
				Node n = extractionAlgorithm.expandNode(new URI(one), typedSparqlQuery);
				ret.addAll(n.toNTriple());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Finished extracting, start conversion");
		StringBuffer nt = new StringBuffer();
		Object[] arr=ret.toArray();
		for (int i = 0; i < arr.length; i++) {
			nt.append((String) arr[i]+"\n");
			if(i%1000==0)System.out.println(i+" of  "+arr.length+" triples done");
		}
		/*
		 String tmp="";
		while ( ret.size() > 0) {
			tmp=ret.first();
			nt+=tmp;
			ret.remove(tmp);
			System.out.println(ret.size());
			
		}
		/*for (String str : ret) {
			nt += str + "\n";
		}*/
		return nt.toString();
	}

	public void addPredicateFilter(String str) {
		this.configuration.getSparqlQueryType().addPredicateFilter(str);

	}

}