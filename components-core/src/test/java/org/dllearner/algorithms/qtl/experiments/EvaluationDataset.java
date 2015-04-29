/**
 * 
 */
package org.dllearner.algorithms.qtl.experiments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.SparqlEndpointKS;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * Contains a knowledge base and a set of SPARQL queries.
 * @author Lorenz Buehmann
 *
 */
public class EvaluationDataset {

	SparqlEndpointKS ks;
	String baseIRI;
	PrefixMapping prefixMapping;
	
	AbstractReasonerComponent reasoner;
	
	List<String> sparqlQueries;
	List<Filter<Statement>> queryTreeFilters = new ArrayList<Filter<Statement>>();
	
	public SparqlEndpointKS getKS() {
		return ks;
	}
	
	public AbstractReasonerComponent getReasoner() {
		return reasoner;
	}
	
	public String getBaseIRI() {
		return baseIRI;
	}
	
	public PrefixMapping getPrefixMapping() {
		return prefixMapping;
	}
	
	public List<String> getSparqlQueries() {
		return sparqlQueries;
	}
	
	public List<Filter<Statement>> getQueryTreeFilters() {
		return queryTreeFilters;
	}
}
