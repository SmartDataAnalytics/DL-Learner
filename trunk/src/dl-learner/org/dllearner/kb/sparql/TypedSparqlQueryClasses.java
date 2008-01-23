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
package org.dllearner.kb.sparql;

import java.net.URI;
import java.util.Set;

import org.dllearner.kb.sparql.configuration.Configuration;
import org.dllearner.kb.sparql.query.CachedSparqlQuery;
import org.dllearner.utilities.StringTuple;

/**
 * Can execute different queries.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class TypedSparqlQueryClasses extends TypedSparqlQuery implements
		TypedSparqlQueryInterface {

	public TypedSparqlQueryClasses(Configuration configuration) {
		super(configuration);
	}

	/*
	 * Special TypedSparqlQuery which returns superclasses of classes
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.kb.sparql.TypedSparqlQuery#getTupelForResource(java.net.URI)
	 */
	@Override
	public Set<StringTuple> getTupelForResource(URI uri) {
		// TODO remove
		String a = "predicate";
		String b = "object";
		// getQuery for all super classes of classes only
		String sparqlQueryString = "SELECT ?predicate ?object " + "WHERE {"
				+ "<" + uri.toString() + "> ?predicate ?object;"
				+ "a ?object . "
				+ " FILTER (!regex(str(?object),'http://xmlns.com/foaf/0.1/'))"
				+ "}";

		CachedSparqlQuery csq = new CachedSparqlQuery(configuration
				.getSparqlEndpoint(), cache, uri.toString(), sparqlQueryString);

		String xml = csq.getAsXMLString();
		// TODO needs to be changed to new format
		Set<StringTuple> s = processResult(xml, a, b);
		try {
			// System.out.println("retrieved " + s.size() + " tupels\n");
		} catch (Exception e) {
		}
		return s;
		// return cachedSparql(u, sparql, "predicate", "object");

	}

}
