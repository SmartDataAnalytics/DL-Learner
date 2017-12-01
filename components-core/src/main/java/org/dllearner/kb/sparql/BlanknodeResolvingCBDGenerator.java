/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;

import java.util.Set;

/**
 * @author Lorenz Buehmann
 *
 */
public class BlanknodeResolvingCBDGenerator implements ConciseBoundedDescriptionGenerator{
	
	private QueryExecutionFactoryModel qef;
	boolean resolveBlankNodes = true;

	public BlanknodeResolvingCBDGenerator(Model model) {
		String query = "prefix : <http://dl-learner.org/ontology/> "
				+ "construct { ?s ?p ?o ; ?type ?s .} "
				+ "where {  ?s ?p ?o .  bind( if(isIRI(?s),:sameIri,:sameBlank) as ?type )}";
		qef = new QueryExecutionFactoryModel(model);
		QueryExecution qe = qef.createQueryExecution(query);
		Model extendedModel = qe.execConstruct();
		qe.close();
		
		qef = new QueryExecutionFactoryModel(extendedModel);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String)
	 */
	@Override
	public Model getConciseBoundedDescription(String resourceURI) {
		return getConciseBoundedDescription(resourceURI, 0);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int)
	 */
	@Override
	public Model getConciseBoundedDescription(String resource, int depth) {
		return getConciseBoundedDescription(resource, depth, false);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int, boolean)
	 */
	@Override
	public Model getConciseBoundedDescription(String resource, int depth, boolean withTypesForLeafs) {
		StringBuilder constructTemplate = new StringBuilder("?s0 ?p0 ?o0 .");
		for(int i = 1; i <= depth; i++){
			constructTemplate.append("?o").append(i-1).append(" ?p").append(i).append(" ?o").append(i).append(" .");
		}
		
		StringBuilder triplesTemplate = new StringBuilder("?s0 ?p0 ?o0 .");
		for(int i = 1; i <= depth; i++){
			triplesTemplate.append("OPTIONAL{").append("?o").append(i-1).append(" ?p").append(i).append(" ?o").append(i).append(" .");
		}
		if(resolveBlankNodes){
			triplesTemplate.append("?o").append(depth).append("((!<x>|!<y>)/:sameBlank)* ?x . ?x ?px ?ox .filter(!(?p in (:sameIri, :sameBlank)))");
		}
		for(int i = 1; i <= depth; i++){
			triplesTemplate.append("}");
		}

		ParameterizedSparqlString query = new ParameterizedSparqlString("prefix : <http://dl-learner.org/ontology/> " + "CONSTRUCT{" + constructTemplate + "}" + " WHERE {" + triplesTemplate + "}");
		query.setIri("s0", resource);
		System.out.println(query);
		QueryExecution qe = qef.createQueryExecution(query.toString());
		Model cbd = qe.execConstruct();
		qe.close();
		return cbd;
	}
	
	@Override
	public void addPropertiesToIgnore(Set<String> properties) {
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#setRestrictToNamespaces(java.util.List)
	 */
	@Override
	public void setAllowedPropertyNamespaces(Set<String> namespaces) {
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#addAllowedObjectNamespaces(java.util.Set)
	 */
	@Override
	public void addAllowedObjectNamespaces(Set<String> namespaces) {
	}

}
