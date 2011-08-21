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

package org.dllearner.test;

import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.ResultBinding;

public class TripleTypeTest {

	
	public static void main(String[] args) {
		String sparqlQueryString ="SELECT * WHERE { <http://dbpedia.org/resource/Angela_Merkel> ?predicate ?object. FILTER (isLiteral(?object))}";
		//sparqlQueryString ="SELECT * WHERE { <http://dbpedia.org/resource/Angela_Merkel> <http://dbpedia.org/property/hasPhotoCollection> ?object }";
		System.out.println(sparqlQueryString);
		
		SPARQLTasks st = new SPARQLTasks (Cache.getDefaultCache(), SparqlEndpoint.getEndpointDBpedia());
		
		ResultSetRewindable rsw = st.queryAsResultSet(sparqlQueryString);
		@SuppressWarnings("unchecked")
		List<QuerySolution> l = ResultSetFormatter.toList(rsw);
		
		for (QuerySolution binding : l) {
			//RDFNode pred = binding.get("predicate");
			RDFNode obj = binding.get("object");
			//System.out.println(pred.toString());
			//System.out.println(obj.toString());
			System.out.println(obj.isLiteral());
			System.out.println(obj.isAnon());
			System.out.println(obj.isResource());
			System.out.println(obj.isURIResource());
			Literal lit =(Literal) obj;
			System.out.println(lit.toString());
			System.out.println(lit.getLanguage());
		}
		
		
	}
}
