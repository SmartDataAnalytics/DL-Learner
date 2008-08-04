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

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.utilities.datastructures.StringTuple;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.core.ResultBinding;

/**
 * @author Sebastian Hellmann Convenience class for SPARQL queries initialized
 *         with a SparqlEndpoint. A Cache can also be used to further improve
 *         query time. Some methods allow basic reasoning
 */
public class SPARQLTasks {

	// TODO collect such things in a static class
	private static final String SUBCLASS_OF = "http://www.w3.org/2000/01/rdf-schema#subClassOf";

	private static Logger logger = Logger.getLogger(SPARQLTasks.class);

	private final Cache cache;

	private final SparqlEndpoint sparqlEndpoint;

	/**
	 * @param sparqlEndpoint
	 *            the Endpoint the sparql queries will be send to
	 */
	public SPARQLTasks(final SparqlEndpoint sparqlEndpoint) {
		super();
		this.cache = null;
		this.sparqlEndpoint = sparqlEndpoint;
	}

	/**
	 * @param cache
	 *            a cache object
	 * @param sparqlEndpoint
	 *            the Endpoint the sparql queries will be send to
	 */
	public SPARQLTasks(final Cache cache, final SparqlEndpoint sparqlEndpoint) {
		super();
		this.cache = cache;
		this.sparqlEndpoint = sparqlEndpoint;
	}

	/**
	 * get all superclasses up to a certain depth, 1 means direct superclasses
	 * only.
	 * 
	 * @param classURI
	 *            the uri of the class with no quotes for which the superclasses
	 *            will be retrieved
	 * @param maxDepth
	 *            how far the RDF graph will be explored (1 means only direct
	 *            SuperClasses)
	 * @return a Sorted String Set of all ClassNames, including the starting
	 *         class
	 */
	public SortedSet<String> getSuperClasses(final String classURI,
			final int maxDepth) {
		// TODO check for quotes in uris
		return getRecursiveSuperOrSubClasses(classURI, maxDepth, false);
	}

	/**
	 * This is the underlying function to get Super and SubClasses.
	 * 
	 * @param classURI
	 *            the uri of the class with no quotes for which the classes will
	 *            be retrieved
	 * @param maxDepth
	 *            how far the RDF graph will be explored (1 means only direct
	 *            related Classes)
	 * @return a Sorted String Set of all retrieved ClassNames, including the
	 *         starting class
	 */
	private SortedSet<String> getRecursiveSuperOrSubClasses(
			final String classURI, final int maxDepth, boolean subclasses) {
		// TODO check for quotes in uris
		int depth = maxDepth;

		final SortedSet<String> toBeRetrieved = new TreeSet<String>();
		toBeRetrieved.add(classURI);

		final SortedSet<String> returnSet = new TreeSet<String>();
		final SortedSet<String> tmpSet = new TreeSet<String>();

		// collect super/subclasses for the depth
		for (; (depth > 0) && (!toBeRetrieved.isEmpty()); depth--) {
			// collect super/subclasses for each class in toBeRetrieved
			// accumulate in tmpSet
			for (String oneClass : toBeRetrieved) {
				if (subclasses) {
					tmpSet.addAll(getDirectSubClasses(oneClass));
				} else {
					tmpSet.addAll(getDirectSuperClasses(oneClass));
				}

			}// end inner for

			// remember all queried classes to return them.
			returnSet.addAll(toBeRetrieved);
			// then discard them
			toBeRetrieved.clear();
			// all that are to be retrieved the next time.
			toBeRetrieved.addAll(tmpSet);
			// small optimization, remove all that have been processed already:
			toBeRetrieved.removeAll(returnSet);
			// reset
			tmpSet.clear();
		}// end outer for

		returnSet.addAll(toBeRetrieved);

		return returnSet;
	}

	/**
	 * gets a SortedSet of all subclasses up to a certain depth
	 * 
	 * @see conceptRewrite(String descriptionKBSyntax, SparqlEndpoint se, Cache
	 *      c, boolean simple )
	 * @param classURI An URI string with no quotes
	 * @param maxDepth
	 * @return TreeSet of subclasses including classURI
	 */
	public SortedSet<String> getSubClasses(final String classURI,
			final int maxDepth) {
//		 TODO check for quotes in uris
		return getRecursiveSuperOrSubClasses(classURI, maxDepth, true);
	}

