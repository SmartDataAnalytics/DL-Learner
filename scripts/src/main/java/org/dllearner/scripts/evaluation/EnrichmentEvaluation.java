/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.scripts.evaluation;

import java.util.Set;
import java.util.TreeSet;

import org.aksw.commons.sparql.sparqler.SelectPaginated;
import org.aksw.commons.sparql.sparqler.Sparqler;
import org.aksw.commons.sparql.sparqler.SparqlerHttp;
import org.dllearner.algorithms.properties.SubPropertyOfAxiomLearner;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.QuerySolution;

/**
 * Evaluation of enrichment algorithms on DBpedia (Live).
 * 
 * @author Jens Lehmann
 *
 */
public class EnrichmentEvaluation {

	// max. execution time for each learner for each entity
	private int maxExecutionTimeInSeconds = 10;
	
	public EnrichmentEvaluation() {

	}
	
	public void start() {
		
		// create DBpedia Live knowledge source
		SparqlEndpoint se = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
		
		Set<ObjectProperty> properties = getAllObjectProperties(se);
		System.out.println(properties);
		
		SparqlEndpointKS ks = new SparqlEndpointKS(se);
		
		SubPropertyOfAxiomLearner learner = new SubPropertyOfAxiomLearner(ks);
		learner.setMaxExecutionTimeInSeconds(10);
		
	}
	
	private void getAllClasses() {


		
	}
	
	private Set<ObjectProperty> getAllObjectProperties(SparqlEndpoint se) {
		Set<ObjectProperty> properties = new TreeSet<ObjectProperty>();
        Sparqler x = new SparqlerHttp(se.getURL().toString());
        SelectPaginated q = new SelectPaginated(x, "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p WHERE {?p a owl:ObjectProperty}", 1000);
        while(q.hasNext()) {
            QuerySolution qs = q.next();
            System.out.println(qs);
            properties.add(new ObjectProperty(qs.getResource("p").getURI()));
        }
		return properties;		
	}
	
	public void printResultsPlain() {
		
	}
	
	public void printResultsLaTeX() {
		
	}
	
	public static void main(String[] args) {
		EnrichmentEvaluation ee = new EnrichmentEvaluation();
		ee.start();
		ee.printResultsPlain();
	}
	
}
