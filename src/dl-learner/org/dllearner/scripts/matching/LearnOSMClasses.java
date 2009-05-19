/**
 * Copyright (C) 2007-2009, Jens Lehmann
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
package org.dllearner.scripts.matching;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.kb.manipulator.StringToResource;
import org.dllearner.kb.manipulator.Rule.Months;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.FastInstanceChecker;

/**
 * Uses learning algorithms to learn class definitions for DBpedia
 * classes in LinkedGeoData. As a final result, we can classify most
 * objects in LinkedGeoData using the DBpedia ontology. 
 * 
 * @author Jens Lehmann
 *
 */
public class LearnOSMClasses {

	private static SparqlEndpoint dbpediaEndpoint = SparqlEndpoint.getEndpointLOCALDBpedia();
	private static SparqlEndpoint geoDataEndpoint = SparqlEndpoint.getEndpointLOCALGeoData();	
	
	public static void main(String args[]) throws IOException, DataFormatException, LearningProblemUnsupportedException, ComponentInitException {
		File matchesFile = new File("log/geodata/owlsameas_en.dat");
		Map<URI,URI> matches = Utility.getMatches(matchesFile);
		
		SPARQLTasks dbpedia = new SPARQLTasks(new Cache("cache/matching"),dbpediaEndpoint);
		Set<String> positives = new TreeSet<String>();
		Set<String> negatives = new TreeSet<String>();
		
		// loop through all matches
		for(Entry<URI,URI> match : matches.entrySet()) {
			URI dbpediaURI = match.getKey();
			URI lgdURI = match.getValue();
			// test whether the dbpediaURI is a city
			String query = "ASK {<"+dbpediaURI+"> a <http://dbpedia.org/ontology/City>}";
			boolean isCity = dbpedia.ask(query);
//			System.out.println(isCity + " " + lgdURI);
			if(isCity) {
				positives.add(lgdURI.toString());
			} else {
				negatives.add(lgdURI.toString());
			}
		}
		
		Set<String> instances = new TreeSet<String>();
		instances.addAll(positives);
		instances.addAll(negatives);
		
		System.out.println(instances.size() + " instances - " + positives.size() + " positive examples");
		
		// plug together DL-Learner components
		ComponentManager cm = ComponentManager.getInstance();
		
		SparqlKnowledgeSource ks = cm.knowledgeSource(SparqlKnowledgeSource.class);
		ks.getConfigurator().setInstances(instances);
		ks.getConfigurator().setPredefinedEndpoint("LOCALGEODATA");
		ks.getConfigurator().setSaveExtractedFragment(true);
		ks.getManipulator().addRule(new StringToResource(Months.DECEMBER,"http://linkedgeodata.org/vocabulary", 40));
		ks.init();
		
		ReasonerComponent reasoner = cm.reasoner(FastInstanceChecker.class, ks);
		reasoner.init();
		
		PosOnlyLP lp = cm.learningProblem(PosOnlyLP.class, reasoner);
		lp.getConfigurator().setPositiveExamples(positives);
		lp.init();
		
		CELOE celoe = cm.learningAlgorithm(CELOE.class, lp, reasoner);
		celoe.getConfigurator().setUseHasValueConstructor(true);
		celoe.getConfigurator().setValueFrequencyThreshold(3);
		celoe.init();
		
		// execute algorithm
		celoe.start();
//		Set<EvaluatedDescriptionPosOnly> solutions = (Set<EvaluatedDescriptionPosOnly>) algorithm.getCurrentlyBestEvaluatedDescriptions();
	}
	
}
