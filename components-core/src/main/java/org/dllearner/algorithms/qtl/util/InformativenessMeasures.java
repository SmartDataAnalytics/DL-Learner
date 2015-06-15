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

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
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
	
	public double getPredicateFrequency(OWLIndividual individual, OWLProperty property, boolean outgoing) {
		String query = outgoing ? "SELECT (COUNT(*) AS ?cnt) WHERE {<%s> <%s> ?o .}" : "SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> <%s> .}";
		query = String.format(query, individual.toStringID(), property.toStringID());
		QueryExecution qe = qef.createQueryExecution(query);
		int pf = qe.execSelect().next().getLiteral("cnt").getInt();
		qe.close();
		
		return pf;
	}
	
	public double getPF_ITF(OWLIndividual individual, OWLProperty property, boolean outgoing) {
		double itf = getInverseTripleFrequency(property);
		double pf = getPredicateFrequency(individual, property, outgoing);
		return pf * itf;
	}
	
	public static void main(String[] args) throws Exception {
		SparqlEndpointKS ks = new SparqlEndpointKS(new SparqlEndpoint(
					new URL("http://sake.informatik.uni-leipzig.de:8890/sparql"), 
					"http://dbpedia.org"));
		ks.init();
		
		OWLProperty p1 = new OWLObjectPropertyImpl(IRI.create("http://dbpedia.org/ontology/birthPlace"));
		OWLProperty p2 = new OWLObjectPropertyImpl(IRI.create("http://dbpedia.org/ontology/genre"));
		
		OWLIndividual ind1 = new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Kid_Canaveral"));
		
		InformativenessMeasures informativenessMeasures = new InformativenessMeasures(ks.getQueryExecutionFactory());
		
		double itf1 = informativenessMeasures.getInverseTripleFrequency(p1);
		System.out.println("itf(" + p1 + ") = " + itf1);
		
		double itf2 = informativenessMeasures.getInverseTripleFrequency(p2);
		System.out.println("itf(" + p2 + ") = " + itf2);
		
		double pf1_out = informativenessMeasures.getPredicateFrequency(ind1, p1, true);
		double pf1_in = informativenessMeasures.getPredicateFrequency(ind1, p1, false);
		System.out.println("pf_out(" + ind1 + "," + p1 + ") = " + pf1_out);
		System.out.println("pf_in(" + ind1 + "," + p1 + ") = " + pf1_in);
		
		double pf2_out = informativenessMeasures.getPredicateFrequency(ind1, p2, true);
		double pf2_in = informativenessMeasures.getPredicateFrequency(ind1, p2, false);
		System.out.println("pf_out(" + ind1 + "," + p2 + ") = " + pf2_out);
		System.out.println("pf_in(" + ind1 + "," + p2 + ") = " + pf2_in);
	}

}