	/**
	 * returns all direct subclasses of String concept
	 * 
	 * @param concept
	 *            An URI string with no quotes
	 * @return SortedSet of direct subclasses as String
	 */
	private SortedSet<String> getDirectSubClasses(String concept) {
		return queryPatternAsSet("?subject", "<" + SUBCLASS_OF + ">", "<"
				+ concept + ">", "subject", 0, false);
	}

	private SortedSet<String> getDirectSuperClasses(String concept) {
		return queryPatternAsSet("<" + concept + ">", "<" + SUBCLASS_OF + ">",
				"?object", "object", 0, false);
	}

	/**
	 * Retrieves all resource for a fixed role and object. These instances are
	 * distinct. QUALITY: buggy because role doesn't work sometimes get subject
	 * with fixed role and object
	 * 
	 * @param role
	 *            An URI string with no quotes
	 * @param object
	 *            An URI string with no quotes
	 * @param sparqlResultLimit
	 *            Limits the ResultSet size
	 * @return SortedSet with the resulting subjects
	 */
	public SortedSet<String> retrieveDISTINCTSubjectsForRoleAndObject(
			String role, String object, int sparqlResultLimit) {
		return queryPatternAsSet("?subject", "<" + role + ">", "<" + object
				+ ">", "subject", sparqlResultLimit, true);
	}

	/**
	 * @param subject
	 *            An URI string with no quotes
	 * @param role
	 *            An URI string with no quotes
	 * @param sparqlResultLimit
	 *            Limits the ResultSet size
	 * @return SortedSet with the resulting objects
	 */
	public SortedSet<String> retrieveObjectsForSubjectAndRole(String subject,
			String role, int sparqlResultLimit) {
		return queryPatternAsSet("<" + subject + ">", "<" + role + ">",
				"?object", "object", sparqlResultLimit, true);
	}

	/**
	 * all instances for a SKOS concept.
	 * 
	 * @param skosConcept
	 *            An URI string with no quotes
	 * @param sparqlResultLimit
	 *            Limits the ResultSet size
	 * @return SortedSet with the instances
	 */
	public SortedSet<String> retrieveInstancesForSKOSConcept(
			String skosConcept, int sparqlResultLimit) {
		return queryPatternAsSet("?subject", "?predicate", "<" + skosConcept
				+ ">", "subject", sparqlResultLimit, false);
	}

