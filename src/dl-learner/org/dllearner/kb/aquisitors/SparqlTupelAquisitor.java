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

import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlQueryMaker;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

/**
 * Can execute different queries.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class SparqlTupelAquisitor extends TupelAquisitor {
	
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SparqlTupelAquisitor.class);
	protected static final String PREDICATE = "predicate";
	protected static final String OBJECT = "object";
	
	protected SparqlQueryMaker sparqlQueryMaker;
	protected SPARQLTasks sparqlTasks;
	
	

	public SparqlTupelAquisitor(SparqlQueryMaker sparqlQueryMaker, SPARQLTasks sparqlTasks) {
		
		this.sparqlQueryMaker = sparqlQueryMaker;
		this.sparqlTasks = sparqlTasks;
	}
	
	@Override
	public SortedSet<RDFNodeTuple> retrieveTupel(String uri){
		// getQuery
		String sparqlQueryString = sparqlQueryMaker.makeSubjectQueryUsingFilters(uri);
		return  sparqlTasks.queryAsRDFNodeTuple(sparqlQueryString, PREDICATE, OBJECT);
		
	}
	@Override
	public SortedSet<RDFNodeTuple> retrieveClassesForInstances(String uri){
		// getQuery
		String sparqlQueryString = sparqlQueryMaker.makeClassQueryUsingFilters(uri);
		return  sparqlTasks.queryAsRDFNodeTuple(sparqlQueryString, PREDICATE, OBJECT);
		
	}
	@Override
	public SortedSet<RDFNodeTuple> retrieveTuplesForClassesOnly(String uri){
		return retrieveTupel(uri);
	}

	
	
	
	
	
	


}
