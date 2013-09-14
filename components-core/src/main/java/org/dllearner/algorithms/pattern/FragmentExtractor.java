/**
 * 
 */
package org.dllearner.algorithms.pattern;

import org.dllearner.core.owl.NamedClass;

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
	Model extractFragment(NamedClass cls, int maxFragmentDepth);

}
