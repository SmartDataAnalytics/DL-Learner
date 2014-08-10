/**
 * 
 */
package org.dllearner.algorithms.pattern;

import org.semanticweb.owlapi.model.OWLClass;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Lorenz Buehmann
 *
 */
public interface FragmentExtractor {
	
	/**
	 * @param cls
	 * @param maxFragmentDepth
	 * @return
	 */
	Model extractFragment(OWLClass cls, int maxFragmentDepth);

}
