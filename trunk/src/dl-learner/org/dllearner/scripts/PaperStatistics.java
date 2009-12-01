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
package org.dllearner.scripts;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dllearner.algorithms.gp.GP;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.statistics.Stat;

/**
 * Utility script for creating statistics for publications.
 * (Warning: Scripts may run for several hours. Results may change
 * when core algorithms are modified.)
 * 
 * @author Jens Lehmann
 *
 */
public class PaperStatistics {

	private static DecimalFormat df = new DecimalFormat();
	
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
		
		// 5 fold cross validation
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
		// - arches => left out, insufficiently many examples
		// - moral (simple)
		// - moral (complex)
		// - poker (pair)
		// - poker (straight)
		// - uncle (FORTE)
		// - more?
		
		String exampleBaseDir = "examples/cross-benchmark/";
		String gnuplotBaseDir = "log/gnuplot/";
		String statBaseDir = "log/stat/";
		
		File[] confFiles = new File[6];
		confFiles[0] = new File(exampleBaseDir + "trains", "trains");
//		confFiles[0] = new File(exampleBaseDir + "arch", "arch");
		confFiles[1] = new File(exampleBaseDir + "moral_reasoner", "moral_43examples_simple");
		confFiles[2] = new File(exampleBaseDir + "moral_reasoner", "moral_43examples_complex");
        confFiles[3] = new File(exampleBaseDir + "poker", "pair");
        confFiles[4] = new File(exampleBaseDir + "poker", "straight");
        confFiles[5] = new File(exampleBaseDir + "forte", "uncle");
		
		String[] examples = new String[6];
		examples[0] = "trains";
//		examples[0] = "arches";
		examples[1] = "moral reasoner (43 examples, simple)";
		examples[2] = "moral reasoner (43 examples, complex)";
		examples[3] = "poker (49 examples, pair)";
		examples[4] = "poker (55 examples, straight)";
		examples[5] = "uncle (FORTE data set)";
		int startExampleNr = 0;		
		
		// for any example, we create conf files for each configuration to be tested
		String[] algorithmPostfix = new String[4];
		algorithmPostfix[0] = "_refexamples";
		algorithmPostfix[1] = "_refexamples_fast";
		algorithmPostfix[2] = "_el";
		algorithmPostfix[3] = "_el_disjunctive";
//		algorithmPostfix[4] = "_gp";
//		algorithmPostfix[5] = "_hybrid";		
		int startAlgorithmNr = 0;

		// only max. 4 folds for straight problem
		int[] folds = new int[] {5,5,5,5,4,5};
		
		File statFile = new File(statBaseDir, "statistics.txt");
		String statString = "**automatically generated statistics**\n\n";
		
