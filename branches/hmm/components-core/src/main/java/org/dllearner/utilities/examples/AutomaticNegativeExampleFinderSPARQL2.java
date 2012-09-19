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

package org.dllearner.utilities.examples;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.datastructures.Datastructures;

/**
 * 
 * Utility class for automatically retrieving negative examples from a 
 * SPARQL endpoint given a set of positive examples.
 * 
 * @author Jens Lehmann
 * @author Sebastian Hellmann
 *
 */
public class AutomaticNegativeExampleFinderSPARQL2 {

	private SparqlEndpoint se;
	
	// for re-using existing queries
	private SPARQLReasoner sr;
	private SPARQLTasks st;
	
	public AutomaticNegativeExampleFinderSPARQL2(SparqlEndpoint se) {
		this.se = se;
		SparqlEndpointKS ks = new SparqlEndpointKS(se);
		sr = new SPARQLReasoner(ks);
		st = new SPARQLTasks(se);
	}
	
	/**
	 * Get negative examples when learning the description of a class, i.e.
	 * all positives are from some known class.
	 * 
	 * Currently, the method implementation is preliminary and does not allow
	 * to configure internals.
	 * 
	 * @param classURI The known class of all positive examples.
	 * @param positiveExamples The existing positive examples.
	 */
	public SortedSet<String> getNegativeExamples(String classURI, SortedSet<String> positiveExamples) {
		// get some individuals from parallel classes (we perform one query per class to avoid
		// only getting individuals from a single class)
		Set<String> parallelClasses = st.getParallelClasses(classURI, 5); // TODO: limit could be configurable
		SortedSet<String> negEx = new TreeSet<String>();
		for(String parallelClass : parallelClasses) {
			Set<String> inds = Datastructures.individualSetToStringSet(sr.getIndividuals(new NamedClass(parallelClass), 10));
			negEx.addAll(inds);
			if(negEx.size()>100) {
				return negEx;
			}
		}
		// add some random instances
		String query = "SELECT ?inst { ?inst <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?x } LIMIT 20";
		negEx.addAll(st.queryAsSet(query, "?inst"));
        return negEx;
	}
	
}
