/**
 * 
 */
package org.dllearner.algorithms.qtl.util;

import java.net.URL;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLProperty;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import com.hp.hpl.jena.query.QueryExecution;

/**
 * @author Lorenz Buehmann
 *
 */
public class InformativenessMeasures {
	

	private QueryExecutionFactory qef;

	public InformativenessMeasures(QueryExecutionFactory qef) {
		this.qef = qef;
	}
	
	public double getInverseTripleFrequency(OWLProperty property) {
		// total number of triples
		String query = "SELECT (COUNT(*) AS ?cnt) WHERE {?s ?p ?o .}";
		QueryExecution qe = qef.createQueryExecution(query);
		int total = qe.execSelect().next().getLiteral("cnt").getInt();
		qe.close();
		
		// number of triples with predicate
		query = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o .}", property.toStringID());
		qe = qef.createQueryExecution(query);
		int frequency = qe.execSelect().next().getLiteral("cnt").getInt();
		qe.close();
		
		double itf = Math.log(total / (double) frequency);
		
		return itf;
	}
	
	public double getOutgoingPredicateFrequency(OWLIndividual individual, OWLProperty property) {
		String query = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {<%s> <%s> ?o .}", individual.toStringID(), property.toStringID());
		QueryExecution qe = qef.createQueryExecution(query);
		int pf = qe.execSelect().next().getLiteral("cnt").getInt();
		qe.close();
		
		return pf;
	}
	
	public double getIncomingPredicateFrequency(OWLIndividual individual, OWLProperty property) {
		String query = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> <%s> .}", property.toStringID(), individual.toStringID());
		QueryExecution qe = qef.createQueryExecution(query);
		int pf = qe.execSelect().next().getLiteral("cnt").getInt();
		qe.close();
		
		return pf;
	}
	
	public static void main(String[] args) throws Exception {
		SparqlEndpointKS ks = new SparqlEndpointKS(new SparqlEndpoint(
					new URL("http://sake.informatik.uni-leipzig.de:8890/sparql"), 
					"http://dbpedia.org"));
		ks.init();
		
		OWLProperty property = new OWLObjectPropertyImpl(IRI.create("http://dbpedia.org/ontology/birthPlace"));
		
		double itf = new InformativenessMeasures(ks.getQueryExecutionFactory()).getInverseTripleFrequency(property);
		
		System.out.println(itf);
	}

}
