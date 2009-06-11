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
package org.dllearner.scripts.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.configurators.CELOEConfigurator;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.statistics.Stat;

/**
 * The script loads an ontology, loops through all classes having at least a
 * specified amount of instances, and evaluates a learning algorithm on those.
 * It measures how many times the algorithm missed an appropriate solution of a
 * problem, how often it found the correct solution (and the position of the
 * solution in the suggestion list). It also tries to find consequences of
 * adding an axiom (either it helped to detect an inconsistency or the class now
 * has more inferred instances).
 * 
 * @author Jens Lehmann
 * 
 */
public class OntologyEngineering {

	private static double minAccuracy = 0.85;

	private static int minInstanceCount = 3;

	private static int algorithmRuntimeInSeconds = 10;

	private static DecimalFormat df = new DecimalFormat();

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws ComponentInitException,
			LearningProblemUnsupportedException, IOException {

		Logger.getRootLogger().setLevel(Level.WARN);

		// OWL file is the first argument of the script
		if (args.length == 0) {
			System.out.println("You need to give an OWL file as argument.");
			System.exit(0);
		}
		File owlFile = new File(args[0]);

		ComponentManager cm = ComponentManager.getInstance();

		// load OWL in reasoner
		OWLFile ks = cm.knowledgeSource(OWLFile.class);
		ks.getConfigurator().setUrl(owlFile.toURI().toURL());
		ks.init();
		ReasonerComponent reasoner = cm.reasoner(FastInstanceChecker.class, ks);
		reasoner.init();
		System.out.println("Loaded ontology " + args[0] + ".");

		String baseURI = reasoner.getBaseURI();
		Map<String, String> prefixes = reasoner.getPrefixes();

		// general statistical information
		String userInputProtocol = "";
		int classCandidatesCount = 0;
		Stat instanceCountStat = new Stat();

		// equivalence classes
		int candidatesAboveThresholdCount = 0;
		int missesCount = 0;
		int foundDescriptionCount = 0;
		int noSensibleDescriptionCount = 0;
		int inconsistencyDetected = 0;
		int moreInstancesCount = 0;
		Stat moreInstancesCountStat = new Stat();
		Stat accStat = new Stat();
		Stat accSelectedStat = new Stat();
		Stat accAboveThresholdStat = new Stat();
		Stat positionStat = new Stat();

		// super classes
		int candidatesAboveThresholdCountSC = 0;
		int missesCountSC = 0;
		int foundDescriptionCountSC = 0;
		int noSensibleDescriptionCountSC = 0;
		int inconsistencyDetectedSC = 0;
		int moreInstancesCountSC = 0;
		Stat moreInstancesCountStatSC = new Stat();
		Stat accStatSC = new Stat();
		Stat accSelectedStatSC = new Stat();
		Stat accAboveThresholdStatSC = new Stat();
		Stat positionStatSC = new Stat();

		// loop through all classes
		Set<NamedClass> classes = new TreeSet<NamedClass>(reasoner.getNamedClasses());
		classes.remove(new NamedClass("http://www.w3.org/2002/07/owl#Thing"));
		// reduce number of classes for testing purposes
//		shrinkSet(classes, 20);
		for (NamedClass nc : classes) {
			// check whether the class has sufficient instances
			int instanceCount = reasoner.getIndividuals(nc).size();
			if (instanceCount < minInstanceCount) {
				System.out.println("class " + nc.toManchesterSyntaxString(baseURI, prefixes)
						+ " has only " + instanceCount + " instances (minimum: " + minInstanceCount
						+ ") - skipping");
			} else {
				System.out.println("\nlearning axioms for class "
						+ nc.toManchesterSyntaxString(baseURI, prefixes) + " with " + instanceCount
						+ " instances");
				classCandidatesCount++;
				instanceCountStat.addNumber(instanceCount);

				TreeSet<EvaluatedDescriptionClass> suggestions;
				// i=0 is equivalence and i=1 is super class
				for (int i = 0; i <= 1; i++) {
					// learn equivalence axiom
					ClassLearningProblem lp = cm.learningProblem(ClassLearningProblem.class,
							reasoner);
					lp.getConfigurator().setClassToDescribe(nc.getURI().toURL());
					if (i == 0) {
						System.out
								.println("generating suggestions for equivalent class (please wait "
										+ algorithmRuntimeInSeconds + " seconds)");
						lp.getConfigurator().setType("equivalence");
					} else {
						System.out.println("suggestions for super class (please wait "
								+ algorithmRuntimeInSeconds + " seconds)");
						lp.getConfigurator().setType("superClass");
					}
					lp.init();

					CELOE celoe = cm.learningAlgorithm(CELOE.class, lp, reasoner);
					CELOEConfigurator cf = celoe.getConfigurator();
					cf.setUseNegation(false);
					cf.setValueFrequencyThreshold(3);
					cf.setMaxExecutionTimeInSeconds(algorithmRuntimeInSeconds);
					cf.setNoisePercentage(0.05);
					cf.setMaxNrOfResults(10);
					celoe.init();

					celoe.start();

					// test whether a solution above the threshold was found
					EvaluatedDescription best = celoe.getCurrentlyBestEvaluatedDescription();
					double bestAcc = best.getAccuracy();
					if (i == 0) {
						accStat.addNumber(bestAcc);
					} else {
						accStatSC.addNumber(bestAcc);
					}
					if (bestAcc < minAccuracy) {
						System.out
								.println("The algorithm did not find a suggestion with an accuracy above the threshold of "
										+ (100 * minAccuracy)
										+ "%. (The best one was \""
										+ best.getDescription().toManchesterSyntaxString(baseURI,
												prefixes)
										+ "\" with an accuracy of "
										+ df.format(bestAcc) + ".) - skipping");
					} else {

						if (i == 0) {
							accAboveThresholdStat.addNumber(bestAcc);
							candidatesAboveThresholdCount++;
						} else {
							accAboveThresholdStatSC.addNumber(bestAcc);
							candidatesAboveThresholdCountSC++;
						}

						suggestions = (TreeSet<EvaluatedDescriptionClass>) celoe
								.getCurrentlyBestEvaluatedDescriptions();
						List<EvaluatedDescriptionClass> suggestionsList = new LinkedList<EvaluatedDescriptionClass>(
								suggestions.descendingSet());

						int nr = 0;
						for (EvaluatedDescription suggestion : suggestionsList) {
							System.out.println(nr
									+ ": "
									+ suggestion.getDescription().toManchesterSyntaxString(baseURI,
											prefixes) + " (accuracy: "
									+ df.format(suggestion.getAccuracy()) + ")");
							nr++;
						}

						// knowledge engineer feedback:
						// - number 0-9: one of axioms is appropriate
						// - m ("missing"): none of the axioms is appropriate,
						// but there is one which was not found
						// - n ("none"): none of the axioms is appropriate and
						// there is probably no other appropriate axiom
						System.out
								.println("Type a number (\"0\"-\""
										+ (suggestions.size() - 1)
										+ "\") if any of the suggestions is appropriate (if several are possible choose the lowest number). Type \"n\" if there is no appropriate suggestion for this class in your opinion. Type \"m\" if there is an appropriate suggestion in your opinion, but the algorithm did not suggest it.");

						String[] inputs = new String[suggestions.size() + 2];
						inputs[0] = "m";
						inputs[1] = "n";
						for (int j = 0; j < suggestions.size(); j++) {
							inputs[j + 2] = "" + j;
						}

						List<String> allowedInputs = Arrays.asList(inputs);
						String input;
						do {
							BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
							input = br.readLine();
						} while (!allowedInputs.contains(input));

						if (input.equals("m")) {
							if (i == 0) {
								missesCount++;
							} else {
								missesCountSC++;
							}
							System.out
									.println("You said the algorithm missed a possible solution.");
						} else if (input.equals("n")) {
							if (i == 0) {
								noSensibleDescriptionCount++;
							} else {
								noSensibleDescriptionCountSC++;
							}
							System.out
									.println("You said that there is no reasonable class expression.");
						} else {
							int selectedNr = Integer.parseInt(input);
							EvaluatedDescriptionClass selectedExpression = suggestionsList
									.get(selectedNr);
							System.out.println("You selected \""
									+ selectedExpression.getDescription().toManchesterSyntaxString(
											baseURI, prefixes) + "\".");
							boolean isConsistent = selectedExpression.isConsistent();
							if (!isConsistent) {
								System.out
										.println("Adding the expression leads to an inconsistency of the knowledge base (which is positive since it is a meaningful expression).");
							}
							// selectedExpression.getAdditionalInstances().
							Set<Individual> addInst = selectedExpression.getAdditionalInstances();
							int additionalInstances = addInst.size();
							if (additionalInstances > 0) {
								System.out.println("Adding the expression leads to "
										+ additionalInstances
										+ " new instances, e.g. \""
										+ addInst.iterator().next().toManchesterSyntaxString(
												baseURI, prefixes) + "\".");
							}

							if (i == 0) {
								accSelectedStat.addNumber(bestAcc);
								positionStat.addNumber(selectedNr);
								foundDescriptionCount++;
								if (!isConsistent) {
									inconsistencyDetected++;
								}
								if (additionalInstances > 0) {
									moreInstancesCount++;
									moreInstancesCountStat.addNumber(additionalInstances);
								}
							} else {
								accSelectedStatSC.addNumber(bestAcc);
								positionStatSC.addNumber(selectedNr);
								foundDescriptionCountSC++;
								if (!isConsistent) {
									inconsistencyDetectedSC++;
								}
								if (additionalInstances > 0) {
									moreInstancesCountSC++;
									moreInstancesCountStatSC.addNumber(additionalInstances);
								}
							}
						}
					}
				}
			}
		}

		// print summary
		System.out.println();
		System.out
				.println("knowledge engineering process finished successfully - summary shown below");
		System.out.println();
		System.out.println("ontology: " + args[0]);
		System.out.println("user input protocol: " + userInputProtocol);
		System.out.println("classes in ontology: " + classes.size());
		System.out.println("classes with at least " + minInstanceCount + " instances: "
				+ classCandidatesCount);
		System.out.println();

		System.out.println("statistics for equivalence axioms:");
		System.out.println("classes above " + (minAccuracy * 100) + "% threshold: "
				+ candidatesAboveThresholdCount);
		System.out.println("axioms learned succesfully: " + foundDescriptionCount);
		System.out.println("axioms missed: " + missesCount);
		System.out.println("axiom with no sensible axioms: " + noSensibleDescriptionCount);
		System.out.println("average accuracy overall: " + accStat.prettyPrint(""));
		System.out.println("average accuracy of selected expressions: "
				+ accSelectedStat.prettyPrint(""));
		System.out.println("average accuracy of expressions above threshold "
				+ accAboveThresholdStat.prettyPrint(""));
		System.out.println("average number typed by user " + positionStat.prettyPrint(""));
		System.out.println();

		System.out.println("statistics for super class axioms:");
		System.out.println("classes above " + (minAccuracy * 100) + "% threshold: "
				+ candidatesAboveThresholdCountSC);
		System.out.println("axioms learned succesfully: " + foundDescriptionCountSC);
		System.out.println("axioms missed: " + missesCountSC);
		System.out.println("axiom with no sensible axioms: " + noSensibleDescriptionCountSC);
		System.out.println("average accuracy overall: " + accStatSC.prettyPrint(""));
		System.out.println("average accuracy of selected expressions: "
				+ accSelectedStatSC.prettyPrint(""));
		System.out.println("average accuracy of expressions above threshold "
				+ accAboveThresholdStatSC.prettyPrint(""));
		System.out.println("average number typed by user " + positionStatSC.prettyPrint(""));
	}

	@SuppressWarnings("unused")
	private static void shrinkSet(Set set, int nrOfElements) {
		while (set.size() > nrOfElements) {
			Iterator it = set.iterator();
			it.next();
			it.remove();
		}
	}
}
