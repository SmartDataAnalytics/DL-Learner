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
package org.dllearner.kb.aquisitors;

import java.net.URI;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.extraction.Configuration;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlQueryMaker;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

/**
 * Can execute different queries.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class LinkedDataTupelAquisitor extends TupelAquisitor {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LinkedDataTupelAquisitor.class);
	
	private Configuration configuration;
	protected SparqlQueryMaker sparqlQueryMaker;
	protected SPARQLTasks sparqlTasks;

	public LinkedDataTupelAquisitor(Configuration Configuration) {
		this.configuration = Configuration;
		this.sparqlQueryMaker = configuration.getSparqlQueryMaker();
		this.sparqlTasks = configuration.sparqlTasks;
	}

	// standard query get a tupels (p,o) for subject s
	@Override
	public SortedSet<RDFNodeTuple> getTupelForResource(URI uri) {
		
		
		String pred = "predicate";
		String obj = "object";
		// getQuery
		String sparqlQueryString = sparqlQueryMaker
				.makeSubjectQueryUsingFilters(uri.toString());
		
		return  sparqlTasks.queryAsRDFNodeTuple(sparqlQueryString, pred, obj);

	}
	
	
	


}
