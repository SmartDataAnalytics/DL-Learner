/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.learningproblems;
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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.accuracymethods.*;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.statistics.Stat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

//import org.dllearner.core.ComponentManager;
//import org.dllearner.core.owl.Description;
//import org.dllearner.core.owl.Individual;
//import org.dllearner.core.owl.NamedClass;
//import org.dllearner.core.owl.Thing;
//import org.dllearner.reasoning.FastInstanceChecker;

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
	
	private static double noisePercent = 5.0;

	private static int minInstanceCount = 3;

	private static int algorithmRuntimeInSeconds = 10;

	private static DecimalFormat df = new DecimalFormat();

	// for performance measurements and development
	private static boolean autoMode = true;
	// if set to true, this overrides the two options below and tests all four combinations => doesn't seem to work
	private static boolean testFCIApprox = true;
	private static boolean useFastInstanceChecker = false;
	private static boolean useApproximations = false;
	private static boolean computeApproxDiff = true;
	private static boolean useFMeasure = true;
	public int i =1;
	public static File f = new File("GS1.txt");
	public static void main(String[] args) throws
			Exception {

		Logger.getRootLogger().setLevel(Level.WARN);
		String fileName = "GeoSkills.owl";
		// OWL file is the first argument of the script
		//File file = new File(filePath);
		File file = new File("/home/hajira/Documents/AKSW/Celoe datasets/"+fileName);
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);
		AbstractKnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();	
		
		AbstractReasonerComponent rc ;
	
		// load OWL in reasoner
			
			
		System.out.println("Loaded ontology " + fileName + ".");
		
	
		// ??? we need to extract classes from LP not give a target class
		//OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://www.example.org/lymphography#Target2_Metastases"));
		
		//OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://www.w3.org/2001/XMLSchema#string"));