	/**
	 * get all instances for a complex concept / class description in KBSyntax.
	 * 
	 * @param conceptKBSyntax
	 *            A description string in KBSyntax
	 * @param sparqlResultLimit
	 *            Limits the ResultSet size
	 * @return SortedSet with the instance uris
	 */
	public SortedSet<String> retrieveInstancesForClassDescription(
			String conceptKBSyntax, int sparqlResultLimit) {

		String sparqlQueryString = "";
		try {
			sparqlQueryString = SparqlQueryDescriptionConvertVisitor
					.getSparqlQuery(conceptKBSyntax, sparqlResultLimit);
		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
		return queryAsSet(sparqlQueryString, "subject");
	}

	/**
	 * same as <code>retrieveInstancesForClassDescription</code> including
	 * RDFS Reasoning.
	 * 
	 * @param conceptKBSyntax
	 *            A description string in KBSyntax
	 * @param sparqlResultLimit
	 *            Limits the ResultSet size
	 * @return SortedSet with the instance uris
	 */
	public SortedSet<String> retrieveInstancesForClassDescriptionIncludingSubclasses(
			String conceptKBSyntax, int sparqlResultLimit, int maxDepth) {

		String sparqlQueryString = "";
		try {
			sparqlQueryString = SparqlQueryDescriptionConvertVisitor
					.getSparqlQueryIncludingSubclasses(conceptKBSyntax,
							sparqlResultLimit, this, maxDepth);

		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
		return queryAsSet(sparqlQueryString, "subject");
	}

	/**
	 * get all direct Classes of an instance.
	 * 
	 * @param instance
	 *            An URI string with no quotes
	 * @param sparqlResultLimit
	 *            Limits the ResultSet size
	 * @return
	 */
	public SortedSet<String> getClassesForInstance(String instance,
			int sparqlResultLimit) {

		// String sparqlQueryString = "SELECT ?subject WHERE { \n " + "<" +
		// instance
		// + ">" + " a " + "?subject " + "\n" + "} " + limit(sparqlResultLimit);
		return queryPatternAsSet("<" + instance + ">", "a", "?object",
				"object", sparqlResultLimit, false);
		// return queryAsSet(sparqlQueryString, "subject");
	}

	/**
	 * Returns all instances that are in the prefield (subject) of the
	 * property/role.
	 * 
	 * Cave: These have to fulfill the following requirements: 1. They are not
	 * literals 2. They have at least a Class assigned 3. DISTINCT is used in
	 * the query
	 * 
	 * TODO there might be a better name for the function
	 * 
	 * @param role
	 *            An URI of a property/role
	 * @param sparqlResultLimit
	 *            ResultSet limit
	 * @return A String Set of instances
	 */
	public SortedSet<String> getDomainInstances(String role,
			int sparqlResultLimit) {

		String sparqlQueryString = "SELECT DISTINCT ?domain " + "WHERE { \n"
				+ "?domain <" + role + "> " + " ?o. \n" + "?domain a []\n."
				+ "FILTER (!isLiteral(?domain))." + "}\n"
				+ limit(sparqlResultLimit);

		return queryAsSet(sparqlQueryString, "domain");

	}

	/**
	 * Returns all instances that are fillers of the property/role. Cave: These
	 * have to fulfill the following requirements: 1. The fillers are not
	 * literals 2. The fillers have at least a Class assigned 3. DISTINCT is
	 * used in the query
	 * 
	 * TODO there might be a better name for the function
	 * 
	 * @param role
	 *            An URI of a property/role
	 * @param sparqlResultLimit
	 *            ResultSet limit
	 * @return A String Set of instances
	 */
	public SortedSet<String> getRangeInstances(String role,
			int sparqlResultLimit) {

		String sparqlQueryString = "SELECT DISTINCT ?range " + "WHERE { \n"
				+ "?s <" + role + "> " + " ?range. \n" + "?range a [].\n"
				+ "FILTER (!isLiteral(?range))." + "}\n"
				+ limit(sparqlResultLimit);

		return queryAsSet(sparqlQueryString, "range");

	}

	/**
	 * query a pattern with a standard SPARQL query. The Query will be of the
	 * form SELECT * WHERE { subject predicate object } LIMIT X. It has a high
	 * degree of freedom, but only one variabla can be retrieved.
	 * 
	 * usage example 1 : queryPatternAsSet( "?subject", "<http://somerole>",
	 * "?object", "subject" ). retrieves all subjects, that have the role,
	 * somerole
	 * 
	 * usage example 1 : queryPatternAsSet( "?subject", "<http://somerole>",
	 * "?object", "object" ). retrieves all objects, that have the role,
	 * somerole
	 * 
	 * @param subject
	 *            An URI string enclosed in <> or a SPARQL variable e.g.
	 *            "?subject"
	 * @param predicate
	 *            An URI string enclosed in <> or a SPARQL variable e.g.
	 *            "?predicate"
	 * @param object
	 *            An URI string enclosed in <> or a SPARQL variable e.g.
	 *            "?object"
	 * @param variable
	 *            The variable to be retrieved and put into the SortedSet
	 * @param sparqlResultLimit
	 *            0 means all
	 * @param distinct
	 *            determines whether distinct is used
	 * @return a String Set with the Bindings of the variable in variable
	 */
	public SortedSet<String> queryPatternAsSet(String subject,
			String predicate, String object, String variable,
			int sparqlResultLimit, boolean distinct) {
		String sparqlQueryString = "SELECT " + ((distinct) ? "DISTINCT" : "")
				+ " * WHERE { \n " + " " + subject + " " + predicate + " "
				+ object + " \n" + "} " + limit(sparqlResultLimit);
		return queryAsSet(sparqlQueryString, variable);
	}
	
	
	public SortedSet<StringTuple> queryAsTuple(String subject, boolean filterLiterals) {
		ResultSetRewindable rs = null;
		String p = "predicate";
		String o = "object";
		String lits = (filterLiterals)? ".FILTER  (!isLiteral(?"+o+"))." : "";
		String sparqlQueryString = "SELECT * WHERE { <"+subject+"> ?"+p+" ?"+o+" "+lits+" } ";
		
		try {
			String jsonString = query(sparqlQueryString);
			rs = SparqlQuery.convertJSONtoResultSet(jsonString);

		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
		
		//SimpleClock sc = new SimpleClock();
		//rw = ResultSetFactory.makeRewindable(rs);
		//sc.printAndSet("rewindable");
		return getTuplesFromResultSet(rs, p, o);
	}

	/**
	 * little higher level, executes query ,returns all resources for a
	 * variable.
	 * 
	 * @param sparqlQueryString
	 *            The query
	 * @param variable
	 *            The single variable used in the query
	 * @return
	 */
	public SortedSet<String> queryAsSet(String sparqlQueryString,
			String variable) {
		ResultSet rs = null;
		try {
			String jsonString = query(sparqlQueryString);
			rs = SparqlQuery.convertJSONtoResultSet(jsonString);

		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
		return getStringSetForVariableFromResultSet(ResultSetFactory
				.makeRewindable(rs), variable);
	}

	/**
	 * low level, executes query returns ResultSet.
	 * 
	 * @param sparqlQueryString
	 *            The query
	 * @return jena ResultSet
	 */
	public ResultSetRewindable queryAsResultSet(String sparqlQueryString) {
		return SparqlQuery.convertJSONtoResultSet(query(sparqlQueryString));

	}

	/**
	 * low level, executes query returns JSON.
	 * 
	 * @param sparqlQueryString
	 *            The query
	 * @return
	 */
	public String query(String sparqlQueryString) {
		String jsonString;
		if (cache == null) {
			
			SparqlQuery sq = new SparqlQuery(sparqlQueryString, sparqlEndpoint);
			//SimpleClock sc = new SimpleClock();
			sq.send();
			//sc.printAndSet("querysend");
			jsonString = sq.getJson();
			
		} else {
			jsonString = cache.executeSparqlQuery(new SparqlQuery(
					sparqlQueryString, sparqlEndpoint));
		}
		return jsonString;
	}

	/**
	 * a String Helper which constructs the limit clause of a sparql query. if
	 * sparqlResultLimit is zero, returns nothing
	 * 
	 * @param sparqlResultLimit
	 *            the resultsetlimit
	 * @return LIMIT sparqlResultLimit if bigger than zero, else returns "";
	 */
	private String limit(int sparqlResultLimit) {
		return (sparqlResultLimit > 0) ? (" LIMIT " + sparqlResultLimit) : "";
	}

	public static SortedSet<String> getStringSetForVariableFromResultSet(
			ResultSetRewindable rs, String variable) {
		final SortedSet<String> result = new TreeSet<String>();

		@SuppressWarnings("unchecked")
		final List<ResultBinding> l = ResultSetFormatter.toList(rs);

		for (ResultBinding resultBinding : l) {
			result.add(resultBinding.get(variable).toString());
		}
		rs.reset();
		return result;

	}
	
	private static SortedSet<StringTuple> getTuplesFromResultSet( 
			ResultSetRewindable rs, String predicate, String object) {
		final SortedSet<StringTuple> returnSet = new TreeSet<StringTuple>();
		//SimpleClock sc = new SimpleClock();
		@SuppressWarnings("unchecked")
		final List<ResultBinding> l = ResultSetFormatter.toList(rs);
		for (ResultBinding resultBinding : l) {
			returnSet.add(new StringTuple(resultBinding.get(predicate).toString(),resultBinding.get(object).toString()));
		}
		//sc.printAndSet("allTuples");
		rs.reset();
		//sc.printAndSet("reset");
		return returnSet;

	}

	public SparqlEndpoint getSparqlEndpoint() {
		return sparqlEndpoint;
	}

}

/*
 * here are some old functions, which were workarounds:
 * 
 * 
 * QUALITY: workaround for a sparql glitch {?a owl:subclassOf ?b} returns an
 * empty set on some endpoints. returns all direct subclasses of String concept
 * 
 * @param concept An URI string with no quotes @return SortedSet of direct
 * subclasses as String
 * 
 * private SortedSet<String> getDirectSubClasses(String concept) {
 * 
 * String sparqlQueryString; SortedSet<String> subClasses = new TreeSet<String>();
 * ResultSet resultSet;
 * 
 * sparqlQueryString = "SELECT * \n " + "WHERE { \n" + " ?subject ?predicate <" +
 * concept + "> \n" + "}\n";
 * 
 * resultSet = queryAsResultSet(sparqlQueryString);
 * 
 * @SuppressWarnings("unchecked") List<ResultBinding> bindings =
 * ResultSetFormatter.toList(resultSet); String subject = ""; String predicate =
 * "";
 * 
 * for (ResultBinding resultBinding : bindings) {
 * 
 * subject = ((resultBinding.get("subject").toString())); predicate =
 * ((resultBinding.get("predicate").toString())); if (predicate
 * .equalsIgnoreCase("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
 * subClasses.add(subject); } } return subClasses; }
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */

