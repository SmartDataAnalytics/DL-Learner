/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.properties.SubPropertyOfAxiomLearner;
import org.dllearner.core.AxiomLearningAlgorithm;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.config.ConfigHelper;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Evaluation of enrichment algorithms on DBpedia (Live).
 * 
 * @author Jens Lehmann
 *
 */
public class EnrichmentEvaluation {

	private static Logger logger = Logger.getLogger(EnrichmentEvaluation.class);
	
	// max. execution time for each learner for each entity
	private int maxExecutionTimeInSeconds = 10;
	
	// number of axioms which will be learned/considered (only applies to
	// some learners)
	private int nrOfAxiomsToLearn = 10;
	
	// can be used to only evaluate a part of DBpedia
	private int maxObjectProperties = 3;
	private int maxDataProperties = 3;
	private int maxClasses = 3;
	private List<Class<? extends AxiomLearningAlgorithm>> objectPropertyAlgorithms;
	
	
	public EnrichmentEvaluation() {
		objectPropertyAlgorithms = new LinkedList<Class<? extends AxiomLearningAlgorithm>>();
		objectPropertyAlgorithms.add(SubPropertyOfAxiomLearner.class);
		
	}
	
	public void start() {
		
		ComponentManager cm = ComponentManager.getInstance();
		
		// create DBpedia Live knowledge source
		SparqlEndpoint se = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
		
		Set<ObjectProperty> properties = getAllObjectProperties(se);
		
		SparqlEndpointKS ks = new SparqlEndpointKS(se);
		
		for(Class<? extends AxiomLearningAlgorithm> algorithmClass : objectPropertyAlgorithms) {
			int objectProperties = 0;
			for(ObjectProperty property : properties) {
//				SubPropertyOfAxiomLearner learner = new SubPropertyOfAxiomLearner(ks);
				AxiomLearningAlgorithm learner = cm.learningAlgorithm(algorithmClass, ks);
				ConfigHelper.configure(learner, "propertyToDescribe", property.toString());
				ConfigHelper.configure(learner, "maxExecutionTimeInSeconds", maxExecutionTimeInSeconds);
				
				
//				learner.setPropertyToDescribe(property);
//				learner.setMaxExecutionTimeInSeconds(10);
				System.out.println("Applying " + ComponentManager.getName(learner) + " on " + property + " ... ");
				learner.start();
				List<EvaluatedAxiom> learnedAxioms = learner.getCurrentlyBestEvaluatedAxioms(nrOfAxiomsToLearn);
				for(EvaluatedAxiom learnedAxiom : learnedAxioms) {
					// TODO: put this in some data structure
					System.out.println(learnedAxiom);
				}
				objectProperties++;
				if(objectProperties > maxObjectProperties) {
					break;
				}
			}
		} 
		
	}
	
	private void getAllClasses() {
	}
	
	private Set<ObjectProperty> getAllObjectProperties(SparqlEndpoint se) {
		Set<ObjectProperty> properties = new TreeSet<ObjectProperty>();
		String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p WHERE {?p a owl:ObjectProperty}";
		SparqlQuery sq = new SparqlQuery(query, se);
		// Claus' API
//        Sparqler x = new SparqlerHttp(se.getURL().toString());
//        SelectPaginated q = new SelectPaginated(x, , 1000);
		ResultSet q = sq.send();
        while(q.hasNext()) {
            QuerySolution qs = q.next();
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