//		OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://www.daml.org/2003/02/fips55/fips-55-ont#Place"));
//		//OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://www.example.org/lymphography#LND10_3"));
//		ClassLearningProblem lp = new ClassLearningProblem(rc);
//		lp.setClassToDescribe(classToDescribe);
//		lp.init();
			
	
//		?????????????????????????????????????? // How to give diff values of heuristics ??
		if(testFCIApprox) {
			useFastInstanceChecker = false;
			useApproximations = false;
			rc=new OWLAPIReasoner(ks);
			rc.init();
			run(rc);
			
			
			useFastInstanceChecker = false;
			useApproximations = true;
			rc=new OWLAPIReasoner(ks);
			rc.init();
			run(rc);
			
			
			useFastInstanceChecker = true;
			useApproximations = false;
			rc = new ClosedWorldReasoner(ks);
			rc.init();
			run(rc);
			
			
			useFastInstanceChecker = true;
			useApproximations = true;
			rc = new ClosedWorldReasoner(ks);
			rc.init();
			run(rc);			
		} else {
			rc = new ClosedWorldReasoner(ks);
			rc.init();
			run(rc);	
		}
	}
		
	@SuppressWarnings("unchecked")
	public static void run(AbstractReasonerComponent reasoner) throws ComponentInitException, IOException {
	//	ComponentManager cm = ComponentManager.getInstance();
		
		String baseURI = reasoner.getBaseURI();
		Map<String, String> prefixes = reasoner.getPrefixes();

		// general statistical information
		String userInputProtocol = "";
		int classCandidatesCount = 0;
		Stat instanceCountStat = new Stat();
		Stat classExpressionTestsStat = new Stat();
		Stat approxDiffStat = new Stat();

		// equivalence classes
		int candidatesAboveThresholdCount = 0;
		int missesCount = 0;
		int foundDescriptionCount = 0;
		int noSensibleDescriptionCount = 0;
		int inconsistencyDetected = 0;
		int moreInstancesCount = 0;
		int nonPerfectCount = 0;
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
		int nonPerfectCountSC = 0;
		Stat moreInstancesCountStatSC = new Stat();
		Stat accStatSC = new Stat();
		Stat accSelectedStatSC = new Stat();
		Stat accAboveThresholdStatSC = new Stat();
		Stat positionStatSC = new Stat();

//	
		
		// loop through all classes
		//Set<NamedClass> classes = new TreeSet<NamedClass>(reasoner.getNamedClasses());
		//classes.remove(new NamedClass("http://www.w3.org/2002/07/owl#Thing"));
		// reduce number of classes for testing purposes
//		shrinkSet(classes, 20);
		
		Set<OWLClass> classes = new TreeSet<>(reasoner.getClasses());
		
		for (OWLClass nc : classes) {
			// check whether the class has sufficient instances
		// ????	
			//int instanceCount = reasoner.getRelatedIndividuals(nc, objectProperty).getIndividuals(nc).size();
			int instanceCount = reasoner.getIndividuals(nc).size();
			if (instanceCount < minInstanceCount) {
				System.out.println("class " + nc.toString()
				
						+ " has only " + instanceCount + " instances (minimum: " + minInstanceCount
						+ ") - skipping");
			} else {
				System.out.println("\nlearning axioms for class "
						+ nc.toString() + " with " + instanceCount
						+ " instances");
				classCandidatesCount++;
				instanceCountStat.addNumber(instanceCount);

				TreeSet<EvaluatedDescriptionClass> suggestions;
				// i=0 is equivalence and i=1 is super class 
				// what is the difference between the two ??
				
				for (int i = 0; i <= 1; i++) {
					// learn equivalence axiom
					ClassLearningProblem lp = new ClassLearningProblem(reasoner);
					lp.setClassToDescribe(nc);
					if (i == 0) {
						System.out
								.println("generating suggestions for equivalent class (please wait "
										+ algorithmRuntimeInSeconds + " seconds)");
						lp.setEquivalence(true);
					} else {
						System.out.println("suggestions for super class (please wait "
								+ algorithmRuntimeInSeconds + " seconds)");
							lp.setEquivalence(false);
					}
					// accuracy methods
					AccMethodTwoValuedApproximate accApprox;
					if (useFMeasure) {
						accApprox = new AccMethodFMeasureApprox();
					} else {
						accApprox = new AccMethodPredAccApprox();
					}
					accApprox.setReasoner(reasoner);
					accApprox.init();
					AccMethodTwoValued acc;
					if (useFMeasure) {
						acc = new AccMethodFMeasure();
					} else {
						acc = new AccMethodPredAcc();
					}
					acc.init();
					lp.setAccuracyMethod(acc);
					lp.init();

					CELOE celoe = new CELOE(lp, reasoner);
					celoe.setMaxExecutionTimeInSeconds(algorithmRuntimeInSeconds);
					celoe.setNoisePercentage(noisePercent);
					celoe.setMaxNrOfResults(10);
					celoe.init();
					
					RhoDRDown op = (RhoDRDown) celoe.getOperator();
					op.setUseNegation(false);
					op.setFrequencyThreshold(10);
					op.setUseDataHasValueConstructor(true);
					
					celoe.start();
					classExpressionTestsStat.addNumber(celoe.getClassExpressionTests());
					
					// test whether a solution above the threshold was found
					EvaluatedDescription best = celoe.getCurrentlyBestEvaluatedDescription();
					double bestAcc = best.getAccuracy();
					if (i == 0) {
						accStat.addNumber(bestAcc);
					} else {
						accStatSC.addNumber(bestAcc);
					}
					//if (bestAcc < minAccuracy || (best.getDescription() instanceof Thing)) {
						if (bestAcc < minAccuracy ) {
						System.out
								.println("The algorithm did not find a suggestion with an accuracy above the threshold of "
										+ (100 * minAccuracy)
										+ "% or the best description is not appropriate. (The best one was \""
										+ best.getDescription().toString()
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
						List<EvaluatedDescriptionClass> suggestionsList = new LinkedList<>(
								suggestions.descendingSet());

						if(computeApproxDiff) {
							for(EvaluatedDescriptionClass ed : suggestionsList) {
								OWLClassExpression d = ed.getDescription();

								lp.setAccuracyMethod(accApprox);
								double approx = lp.getAccuracyOrTooWeak(d, noisePercent/(double)100);

								lp.setAccuracyMethod(acc);
								double exact = lp.getAccuracyOrTooWeak(d, noisePercent/(double)100);
								
								double diff = Math.abs(approx-exact);
								// do not count "too weak"
								if(approx > -0.01 && exact > -0.01) {
									approxDiffStat.addNumber(diff);
								}
							}
						}
				
						int nr = 0;
						for (EvaluatedDescription suggestion : suggestionsList) {
							System.out.println(nr
									+ ": "
									+ suggestion.getDescription().toString() + " (accuracy: "
									+ df.format(suggestion.getAccuracy()) + ")");
							nr++;
						}

						// knowledge engineer feedback:
						// - number 0-9: one of axioms is appropriate
						// - m ("missing"): none of the axioms is appropriate,
						// but there is one which was not found
						// - n ("none"): none of the axioms is appropriate and
						// there is probably no other appropriate axiom
						System.out.println("Type a number (\"0\"-\""
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
						if(autoMode) {
							input = "n";
						} else {
							do {
								BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
								input = br.readLine();
							} while (!allowedInputs.contains(input));							
						}
					
						userInputProtocol += input;

							switch (input) {
								case "m":
									if (i == 0) {
										missesCount++;
									} else {
										missesCountSC++;
									}
									System.out
											.println("You said the algorithm missed a possible solution.");
									break;
								case "n":
									if (i == 0) {
										noSensibleDescriptionCount++;
									} else {
										noSensibleDescriptionCountSC++;
									}
									System.out
											.println("You said that there is no reasonable class expression.");
									break;
								default:
									int selectedNr = Integer.parseInt(input);
									EvaluatedDescriptionClass selectedExpression = suggestionsList
											.get(selectedNr);
									System.out.println("You selected \""
											+ selectedExpression.getDescription().toString() + "\".");
									boolean isConsistent = selectedExpression.isConsistent();
									if (!isConsistent) {
										System.out
												.println("Adding the expression leads to an inconsistency of the knowledge base (which is positive since it is a meaningful expression).");
									}
									// selectedExpression.getAdditionalInstances().
									Set<OWLIndividual> addInst = selectedExpression.getAdditionalInstances();
									int additionalInstances = addInst.size();
									if (additionalInstances > 0) {
										System.out.println("Adding the expression leads to "
												+ additionalInstances
												+ " new instances, e.g. \""
												+ addInst.iterator().next().toString(
										) + "\".");
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
										if (bestAcc < 0.9999) {
											nonPerfectCount++;
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
										if (bestAcc < 0.9999) {
											nonPerfectCountSC++;
										}
									}
									break;
							}
					}
					
										
				}
			}
		}

		// print summary
				
		
		 
         if(!f.exists())
         {
           try {
                   f.createNewFile();
               } catch (Exception e) {
                   e.printStackTrace();
               }
         }

       
               FileOutputStream fos = new FileOutputStream(f);
               PrintStream ps = new PrintStream(fos,true);
               System.setOut(ps);
       
      
       
		
		
		System.out.println();
		System.out
				.println("knowledge engineering process finished successfully - summary shown below");
		System.out.println();
//		System.out.println("ontology: " + args[0]);
		System.out.println("settings: " + minAccuracy + " min accuracy, " + minInstanceCount + " min instances, " + algorithmRuntimeInSeconds + "s algorithm runtime");
		System.out.println("user input protocol: " + userInputProtocol);
		System.out.println("classes in ontology: " + classes.size());
		System.out.println("classes with at least " + minInstanceCount + " instances: "
				+ classCandidatesCount);
		System.out.println("class expressions tested: " + classExpressionTestsStat.prettyPrint(""));
		if(computeApproxDiff) {
			System.out.println("approximation difference: " + approxDiffStat.prettyPrint());
		}
		System.out.println();

		System.out.println("statistics for equivalence axioms:");
		System.out.println("classes above " + (minAccuracy * 100) + "% threshold: "
				+ candidatesAboveThresholdCount);
		System.out.println("axioms learned succesfully: " + foundDescriptionCount);
		System.out.println("axioms missed: " + missesCount);
		System.out.println("class with no sensible axioms: " + noSensibleDescriptionCount);
		System.out.println("inconsistencies detected: " + inconsistencyDetected);
		System.out.println("additional instances found: " + moreInstancesCountStat.prettyPrint(""));
		System.out.println("average accuracy overall: " + accStat.prettyPrint(""));
		System.out.println("average accuracy of selected expressions: "
				+ accSelectedStat.prettyPrint(""));
		System.out.println("average accuracy of expressions above threshold: "
				+ accAboveThresholdStat.prettyPrint(""));
		System.out.println("non-perfect (not 100% accuracy) axioms selected: " + nonPerfectCount);
		System.out.println("average number typed by user: " + positionStat.prettyPrint(""));
		System.out.println();

		System.out.println("statistics for super class axioms:");
		System.out.println("classes above " + (minAccuracy * 100) + "% threshold: "
				+ candidatesAboveThresholdCountSC);
		System.out.println("axioms learned succesfully: " + foundDescriptionCountSC);
		System.out.println("axioms missed: " + missesCountSC);
		System.out.println("class with no sensible axioms: " + noSensibleDescriptionCountSC);
		System.out.println("inconsistencies detected: " + inconsistencyDetectedSC);
		System.out.println("additional instances found: " + moreInstancesCountStatSC.prettyPrint(""));
		System.out.println("average accuracy overall: " + accStatSC.prettyPrint(""));
		System.out.println("average accuracy of selected expressions: "
				+ accSelectedStatSC.prettyPrint(""));
		System.out.println("average accuracy of expressions above threshold: "
				+ accAboveThresholdStatSC.prettyPrint(""));
		System.out.println("non-perfect (not 100% accuracy) axioms selected: " + nonPerfectCountSC);	
		System.out.println("average number typed by user: " + positionStatSC.prettyPrint(""));
		System.out.println();
		
		System.out.println("merged statistics for equivalence/superclass:");
		System.out.println("classes above " + (minAccuracy * 100) + "% threshold: "
				+ (candidatesAboveThresholdCount+candidatesAboveThresholdCountSC));
		System.out.println("axioms learned succesfully: " + (foundDescriptionCount+foundDescriptionCountSC));
		System.out.println("axioms missed: " + (missesCount+missesCountSC));
		System.out.println("class with no sensible axioms: " + (noSensibleDescriptionCount+noSensibleDescriptionCountSC));
		System.out.println("inconsistencies detected: " + (inconsistencyDetected+inconsistencyDetectedSC));
		System.out.println("additional instances found: " + new Stat(moreInstancesCountStat,moreInstancesCountStatSC).prettyPrint(""));
		System.out.println("average accuracy overall: " + new Stat(accStat,accStatSC).prettyPrint(""));
		System.out.println("average accuracy of selected expressions: "
				+ new Stat(accSelectedStat,accSelectedStatSC).prettyPrint(""));
		System.out.println("average accuracy of expressions above threshold: "
				+ new Stat(accAboveThresholdStat,accAboveThresholdStatSC).prettyPrint(""));
		System.out.println("non-perfect (not 100% accuracy) axioms selected: " + (nonPerfectCount+nonPerfectCountSC));
		System.out.println("average number typed by user: " + new Stat(positionStat,positionStatSC).prettyPrint(""));
		System.out.println();		
		//out.close();
		ps.close();
	}

	@SuppressWarnings("unused")
	private static void shrinkSet(Set<?> set, int nrOfElements) {
		while (set.size() > nrOfElements) {
			Iterator<?> it = set.iterator();
			it.next();
			it.remove();
		}
	}
}