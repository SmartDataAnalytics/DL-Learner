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
import org.dllearner.Config;
import org.dllearner.ConfigurationManager;
import org.dllearner.LearningProblem;
import org.dllearner.Main;
import org.dllearner.OntologyFileFormat;
import org.dllearner.Score;
import org.dllearner.Config.Algorithm;
import org.dllearner.algorithms.LearningAlgorithm;
import org.dllearner.algorithms.gp.GP;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.dl.AtomicConcept;
import org.dllearner.dl.Individual;
import org.dllearner.dl.KB;
import org.dllearner.parser.DLLearner;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.reasoning.Reasoner;
import org.dllearner.reasoning.ReasoningMethodUnsupportedException;
import org.dllearner.reasoning.ReasoningService;

/**
 * Utility script for creating statistics for publications.
 * (Warning: Scripts may run for several hours. Results may change
 * when core algorithms are modified.)
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
		String gnuplotBaseDir = "log/gnuplot/";
		String statBaseDir = "log/stat/";
		
		File[] confFiles = new File[2];
		confFiles[0] = new File(exampleBaseDir + "trains", "trains_owl.conf"); 	
		confFiles[1] = new File(exampleBaseDir, "father2.conf");
		
		String[] examples = new String[2];
		examples[0] = "trains";
		examples[1] = "father";
		
		String[] algorithms = new String[3];
		algorithms[0] = "refinement";
		algorithms[1] = "gp";
		algorithms[2] = "hybrid";
		
		int[] algorithmRuns = {1,10,10};
		
		// do not plot anything
		// File[][][] gnuplotFiles = new File[examples.length][algorithms.length][3];
		// for(int i=0; i<examples.length; i++) {
		//	for(int j=0; j<algorithms.length; j++) {
		//		gnuplotFiles[i][j][0] = new File(gnuplotBaseDir, examples[i] + "_classification_" + algorithms[j] + ".data");
		//		gnuplotFiles[i][j][1] = new File(gnuplotBaseDir, examples[i] + "_length_" + algorithms[j] + ".data");
		//		gnuplotFiles[i][j][2] = new File(gnuplotBaseDir, examples[i] + "_runtime_" + algorithms[j] + ".data");
		//	}
		//}
		
		File statFile = new File(statBaseDir, "statistics.txt");
		String statString = "**automatically generated statistics**\n\n";
		
		// just set default options
		ConfigurationManager confMgr = new ConfigurationManager();
		confMgr.applyOptions();
		
		for(int exampleNr=0; exampleNr < examples.length; exampleNr++) {
			
			// parse current conf file
			DLLearner.parseFile(confFiles[exampleNr].toString());
			
			// read which files were imported (internal KB is ignored) and initialise reasoner
			Map<URL, OntologyFileFormat> imports = getImports(DLLearner.getFunctionCalls(), confFiles[exampleNr]);
			
			// detect specified positive and negative examples
			SortedSet<Individual> positiveExamples = null;
			SortedSet<Individual> negativeExamples = null;
			Map<AtomicConcept,SortedSet<Individual>> posExamplesTmp = DLLearner.getPositiveExamples();
			Map<AtomicConcept,SortedSet<Individual>> negExamplesTmp = DLLearner.getNegativeExamples();
			for (AtomicConcept target : posExamplesTmp.keySet())
				positiveExamples = posExamplesTmp.get(target);
			for (AtomicConcept target : negExamplesTmp.keySet())
				negativeExamples = negExamplesTmp.get(target);
			int nrOfExamples = positiveExamples.size() + negativeExamples.size();
			
			statString += "example: " + examples[exampleNr] + "\n\n";
			
			for(int algorithmNr=0; algorithmNr < algorithms.length; algorithmNr++) {
							
				Stat classification = new Stat();
				Stat length = new Stat();
				Stat runtime = new Stat();
				
				for(int runNr=0; runNr < algorithmRuns[algorithmNr]; runNr++) {
					
					// create reasoner (this has to be done in this inner loop to 
					// ensure that none of the algorithm benefits from e.g. caching
					// of previous reasoning requests
					Reasoner reasoner = Main.createReasoner(new KB(), imports);
					ReasoningService rs = new ReasoningService(reasoner);					
					
					// prepare reasoner for using subsumption and role hierarchy
					// TODO: currently, it is a small unfairness that each algorithm
					// uses the same reasoning object (e.g. the second algorithm may
					// have a small advantage if the reasoner cached reasoning requests
					// of the first algorithm)
					Main.autoDetectConceptsAndRoles(rs);
					try {
						reasoner.prepareSubsumptionHierarchy();
						reasoner.prepareRoleHierarchy();
						// improving the subsumption hierarchy makes only sense
						// for the refinement based algorithm
						if(algorithmNr==0)
							reasoner.getSubsumptionHierarchy().improveSubsumptionHierarchy();
					} catch (ReasoningMethodUnsupportedException e) {
						e.printStackTrace();
					}
					
					// create learning problem
					LearningProblem learningProblem = new LearningProblem(rs, positiveExamples, negativeExamples);
					
					LearningAlgorithm learningAlgorithm = null;
					if(algorithmNr==0) {
						Config.algorithm = Algorithm.REFINEMENT;
						learningAlgorithm = new ROLearner(learningProblem);
					} else if(algorithmNr==1) {
						Config.algorithm = Algorithm.GP;
						Config.GP.numberOfIndividuals = 21;
						Config.GP.algorithmType = GP.AlgorithmType.GENERATIONAL;
						Config.GP.refinementProbability = 0;
						Config.GP.mutationProbability = 0.02;
						Config.GP.crossoverProbability = 0.8;
						Config.GP.hillClimbingProbability = 0;			
						learningAlgorithm = new GP(learningProblem);
					} else if(algorithmNr==2) {
						Config.algorithm = Algorithm.HYBRID_GP;
						Config.GP.numberOfIndividuals = 11;
						Config.GP.algorithmType = GP.AlgorithmType.GENERATIONAL;
						Config.GP.refinementProbability = 0.65;
						Config.GP.mutationProbability = 0.02;
						Config.GP.crossoverProbability = 0.2;
						Config.GP.hillClimbingProbability = 0;
						learningAlgorithm = new GP(learningProblem);
					}
					
					rs.resetStatistics();
					long algorithmStartTime = System.nanoTime();
					learningAlgorithm.start();
					long algorithmTime = System.nanoTime() - algorithmStartTime;
					// long algorithmTimeSeconds = algorithmTime / 1000000000;	
					
					int conceptLength = learningAlgorithm.getBestSolution().getLength();
					Score bestScore = learningAlgorithm.getSolutionScore();
					int misClassifications = bestScore.getCoveredNegatives().size()
							+ bestScore.getNotCoveredPositives().size();
					double classificationRatePercent = 100 * ((nrOfExamples - misClassifications) / (double) nrOfExamples);
					
					classification.addNumber(classificationRatePercent);
					length.addNumber(conceptLength);
					runtime.addNumber(algorithmTime);
					
					// free knowledge base to avoid memory leaks
					((DIGReasoner) reasoner).releaseKB();				
				
				}				
				
				statString += "algorithm: " + algorithms[algorithmNr] + " (runs: " + algorithmRuns[algorithmNr] + ")\n";
				statString += "classification: " + classification.getMean() + "% (standard deviation: " + classification.getStandardDeviation() + "%)\n";
				statString += "concept length: " + length.getMean() + " (standard deviation: " + length.getStandardDeviation() + ")\n";
				statString += "runtime: " + Helper.prettyPrintNanoSeconds(Math.round(runtime.getMean())) + " (standard deviation: " + Helper.prettyPrintNanoSeconds(Math.round(runtime.getStandardDeviation())) + ")\n\n";
			
			}

		}
		
		Main.createFile(statFile, statString);
	}
	
	private static Map<URL, OntologyFileFormat> getImports(List<List<String>> functionCalls, File confFile) {
		Map<URL, OntologyFileFormat> importedFiles = new HashMap<URL, OntologyFileFormat>();
		
		OntologyFileFormat format = null;
		URL url = null;
		
		for (List<String> call : functionCalls) {
			
			if(call.get(0).equals("import")) {

				try {				
					String fileString = call.get(1);
					if(fileString.startsWith("http:")) {
						url = new URL(fileString);
					} else {
						File f = new File(confFile.getParent(), call.get(1));
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
