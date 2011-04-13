package org.dllearner.test;

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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.fuzzydll.FuzzyCELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.configurators.CELOEConfigurator;
import org.dllearner.core.options.fuzzydll.FuzzyExample;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.fuzzydll.FuzzyPosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.fuzzydll.FuzzyOWLAPIReasoner;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.SortedSetTuple;

/**
 * A script, which learns definitions / super classes of classes in the DBpedia ontology.
 * 
 * @author Jens Lehmann
 *
 */
public class FuzzyDLLTest {
	
	String[] posEx = {
			"http://example.com/foodItems.owl#item0.99-100",
			"http://example.com/foodItems.owl#item1.00-110",
			"http://example.com/foodItems.owl#item1.15-100",
			"http://example.com/foodItems.owl#item1.20-100"
	};
	String[] negEx = {
			"http://example.com/foodItems.owl#item1.50-250",
			"http://example.com/foodItems.owl#item3.75-800"
	};
	
	public Description learn() throws LearningProblemUnsupportedException, IOException, ComponentInitException {	
		
		//
		// positive and negative examples
		//
		SortedSet<String> positiveExamples = new TreeSet<String>();
		SortedSet<String> negativeExamples = new TreeSet<String>();
		for (int i=0; i<posEx.length; i++) {
			positiveExamples.add(posEx[i]);
		}
		for (int i=0; i<negEx.length; i++) {
			negativeExamples.add(negEx[i]);
		}
		
		//
		// fuzzy examples
		//
		SortedSet<Object> fuzzyExamples = new TreeSet<Object>();
		for (int i=0; i<posEx.length; i++) {
			fuzzyExamples.add(new FuzzyExample(posEx[i],1.0));
		}
		for (int i=0; i<negEx.length; i++) {
			fuzzyExamples.add(new FuzzyExample(negEx[i],0.0));
		}
		
		ComponentManager cm = ComponentManager.getInstance();
		
		OWLFile ks = cm.knowledgeSource(OWLFile.class);
		ks.getConfigurator().setUrl(new URL("file:///Users/josue/Documents/PhD/AKSW/ontologies/fuzzyFoodItems_v1.owl"));
		ks.init();

		//ReasonerComponent rc = cm.reasoner(OWLAPIReasoner.class, ks);
		ReasonerComponent rc = cm.reasoner(FuzzyOWLAPIReasoner.class, ks);
		rc.init();
		
		FuzzyPosNegLPStandard lp = cm.learningProblem(FuzzyPosNegLPStandard.class, rc);
		//PosNegLPStandard lp = cm.learningProblem(PosNegLPStandard.class, rc);
		lp.getConfigurator().setPositiveExamples(positiveExamples);
		lp.getConfigurator().setNegativeExamples(negativeExamples);
		lp.getConfigurator().setFuzzyExamples(fuzzyExamples);
		lp.init();
		
		FuzzyCELOE fc = cm.learningAlgorithm(FuzzyCELOE.class, lp, rc);
		//CELOE fc = cm.learningAlgorithm(CELOE.class, lp, rc);
//		Set<String> kkkkkkkkkk = new TreeSet<String>();
//		kkkkkkkkkk.add("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
//		fc.getConfigurator().setAllowedConcepts(kkkkkkkkkk);
//		Set<String> aaaaaaaaaa = new TreeSet<String>();
//		aaaaaaaaaa.add("Nothing");
//		fc.getConfigurator().setIgnoredConcepts(aaaaaaaaaa);
		fc.getConfigurator().setMaxClassDescriptionTests(50);
		fc.getConfigurator().setMaxExecutionTimeInSeconds(0);
		fc.getConfigurator().setUseDoubleDatatypes(false);
		fc.init();
		fc.start();		
		
		return fc.getCurrentlyBestDescription();
	}
	
	public static void main(String args[]) throws LearningProblemUnsupportedException, IOException, ComponentInitException {
		FuzzyDLLTest test = new FuzzyDLLTest();
		test.learn();
	}
	
}