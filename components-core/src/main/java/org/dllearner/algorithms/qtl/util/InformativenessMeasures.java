/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

import org.apache.jena.query.QueryExecution;

/**
 * Contains a set of measures to compute the informativeness of a triple based on the work proposed in 
 * REWOrD: Semantic Relatedness, AAAI 2012.
 * 
 * 
 * @author Lorenz Buehmann
 *
 */
public class InformativenessMeasures {
	

	private QueryExecutionFactory qef;

	public InformativenessMeasures(QueryExecutionFactory qef) {
		this.qef = qef;
	}
	
	/**
	 * <p>
	 * The inverse triple frequency ITF(p), considers how many times a predicate
	 * is used in some RDF triple w.r.t. the total number of triples, and is
	 * defined as: 
	 * </p>
	 * 
	 * <p><code>log(|T|/|T(p)|)</code></p>
	 * 
	 * <p>
	 * where |T| is the total number of triples in the knowledge base and |T(p)|
	 * the total number of triples having p as a predicate.
	 * </p>
	 * 
	 * @param property the predicate
	 * @return the inverse triple frequency
	 */
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
	
	/**
	 * Predicate Frequency(PF) quantifies the informativeness of a predicate p
	 * in the context of a URI u. With context we mean the RDF triples where p
	 * and u appear together.
	 * 
	 * @param individual
	 * @param property the predicate
	 * @param outgoing
	 * @return
	 */
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
					new URL("http://dbpedia.org/sparql"), 
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
