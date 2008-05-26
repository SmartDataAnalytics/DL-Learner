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
package org.dllearner.kb.extraction;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.kb.sparql.SparqlQueryMaker;
import org.dllearner.utilities.datastructures.StringTuple;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.core.ResultBinding;

/**
 * Can execute different queries.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class TypedSparqlQuery implements TypedSparqlQueryInterface {
	
	private static Logger logger = Logger.getLogger(KnowledgeSource.class);

	
	boolean print_flag = false;
	protected Configuration configuration;
	private SparqlQueryMaker sparqlQueryMaker;
	Cache cache;

	// boolean debug_no_cache = false;// true means no cache is used
	// private SparqlHTTPRequest SparqlHTTPRequest;
	// private SparqlQuery sparqlQuery;
	// private CachedSparqlQuery cachedSparqlQuery;

	public TypedSparqlQuery(Configuration Configuration) {
		this.configuration = Configuration;
		this.sparqlQueryMaker = new SparqlQueryMaker(Configuration
				.getSparqlQueryType());
		
		this.cache = new Cache(configuration.cacheDir); 
		// this.sparqlQuery=new SparqlQuery(configuration.getSparqlEndpoint());
		// this.cachedSparqlQuery=new
		// CachedSparqlQuery(this.sparqlQuery,this.cache);
	}

	// standard query get a tupels (p,o) for subject s
	/**
	 * uses a cache and gets the result tuples for a resource u
	 * 
	 * @param uri
	 *            the resource
	 * @param sparqlQueryString
	 * @param a
	 *            the name of the first bound variable for xml parsing, normally
	 *            predicate
	 * @param b
	 *            the name of the second bound variable for xml parsing,
	 *            normally object
	 * @return
	 */
	@SuppressWarnings({"unchecked"})
	public Set<StringTuple> getTupelForResource(URI uri) {
		Set<StringTuple> s = new HashSet<StringTuple>();
		
		String a = "predicate";
		String b = "object";
		// getQuery
		String sparqlQueryString = sparqlQueryMaker
				.makeSubjectQueryUsingFilters(uri.toString());

//		CachedSparqlQuery csq = new CachedSparqlQuery(configuration
//				.getSparqlEndpoint(), cache, uri.toString(), sparqlQueryString);

		SparqlQuery query = new SparqlQuery(sparqlQueryString, configuration.getSparqlEndpoint());
		query.extraDebugInfo=uri.toString();
		String JSON = cache.executeSparqlQuery(query);
		
		ResultSet rs = SparqlQuery.JSONtoResultSet(JSON);
		
		List<ResultBinding> l = ResultSetFormatter.toList(rs);
		
		logger.trace(l.toString());
		for (ResultBinding resultBinding : l) {
					
			s.add(new StringTuple(resultBinding.get(a).toString(),
					resultBinding.get(b).toString()));
		}
		return s;
	}

	

	

	
	



}
