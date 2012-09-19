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

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.options.fuzzydll.FuzzyExample;
import org.dllearner.core.owl.Description;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Helper;

/**
 * A script, which learns definitions / super classes of classes in the DBpedia ontology.
 * 
 * @author Jens Lehmann
 *
 */
public class FuzzyDLLTest_noFuzzyTrains {
	
	String[] negEx = {
			"http://www.example.com/fuzzyTrains.owl#east1",
			"http://www.example.com/fuzzyTrains.owl#east2",
			"http://www.example.com/fuzzyTrains.owl#east3",
			"http://www.example.com/fuzzyTrains.owl#east4",
			"http://www.example.com/fuzzyTrains.owl#east5"
	};
	String[] posEx = {
			"http://www.example.com/fuzzyTrains.owl#west6",
			"http://www.example.com/fuzzyTrains.owl#west7",
			"http://www.example.com/fuzzyTrains.owl#west8",
			"http://www.example.com/fuzzyTrains.owl#west9",
			"http://www.example.com/fuzzyTrains.owl#west0"
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
		// ks.getConfigurator().setUrl(new URL("file:///Users/josue/Documents/PhD/AKSW/ontologies/fuzzyTrains/fuzzyTrains_v1.0.owl"));
		ks.setURL(new URL("file", null, "../examples/fuzzydll/noFuzzyTrains_v1.5.owl"));
		ks.init();

		AbstractReasonerComponent rc = cm.reasoner(OWLAPIReasoner.class, ks);
		//ReasonerComponent rc = cm.reasoner(FuzzyOWLAPIReasoner.class, ks);
		rc.init();
		System.out.println(rc.getClassHierarchy());
		
		//FuzzyPosNegLPStandard lp = cm.learningProblem(FuzzyPosNegLPStandard.class, rc);
		PosNegLPStandard lp = cm.learningProblem(PosNegLPStandard.class, rc);
		lp.setPositiveExamples(Helper.getIndividualSet(positiveExamples));
		lp.setNegativeExamples(Helper.getIndividualSet(negativeExamples));
		//lp.getConfigurator().setFuzzyExamples(fuzzyExamples);
		lp.init();
		//FuzzyCELOE fc = cm.learningAlgorithm(FuzzyCELOE.class, lp, rc);
		CELOE fc = cm.learningAlgorithm(CELOE.class, lp, rc);
//		Set<String> kkkkkkkkkk = new TreeSet<String>();
//		kkkkkkkkkk.add("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
//		fc.getConfigurator().setAllowedConcepts(kkkkkkkkkk);
//		Set<String> aaaaaaaaaa = new TreeSet<String>();
//		aaaaaaaaaa.add("Nothing");
//		fc.getConfigurator().setIgnoredConcepts(aaaaaaaaaa);
		fc.setMaxClassDescriptionTests(1000);
		fc.setMaxExecutionTimeInSeconds(0);
		((RhoDRDown)fc.getOperator()).setUseDoubleDatatypes(false);
		((RhoDRDown)fc.getOperator()).setUseCardinalityRestrictions(false);
		fc.setWriteSearchTree(true);
		fc.setSearchTreeFile("log/searchTreeNonFuzzy.txt");
		fc.init();
		fc.start();		
		
		return fc.getCurrentlyBestDescription();
	}
	
	public static void main(String args[]) throws LearningProblemUnsupportedException, IOException, ComponentInitException {
		long start = System.currentTimeMillis();
		
		FuzzyDLLTest_noFuzzyTrains test = new FuzzyDLLTest_noFuzzyTrains();
		test.learn();
		
		System.err.println("running time (s) = " + (System.currentTimeMillis() - start)/1000);
	}
	
}