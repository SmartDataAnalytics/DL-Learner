/**
 * Copyright (C) 2007, Jens Lehmann
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
package org.dllearner.scripts;

import java.net.URI;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Individual;
import org.dllearner.utilities.datastructures.StringTuple;

/**
 * Here are just some code snippets, which can be used The basic algorithm is
 * simple:
 * 
 * input: 
 * 1. domain or range 
 * 2. number of instances n choose domain or range of a role 
 * Positive Examples: get the first n instances of domain or range
 * Negative Examples: get the first n instances of range or domain 
 * The Queries can be found in SPARQLqueryType
 * 
 */
public class RoleLearning {

	Set<Individual> positiveExamples;
	Set<Individual> negativeExamples;
	
	public RoleLearning(ReasonerComponent reasoningService) {
//		super(reasoningService);
	}

	public RoleLearning(ReasonerComponent reasoningService,
			SortedSet<Individual> positiveExamples,
			SortedSet<Individual> negativeExamples) {
//		super(reasoningService);
//		this.configurator = new RoleLearningConfigurator(this);
		// TODO sets have to be queried
		this.positiveExamples = positiveExamples;
		this.negativeExamples = negativeExamples;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#getName()
	 */
	public static String getName() {
		return "role learning";
	}

	// can be replaced by a static query, but it also can make use of filters as
	// it is done here
	// query get a tupels (s,o) for role p
	public Set<StringTuple> getTupelsForRole(URI u) {

		// getQuery
		/*
		 * String sparql = sparqlQueryMaker
		 * .makeRoleQueryUsingFilters(u.toString());
		 * 
		 * Set<StringTuple> s = cachedSparql(u, sparql, "subject", "object"); //
		 * System.out.println(s); return s;
		 */return null;
	}

	// can be replaced by a static query, but it also can make use of filters as
	// it is done here
	public Set<StringTuple> getTupelsForRole(URI u, boolean domain) {
		/*
		 * // getQuery String sparql =
		 * sparqlQueryMaker.makeRoleQueryUsingFilters( u.toString(), domain);
		 * 
		 * Set<StringTuple> s = cachedSparql(u, sparql, "subject", "object"); //
		 * System.out.println(s); return s;
		 */
		return null;
	}

	public Set<String> getDomainInstancesForRole(String role) {
		/*
		 * URI u = null; try { u = new URI(role); } catch (Exception e) {
		 * e.printStackTrace(); } Set<StringTuple> t =
		 * ((TypedSparqlQuery)this.typedSparqlQuery).getTupelsForRole(u, true);
		 * Set<String> ret = new HashSet<String>(); for (StringTuple one : t) {
		 * 
		 * ret.add(one.a); } return ret;
		 */
		return null;
	}

	public Set<String> getRangeInstancesForRole(String role) {
		/*
		 * URI u = null; try { u = new URI(role); } catch (Exception e) {
		 * e.printStackTrace(); } Set<StringTuple> t =
		 * ((TypedSparqlQuery)this.typedSparqlQuery).getTupelsForRole(u,false);
		 * Set<String> ret = new HashSet<String>(); for (StringTuple one : t) {
		 * 
		 * ret.add(one.b); } return ret;
		 */
		return null;
	}

	public void fromKnowledgeSource() {
		/*
		 * if (learnDomain || learnRange) { Set<String> pos = new HashSet<String>();
		 * Set<String> neg = new HashSet<String>(); if (learnDomain) { pos =
		 * m.getDomainInstancesForRole(role); neg =
		 * m.getRangeInstancesForRole(role); } else if (learnRange) { neg =
		 * m.getDomainInstancesForRole(role); pos =
		 * m.getRangeInstancesForRole(role); } // choose 30
		 * 
		 * Set<String> tmp = new HashSet<String>(); for (String one : pos) {
		 * tmp.add(one); if (tmp.size() >= numberOfInstancesUsedForRoleLearning)
		 * break; } pos = tmp; logger.info("Instances used: " + pos.size());
		 * 
		 * tmp = new HashSet<String>(); for (String one : neg) { tmp.add(one);
		 * if (tmp.size() >= numberOfInstancesUsedForRoleLearning) break; } neg =
		 * tmp;
		 * 
		 * instances = new HashSet<String>(); instances.addAll(pos);
		 * 
		 * instances.addAll(neg);
		 * 
		 * for (String one : pos) { logger.info("+\"" + one + "\""); } for
		 * (String one : neg) { logger.info("-\"" + one + "\""); }
		 */
		/*
		 * Random r= new Random();
		 * 
		 * 
		 * Object[] arr=instances.toArray(); while(instances.size()>=30){ }
		 */
		/*
		 * // add the role to the filter(a solution is always EXISTS //
		 * role.TOP) m.addPredicateFilter(role); //
		 * System.out.println(instances); // THIS is a workaround
		 */
	}

}
