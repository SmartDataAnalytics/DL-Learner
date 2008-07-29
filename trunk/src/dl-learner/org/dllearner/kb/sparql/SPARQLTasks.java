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

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.core.ResultBinding;


/**
 * @author Sebastian Hellmann
 * Convenience class for SPARQL queries
 * initialized with a SparqlEndpoint. 
 * A Cache can also be used to further improve query time.
 * Some methods allow basic reasoning
 */
public class SPARQLTasks {

	private static final Logger logger = Logger.getLogger(SPARQLTasks.class);

	private final Cache cache;

	private final SparqlEndpoint sparqlEndpoint;

	public SPARQLTasks(final Cache cache, final SparqlEndpoint sparqlEndpoint) {
		super();
		this.cache = cache;
		this.sparqlEndpoint = sparqlEndpoint;
	}

	public SPARQLTasks(final SparqlEndpoint sparqlEndpoint) {
		super();
		this.cache = null;
		this.sparqlEndpoint = sparqlEndpoint;
	}

	/**
	 * QUALITY: doesn't seem optimal, check! get all superclasses up to a
	 * certain depth 1 means direct superclasses depth
	 * 
	 * @param superClasses
	 * @param maxdepth
	 * @return
	 */
	public SortedSet<String> getSuperClasses(final String oneClass,
			final int maxdepth) {
		int depth = maxdepth;
		SortedSet<String> superClasses = new TreeSet<String>();
		superClasses.add(oneClass);
		SortedSet<String> ret = new TreeSet<String>();
		SortedSet<String> tmpset = new TreeSet<String>();
		String SPARQLquery = "";
		// ret.addAll(superClasses);
		// logger.debug(superClasses);

		for (; depth != 0; depth--) {
			for (String oneSuperClass : superClasses) {

				// tmp = oneSuperClass.replace("\"", "");
				SPARQLquery = "SELECT * WHERE { \n"
						+ "<"
						+ oneSuperClass
						+ "> "
						+ "<http://www.w3.org/2000/01/rdf-schema#subClassOf>  ?superclass. \n"
						+ "}";

				tmpset.addAll(queryAsSet(SPARQLquery, "superclass"));

			}// end inner for
			ret.addAll(tmpset);
			// logger.debug(ret);
			superClasses.clear();
			superClasses.addAll(tmpset);
			tmpset.clear();
		}// end outer for
		// logger.debug(concept);
		// logger.debug(query);
		return ret;
	}

	/**
	 * gets a SortedSet of all subclasses QUALITY: maybe it is better to have a
	 * parameter int depth, to choose a depth of subclass interference
	 * 
	 * @see conceptRewrite(String descriptionKBSyntax, SparqlEndpoint se, Cache
	 *      c, boolean simple )
	 * @param description
	 * @param sparqlEndpoint
	 * @param cache
	 * @param simple
	 * @return
	 */
	public SortedSet<String> getSubClasses(final String description,
			final boolean simple) {
		// ResultSet rs = null;
		// System.out.println(description);
		SortedSet<String> alreadyQueried = new TreeSet<String>();
		try {

			// initialisation get direct Subclasses
			LinkedList<String> remainingClasses = new LinkedList<String>();

			// collect remaining classes
			remainingClasses.addAll(getDirectSubClasses(description.replaceAll(
					"\"", "")));

			// remainingClasses.addAll(alreadyQueried);

			// alreadyQueried = new TreeSet<String>();
			alreadyQueried.add(description.replaceAll("\"", ""));

			if (simple) {
				alreadyQueried.addAll(remainingClasses);

			} else {

				logger
						.warn("Retrieval auf all subclasses via SPARQL is cost intensive and might take a while");
				SortedSet<String> tmpSet = new TreeSet<String>();
				while (!remainingClasses.isEmpty()) {

					String tmp = remainingClasses.removeFirst();
					alreadyQueried.add(tmp);

					tmpSet = getDirectSubClasses(tmp);
					for (String string : tmpSet) {
						if (!(alreadyQueried.contains(string))) {
							remainingClasses.add(string);
						}// if
					}// for
					tmpSet.clear();
				}// while
			}// else

		} catch (Exception e) {
			logger.error(e.getStackTrace());
		}

		return alreadyQueried;
	}

