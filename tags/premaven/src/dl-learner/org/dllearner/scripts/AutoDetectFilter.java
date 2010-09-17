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
 **/
package org.dllearner.scripts;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;

/**
 * @author Sebastian Hellmann
 * This class is under developement and can not be completed,
 * unless the & bug mentioned in SparqlQuery is completed
 */
public class AutoDetectFilter {
	private SPARQLTasks sparqlTasks;
	private String resource;
	public AutoDetectFilter(SPARQLTasks sparqlTasks, String resource) {
		super();
		this.sparqlTasks = sparqlTasks;
		this.resource = resource;
	}
	
	public static void main(String[] args) {
	//	String url = "http://139.18.2.37:8890/sparql";
		String resource = "http://dbpedia.org/resource/Angela_Merkel";
		//resource = "http://dbpedia.org/resource/Lutheran";
		AutoDetectFilter adf = new AutoDetectFilter(new SPARQLTasks(Cache.getDefaultCache(),
				SparqlEndpoint.getEndpointDBpedia()),resource);
		
		adf.detect();
	}
	
	public void detect(){
		String s1 = "SELECT * WHERE { <"+resource+"> ?predicate ?object .FILTER (!isLiteral(?object))}";
		String s2 = "SELECT * WHERE { <"+resource+"> ?predicate ?object .FILTER (!isLiteral(?object))." +
				"?object ?p2 ?o2." +
				" }";
		
		System.out.println(s1);
		//System.out.println(sparqlTasks.query(s2));
		//System.exit(0);
		
		SortedSet<String> predicates = new TreeSet<String>();
		SortedSet<String> objects = new TreeSet<String>();
		SortedSet<String> legalpreds = new TreeSet<String>();
		SortedSet<String> legalObjs = new TreeSet<String>();
		
		
		predicates = sparqlTasks.queryAsSet(s1, "predicate");
		objects = sparqlTasks.queryAsSet(s1, "object");
		legalpreds = sparqlTasks.queryAsSet(s2, "predicate");
		legalObjs = sparqlTasks.queryAsSet(s2, "object");
		
		
		System.out.println(predicates);
		System.out.println(legalpreds);
		predicates.removeAll(legalpreds);
		System.out.println(predicates);
		
		System.out.println(objects);
		System.out.println(legalObjs);
		objects.removeAll(legalObjs);
		System.out.println(objects);
		
	}
	
}
