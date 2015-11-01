package org.dllearner.kb.sparql;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;


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
	Model getConciseBoundedDescription(String resourceURI);
	
	/**
	 * @return the CBD of given depth for the given resource
	 */
	Model getConciseBoundedDescription(String resourceURI, int depth);
	
	/**
	 * @return the CBD of given depth for the given resource with optionally additional 
	 * information about the types of leaf nodes
	 */
	Model getConciseBoundedDescription(String resourceURI, int depth, boolean withTypesForLeafs);
	
	void addAllowedPropertyNamespaces(Set<String> namespaces);
	
	void addAllowedObjectNamespaces(Set<String> namespaces);
	
	void addPropertiesToIgnore(Set<String> properties);
	
	void setRecursionDepth(int maxRecursionDepth);

	
	
}
