/**
 * Copyright (C) 2007, Jens Lehmann
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
package org.dllearner.utilities;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.Config;
import org.dllearner.LearningProblem;
import org.dllearner.Main;
import org.dllearner.OntologyFileFormat;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.dl.AtomicConcept;
import org.dllearner.dl.Individual;
import org.dllearner.dl.KB;
import org.dllearner.parser.DLLearner;
import org.dllearner.reasoning.Reasoner;
import org.dllearner.reasoning.ReasoningMethodUnsupportedException;
import org.dllearner.reasoning.ReasoningService;

/**
 * Utility script for creating statistics for publications.
 * (Warning: Scripts may run for several hours.)
 * 
 * @author Jens Lehmann
 *
 */
public class PaperStatistics {

	/**
	 * Points to the current statistic generation function.
	 * 
	 * @param args None.
	 */
	public static void main(String[] args) {
		createStatistics();
	}
	
	@SuppressWarnings("unused")
	private static void createStatistics() {
		
		// set reasoner URL
		try {
			Config.digReasonerURL = new URL("http://localhost:8081");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}			
		
		// used OWL files
		File fatherOwlFile = new File("examples/father.owl");
		String fatherConfFile = "examples/father2.conf";
		URL ontologyFatherURL = null;
		try {
			ontologyFatherURL = new URL("file", "localhost", fatherOwlFile.getAbsolutePath());
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		
		Map<URL, OntologyFileFormat> m = new HashMap<URL, OntologyFileFormat>();
		m.put(ontologyFatherURL, OntologyFileFormat.RDF_XML);
		
		// initialise reasoner
		Reasoner reasoner = Main.createReasoner(new KB(), m);
		ReasoningService rs = new ReasoningService(reasoner);		
		
		Main.autoDetectConceptsAndRoles(rs);
		if (Config.Refinement.improveSubsumptionHierarchy) {
			try {
				reasoner.prepareSubsumptionHierarchy();
				reasoner.prepareRoleHierarchy();
				reasoner.getSubsumptionHierarchy().improveSubsumptionHierarchy();
			} catch (ReasoningMethodUnsupportedException e) {
				e.printStackTrace();
			}
		}
		
		// file is parsed, but we use only the specified examples really
		// (everything else is ignored)
		DLLearner.parseFile(fatherConfFile);
		
		SortedSet<Individual> positiveExamples = new TreeSet<Individual>();
		SortedSet<Individual> negativeExamples = new TreeSet<Individual>();
		Map<AtomicConcept,SortedSet<Individual>> posExamplesTmp = DLLearner.getPositiveExamples();
		Map<AtomicConcept,SortedSet<Individual>> negExamplesTmp = DLLearner.getNegativeExamples();
		
		for (AtomicConcept target : posExamplesTmp.keySet())
			positiveExamples = posExamplesTmp.get(target);

		for (AtomicConcept target : negExamplesTmp.keySet())
			negativeExamples = negExamplesTmp.get(target);
		
		LearningProblem learningProblem = new LearningProblem(rs, positiveExamples, negativeExamples);
		ROLearner learner = new ROLearner(learningProblem);
		learner.start();
		System.out.println(learner.getBestSolution().toString());
		
	}
	
}
