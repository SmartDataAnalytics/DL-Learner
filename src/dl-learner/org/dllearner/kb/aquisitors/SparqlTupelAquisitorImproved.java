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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlQueryMaker;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;

/**
 * Can execute different queries.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class SparqlTupelAquisitorImproved extends SparqlTupelAquisitor {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SparqlTupelAquisitorImproved.class);
	private Map<String,SortedSet<RDFNodeTuple>> resources = new HashMap<String, SortedSet<RDFNodeTuple>>();
	int recursionDepth;
	

	public SparqlTupelAquisitorImproved(SparqlQueryMaker sparqlQueryMaker, SPARQLTasks sparqlTasks, int recursionDepth) {
		super(sparqlQueryMaker, sparqlTasks);
		this.recursionDepth = recursionDepth;
		
	}

	// standard query get a tupels (p,o) for subject s
//	 standard query get a tupels (p,o) for subject s
	@Override
	public SortedSet<RDFNodeTuple> getTupelForResource(String uri) {
		checkURIforValidity(uri);
		String sparqlQueryString = "";
		String pred = "predicate";
		String obj = "object";
		
		// getQuery
		if (classMode) {
			
			
			 sparqlQueryString = sparqlQueryMaker.makeClassQueryUsingFilters(uri);
			 return  sparqlTasks.queryAsRDFNodeTuple(sparqlQueryString, pred, obj);
		}
		
		SortedSet<RDFNodeTuple> cachedSet = resources.get(uri);
		if(cachedSet!=null) {
			return cachedSet;
			}
		
		//SortedSet<RDFNodeTuple> tmp = new TreeSet<RDFNodeTuple>();
		sparqlQueryString = sparqlQueryMaker.makeSubjectQueryLevel(uri, recursionDepth);
		ResultSetRewindable rsw= sparqlTasks.queryAsResultSet(sparqlQueryString);
		@SuppressWarnings("unchecked")
		List<ResultBinding> l = ResultSetFormatter.toList(rsw);
		rsw.reset();
		
		int resultsetcount = 0;
		int i = 0;
		for (ResultBinding binding : l) {
			i=0;
			RDFNode nextURI = binding.get(obj+i);
			add(uri, new RDFNodeTuple(binding.get(pred+i), nextURI ));
						
			for (i=1; i < recursionDepth; i++) {
				RDFNode tmpURI = binding.get(obj+i);
				add(nextURI.toString(), new RDFNodeTuple(binding.get(pred+i),tmpURI));
				logger.trace("For: "+nextURI.toString()+ " added :"+resources.get(nextURI.toString()));
				nextURI = tmpURI;
			}
			
			resultsetcount++;
		}
		
		if(resultsetcount>999) {
			logger.warn("SparqlTupelAquisitor retrieved more than 1000 results, there might some be missing");
		}
		return resources.get(uri);
		
		//return  sparqlTasks.queryAsRDFNodeTuple(sparqlQueryString, pred, obj);
	}
	
	private void add(String uri, RDFNodeTuple tuple){
		SortedSet<RDFNodeTuple> set = resources.get(uri);
		if(set==null){
			set = new TreeSet<RDFNodeTuple>();
			set.add(tuple);
			resources.put(uri, set );
		}else {
			set.add(tuple);
		}
	}



}