		for(int exampleNr=startExampleNr; exampleNr < examples.length; exampleNr++) {
			
			for(int algorithmNr=startAlgorithmNr; algorithmNr < algorithmPostfix.length; algorithmNr++) {
				// reset algorithm number (next example starts with first algorithm)
				startAlgorithmNr = 0;		
				
				File confFile = new File(confFiles[exampleNr] + algorithmPostfix[algorithmNr] + ".conf"); 
				
				System.out.println("running " + folds[exampleNr] + " fold cross validation on " + confFile);
				
				CrossValidation cv = new CrossValidation(confFile, folds[exampleNr], false);
				Stat accuracy = cv.getAccuracy();
				Stat length = cv.getLength();
				Stat runtime = cv.getRuntime();
				
				statString += "conf file: " + confFile + "\n";
//				statString += "classification: " + classification.getMean() + "% (standard deviation: " + classification.getStandardDeviation() + "%)\n";
//				statString += "concept length: " + length.getMean() + " (standard deviation: " + length.getStandardDeviation() + ")\n";
//				statString += "runtime: " + runtime.getMean() + " (standard deviation: " + runtime.getStandardDeviation() + ")\n\n";
			
				statString += "accuracy: " + CrossValidation.statOutput(df, accuracy, "%") + "\n";
				statString += "length: " + CrossValidation.statOutput(df, length, "") + "\n";				
				statString += "runtime: " + CrossValidation.statOutput(df, runtime, "s") + "\n\n";

				Files.createFile(statFile, statString);
				
			} // end algorithm loop
			
		} // end example loop
		
	}
	
	@SuppressWarnings({"unused"})
	private static Map<URL, OntologyFormat> getImports(Map<String,List<List<String>>> functionCalls, File confFile) {
		Map<URL, OntologyFormat> importedFiles = new HashMap<URL, OntologyFormat>();
		
		OntologyFormat format = null;
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
					importedFiles.put(url, OntologyFormat.RDF_XML);
				else {
					String formatString = call.get(2);
					if (formatString.equals("RDF/XML"))
						format = OntologyFormat.RDF_XML;
					else
						format = OntologyFormat.N_TRIPLES;
					importedFiles.put(url, format);
				}
			// }			
		}
		
		return importedFiles;
	}
	
	/**
	 * Has been used to create the statistics for the MLDM 2007 paper.
	 * Warning: this method runs for several hours
	 * 
	 * TODO This method has not been fully adapted to the base structure
	 * changes. To reproduce the results, the method has to be implemented
	 * properly.
	 */
	@SuppressWarnings("unused")
	public static void createStatisticsMLDMPaper(PosNegLPStandard learningProblem, String baseDir) {
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
					// rs = new ReasonerComponent(reasoner);
//					ReasonerComponent rs = cm.reasoningService(reasoner);
					// learningProblem = new LearningProblem(rs, posExamples, negExamples);
					learningProblem = cm.learningProblem(PosNegLPStandard.class, reasoner);
					
					// TODO: set up pos/neg examples
					cm.applyConfigEntry(learningProblem, "positiveExamples", null);
					cm.applyConfigEntry(learningProblem, "negativeExamples", null);

					if (j == 0) {
						// Config.algorithm = Algorithm.HYBRID_GP;
//						Config.GP.numberOfIndividuals = i + 1;
//						Config.GP.refinementProbability = 0.85;
//						Config.GP.mutationProbability = 0.02;
//						Config.GP.crossoverProbability = 0.05;
//						Config.GP.hillClimbingProbability = 0;
					} else if (j == 1) {
						// Config.algorithm = Algorithm.HYBRID_GP;
//						Config.GP.numberOfIndividuals = i + 1;
//						Config.GP.refinementProbability = 0.4;
//						Config.GP.mutationProbability = 0.02;
//						Config.GP.crossoverProbability = 0.4;
//						Config.GP.hillClimbingProbability = 0;
					} else if (j == 2) {
						// Config.algorithm = Algorithm.GP;
//						Config.GP.numberOfIndividuals = i + 1;
//						Config.GP.refinementProbability = 0;
//						Config.GP.mutationProbability = 0.02;
//						Config.GP.crossoverProbability = 0.8;
//						Config.GP.hillClimbingProbability = 0;
					} else if (j == 3) {
						// Config.algorithm = Algorithm.HYBRID_GP;
//						Config.GP.numberOfIndividuals = i + 1;
//						Config.GP.refinementProbability = 0.7;
//						Config.GP.mutationProbability = 0.02;
//						Config.GP.crossoverProbability = 0.1;
//						Config.GP.hillClimbingProbability = 0;
					}

					algorithmStartTime = System.nanoTime();
//					gp = new GP(learningProblem);
					long algorithmTime = System.nanoTime() - algorithmStartTime;
					long algorithmTimeSeconds = algorithmTime / 1000000000;

					// Release, damit Pellet (hoffentlich) Speicher wieder
					// freigibt
					((DIGReasoner) reasoner).releaseKB();

//					int conceptLength = gp.getBestSolution().getLength();
//					Score bestScore = gp.getSolutionScore();
//					int misClassifications = bestScore.getCoveredNegatives().size()
//							+ bestScore.getNotCoveredPositives().size();
//					double classificationRatePercent = 100 * ((nrOfExamples - misClassifications) / (double) nrOfExamples);
//
//					statAr[j][0].addNumber(classificationRatePercent);
//					statAr[j][1].addNumber(conceptLength);
//					statAr[j][2].addNumber(algorithmTimeSeconds);

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
