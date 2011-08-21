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
 */

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

import java.io.IOException;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;
import org.dllearner.algorithms.fuzzydll.FuzzyCELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.options.fuzzydll.FuzzyExample;
import org.dllearner.core.owl.Description;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.fuzzydll.FuzzyPosNegLPStandard;
import org.dllearner.reasoning.fuzzydll.FuzzyOWLAPIReasoner;

/**
 * A script, which learns definitions / super classes of classes in the DBpedia ontology.
 * 
 * @author Jens Lehmann
 *
 */
public class FuzzyDLLTest_Trains_noFuzzyIndividuals {
	
	String[] posEx = {
			"http://www.example.com/fuzzyTrains.owl#east1",
			"http://www.example.com/fuzzyTrains.owl#east2"
	};
	String[] negEx = {
			"http://www.example.com/fuzzyTrains.owl#west6",
			"http://www.example.com/fuzzyTrains.owl#west7"
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
		ks.getConfigurator().setUrl(new URL("file", null, "../examples/fuzzydll/fuzzyTrains_v2.1a.owl"));
		ks.init();

//		ReasonerComponent rc = cm.reasoner(OWLAPIReasoner.class, ks);
		FuzzyOWLAPIReasoner rc = cm.reasoner(FuzzyOWLAPIReasoner.class, ks);
		rc.getConfigurator().setReasonerType("fuzzydl");
		rc.init();
		// System.out.println(rc.getClassHierarchy());
		
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
		fc.getConfigurator().setMaxClassDescriptionTests(1000);
		fc.getConfigurator().setMaxExecutionTimeInSeconds(0);
		fc.getConfigurator().setUseDoubleDatatypes(false);
		fc.getConfigurator().setUseCardinalityRestrictions(false);
		fc.getConfigurator().setWriteSearchTree(true);
		fc.getConfigurator().setSearchTreeFile("log/searchTreeFuzzy.txt");
		fc.getConfigurator().setNoisePercentage(10);
		fc.init();
		fc.start();		
		
		return fc.getCurrentlyBestDescription();
	}
	
	public static void main(String args[]) throws LearningProblemUnsupportedException, IOException, ComponentInitException {
		long start = System.currentTimeMillis();

		FuzzyDLLTest_Trains_noFuzzyIndividuals test = new FuzzyDLLTest_Trains_noFuzzyIndividuals();
		test.learn();
		
		System.err.println("running time (s) = " + (System.currentTimeMillis() - start)/1000);

	}
	
}