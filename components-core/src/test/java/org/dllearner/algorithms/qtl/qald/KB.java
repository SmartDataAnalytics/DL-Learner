package org.dllearner.algorithms.qtl.qald;

import java.util.List;
import java.util.Map;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.SparqlEndpointKS;

import com.hp.hpl.jena.shared.PrefixMapping;

class KB {
	
	static String id;
	SparqlEndpointKS ks;
	AbstractReasonerComponent reasoner;
	Map<String, String> prefixes;
	List<String> questionFiles;
	String baseIRI;
	PrefixMapping prefixMapping;
	
	public KB(){};

	public KB(SparqlEndpointKS ks, AbstractReasonerComponent reasoner, Map<String, String> prefixes,
			List<String> questionFiles) {
		this.ks = ks;
		this.reasoner = reasoner;
		this.prefixes = prefixes;
		this.questionFiles = questionFiles;
	}
}