/**
 * 
 */
package org.dllearner.algorithms.qtl.util;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * @author Lorenz Buehmann
 *
 */
public class StopURIsSKOS {
	static final Set<String> uris = ImmutableSet.of(
			"http://www.w3.org/2004/02/skos/core#Concept",
			"http://www.w3.org/2004/02/skos/core#prefLabel",
			"http://www.w3.org/2004/02/skos/core#altLabel"
			);
	
	public static Set<String> get() {
		return uris;
	}
}
