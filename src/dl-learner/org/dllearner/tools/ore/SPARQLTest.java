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
package org.dllearner.tools.ore;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;

/**
 * Test class for SPARQL mode.
 * 
 * @author Lorenz Buehmann
 * 
 */
public class SPARQLTest {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws MalformedURLException {
		String exampleClass = "http://dbpedia.org/ontology/HistoricPlace";
		String exampleClassKBString = "\"" + exampleClass + "\"";
		
		ComponentManager cm = ComponentManager.getInstance();

		SparqlEndpoint endPoint = SparqlEndpoint.getEndpointDBpedia();
		
		
		
		String queryString = "SELECT DISTINCT ?class, ?label WHERE {" +
							"?class rdf:type owl:Class ." +
							"?class rdfs:label ?label .}" ;
							//"FILTER(regex(?label, '^$deinstring')) }";
		SPARQLTasks task = new SPARQLTasks(endPoint);
		System.out.println(task.queryAsSet(queryString, "label"));
//		SparqlQuery query = new SparqlQuery(queryString, endPoint);
//		query.send();
//		String json = query.getJson();
//		ResultSet rs = SparqlQuery.convertJSONtoResultSet(json);
//		Set<String> results = SPARQLTasks.getStringSetForVariableFromResultSet
//												(ResultSetFactory.makeRewindable(rs), "label");
//
//		System.out.println(results);
		

		AutomaticPositiveExampleFinderSPARQL pos = new AutomaticPositiveExampleFinderSPARQL(task);
		pos.makePositiveExamplesFromConcept(exampleClassKBString);
		
		SortedSet<String> allPosExamples = pos.getPosExamples();
		SortedSet<String> posExamples = SetManipulation.stableShrink(allPosExamples, 20);
		System.out.println(posExamples.size());
		System.out.println(posExamples);

	
//		AutomaticNegativeExampleFinderSPARQL neg = new AutomaticNegativeExampleFinderSPARQL(
//				posExamples, task, new TreeSet<String>());
//		neg.makeNegativeExamplesFromSuperClasses(exampleClass, 1000);
//		SortedSet<String> negExamples = neg.getNegativeExamples(20);
//		System.out.println(negExamples);

		SortedSet<String> instances = new TreeSet<String>(posExamples);
//		instances.addAll(negExamples);
		
		try {

			SparqlKnowledgeSource ks = cm.knowledgeSource(SparqlKnowledgeSource.class);
			cm.applyConfigEntry(ks, "predefinedEndpoint", "DBPEDIA");
			ks.getConfigurator().setInstances(instances);
//			ks.getConfigurator().setPredefinedFilter("YAGO");
			ks.init();
			ReasonerComponent reasoner = cm.reasoner(FastInstanceChecker.class, ks);
			reasoner.init();
			ClassLearningProblem lp = cm.learningProblem(ClassLearningProblem.class, reasoner);
			lp.getConfigurator().setClassToDescribe(new URL(exampleClass));
			lp.init();
			LearningAlgorithm la = cm.learningAlgorithm(CELOE.class, lp, reasoner);
			la.init();

			la.start();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LearningProblemUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
