/**
 * 
 */
package org.dllearner.algorithms.qtl.util;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Lorenz Buehmann
 *
 */
public class StopURIsRDFS {
	static final Set<String> uris = ImmutableSet.of(
			RDFS.label.getURI(),
			RDFS.comment.getURI(),
			RDFS.isDefinedBy.getURI()
			);
	
	public static Set<String> get() {
		return uris;
	}
}
