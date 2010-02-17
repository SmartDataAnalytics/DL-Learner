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

import org.dllearner.kb.sparql.configuration.SparqlQueryType;

/**
 * Can assemble sparql queries. can make queries for subject, predicate, object
 * according to the filter settings object SparqlQueryType, which gives the
 * predicate and object lists
 * 
 * @author Sebastian Hellmann
 * 
 */
public class SparqlQueryMaker {
	String lineend = "\n";
	boolean print_flag = false;
	private SparqlQueryType sparqlQueryType;

	public SparqlQueryMaker(SparqlQueryType SparqlQueryType) {
		this.sparqlQueryType = SparqlQueryType;
	}

	public String makeSubjectQueryUsingFilters(String subject) {

		String Filter = internalFilterAssemblySubject();
		String ret = "SELECT * WHERE { " + lineend + "<" + subject
				+ "> ?predicate ?object. " + lineend + "FILTER( " + lineend
				+ "(" + Filter + ").}";
		// System.out.println(ret);
		// System.out.println(sparqlQueryType.getPredicatefilterlist().length);
		return ret;
	}

	/**
	 * 
	 * @param role
	 * @return
	 */
	public String makeRoleQueryUsingFilters(String role) {

		String Filter = internalFilterAssemblyRole();
		String ret = "SELECT * WHERE { " + lineend + " ?subject <" + role
				+ "> ?object. " + lineend + "FILTER( " + lineend + "(" + Filter
				+ ").}";
		// System.out.println(ret);

		return ret;
	}

	public String makeRoleQueryUsingFilters(String role, boolean domain) {

		String Filter = internalFilterAssemblyRole();
		String ret = "";
		if (domain) {
			ret = "SELECT * WHERE { " + lineend + "?subject <" + role
					+ "> ?object; a []. " + lineend + "FILTER( " + lineend
					+ "(" + Filter + ").}";
			// "ORDER BY ?subject";
			// System.out.println(ret);
		} else {
			ret = "SELECT * WHERE { " + lineend + "?object a [] . "
					+ "?subject <" + role + "> ?object . " + lineend
					+ "FILTER( " + lineend + "(" + Filter + ").}";
			// "ORDER BY ?object";

		}
		// System.out.println(ret);

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

	private String filterSubject(String ns) {
		return "&&( !regex(str(?subject), '" + ns + "') )";
	}

	private static String filterPredicate(String ns) {
		return "&&( !regex(str(?predicate), '" + ns + "') )";
	}

	private static String filterObject(String ns) {
		return "&&( !regex(str(?object), '" + ns + "') )";
	}

	/*private void p(String str) {
		if (print_flag) {
			System.out.println(str);
		}
	}*/

	/**
	 * creates a query with the specified filters for all triples with subject
	 * 
	 * @param subject
	 *            the searched subject
	 * @param sf
	 *            special object encapsulating all options
	 * @return sparql query
	 */
	/*
	 * public static String makeQueryFilter(String subject, oldSparqlFilter sf) {
	 * 
	 * String Filter = ""; if (!sf.useLiterals) Filter += "!isLiteral(?object)";
	 * for (String p : sf.getPredFilter()) { Filter += "\n" +
	 * filterPredicate(p); } for (String o : sf.getObjFilter()) { Filter += "\n" +
	 * filterObject(o); }
	 * 
	 * String ret = "SELECT * WHERE { \n" + "<" + subject + "> ?predicate
	 * ?object.\n"; if (!(Filter.length() == 0)) ret += "FILTER( \n" + "(" +
	 * Filter + "))."; ret += "}"; // System.out.println(ret); return ret; }
	 */

	/*
	 * moved to SparqlQuery TODO remove here creates a query for subjects with
	 * the specified label @param label a phrase that is part of the label of a
	 * subject @param limit this limits the amount of results @return
	 * 
	 * @Deprecated public static String makeLabelQuery(String label,int limit){
	 *             //TODO maybe use http://xmlns:com/foaf/0.1/page return
	 *             "SELECT DISTINCT ?subject\n"+ "WHERE { ?subject
	 *             <http://www.w3.org/2000/01/rdf-schema#label> ?object.?object
	 *             bif:contains '\""+label+"\"'@en}\n"+ "LIMIT "+limit; }
	 * 
	 * 
	 * creates a query for all subjects that are of the type concept @param
	 * concept the type that subjects are searched for @return
	 * 
	 * 
	 * moved to SparqlQuery TODO remove here
	 * @Deprecated public static String makeConceptQuery(String concept){ return
	 *             "SELECT DISTINCT ?subject\n"+ "WHERE { ?subject a
	 *             <"+concept+">}\n"; } moved to SparqlQuery TODO remove here
	 * @Deprecated public static String makeArticleQuery(String subject){ return
	 *             "SELECT ?predicate,?object\n"+ "WHERE { <"+subject+">
	 *             ?predicate ?object}\n"; }
	 */
}
