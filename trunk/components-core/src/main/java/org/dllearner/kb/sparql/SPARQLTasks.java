/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 */

package org.dllearner.kb.sparql;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.datastructures.StringTuple;
import org.dllearner.utilities.owl.OWLVocabulary;

import com.clarkparsia.owlapiv3.OWL;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;

/**
 * Convenience class for SPARQL queries initialized
 *         with a SparqlEndpoint. A Cache can also be used to further improve
 *         query time. Some methods allow basic reasoning
 * 
 * @author Sebastian Hellmann 
 * @author Jens Lehmann
 */
public class SPARQLTasks {

	private static Logger logger = Logger.getLogger(SPARQLTasks.class);

	private final Cache cache;

	private final SparqlEndpoint sparqlEndpoint;

	/**
	 * @param sparqlEndpoint
	 *            the Endpoint the sparql queries will be send to
	 */
	public SPARQLTasks(final SparqlEndpoint sparqlEndpoint) {
//		super();
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
//		super();
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
	
	public SortedSet<String> getParallelClasses(String classURI, int limit) {
		String query = "SELECT ?sub WHERE { <" + classURI + "> <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?super .";
		query += "?sub <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?super .";
		query += "FILTER( ?sub != <" + classURI + ">) . } LIMIT " + limit;
		return queryAsSet(query, "?sub");
//		SparqlQuery sq = new SparqlQuery(query, sparqlEndpoint);
//		ResultSet rs = sq.send();
		
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
	 * TODO the mentioned method does not exist
	 * conceptRewrite(String descriptionKBSyntax, SparqlEndpoint se, Cache
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
		return queryPatternAsSet("?subject", "<" + OWLVocabulary.RDFS_SUBCLASS_OF + ">", "<"
				+ concept + ">", "subject", 0, false);
	}

	private SortedSet<String> getDirectSuperClasses(String concept) {
		return queryPatternAsSet("<" + concept + ">", "<" + OWLVocabulary.RDFS_SUBCLASS_OF + ">",
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
					.getSparqlQuery(conceptKBSyntax, sparqlResultLimit, false, false);
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
	
	@Deprecated
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

	@Deprecated
	public SortedSet<StringTuple> queryAsTuple(String sparqlQueryString, String var1, String var2) {
		ResultSetRewindable rs = null;
		try {
			String jsonString = query(sparqlQueryString);
			rs = SparqlQuery.convertJSONtoResultSet(jsonString);

		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
		
		//SimpleClock sc = new SimpleClock();
		//rw = ResultSetFactory.makeRewindable(rs);
		//sc.printAndSet("rewindable");
		return getTuplesFromResultSet(rs, var1, var2);
	}
	
	@SuppressWarnings("unchecked")
	public SortedSet<RDFNodeTuple> queryAsRDFNodeTuple(String sparqlQueryString, String var1, String var2) {
		ResultSetRewindable rsw = null;
		SortedSet<RDFNodeTuple> returnSet = new TreeSet<RDFNodeTuple>();
		
		try {
			String jsonString = query(sparqlQueryString);
			rsw = SparqlQuery.convertJSONtoResultSet(jsonString);

		
		
		List<QuerySolution> l = ResultSetFormatter.toList(rsw);
		for (QuerySolution resultBinding : l) {
			returnSet.add(new RDFNodeTuple(resultBinding.get(var1),resultBinding.get(var2)));
		}
		
		rsw.reset();
		} catch (Exception e) {
			logger.info("ignoring (see log for details): Exception caught in SPARQLTasks, passing emtpy result: "+e.getMessage());
		}
		
		return returnSet;
	}

	
	/**
	 * little higher level, executes query ,returns all resources for a
	 * variable.
	 * 
	 * @param sparqlQueryString
	 *            The query
	 * @param variable
	 *            The single variable used in the query
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
		SparqlQuery sq = new SparqlQuery(sparqlQueryString, sparqlEndpoint);
		if(cache == null) {
			return sq.send();
		} else {
			// get JSON from cache and convert to result set
			String json = cache.executeSparqlQuery(sq);
			return SparqlQuery.convertJSONtoResultSet(json);
		}
	}
	
	/**
	 * variable must be ?count
	 * @param sparqlQueryString
	 * @return -1 on failure count on success
	 */
	public int queryAsCount(String sparqlQueryString) {
		SparqlQuery sq = new SparqlQuery(sparqlQueryString, sparqlEndpoint);
		ResultSetRewindable rsw = null;
		if(cache == null) {
			rsw = sq.send();
		} else {
			// get JSON from cache and convert to result set
			String json = cache.executeSparqlQuery(sq);
			rsw =  SparqlQuery.convertJSONtoResultSet(json);
		}
		int ret = -1;
		while(rsw.hasNext()){
			QuerySolution qs = rsw.nextSolution();
			ret = qs.getLiteral("count").getInt();
			
		}
		return ret;
		
	}
	
	/**
	 * low level, executes query returns JSON.
	 * 
	 * @param sparqlQueryString
	 *            The query
	 */
	public String query(String sparqlQueryString) {
		String jsonString;
		if (cache == null) {
			
			SparqlQuery sq = new SparqlQuery(sparqlQueryString, sparqlEndpoint);
			//SimpleClock sc = new SimpleClock();
			sq.send(false);
			//sc.printAndSet("querysend");
			jsonString = sq.getJson();
			
		} else {
			jsonString = cache.executeSparqlQuery(new SparqlQuery(
					sparqlQueryString, sparqlEndpoint));
		}
		return jsonString;
	}

	public boolean ask(String askQueryString) {
		if(cache == null) {
			SparqlQuery sq = new SparqlQuery(askQueryString, sparqlEndpoint);
			return sq.sendAsk();
		} else {
			return cache.executeSparqlAskQuery(new SparqlQuery(askQueryString, sparqlEndpoint));
		}
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
		final List<QuerySolution> l = ResultSetFormatter.toList(rs);

		for (QuerySolution resultBinding : l) {
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
		final List<QuerySolution> l = ResultSetFormatter.toList(rs);
		for (QuerySolution resultBinding : l) {
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
	
	public static SPARQLTasks getPredefinedSPARQLTasksWithCache(String endpointName) {
		return new SPARQLTasks( Cache.getDefaultCache(), SparqlEndpoint.getEndpointByName(endpointName) );
	}

	// tries to detect the type of the resource
	public Entity guessResourceType(String resource) {
		SortedSet<String> types = retrieveObjectsForSubjectAndRole(resource, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 10000);
//		System.out.println(types);
		if(types.contains("http://www.w3.org/2002/07/owl#ObjectProperty")) {
			return new ObjectProperty(resource);
		} else if(types.contains("http://www.w3.org/2002/07/owl#DatatypeProperty")) {
			return new DatatypeProperty(resource);
		} else if(types.contains("http://www.w3.org/2002/07/owl#Class")) {
			return new NamedClass(resource);
		} else {
			return null;
		}
	}
	
	// tries to detect the type of the resource
	public Entity guessResourceType(String resource, boolean byTriples) {
		SortedSet<String> types = retrieveObjectsForSubjectAndRole(resource, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 10000);
//		System.out.println(types);
		if(types.contains("http://www.w3.org/2002/07/owl#ObjectProperty")) {
			return new ObjectProperty(resource);
		} else if(types.contains("http://www.w3.org/2002/07/owl#DatatypeProperty")) {
			return new DatatypeProperty(resource);
		} else if(types.contains("http://www.w3.org/2002/07/owl#Class")) {
			return new NamedClass(resource);
		} else {
			if(byTriples){
				String queryString = String.format("ASK {?s a <%s>}", resource);
				SparqlQuery sq = new SparqlQuery(queryString, sparqlEndpoint);
				boolean isClass = sq.sendAsk();
				if(isClass){
					return new NamedClass(resource);
				} else {
					queryString = String.format("SELECT ?o WHERE {?s <%s> ?o.} LIMIT 10", resource);
					sq = new SparqlQuery(queryString, sparqlEndpoint);
					ResultSet rs = sq.send(false);
					QuerySolution qs = null;
					boolean isDataProperty = false;
					boolean isObjectProperty = false;
					while(rs.hasNext()){
						qs = rs.next();
						if(qs.get("o").isLiteral()){
							isDataProperty = true;
						} else if(qs.get("o").isResource()){
							isObjectProperty = true;
						}
						
					}
					if(isDataProperty && !isObjectProperty){
						return new DatatypeProperty(resource);
					} else if(!isDataProperty && isObjectProperty){
						return new ObjectProperty(resource);
					}
				}
			}
			
			return null;
		}
	}
	
	public Set<ObjectProperty> getAllObjectProperties() {
		Set<ObjectProperty> properties = new TreeSet<ObjectProperty>();
		String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p WHERE {?p a owl:ObjectProperty}";
		SparqlQuery sq = new SparqlQuery(query, sparqlEndpoint);
		ResultSet q = sq.send(false);
		while (q.hasNext()) {
			QuerySolution qs = q.next();
			properties.add(new ObjectProperty(qs.getResource("p").getURI()));
		}
		return properties;
	}
	
	public Set<DatatypeProperty> getAllDataProperties() {
		Set<DatatypeProperty> properties = new TreeSet<DatatypeProperty>();
		String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p WHERE {?p a owl:DatatypeProperty}";
		SparqlQuery sq = new SparqlQuery(query, sparqlEndpoint);
		ResultSet q = sq.send(false);
		while (q.hasNext()) {
			QuerySolution qs = q.next();
			properties.add(new DatatypeProperty(qs.getResource("p").getURI()));
		}
		return properties;
	}
	
	public Set<NamedClass> getAllClasses() {
		Set<NamedClass> classes = new TreeSet<NamedClass>();
		String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?c WHERE {?c a owl:Class} LIMIT 1000";
		/*
		 * String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"SELECT ?c WHERE {{?c a owl:Class} UNION {?c rdfs:subClassOf ?d} UNION {?d rdfs:subClassOf ?c}} LIMIT 1000";
		 */
		SparqlQuery sq = new SparqlQuery(query, sparqlEndpoint);
		ResultSet q = sq.send(false);
		while (q.hasNext()) {
			QuerySolution qs = q.next();
			if(qs.getResource("c").isURIResource()){
				classes.add(new NamedClass(qs.getResource("c").getURI()));
			}
			
		}
		//remove trivial classes
		classes.remove(new NamedClass(OWL.Nothing.toStringID()));
		classes.remove(new NamedClass(OWL.Thing.toStringID()));
		return classes;
	}	
	
	public boolean supportsSPARQL_1_1(){
		String query = "SELECT * WHERE {?s ?p ?o. {SELECT * WHERE {?s ?p ?o.} LIMIT 1} } LIMIT 1";
		SparqlQuery sq = new SparqlQuery(query, sparqlEndpoint);
		try {
			sq.send(false);
			return true;
		} catch (Exception e) {
			System.out.println("Endpoint doesn't seem to support SPARQL 1.1 .");
		}
		return false;
	}
	
}

/*
 * here are some old functions, which were workarounds:
 * 
 * 
 *  workaround for a sparql glitch {?a owl:subclassOf ?b} returns an
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

