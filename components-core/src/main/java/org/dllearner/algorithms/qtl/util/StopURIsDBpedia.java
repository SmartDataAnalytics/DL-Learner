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
public class StopURIsDBpedia {
	static final Set<String> uris = ImmutableSet.of(
			"http://dbpedia.org/ontology/abstract",
			"http://dbpedia.org/ontology/thumbnail",
			"http://www.w3.org/ns/prov#wasDerivedFrom",
			"http://dbpedia.org/ontology/wikiPageExternalLink",
			"http://dbpedia.org/ontology/wikiPageID",
			"http://dbpedia.org/ontology/wikiPageInLinkCount",
			"http://dbpedia.org/ontology/wikiPageOutLinkCount",
			"http://dbpedia.org/ontology/wikiPageRevisionID"
			);
	
	public static Set<String> get() {
		return uris;
	}
}
