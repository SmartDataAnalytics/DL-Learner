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
import java.util.List;
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
		
		// experimental setup:
		
		// algorithms: refinement, GP, hybrid GP (YinYang)
		// settings GP:
		// - average over 10 runs
		// ...
		// settings Hybrid GP:
		// - average over 10 runs
		// ...
		// settings refinement:
		// - single run
		// ...
		
		// observations: 
		// - correctness
		// - concept length
		// - runtime
		
		// learning examples:
		// - trains
		// - arches
		// - moral (simple)
		// - moral (complex)
		// - poker (pair)
		// - poker (straight)
		// - uncle (FORTE)
		// - more?
		
		String exampleBaseDir = "examples/";
		
		File[] confFiles = new File[1];
		confFiles[0] = new File(exampleBaseDir, "trains/trains.conf"); 		
		
		/*
		Stat[][] statAr = new Stat[4][3];
		File[][] fileAr = new File[4][3];		
		
		fileAr[0][0] = new File(baseDir, "gnuplot/hybrid100classification.data");
		fileAr[0][1] = new File(baseDir, "gnuplot/hybrid100length.data");
		fileAr[0][2] = new File(baseDir, "gnuplot/hybrid100runtime.data");
		fileAr[1][0] = new File(baseDir, "gnuplot/hybrid50classification.data");
		fileAr[1][1] = new File(baseDir, "gnuplot/hybrid50length.data");
		fileAr[1][2] = new File(baseDir, "gnuplot/hybrid50runtime.data");
		fileAr[2][0] = new File(baseDir, "gnuplot/gpclassification.data");
		fileAr[2][1] = new File(baseDir, "gnuplot/gplength.data");
		fileAr[2][2] = new File(baseDir, "gnuplot/gpruntime.data");		
		*/
		
		// set reasoner URL
		try {
			Config.digReasonerURL = new URL("http://localhost:8081");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}			
		
		String fatherConfFile = "examples/father2.conf";
		
		// file is parsed, but we use only the specified examples really
		// (everything else is ignored)
		DLLearner.parseFile(fatherConfFile);
		// DLLearner.parseFile(fatherConfFile);		
		
		Map<URL, OntologyFileFormat> imports = getImports(DLLearner.getFunctionCalls());
		
		// initialise reasoner
		Reasoner reasoner = Main.createReasoner(new KB(), imports);
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
	
	private static Map<URL, OntologyFileFormat> getImports(List<List<String>> functionCalls) {
		Map<URL, OntologyFileFormat> importedFiles = new HashMap<URL, OntologyFileFormat>();
		
		OntologyFileFormat format = null;
		URL url = null;
		
		for (List<String> call : functionCalls) {
			
			if(call.get(0).equals("import")) {
				// alte Methode mit file statt URI
				// File f = new File(baseDir, call.get(1));
				
				try {				
					String fileString = call.get(1);
					if(fileString.startsWith("http:")) {
						url = new URL(fileString);
					} else {
						File f = new File("examples", call.get(1));
						url = f.toURI().toURL();
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (call.size() == 2)
					// falls nichts angegeben, dann wird RDF/XML gewählt
					importedFiles.put(url, OntologyFileFormat.RDF_XML);
				else {
					String formatString = call.get(2);
					if (formatString.equals("RDF/XML"))
						format = OntologyFileFormat.RDF_XML;
					else
						format = OntologyFileFormat.N_TRIPLES;
					importedFiles.put(url, format);
				}
			}			
		}
		
		return importedFiles;
	}
	
}
