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

/**
 * Can assemble sparql queries.
 * 
 * @author Sebastian Hellmann
 *
 */
public class SparqlQueryMaker {
	String lineend="\n";
	boolean print_flag=false;
	/* can make queries for subject, predicate, object
	 * according to the filter settings
	 * object not yet implemented
	 * 
	 * */
	
	private SparqlQueryType sparqlQueryType;

	public SparqlQueryMaker(SparqlQueryType SparqlQueryType) {
		this.sparqlQueryType = SparqlQueryType;
	}

	public String makeSubjectQueryUsingFilters(String subject) {
		
		String Filter = internalFilterAssemblySubject();
		String ret = "SELECT * WHERE { " + lineend + "<" + subject + "> ?predicate ?object. "
				+ lineend + "FILTER( " + lineend + "(" + Filter + ").}";
		// System.out.println(ret);
		//System.out.println(sparqlQueryType.getPredicatefilterlist().length);
		return ret;
	}

	public String makeRoleQueryUsingFilters(String role) {
		
		String Filter = internalFilterAssemblyRole();
		String ret = "SELECT * WHERE { " + lineend + " ?subject <" + role + "> ?object. " + lineend
				+ "FILTER( " + lineend + "(" + Filter + ").}";
		// System.out.println(ret);

		return ret;
	}
	public String makeRoleQueryUsingFilters(String role,boolean domain) {
		
		String Filter = internalFilterAssemblyRole();
		String ret="";
		if(domain){
			ret = "SELECT * WHERE { " + lineend + 
				"?subject <" + role + "> ?object; a []. " + lineend
				+ "FILTER( " + lineend + "(" + Filter + ").}" ;
						//"ORDER BY ?subject";
		// System.out.println(ret);
		}else{
			 ret = "SELECT * WHERE { " + lineend + 
			"?object a [] . " +
			"?subject <" + role + "> ?object . " + lineend
			+ "FILTER( " + lineend + "(" + Filter + ").}";
			//"ORDER BY ?object";
			
		}
		//System.out.println(ret);

		return ret;
	}

	private String internalFilterAssemblySubject() {
		
		String Filter = "";
		if (!this.sparqlQueryType.isLiterals())
			Filter += "!isLiteral(?object))";
		for (String p : sparqlQueryType.getPredicatefilterlist()) {
			Filter += lineend + filterPredicate(p);
		}
		for (String o : sparqlQueryType.getObjectfilterlist()) {
			Filter += lineend + filterObject(o);
		}
		return Filter;
	}

	private String internalFilterAssemblyRole() {
		
		String Filter = "";
		if (!this.sparqlQueryType.isLiterals())
			Filter += "!isLiteral(?object))";
		for (String s : sparqlQueryType.getObjectfilterlist()) {
			Filter += lineend + filterSubject(s);
		}
		for (String o : sparqlQueryType.getObjectfilterlist()) {
			Filter += lineend + filterObject(o);
		}
		return Filter;
	}

	public String filterSubject(String ns) {
		return "&&( !regex(str(?subject), '" + ns + "') )";
	}

	public String filterPredicate(String ns) {
		return "&&( !regex(str(?predicate), '" + ns + "') )";
	}

	public String filterObject(String ns) {
		return "&&( !regex(str(?object), '" + ns + "') )";
	}
	
	public void p(String str){
		if(print_flag){
			System.out.println(str);
		}
	}
}
