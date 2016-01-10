package org.dllearner.algorithms.pattern;

import org.semanticweb.owlapi.model.OWLClass;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Lorenz Buehmann
 *
 */
public interface FragmentExtractor {
	
	/**
	 * Extracts a fragment of the knowledge base for the given class C with a max. depth of of triples starting from
	 * instances of C.
	 * @param cls the class
	 * @param maxFragmentDepth the maximum depth
	 * @return the fragment
	 */
	Model extractFragment(OWLClass cls, int maxFragmentDepth);

}
