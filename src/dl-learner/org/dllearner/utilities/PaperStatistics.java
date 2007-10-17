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
import org.dllearner.algorithms.gp.GP;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.OntologyFileFormat;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.parser.ConfParser;
import org.dllearner.reasoning.DIGReasoner;

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
		
		File[] confFiles = new File[7];
		confFiles[0] = new File(exampleBaseDir + "trains", "trains_owl.conf");
		confFiles[1] = new File(exampleBaseDir + "arch", "arch_owl.conf");
		confFiles[2] = new File(exampleBaseDir + "moral_reasoner", "moral_43examples_owl.conf");
		confFiles[3] = new File(exampleBaseDir + "moral_reasoner", "moral_43examples_complex_owl.conf");
        confFiles[4] = new File(exampleBaseDir + "poker", "pair_owl.conf");
        confFiles[5] = new File(exampleBaseDir + "poker", "straight_owl.conf");
        confFiles[6] = new File(exampleBaseDir + "forte", "forte_uncle_owl.conf");
		
		String[] examples = new String[7];
		examples[0] = "trains";
		examples[1] = "arches";
		examples[2] = "moral reasoner (43 examples, simple)";
		examples[3] = "moral reasoner (43 examples, complex)";
		examples[4] = "poker (49 examples, pair)";
		examples[5] = "poker (55 examples, straight)";
		examples[6] = "uncle (FORTE data set)";
		int startExampleNr = 0;		
		
		String[] algorithms = new String[3];
		algorithms[0] = "refinement";
		algorithms[1] = "gp";
		algorithms[2] = "hybrid";
		
		int[] algorithmRuns = {1,10,10};
		int startAlgorithmNr = 0;

		// Config.GP.maxConceptLength = 30;
		// Config.writeDIGProtocol = true;
		// Config.digProtocolFile = new File(statBaseDir, "dig.log");
		
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
		File statDetailsFile = new File(statBaseDir, "statistics_details.txt");
		String statString = "**automatically generated statistics**\n\n";
		String statDetailsString = statString;
		
		ComponentManager cm = ComponentManager.getInstance();
		
		// just set default options
		ConfigurationManager confMgr = new ConfigurationManager();
		confMgr.applyOptions();
		
		for(int exampleNr=startExampleNr; exampleNr < examples.length; exampleNr++) {
			
			// parse current conf file
			ConfParser learner = ConfParser.parseFile(confFiles[exampleNr]);
			
			String baseDir = confFiles[exampleNr].getParent();
			
			// read which files were imported (internal KB is ignored) and initialise reasoner
			Map<URL, OntologyFileFormat> imports = getImports(learner.getFunctionCalls(), confFiles[exampleNr]);
			//Map<URL, Class<? extends KnowledgeSource>> imports = Start.getImportedFiles(learner, baseDir);
			
			// detect specified positive and negative examples
			SortedSet<String> positiveExamples = learner.getPositiveExamples();
			SortedSet<String> negativeExamples = learner.getNegativeExamples();
			int nrOfExamples = positiveExamples.size() + negativeExamples.size();
			
			statString += "example: " + examples[exampleNr] + "\n\n";
			
			for(int algorithmNr=startAlgorithmNr; algorithmNr < algorithms.length; algorithmNr++) {
				// reset algorithm number (next example starts with first algorithm)
				startAlgorithmNr = 0;		
				
				Stat classification = new Stat();
				Stat length = new Stat();
				Stat runtime = new Stat();
				
				for(int runNr=0; runNr < algorithmRuns[algorithmNr]; runNr++) {
					
					// create reasoner (this has to be done in this inner loop to 
					// ensure that none of the algorithm benefits from e.g. caching
					// of previous reasoning requests
					// Reasoner reasoner = Main.createReasoner(new KB(), imports);
					// TODO: needs fixing
					KnowledgeSource ks = cm.knowledgeSource(OWLFile.class);
					ReasonerComponent reasoner = cm.reasoner(DIGReasoner.class, ks);
					ReasoningService rs = new ReasoningService(reasoner);					
					
					// System.out.println(positiveExamples);
					// System.out.println(negativeExamples);
					// System.exit(0);
					
					// create learning problem
					// LearningProblem learningProblem = new LearningProblem(rs, positiveExamples, negativeExamples);
					LearningProblem learningProblem = cm.learningProblem(PosNegDefinitionLP.class, rs);
					
					// prepare reasoner for using subsumption and role hierarchy
					// TODO: currently, it is a small unfairness that each algorithm
					// uses the same reasoning object (e.g. the second algorithm may
					// have a small advantage if the reasoner cached reasoning requests
					// of the first algorithm)
//					Helper.autoDetectConceptsAndRoles(rs);
//					try {
//						reasoner.prepareSubsumptionHierarchy();
//						reasoner.prepareRoleHierarchy();
//						// improving the subsumption hierarchy makes only sense
//						// for the refinement based algorithm
//						if(algorithmNr==0)
//							reasoner.getSubsumptionHierarchy().improveSubsumptionHierarchy();
//					} catch (ReasoningMethodUnsupportedException e) {
//						e.printStackTrace();
//					}

					LearningAlgorithm learningAlgorithm = null;
					if(algorithmNr==0) {
						// Config.algorithm = Algorithm.REFINEMENT;
						// Config.Refinement.heuristic = Config.Refinement.Heuristic.FLEXIBLE;
						Config.Refinement.horizontalExpansionFactor = 0.6;
						Config.Refinement.quiet = true;
						// Config.percentPerLengthUnit = 0.05;
						// learningAlgorithm = new ROLearner(learningProblem);
						// learningAlgorithm = cm.learningAlgorithm(ROLearner.class, learningProblem);
					} else if(algorithmNr==1) {
						// Config.algorithm = Algorithm.GP;
						Config.GP.algorithmType = GP.AlgorithmType.GENERATIONAL;						
						Config.GP.selectionType = GP.SelectionType.RANK_SELECTION;
						Config.GP.generations = 50;	
						Config.GP.useFixedNumberOfGenerations = true;
						Config.GP.numberOfIndividuals = 201;
						// if(exampleNr == 3 || exampleNr == 4)
						// 	Config.GP.numberOfIndividuals = 51;
						Config.GP.refinementProbability = 0;
						Config.GP.mutationProbability = 0.02;
						Config.GP.crossoverProbability = 0.8;
						Config.GP.hillClimbingProbability = 0;
						// Config.percentPerLengthUnit = 0.005;
						// give GP a chance to find the long solution of the
						// uncle problem
						// if(exampleNr==3 || exampleNr==5 || exampleNr == 6)
						//	Config.percentPerLengthUnit = 0.002;
						// learningAlgorithm = new GP(learningProblem);
						learningAlgorithm = cm.learningAlgorithm(GP.class, learningProblem, rs);
					} else if(algorithmNr==2) {
						// Config.algorithm = Algorithm.HYBRID_GP;
						Config.GP.algorithmType = GP.AlgorithmType.GENERATIONAL;						
						Config.GP.selectionType = GP.SelectionType.RANK_SELECTION;
						Config.GP.generations = 50;
						Config.GP.useFixedNumberOfGenerations = true;
						Config.GP.numberOfIndividuals = 201;
						//if(exampleNr == 3 || exampleNr == 4)
						//	Config.GP.numberOfIndividuals = 51;						
						Config.GP.refinementProbability = 0.65;
						Config.GP.mutationProbability = 0.02;
						Config.GP.crossoverProbability = 0.2;
						Config.GP.hillClimbingProbability = 0;
						// Config.percentPerLengthUnit = 0.005;
						// if(exampleNr == 3 || exampleNr==5 || exampleNr==6)
//							Config.percentPerLengthUnit = 0.002;						
						// learningAlgorithm = new GP(learningProblem);
						learningAlgorithm = cm.learningAlgorithm(GP.class, learningProblem, rs);
					}
					
					// rs.resetStatistics();
					
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
					
					statDetailsString += "example: " + examples[exampleNr] + "\n";
					statDetailsString += "algorithm: " + algorithms[algorithmNr] + "\n";
					statDetailsString += "learned concept: " + learningAlgorithm.getBestSolution() + "\n";
					statDetailsString += "classification: " + classificationRatePercent + "%\n";
					statDetailsString += "concept length: " +  conceptLength + "\n";
					statDetailsString += "runtime: " + Helper.prettyPrintNanoSeconds(algorithmTime) + "\n\n";
				
					Files.createFile(statDetailsFile, statDetailsString);
					
				} // end run loop		
				
				statString += "algorithm: " + algorithms[algorithmNr] + " (runs: " + algorithmRuns[algorithmNr] + ")\n";
				statString += "classification: " + classification.getMean() + "% (standard deviation: " + classification.getStandardDeviation() + "%)\n";
				statString += "concept length: " + length.getMean() + " (standard deviation: " + length.getStandardDeviation() + ")\n";
				statString += "runtime: " + Helper.prettyPrintNanoSeconds(Math.round(runtime.getMean())) + " (standard deviation: " + Helper.prettyPrintNanoSeconds(Math.round(runtime.getStandardDeviation())) + ")\n\n";
			
				Files.createFile(statFile, statString);
				
			} // end algorithm loop
			
		} // end example loop
		
	}
	
	private static Map<URL, OntologyFileFormat> getImports(Map<String,List<List<String>>> functionCalls, File confFile) {
		Map<URL, OntologyFileFormat> importedFiles = new HashMap<URL, OntologyFileFormat>();
		
		OntologyFileFormat format = null;
		URL url = null;
		
		List<List<String>> imports = functionCalls.get("import");
		
		for (List<String> call : imports) {
			
			//if(call.get(0).equals("import")) {

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
			// }			
		}
		
		return importedFiles;
	}
	
	// erzeugt Statistiken für MLDM-Paper zur Verarbeitung mit GnuPlot
	// Vorsicht: Laufzeit von mehreren Stunden
	
	/**
	 * Has been used to create the statistics for the MLDM 2007 paper.
	 * Warning: this method runs for several hours
	 * 
	 * @todo: This method has not been fully adapted to the base structure
	 * changes. To reproduce the results, the method has to be implemented
	 * properly.
	 */
	@SuppressWarnings("unused")
	public static void createStatisticsMLDMPaper(PosNegDefinitionLP learningProblem, String baseDir) {
		// Algorithmus 1: hybrid GP (100% refinement)
		// Algorithmus 2: 50% refinement, 40% crossover, 1% mutation
		// Algorithmus 3: 80% crossover, 2% mutation

		// Diagramm 1: Prozentzahl richtig klassifiziert
		// Diagramm 2: Konzeptlänge
		// Diagramm 3: Laufzeit

		int runs = 9;
		GP gp;
		long algorithmStartTime;
		int nrOfExamples = learningProblem.getPositiveExamples().size()
				+ learningProblem.getNegativeExamples().size();

		Stat[][] statAr = new Stat[4][3];
		File[][] fileAr = new File[4][3];
		StringBuilder[][] exportString = new StringBuilder[4][3];
		// initialise export strings
		for (int j = 0; j < 4; j++) {
			for (int k = 0; k < 3; k++) {
				exportString[j][k] = new StringBuilder();
			}
		}

		fileAr[0][0] = new File(baseDir, "gnuplot/hybrid100classification.data");
		fileAr[0][1] = new File(baseDir, "gnuplot/hybrid100length.data");
		fileAr[0][2] = new File(baseDir, "gnuplot/hybrid100runtime.data");
		fileAr[1][0] = new File(baseDir, "gnuplot/hybrid50classification.data");
		fileAr[1][1] = new File(baseDir, "gnuplot/hybrid50length.data");
		fileAr[1][2] = new File(baseDir, "gnuplot/hybrid50runtime.data");
		fileAr[2][0] = new File(baseDir, "gnuplot/gpclassification.data");
		fileAr[2][1] = new File(baseDir, "gnuplot/gplength.data");
		fileAr[2][2] = new File(baseDir, "gnuplot/gpruntime.data");

		// Extra-Test
		fileAr[3][0] = new File(baseDir, "gnuplot/extraclassification.data");
		fileAr[3][1] = new File(baseDir, "gnuplot/extralength.data");
		fileAr[3][2] = new File(baseDir, "gnuplot/extraruntime.data");

		ComponentManager cm = ComponentManager.getInstance();
		
		long overallTimeStart = System.nanoTime();

		// allgemeine Einstellungen
		// Config.GP.elitism = true;

		for (int i = 700; i <= 700; i += 100) {
			// initialise statistics array
			for (int j = 0; j < 4; j++) {
				for (int k = 0; k < 3; k++) {
					statAr[j][k] = new Stat();
				}
			}

			for (int run = 0; run < runs; run++) {
				System.out.println("=============");
				System.out.println("i " + i + " run " + run);
				System.out.println("=============");

				// nur ein Test durchlaufen
				for (int j = 0; j < 3; j++) {

					// Reasoner neu erstellen um Speicherprobleme zu vermeiden
					// reasoner = new DIGReasoner(kb, Config.digReasonerURL, importedFiles);
					// TODO: set up knowledge source
					KnowledgeSource ks = cm.knowledgeSource(OWLFile.class);
					ReasonerComponent reasoner = cm.reasoner(DIGReasoner.class, ks);
					// reasoner.prepareSubsumptionHierarchy();
					// rs = new ReasoningService(reasoner);
					ReasoningService rs = cm.reasoningService(reasoner);
					// learningProblem = new LearningProblem(rs, posExamples, negExamples);
					learningProblem = cm.learningProblem(PosNegDefinitionLP.class, rs);
					
					// TODO: set up pos/neg examples
					cm.applyConfigEntry(learningProblem, "positiveExamples", null);
					cm.applyConfigEntry(learningProblem, "negativeExamples", null);

					if (j == 0) {
						// Config.algorithm = Algorithm.HYBRID_GP;
						Config.GP.numberOfIndividuals = i + 1;
						Config.GP.refinementProbability = 0.85;
						Config.GP.mutationProbability = 0.02;
						Config.GP.crossoverProbability = 0.05;
						Config.GP.hillClimbingProbability = 0;
					} else if (j == 1) {
						// Config.algorithm = Algorithm.HYBRID_GP;
						Config.GP.numberOfIndividuals = i + 1;
						Config.GP.refinementProbability = 0.4;
						Config.GP.mutationProbability = 0.02;
						Config.GP.crossoverProbability = 0.4;
						Config.GP.hillClimbingProbability = 0;
					} else if (j == 2) {
						// Config.algorithm = Algorithm.GP;
						Config.GP.numberOfIndividuals = i + 1;
						Config.GP.refinementProbability = 0;
						Config.GP.mutationProbability = 0.02;
						Config.GP.crossoverProbability = 0.8;
						Config.GP.hillClimbingProbability = 0;
					} else if (j == 3) {
						// Config.algorithm = Algorithm.HYBRID_GP;
						Config.GP.numberOfIndividuals = i + 1;
						Config.GP.refinementProbability = 0.7;
						Config.GP.mutationProbability = 0.02;
						Config.GP.crossoverProbability = 0.1;
						Config.GP.hillClimbingProbability = 0;
					}

					algorithmStartTime = System.nanoTime();
					gp = new GP(learningProblem);
					long algorithmTime = System.nanoTime() - algorithmStartTime;
					long algorithmTimeSeconds = algorithmTime / 1000000000;

					// Release, damit Pellet (hoffentlich) Speicher wieder
					// freigibt
					((DIGReasoner) reasoner).releaseKB();

					int conceptLength = gp.getBestSolution().getLength();
					Score bestScore = gp.getSolutionScore();
					int misClassifications = bestScore.getCoveredNegatives().size()
							+ bestScore.getNotCoveredPositives().size();
					double classificationRatePercent = 100 * ((nrOfExamples - misClassifications) / (double) nrOfExamples);

					statAr[j][0].addNumber(classificationRatePercent);
					statAr[j][1].addNumber(conceptLength);
					statAr[j][2].addNumber(algorithmTimeSeconds);

				}
			}

			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 3; k++) {
					exportString[j][k].append(i + " " + statAr[j][k].getMean() + " "
							+ statAr[j][k].getStandardDeviation() + "\n");
				}
			}

			// Daten werden nach jeder Populationserhöhung geschrieben, nicht
			// nur
			// am Ende => man kann den Test also auch zwischendurch abbrechen
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 3; k++) {
					Files.createFile(fileAr[j][k], exportString[j][k].toString());
				}
			}
		}

		long overallTime = System.nanoTime() - overallTimeStart;
		System.out.println("\noverall time: "
				+ Helper.prettyPrintNanoSeconds(overallTime));
	}
	
}
