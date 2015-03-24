/**
 * 
 */
package org.dllearner.algorithms.qtl.util;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * @author Lorenz Buehmann
 *
 */
public class StopURIsOWL {
	static final Set<String> uris = ImmutableSet.of(
			OWL.sameAs.getURI(),
			OWL.Class.getURI(),
			OWL.Thing.getURI()
			);
	
	public static Set<String> get() {
		return uris;
	}
}
