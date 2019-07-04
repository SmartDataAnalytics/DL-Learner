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

import org.apache.jena.rdf.model.Model;

import java.util.Set;

/**
 * According to the definition at http://www.w3.org/Submission/CBD/
 * <p>
 * <p>...a concise bounded description of a resource in terms of
 * an RDF graph, as a general and broadly optimal unit of specific knowledge
 * about that resource to be utilized by, and/or interchanged between, semantic
 * web agents.
 * </p>
 * <p>
 * <p>
 * Given a particular node in a particular RDF graph, a <em>concise bounded
 * description</em> is a subgraph consisting of those statements which together
 * constitute a focused body of knowledge about the resource denoted by that
 * particular node. The precise nature of that subgraph will hopefully become
 * clear given the definition, discussion and example provided below.
 * </p>
 * <p>
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
 */
public interface ConciseBoundedDescriptionGenerator {

    String TP = "%s %s %s .\n";
    default String triplePattern(String s, String p, String o) {
        return String.format(TP, s, p, o);
    }

    /**
     * Computes the CBD (of depth 1) for the given resource.
     *
     * @param resource the resource URI
     * @return the CBD
     */
    default Model getConciseBoundedDescription(String resource) {
        return getConciseBoundedDescription(resource, 1);
    }

    /**
     * Computes the CBD of given depth for the given resource.
     *
     * @param resource the resource URI
     * @param depth    the max. depth of the CBD
     * @return the CBD
     */
    default Model getConciseBoundedDescription(String resource, int depth) {
        return getConciseBoundedDescription(resource, depth, false);
    }

    /**
     * Computes the CBD of given depth for the given resource. Optionally, additional
     * information about the types for the leaf nodes is retrieved.
     *
     * @param resource          the resource URI
     * @param depth             the max. depth of the CBD
     * @param withTypesForLeafs whether to get the types of the leaf nodes
     * @return the CBD
     */
    Model getConciseBoundedDescription(String resource, int depth, boolean withTypesForLeafs);

    /**
     * Computes the CBDs (of given depth 1) for the given resources and puts them into a single model.
     *
     * @param resources the resource URIs
     * @return the CBDs in a single model
     */
    default Model getConciseBoundedDescription(Set<String> resources) {
        return getConciseBoundedDescription(resources, 1);
    }

    /**
     * Computes the CBDs of given depth for the given resources and puts them into a single model.
     *
     * @param resources the resource URIs
     * @param depth     the max. depth of the CBDs
     * @return the CBDs in a single model
     */
    default Model getConciseBoundedDescription(Set<String> resources, int depth) {
        return getConciseBoundedDescription(resources, depth, false);
    }

    /**
     * Computes the CBD of given depth for the given resources and puts them into a single model. Optionally, additional
     * information about the types for the leaf nodes is retrieved.
     *
     * @param resources         the resource URIs
     * @param depth             the max. depth of the CBDs
     * @param withTypesForLeafs whether to get the types of the leaf nodes
     *
     * @return the CBDs in a single model
     */
    default Model getConciseBoundedDescription(Set<String> resources, int depth, boolean withTypesForLeafs) {
        return resources.stream()
                .map(r -> getConciseBoundedDescription(r, depth, withTypesForLeafs))
                .reduce(Model::union)
                .orElseThrow(() -> new RuntimeException("Failed to compute CBD for resources " + resources));

    }


    /**
     * Set the property namespaces allowed to occur in triples of the generated CBD. Filtering can happen either remotely
     * via SPARQL queries or might be implemented locally via post-processing on the retrieved triples.
     *
     * @param namespaces the allowed property namespaces
     */
    void setAllowedPropertyNamespaces(Set<String> namespaces);

    /**
     * Set the allowed namespaces for resources occuring in object position of the retrieved triples.
     * Filtering can happen either remotely via SPARQL queries or might be implemented locally via post-processing
     * on the retrieved triples.
     *
     * @param namespaces the allowed namespaces
     */
    void setAllowedObjectNamespaces(Set<String> namespaces);

    /**
     * Set the allowed namespaces for classes occuring in object position of the retrieved triples.
     * Filtering can happen either remotely via SPARQL queries or might be implemented locally via post-processing
     * on the retrieved triples.
     *
     * @param namespaces the allowed namespaces
     */
    default void setAllowedClassNamespaces(Set<String> namespaces){
        throw new UnsupportedOperationException("Method not implemented for class " + this.getClass());
    };

    /**
     * Set the properties allowed to occur in triples of the generated CBD. Filtering can happen either remotely
     * via SPARQL queries or might be implemented locally via post-processing on the retrieved triples.
     *
     * @param properties the ignored properties
     */
    void setIgnoredProperties(Set<String> properties);


}
