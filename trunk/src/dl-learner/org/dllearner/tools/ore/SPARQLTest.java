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
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.ComponentManager;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.utilities.datastructures.SetManipulation;

import com.hp.hpl.jena.query.ResultSetRewindable;

/**
 * Test class for SPARQL mode.
 * 
 * @author Lorenz Buehmann
 * 
 */
public class SPARQLTest {

	public static void main(String[] args) throws MalformedURLException {
		String exampleClass = "http://dbpedia.org/ontology/Place";

		ComponentManager cm = ComponentManager.getInstance();

		SparqlEndpoint endPoint = SparqlEndpoint.getEndpointDBpedia();

		SPARQLTasks task = new SPARQLTasks(endPoint);

		SortedSet<String> examples = new TreeSet<String>();
		SortedSet<String> superClasses = task.getSuperClasses(exampleClass, 2);
		for (String sup : superClasses) {
			examples.addAll(task.retrieveInstancesForClassDescription("\""
					+ sup + "\"", 20));

		}

		SortedSet<String> posExamples = SetManipulation.stableShrink(examples,
				20);

		SparqlKnowledgeSource ks = cm
				.knowledgeSource(SparqlKnowledgeSource.class);
		ks.getConfigurator().setUrl(endPoint.getURL());
		ks.getConfigurator().setInstances(posExamples);
		ks.getConfigurator().setDissolveBlankNodes(false);
		ks.init();
		// ReasonerComponent reasoner = cm.reasoner(FastInstanceChecker.class,
		// ks);
		// reasoner.init();
		// ClassLearningProblem lp =
		// cm.learningProblem(ClassLearningProblem.class, reasoner);
		// lp.getConfigurator().setClassToDescribe(new URL(exampleClass));
		// lp.init();
		// LearningAlgorithm la = cm.learningAlgorithm(CELOE.class, lp,
		// reasoner);
		// la.init();
		//
		// la.start();

	}
}
