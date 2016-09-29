/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 * <p>
 * This file is part of DL-Learner.
 * <p>
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.kb.sparql;

import java.util.Set;

import org.apache.jena.rdf.model.Model;

/**
 * According to the definition at http://www.w3.org/Submission/CBD/
 *
 * <p>...a concise bounded description of a resource in terms of
 * an RDF graph, as a general and broadly optimal unit of specific knowledge
 * about that resource to be utilized by, and/or interchanged between, semantic
 * web agents.
 * </p>
 *
 * <p>
 * Given a particular node in a particular RDF graph, a <em>concise bounded
 * description</em> is a subgraph consisting of those statements which together
 * constitute a focused body of knowledge about the resource denoted by that
 * particular node. The precise nature of that subgraph will hopefully become
 * clear given the definition, discussion and example provided below.
 * </p>
 *
 * <p>
 * Optimality is, of course, application dependent and it is not presumed that a
 * concise bounded description is an optimal form of description for every
 * application; however, it is presented herein as a reasonably general and
 * broadly optimal form of description for many applications, and unless
 * otherwise warranted, constitutes a reasonable default response to the request
 * "tell me about this resource".
 * </p>
 *
 * @author Lorenz Buehmann
 *
 */
public interface ConciseBoundedDescriptionGenerator {

	/**
	 * @return the CBD of depth 1 for the given resource
	 */
	default Model getConciseBoundedDescription(String resourceURI) {
		return getConciseBoundedDescription(resourceURI, 1);
	}

	/**
	 * @return the CBD of given depth for the given resource
	 */
	default Model getConciseBoundedDescription(String resourceURI, int depth) {
		return getConciseBoundedDescription(resourceURI, depth, false);
	}

	/**
	 * Computes the CBD of given depth for the given resource. Optionally, additional
	 * information about the types for the leaf nodes is retrieved.
	 *
	 * @return the CBD
	 */
	Model getConciseBoundedDescription(String resourceURI, int depth, boolean withTypesForLeafs);

	void addAllowedPropertyNamespaces(Set<String> namespaces);

	void addAllowedObjectNamespaces(Set<String> namespaces);

	void addPropertiesToIgnore(Set<String> properties);
}