	/**
	 * QUALITY: workaround for a sparql glitch {?a owl:subclassOf ?b} returns an
	 * empty set on some endpoints. returns all direct subclasses of String
	 * concept
	 * 
	 * @param concept
	 * @return SortedSet of direct subclasses as String
	 */
	private SortedSet<String> getDirectSubClasses(final String concept) {

		String SPARQLquery;
		SortedSet<String> subClasses = new TreeSet<String>();
		ResultSet resultSet;

		SPARQLquery = "SELECT * \n " + "WHERE { \n" + " ?subject ?predicate  <"
				+ concept + "> \n" + "}\n";

		resultSet = queryAsResultSet(SPARQLquery);

		@SuppressWarnings("unchecked")
		List<ResultBinding> bindings = ResultSetFormatter.toList(resultSet);
		String subject = "";
		String predicate = "";

		for (ResultBinding resultBinding : bindings) {

			subject = ((resultBinding.get("subject").toString()));
			predicate = ((resultBinding.get("predicate").toString()));
			if (predicate
					.equalsIgnoreCase("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
				subClasses.add(subject);
			}
		}
		return subClasses;
	}

	/**
	 * QUALITY: buggy because role doesn't work sometimes get subject with fixed
	 * role and object
	 * 
	 * @param role
	 * @param object
	 * @param resultLimit
	 * @return
	 */
	public SortedSet<String> retrieveDISTINCTSubjectsForRoleAndObject(
			String role, String object, int resultLimit) {
		String SPARQLquery = "SELECT DISTINCT * WHERE { \n " + "?subject "
				+ "<" + role + "> " + "<" + object + "> \n" + "} "
				+ limit(resultLimit);

		return queryAsSet(SPARQLquery, "subject");
	}

	public SortedSet<String> retrieveObjectsForSubjectAndRole(String subject,
			String role, int resultLimit) {
		String SPARQLquery = "SELECT DISTINCT * WHERE { \n " + "<" + subject
				+ "> " + "<" + role + "> " + " ?object \n" + "} LIMIT "
				+ resultLimit;

		return queryAsSet(SPARQLquery, "object");
	}

	/**
	 * all instances for a SKOS concept
	 * 
	 * @param SKOSconcept
	 * @param resultLimit
	 * @return
	 */
	public SortedSet<String> retrieveInstancesForSKOSConcept(
			String SKOSconcept, int resultLimit) {
		return queryPatternAsSet("?subject", "?predicate", "<" + SKOSconcept
				+ ">", "subject", resultLimit);
		// return
		// retrieveDISTINCTSubjectsForRoleAndObject("http://www.w3.org/2004/02/skos/core#subject",
	}

	/**
	 * get all instances for a concept
	 * 
	 * @param conceptKBSyntax
	 * @param sparqlResultLimit
	 * @return
	 */
	public SortedSet<String> retrieveInstancesForConcept(
			String conceptKBSyntax, int sparqlResultLimit) {

		String SPARQLquery = "";
		try {
			SPARQLquery = SparqlQueryDescriptionConvertVisitor.getSparqlQuery(
					conceptKBSyntax, sparqlResultLimit);
		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
		return queryAsSet(SPARQLquery, "subject");
	}

	/**
	 * get all instances for a concept including RDFS Reasoning
	 * 
	 * @param conceptKBSyntax
	 * @param sparqlResultLimit
	 * @return
	 */
	public SortedSet<String> retrieveInstancesForConceptIncludingSubclasses(
			String conceptKBSyntax, int sparqlResultLimit) {

		String SPARQLquery = "";
		try {
			SPARQLquery = SparqlQueryDescriptionConvertVisitor
					.getSparqlQueryIncludingSubclasses(conceptKBSyntax,
							sparqlResultLimit, this, true);

		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
		return queryAsSet(SPARQLquery, "subject");
	}

	/**
	 * get all direct Classes of an instance
	 * 
	 * @param instance
	 * @param resultLimit
	 * @return
	 */
	public SortedSet<String> getClassesForInstance(String instance,
			int resultLimit) {

		String SPARQLquery = "SELECT ?subject WHERE { \n " + "<" + instance
				+ ">" + " a " + "?subject " + "\n" + "} " + limit(resultLimit);

		return queryAsSet(SPARQLquery, "subject");
	}

	public SortedSet<String> getDomain(String role, int resultLimit) {

		String SPARQLquery = "SELECT DISTINCT ?domain " + "WHERE { \n"
				+ "?domain <" + role + "> " + " ?o. \n" + "?domain a []\n."
				+ "FILTER (!isLiteral(?domain))." + "}\n" + limit(resultLimit);

		return queryAsSet(SPARQLquery, "domain");

	}

	public SortedSet<String> getRange(String role, int resultLimit) {

		String SPARQLquery = "SELECT DISTINCT ?range " + "WHERE { \n" + "?s <"
				+ role + "> " + " ?range. \n" + "?range a [].\n"
				+ "FILTER (!isLiteral(?range))." + "}\n" + limit(resultLimit);

		return queryAsSet(SPARQLquery, "range");

	}

	/**
	 * query a pattern with a standard SPARQL query usage (?subject, ?predicate,
	 * <http:something> , subject )
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @param var
	 * @return
	 */
	public SortedSet<String> queryPatternAsSet(String subject,
			String predicate, String object, String var, int resultLimit) {
		String SPARQLquery = "SELECT ?subject WHERE { \n " + " " + subject
				+ " " + predicate + " " + object + " \n" + "} "
				+ limit(resultLimit);
		return queryAsSet(SPARQLquery, var);
	}

	/**
	 * little higher level, executes query ,returns all resources for a variable
	 * 
	 * @param SPARQLquery
	 * @param var
	 * @return
	 */
	public SortedSet<String> queryAsSet(String SPARQLquery, String var) {
		ResultSet rs = null;
		try {
			String JSON = query(SPARQLquery);
			rs = SparqlQuery.JSONtoResultSet(JSON);

		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
		return getStringListForVariable(rs, var);
	}

	/**
	 * low level, executes query returns ResultSet
	 * 
	 * @param SPARQLquery
	 * @return jena ResultSet
	 */
	public ResultSet queryAsResultSet(String SPARQLquery) {
		return SparqlQuery.JSONtoResultSet(query(SPARQLquery));

	}

	/**
	 * low level, executes query returns JSON
	 * 
	 * @param SPARQLquery
	 * @return
	 */
	public String query(String SPARQLquery) {
		String JSON;
		if (cache == null) {
			SparqlQuery sq = new SparqlQuery(SPARQLquery, sparqlEndpoint);
			sq.send();
			JSON = sq.getJson();
		} else {
			JSON = cache.executeSparqlQuery(new SparqlQuery(SPARQLquery,
					sparqlEndpoint));
		}
		return JSON;
	}

	private String limit(int resultLimit) {
		return (resultLimit > 0) ? " LIMIT " + resultLimit : "";
	}

	public static SortedSet<String> getStringListForVariable(ResultSet rs,
			String var) {
		SortedSet<String> result = new TreeSet<String>();

		// String s=ResultSetFormatter.asXMLString(this.rs);
		@SuppressWarnings("unchecked")
		List<ResultBinding> l = ResultSetFormatter.toList(rs);

		for (ResultBinding resultBinding : l) {
			result.add(resultBinding.get(var).toString());
		}

		return result;

	}

}
