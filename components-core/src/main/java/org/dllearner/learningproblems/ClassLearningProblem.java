/**
 * Copyright (C) 2007-2012, Jens Lehmann
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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.learningproblems.Heuristics.HeuristicType;
import org.dllearner.utilities.Helper;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * The problem of learning the OWL class expression of an existing class
 * in an OWL ontology.
 * 
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "ClassLearningProblem", shortName = "clp", version = 0.6)
public class ClassLearningProblem extends AbstractClassExpressionLearningProblem<ClassScore> {
	
	private static Logger logger = LoggerFactory.getLogger(ClassLearningProblem.class);
    private long nanoStartTime;
    @ConfigOption(name="maxExecutionTimeInSeconds",defaultValue="10",description="Maximum execution time in seconds")
	private int maxExecutionTimeInSeconds = 10;
	
	@ConfigOption(name = "classToDescribe", description="class of which an OWL class expression should be learned", required=true)
	private OWLClass classToDescribe;
	
	private List<OWLIndividual> classInstances;
	private TreeSet<OWLIndividual> classInstancesSet;
	@ConfigOption(name="equivalence",defaultValue="true",description="Whether this is an equivalence problem (or superclass learning problem)")
	private boolean equivalence = true;

	@ConfigOption(name = "approxDelta", description = "The Approximate Delta", defaultValue = "0.05", required = false)	
	private double approxDelta = 0.05;
	
	@ConfigOption(name = "useApproximations", description = "Use Approximations", defaultValue = "false", required = false)
	private boolean useApproximations;
	
	// factor for higher weight on recall (needed for subclass learning)
	private double coverageFactor;
	
	@ConfigOption(name = "betaSC", description="beta index for F-measure in super class learning", required=false, defaultValue="3.0")
	private double betaSC = 3.0;
	
	@ConfigOption(name = "betaEq", description="beta index for F-measure in definition learning", required=false, defaultValue="1.0")	
	private double betaEq = 1.0;
	
	// instances of super classes excluding instances of the class itself
	private List<OWLIndividual> superClassInstances;
	// instances of super classes including instances of the class itself
	private List<OWLIndividual> classAndSuperClassInstances;
	// specific variables for generalised F-measure
	private TreeSet<OWLIndividual> negatedClassInstances;
	
    @ConfigOption(name = "accuracyMethod", description = "Specifies, which method/function to use for computing accuracy. Available measues are \"pred_acc\" (predictive accuracy), \"fmeasure\" (F measure), \"generalised_fmeasure\" (generalised F-Measure according to Fanizzi and d'Amato).",defaultValue = "pred_acc")
    private String accuracyMethod = "pred_acc";
    
	private HeuristicType heuristic = HeuristicType.AMEASURE;
	
	@ConfigOption(name = "checkConsistency", description = "whether to check for consistency of suggestions (when added to ontology)", required=false, defaultValue="true")
	private boolean checkConsistency = true;
	
	private OWLDataFactory df = new OWLDataFactoryImpl();
	@ConfigOption(name="useInstanceChecks",defaultValue="true",description="Whether to use instance checks (or get individiuals query)")
	private boolean useInstanceChecks = true;
	
	public ClassLearningProblem() {
		
	}
	
//	public ClassLearningProblemConfigurator getConfigurator(){
//		return configurator;
//	}	
	
	public ClassLearningProblem(AbstractReasonerComponent reasoner) {
		super(reasoner);
//		configurator = new ClassLearningProblemConfigurator(this);
	}
	
	/*
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		URLConfigOption classToDescribeOption = new URLConfigOption("classToDescribe", "class of which a OWLClassExpression should be learned", null, true, false);
		classToDescribeOption.setRefersToOWLClass(true);
		options.add(classToDescribeOption);
		StringConfigOption type = new StringConfigOption("type", "whether to learn an equivalence class or super class axiom","equivalence"); //  or domain/range of a property.
		type.setAllowedValues(new String[] {"equivalence", "superClass"}); // , "domain", "range"});
		options.add(type);	
		BooleanConfigOption approx = new BooleanConfigOption("useApproximations", "whether to use stochastic approximations for computing accuracy", true);
		options.add(approx);
		DoubleConfigOption approxAccuracy = new DoubleConfigOption("approxAccuracy", "accuracy of the approximation (only for expert use)", 0.05);
		options.add(approxAccuracy);
		StringConfigOption accMethod = new StringConfigOption("accuracyMethod", "Specifies, which method/function to use for computing accuracy.","standard"); //  or domain/range of a property.
		accMethod.setAllowedValues(new String[] {"standard", "fmeasure", "pred_acc", "generalised_fmeasure", "jaccard"});
		options.add(accMethod);
		BooleanConfigOption consistency = new BooleanConfigOption("checkConsistency", "Specify whether to check consistency for solution candidates. This is convenient for user interfaces, but can be performance intensive.", true);
		options.add(consistency);	
		options.add(CommonConfigOptions.maxExecutionTimeInSeconds(10));
		DoubleConfigOption betaSC = new DoubleConfigOption("betaSC", "Higher values of beta rate recall higher than precision or in other words, covering the instances of the class to describe is more important even at the cost of covering additional instances. The actual implementation depends on the selected heuristic. This values is used only for super class learning.", 3.0);
		options.add(betaSC);
		DoubleConfigOption betaEq = new DoubleConfigOption("betaEq", "Higher values of beta rate recall higher than precision or in other words, covering the instances of the class to describe is more important even at the cost of covering additional instances. The actual implementation depends on the selected heuristic. This values is used only for equivalence class learning.", 1.0);
		options.add(betaEq);
		return options;
	}
	*/

	public static String getName() {
		return "class learning problem";
	}	
	
	@Override
	public void init() throws ComponentInitException {
//		classToDescribe = df.getOWLClass(IRI.create(configurator.getClassToDescribe().toString());
//		useApproximations = configurator.getUseApproximations();
		
		String accM = accuracyMethod;
		if(accM.equals("standard")) {
			heuristic = HeuristicType.AMEASURE;
		} else if(accM.equals("fmeasure")) {
			heuristic = HeuristicType.FMEASURE;
		} else if(accM.equals("generalised_fmeasure")) {
			heuristic = HeuristicType.GEN_FMEASURE;
		} else if(accM.equals("jaccard")) {
			heuristic = HeuristicType.JACCARD;
		} else if(accM.equals("pred_acc")) {
			heuristic = HeuristicType.PRED_ACC;
		}
		
		if(useApproximations && heuristic.equals(HeuristicType.PRED_ACC)) {
			System.err.println("Approximating predictive accuracy is an experimental feature. USE IT AT YOUR OWN RISK. If you consider to use it for anything serious, please extend the unit tests at org.dllearner.test.junit.HeuristicTests first to verify that it works.");
		}		
		
		if(useApproximations && !(heuristic.equals(HeuristicType.PRED_ACC) || heuristic.equals(HeuristicType.AMEASURE) || heuristic.equals(HeuristicType.FMEASURE))) {
			throw new ComponentInitException("Approximations only supported for F-Measure or Standard-Measure. It is unsupported for \"" + heuristic + ".\"");
		}
		
//		useFMeasure = configurator.getAccuracyMethod().equals("fmeasure");
//		approxDelta = configurator.getApproxAccuracy();
		
		if(!getReasoner().getClasses().contains(classToDescribe)) {
			throw new ComponentInitException("The class \"" + classToDescribe + "\" does not exist. Make sure you spelled it correctly.");
		}
		
		classInstances = new LinkedList<OWLIndividual>(getReasoner().getIndividuals(classToDescribe));
		// sanity check
		if(classInstances.size() == 0) {
			throw new ComponentInitException("Class " + classToDescribe + " has 0 instances according to \"" + AnnComponentManager.getName(getReasoner().getClass()) + "\". Cannot perform class learning with 0 instances.");
		}
		
		classInstancesSet = new TreeSet<OWLIndividual>(classInstances);
//		equivalence = (configurator.getType().equals("equivalence"));
//		maxExecutionTimeInSeconds = configurator.getMaxExecutionTimeInSeconds();
		
		if(equivalence) {
			coverageFactor = betaEq;
		} else {
			coverageFactor = betaSC;
		}
		
		// we compute the instances of the super class to perform
		// optimisations later on
		Set<OWLClassExpression> superClasses = getReasoner().getSuperClasses(classToDescribe);
		TreeSet<OWLIndividual> superClassInstancesTmp = new TreeSet<OWLIndividual>(getReasoner().getIndividuals());
		for(OWLClassExpression superClass : superClasses) {
			superClassInstancesTmp.retainAll(getReasoner().getIndividuals(superClass));
		}
		// we create one list, which includes instances of the class (an instance of the class is also instance of all super classes) ...
		classAndSuperClassInstances = new LinkedList<OWLIndividual>(superClassInstancesTmp);
		// ... and a second list not including them
		superClassInstancesTmp.removeAll(classInstances);
		// since we use the instance list for approximations, we want to avoid
		// any bias through URI names, so we shuffle the list once pseudo-randomly
		superClassInstances = new LinkedList<OWLIndividual>(superClassInstancesTmp);
		Random rand = new Random(1);
		Collections.shuffle(classInstances, rand);
		Collections.shuffle(superClassInstances, rand);
		
		if(heuristic.equals(HeuristicType.GEN_FMEASURE)) {
			OWLClassExpression classToDescribeNeg = df.getOWLObjectComplementOf(classToDescribe);
			negatedClassInstances = new TreeSet<OWLIndividual>();
			for(OWLIndividual ind : superClassInstances) {
				if(getReasoner().hasType(classToDescribeNeg, ind)) {
					negatedClassInstances.add(ind);
				}
			}
//			System.out.println("negated class instances: " + negatedClassInstances);
		}
		
//		System.out.println(classInstances.size() + " " + superClassInstances.size());
	}
		
	@Override
	public ClassScore computeScore(OWLClassExpression description, double noise) {
		
		// TODO: reuse code to ensure that we never return inconsistent results
		// between getAccuracy, getAccuracyOrTooWeak and computeScore
		
		// overhang
		Set<OWLIndividual> additionalInstances = new TreeSet<OWLIndividual>();
		for(OWLIndividual ind : superClassInstances) {
			if(getReasoner().hasType(description, ind)) {
				additionalInstances.add(ind);
			}
		}
		
		// coverage
		Set<OWLIndividual> coveredInstances = new TreeSet<OWLIndividual>();
		for(OWLIndividual ind : classInstances) {
			if(getReasoner().hasType(description, ind)) {
				coveredInstances.add(ind);
			}
		}
		
		double recall = coveredInstances.size()/(double)classInstances.size();
		double precision = (additionalInstances.size() + coveredInstances.size() == 0) ? 0 : coveredInstances.size()/(double)(coveredInstances.size()+additionalInstances.size());
		// for each OWLClassExpression with less than 100% coverage, we check whether it is
		// leads to an inconsistent knowledge base
		
		double acc = 0;
		if(heuristic.equals(HeuristicType.FMEASURE)) {
			acc = Heuristics.getFScore(recall, precision, coverageFactor);
		} else if(heuristic.equals(HeuristicType.AMEASURE)) {
			acc = Heuristics.getAScore(recall, precision, coverageFactor);
		} else {
			// TODO: some superfluous instance checks are required to compute accuracy => 
			// move accuracy computation here if possible 
			acc = getAccuracyOrTooWeakExact(description, noise);
		}
		
		if(checkConsistency) {
			
			// we check whether the axiom already follows from the knowledge base
//			boolean followsFromKB = reasoner.isSuperClassOf(description, classToDescribe);			
			
//			boolean followsFromKB = equivalence ? reasoner.isEquivalentClass(description, classToDescribe) : reasoner.isSuperClassOf(description, classToDescribe);
			boolean followsFromKB = followsFromKB(description);
			
			// workaround due to a bug (see http://sourceforge.net/tracker/?func=detail&aid=2866610&group_id=203619&atid=986319)
//			boolean isConsistent = coverage >= 0.999999 || isConsistent(description);
			// (if the axiom follows, then the knowledge base remains consistent)
			boolean isConsistent = followsFromKB || isConsistent(description);
			
//			double acc = useFMeasure ? getFMeasure(coverage, protusion) : getAccuracy(coverage, protusion);
			return new ClassScore(coveredInstances, Helper.difference(classInstancesSet, coveredInstances), recall, additionalInstances, precision, acc, isConsistent, followsFromKB);
		
		} else {
			return new ClassScore(coveredInstances, Helper.difference(classInstancesSet, coveredInstances), recall, additionalInstances, precision, acc);
		}
	}	
	
	public boolean isEquivalenceProblem() {
		return equivalence;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#getAccuracy(org.dllearner.core.owl.Description)
	 */
	@Override
	public double getAccuracy(OWLClassExpression description, double noise) {
		// a noise value of 1.0 means that we never return too weak (-1.0) 
		return getAccuracyOrTooWeak(description, noise);
	}

	@Override
	public double getAccuracyOrTooWeak(OWLClassExpression description, double noise) {
		// delegates to the appropriate methods
		return useApproximations ? getAccuracyOrTooWeakApprox(description, noise) : getAccuracyOrTooWeakExact(description, noise);		
	}
	
	// instead of using the standard operation, we use optimisation
	// and approximation here
	public double getAccuracyOrTooWeakApprox(OWLClassExpression description, double noise) {
		if(heuristic.equals(HeuristicType.FMEASURE)) {
			// we abort when there are too many uncovered positives
			int maxNotCovered = (int) Math.ceil(noise*classInstances.size());
			int instancesCovered = 0;
			int instancesNotCovered = 0;
			
			for(OWLIndividual ind : classInstances) {
				if(getReasoner().hasType(description, ind)) {
					instancesCovered++;
				} else {
					instancesNotCovered ++;
					if(instancesNotCovered > maxNotCovered) {
						return -1;
					}
				}
			}	
			
			double recall = instancesCovered/(double)classInstances.size();
			
			int testsPerformed = 0;
			int instancesDescription = 0;
			
			for(OWLIndividual ind : superClassInstances) {

				if(getReasoner().hasType(description, ind)) {
					instancesDescription++;
				}
				testsPerformed++;
				
				// check whether approximation is sufficiently accurate
				double[] approx = Heuristics.getFScoreApproximation(instancesCovered, recall, coverageFactor, superClassInstances.size(), testsPerformed, instancesDescription);
				if(approx[1]<approxDelta) {
					return approx[0];
				}
				
			}		
			
			// standard computation (no approximation)
			double precision = instancesCovered/(double)(instancesDescription+instancesCovered);
//			if(instancesCovered + instancesDescription == 0) {
//				precision = 0;
//			}
			return Heuristics.getFScore(recall, precision, coverageFactor);
			
		} else if(heuristic.equals(HeuristicType.AMEASURE)) {
			// the F-MEASURE implementation is now separate (different optimisation
			// strategy)
			
			// we abort when there are too many uncovered positives
			int maxNotCovered = (int) Math.ceil(noise*classInstances.size());
			int instancesCovered = 0;
			int instancesNotCovered = 0;
			int total = 0;
			boolean estimatedA = false;
			
			double lowerBorderA = 0;
			int lowerEstimateA = 0;
			double upperBorderA = 1;
			int upperEstimateA = classInstances.size();
			
			for(OWLIndividual ind : classInstances) {
				if(getReasoner().hasType(description, ind)) {
					instancesCovered++;
				} else {
					instancesNotCovered ++;
					if(instancesNotCovered > maxNotCovered) {
						return -1;
					}
				}
				
				// approximation step (starting after 10 tests)
				total = instancesCovered + instancesNotCovered;
				if(total > 10) {
					// compute confidence interval
					double p1 = p1(instancesCovered, total);
					double p2 = p3(p1, total);
					lowerBorderA = Math.max(0, p1 - p2);
					upperBorderA = Math.min(1, p1 + p2);
					double size = upperBorderA - lowerBorderA;
					// if the interval has a size smaller than 10%, we can be confident
					if(size < 2 * approxDelta) {
						// we have to distinguish the cases that the accuracy limit is
						// below, within, or above the limit and that the mean is below
						// or above the limit
						double mean = instancesCovered/(double)total;
						
						// we can estimate the best possible concept to reach with downward refinement
						// by setting precision to 1 and recall = mean stays as it is
						double optimumEstimate = heuristic.equals(HeuristicType.FMEASURE) ? ((1+Math.sqrt(coverageFactor))*mean)/(Math.sqrt(coverageFactor)+1) : (coverageFactor*mean+1)/(double)(coverageFactor+1);
						
						// if the mean is greater than the required minimum, we can accept;
						// we also accept if the interval is small and close to the minimum
						// (worst case is to accept a few inaccurate descriptions)
						if(optimumEstimate > 1-noise-0.03) {
//								|| (upperBorderA > mean && size < 0.03)) {
							instancesCovered = (int) (instancesCovered/(double)total * classInstances.size());
							upperEstimateA = (int) (upperBorderA * classInstances.size());
							lowerEstimateA = (int) (lowerBorderA * classInstances.size());
							estimatedA = true;
							break;
						}
						
						// reject only if the upper border is far away (we are very
						// certain not to lose a potential solution)
//						if(upperBorderA + 0.1 < 1-noise) {
						double optimumEstimateUpperBorder = heuristic.equals(HeuristicType.FMEASURE) ? ((1+Math.sqrt(coverageFactor))*(upperBorderA+0.1))/(Math.sqrt(coverageFactor)+1) : (coverageFactor*(upperBorderA+0.1)+1)/(double)(coverageFactor+1);
						if(optimumEstimateUpperBorder < 1 - noise) {
							return -1;
						}
					}				
				}
			}	
			
			double recall = instancesCovered/(double)classInstances.size();
			
//			MonitorFactory.add("estimatedA","count", estimatedA ? 1 : 0);
//			MonitorFactory.add("aInstances","count", total);
			
			// we know that a definition candidate is always subclass of the
			// intersection of all super classes, so we test only the relevant instances
			// (leads to undesired effects for descriptions not following this rule,
			// but improves performance a lot);
			// for learning a superclass of a defined class, similar observations apply;


			int testsPerformed = 0;
			int instancesDescription = 0;
//			boolean estimatedB = false;
			
			for(OWLIndividual ind : superClassInstances) {

				if(getReasoner().hasType(description, ind)) {
					instancesDescription++;
				}
				
				testsPerformed++;
				
				if(testsPerformed > 10) {
					
					// compute confidence interval
					double p1 = p1(instancesDescription, testsPerformed);
					double p2 = p3(p1, testsPerformed);
					double lowerBorder = Math.max(0, p1 - p2);
					double upperBorder = Math.min(1, p1 + p2);
					int lowerEstimate = (int) (lowerBorder * superClassInstances.size());
					int upperEstimate = (int) (upperBorder * superClassInstances.size());
					
					double size;
					if(estimatedA) {
//						size = 1/(coverageFactor+1) * (coverageFactor * (upperBorderA-lowerBorderA) + Math.sqrt(upperEstimateA/(upperEstimateA+lowerEstimate)) + Math.sqrt(lowerEstimateA/(lowerEstimateA+upperEstimate)));
						size = heuristic.equals(HeuristicType.FMEASURE) ? getFMeasure(upperBorderA, upperEstimateA/(double)(upperEstimateA+lowerEstimate)) - getFMeasure(lowerBorderA, lowerEstimateA/(double)(lowerEstimateA+upperEstimate)) : Heuristics.getAScore(upperBorderA, upperEstimateA/(double)(upperEstimateA+lowerEstimate), coverageFactor) - Heuristics.getAScore(lowerBorderA, lowerEstimateA/(double)(lowerEstimateA+upperEstimate),coverageFactor);					
					} else {
//						size = 1/(coverageFactor+1) * (coverageFactor * coverage + Math.sqrt(instancesCovered/(instancesCovered+lowerEstimate)) + Math.sqrt(instancesCovered/(instancesCovered+upperEstimate)));
						size = heuristic.equals(HeuristicType.FMEASURE) ? getFMeasure(recall, instancesCovered/(double)(instancesCovered+lowerEstimate)) - getFMeasure(recall, instancesCovered/(double)(instancesCovered+upperEstimate)) : Heuristics.getAScore(recall, instancesCovered/(double)(instancesCovered+lowerEstimate),coverageFactor) - Heuristics.getAScore(recall, instancesCovered/(double)(instancesCovered+upperEstimate),coverageFactor);
					}
					
					if(size < 0.1) {
//						System.out.println(instancesDescription + " of " + testsPerformed);
//						System.out.println("interval from " + lowerEstimate + " to " + upperEstimate);
//						System.out.println("size: " + size);
						
//						estimatedB = true;
						// calculate total number of instances
						instancesDescription = (int) (instancesDescription/(double)testsPerformed * superClassInstances.size());
						break;
					}
				}
			}
			
			// since we measured/estimated accuracy only on instances outside A (superClassInstances
			// does not include instances of A), we need to add it in the denominator
			double precision = instancesCovered/(double)(instancesDescription+instancesCovered);
			if(instancesCovered + instancesDescription == 0) {
				precision = 0;
			}
			
			return heuristic.equals(HeuristicType.FMEASURE) ? getFMeasure(recall, precision) : Heuristics.getAScore(recall, precision, coverageFactor);
						
		} else if(heuristic.equals(HeuristicType.FMEASURE)) {
			int maxNotCovered = (int) Math.ceil(noise*classInstances.size());
			
			int notCoveredPos = 0;
//			int notCoveredNeg = 0;
			
			int posClassifiedAsPos = 0;
			int negClassifiedAsNeg = 0;
			
			int nrOfPosChecks = 0;
			int nrOfNegChecks = 0;
			
			// special case: we test positive and negative examples in turn
			Iterator<OWLIndividual> itPos = classInstances.iterator();
			Iterator<OWLIndividual> itNeg = superClassInstances.iterator();
			
			do {
				// in each loop we pick 0 or 1 positives and 0 or 1 negative
				// and classify it
				
				if(itPos.hasNext()) {
					OWLIndividual posExample = itPos.next();
//					System.out.println(posExample);
					
					if(getReasoner().hasType(description, posExample)) {
						posClassifiedAsPos++;
					} else {
						notCoveredPos++;
					}
					nrOfPosChecks++;
					
					// take noise into account
					if(notCoveredPos > maxNotCovered) {
						return -1;
					}
				}
				
				if(itNeg.hasNext()) {
					OWLIndividual negExample = itNeg.next();
					if(!getReasoner().hasType(description, negExample)) {
						negClassifiedAsNeg++;
					}
					nrOfNegChecks++;
				}
			
				// compute how accurate our current approximation is and return if it is sufficiently accurate
				double approx[] = Heuristics.getPredAccApproximation(classInstances.size(), superClassInstances.size(), 1, nrOfPosChecks, posClassifiedAsPos, nrOfNegChecks, negClassifiedAsNeg);
				if(approx[1]<approxDelta) {
//					System.out.println(approx[0]);
					return approx[0];
				}
				
			} while(itPos.hasNext() || itNeg.hasNext());
			
			double ret = Heuristics.getPredictiveAccuracy(classInstances.size(), superClassInstances.size(), posClassifiedAsPos, negClassifiedAsNeg, 1);
			return ret;			
		} else {
			throw new Error("Approximation for " + heuristic + " not implemented.");
		}
		
	}
	

	// exact computation for 5 heuristics; each one adapted to super class learning;
	// each one takes the noise parameter into account
	public double getAccuracyOrTooWeakExact(OWLClassExpression description, double noise) {

		nanoStartTime = System.nanoTime();
		
		if(heuristic.equals(HeuristicType.JACCARD)) {
			
			// computing R(A)
			TreeSet<OWLIndividual> coveredInstancesSet = new TreeSet<OWLIndividual>();
			for(OWLIndividual ind : classInstances) {
				if(getReasoner().hasType(description, ind)) {
					coveredInstancesSet.add(ind);
				}
				if(terminationTimeExpired()){
					return 0;
				}
			}				
			
			// if even the optimal case (no additional instances covered) is not sufficient, 
			// the concept is too weak
			if(coveredInstancesSet.size() / (double) classInstances.size() <= 1 - noise) {
				return -1;
			}
			
			// computing R(C) restricted to relevant instances
			TreeSet<OWLIndividual> additionalInstancesSet = new TreeSet<OWLIndividual>();
			for(OWLIndividual ind : superClassInstances) {
				if(getReasoner().hasType(description, ind)) {
					additionalInstancesSet.add(ind);
				}
				if(terminationTimeExpired()){
					return 0;
				}
			}
					
			Set<OWLIndividual> union = Helper.union(classInstancesSet, additionalInstancesSet);
			return Heuristics.getJaccardCoefficient(coveredInstancesSet.size(), union.size());
			
		} else if (heuristic.equals(HeuristicType.AMEASURE) || heuristic.equals(HeuristicType.FMEASURE) || heuristic.equals(HeuristicType.PRED_ACC)) {
			
			// computing R(C) restricted to relevant instances
			int additionalInstances = 0;
			if(useInstanceChecks) {
				for(OWLIndividual ind : superClassInstances) {
					if(getReasoner().hasType(description, ind)) {
						additionalInstances++;
					}
					if(terminationTimeExpired()){
						return 0;
					}
				}
			} else {
				SortedSet<OWLIndividual> individuals = getReasoner().getIndividuals(description);
				individuals.retainAll(superClassInstances);
				additionalInstances = individuals.size();
			}
			
			
			// computing R(A)
			int coveredInstances = 0;
			if(useInstanceChecks) {
				for(OWLIndividual ind : classInstances) {
					if(getReasoner().hasType(description, ind)) {
						coveredInstances++;
					}
					if(terminationTimeExpired()){
						return 0;
					}
				}
			} else {
				SortedSet<OWLIndividual> individuals = getReasoner().getIndividuals(description);
				individuals.retainAll(classInstances);
				coveredInstances = individuals.size();
			}
//			System.out.println(description + ":" + coveredInstances + "/" + classInstances.size());
			double recall = coveredInstances/(double)classInstances.size();
			
			// noise computation is incorrect
//			if(recall < 1 - noise) {
//				return -1;
//			}
			
			double precision = (additionalInstances + coveredInstances == 0) ? 0 : coveredInstances / (double) (coveredInstances + additionalInstances);

			if(heuristic.equals(HeuristicType.AMEASURE)) {
				// best reachable concept has same recall and precision 1:
				// 1/t+1 * (t*r + 1)
				if((coverageFactor*recall+1)/(double)(coverageFactor+1) <(1-noise)) {
					return -1;
				} else {
					return Heuristics.getAScore(recall, precision, coverageFactor);
				}
			} else if(heuristic.equals(HeuristicType.FMEASURE)) {
				// best reachable concept has same recall and precision 1:
				if(((1+Math.sqrt(coverageFactor))*recall)/(Math.sqrt(coverageFactor)+1)<1-noise) {
					return -1;
				} else {
					return Heuristics.getFScore(recall, precision, coverageFactor);
				}
			} else if(heuristic.equals(HeuristicType.PRED_ACC)) {
				if((coverageFactor * coveredInstances + superClassInstances.size()) / (double) (coverageFactor * classInstances.size() + superClassInstances.size()) < 1 -noise) {
					return -1;
				} else {
					// correctly classified divided by all examples
					return (coverageFactor * coveredInstances + superClassInstances.size() - additionalInstances) / (double) (coverageFactor * classInstances.size() + superClassInstances.size());					
				}
			}

//			return heuristic.equals(HeuristicType.FMEASURE) ? getFMeasure(recall, precision) : getAccuracy(recall, precision);			
		} else if (heuristic.equals(HeuristicType.GEN_FMEASURE)) {
			
			// implementation is based on:
			// http://sunsite.informatik.rwth-aachen.de/Publications/CEUR-WS/Vol-426/swap2008_submission_14.pdf
			// default negation should be turned off when using fast instance checker
			
			// compute I_C (negated and non-negated concepts separately)
			TreeSet<OWLIndividual> icPos = new TreeSet<OWLIndividual>();
			TreeSet<OWLIndividual> icNeg = new TreeSet<OWLIndividual>();
			OWLClassExpression descriptionNeg = df.getOWLObjectComplementOf(description);
			// loop through all relevant instances
			for(OWLIndividual ind : classAndSuperClassInstances) {
				if(getReasoner().hasType(description, ind)) {
					icPos.add(ind);
				} else if(getReasoner().hasType(descriptionNeg, ind)) {
					icNeg.add(ind);
				}
				if(terminationTimeExpired()){
					return 0;
				}
			}
			
			// semantic precision
			// first compute I_C \cap Cn(DC)
			// it seems that in our setting, we can ignore Cn, because the examples (class instances)
			// are already part of the background knowledge
			Set<OWLIndividual> tmp1Pos = Helper.intersection(icPos, classInstancesSet);
			Set<OWLIndividual> tmp1Neg = Helper.intersection(icNeg, negatedClassInstances);
			int tmp1Size = tmp1Pos.size() + tmp1Neg.size();
			
			// Cn(I_C) \cap D_C is the same set if we ignore Cn ...
			
			int icSize = icPos.size() + icNeg.size();
			double prec = (icSize == 0) ? 0 : tmp1Size / (double) icSize;
			double rec = tmp1Size / (double) (classInstances.size() + negatedClassInstances.size());
			
//			System.out.println(description);
			
//			System.out.println("I_C pos: " + icPos);
//			System.out.println("I_C neg: " + icNeg);
//			System.out.println("class instances: " + classInstances);
//			System.out.println("negated class instances: " + negatedClassInstances);
			
//			System.out.println(prec);
//			System.out.println(rec);
//			System.out.println(coverageFactor);
			
			// too weak: see F-measure above
			// => does not work for generalised F-measure, because even very general 
			// concepts do not have a recall of 1
//			if(((1+Math.sqrt(coverageFactor))*rec)/(Math.sqrt(coverageFactor)+1)<1-noise) {
//				return -1;
//			}
			// we only return too weak if there is no recall
			if(rec <= 0.0000001) {
				return -1;
			}
			
			return getFMeasure(rec,prec);
		}
		
		throw new Error("ClassLearningProblem error: not implemented");
	}
	
	private boolean terminationTimeExpired(){
		boolean val = ((System.nanoTime() - nanoStartTime) >= (maxExecutionTimeInSeconds*1000000000l));
		if(val) {
			logger.warn("Description test aborted, because it took longer than " + maxExecutionTimeInSeconds + " seconds.");
		}
		return val;
	}
	
//	@Deprecated
//	public double getAccuracyOrTooWeakStandard(OWLClassExpression description, double minAccuracy) {
//		// since we have to perform a retrieval operation anyway, we cannot easily
//		// get a benefit from the accuracy limit
//		double accuracy = getAccuracy(description);
//		if(accuracy >= minAccuracy) {
//			return accuracy;
//		} else {
//			return -1;
//		}
//	}
	
	// please note that getting recall and precision wastes some computational
	// resource, because both methods need to compute the covered instances
	public double getRecall(OWLClassExpression description) {
		int coveredInstances = 0;
		for(OWLIndividual ind : classInstances) {
			if(getReasoner().hasType(description, ind)) {
				coveredInstances++;
			}
		}		
		return coveredInstances/(double)classInstances.size();
	}
	
	public double getPrecision(OWLClassExpression description) {

		int additionalInstances = 0;
		for(OWLIndividual ind : superClassInstances) {
			if(getReasoner().hasType(description, ind)) {
				additionalInstances++;
			}
		}
		
		int coveredInstances = 0;
		for(OWLIndividual ind : classInstances) {
			if(getReasoner().hasType(description, ind)) {
				coveredInstances++;
			}
		}

		return (additionalInstances + coveredInstances == 0) ? 0 : coveredInstances / (double) (coveredInstances + additionalInstances);
	}
	
	public double getPredictiveAccuracy() {
		return 0;
	}
	
	// see http://sunsite.informatik.rwth-aachen.de/Publications/CEUR-WS/Vol-426/swap2008_submission_14.pdf
	// for all methods below (currently dummies)
	public double getMatchRate() {
		return 0;
	}
	
	public double getOmissionError() {
		return 0;
	}
	
	public double getInductionRate() {
		return 0;
	}
	
	public double getComissionError() {
		return 0;
	}
	
	public double getGeneralisedRecall() {
		return 0;
	}	
	
	public double getGeneralisedPrecision() {
		return 0;
	}		
	
	@SuppressWarnings("unused")
	private double getInverseJaccardDistance(TreeSet<OWLIndividual> set1, TreeSet<OWLIndividual> set2) {
		Set<OWLIndividual> intersection = Helper.intersection(set1, set2);
		Set<OWLIndividual> union = Helper.union(set1, set2);
		return 1 - (union.size() - intersection.size()) / (double) union.size();
	}
	
	// computes accuracy from coverage and protusion (changing this function may
	// make it necessary to change the appoximation too) => not the case anymore
//	private double getAccuracy(double recall, double precision) {
//		return (coverageFactor * coverage + Math.sqrt(protusion)) / (coverageFactor + 1);
		// log: changed from precision^^0.5 (root) to precision^^0.8 as the root is too optimistic in some cases
//		return (coverageFactor * recall + Math.pow(precision, 0.8)) / (coverageFactor + 1);
//	}
	
	private double getFMeasure(double recall, double precision) {
		// balanced F measure
//		return (precision + recall == 0) ? 0 : 2 * precision * recall / (precision + recall);
		// see e.g. http://en.wikipedia.org/wiki/F-measure
		return (precision + recall == 0) ? 0 :
		  ( (1+Math.sqrt(coverageFactor)) * (precision * recall)
				/ (Math.sqrt(coverageFactor) * precision + recall) ); 
	}
	
	// see paper: expression used in confidence interval estimation
	public static double p3(double p1, int total) {
		return 1.96 * Math.sqrt(p1*(1-p1)/(total+4));
	}		
	
	// see paper: expression used in confidence interval estimation
//	private static double p2(int success, int total) {
//		double p1 = p1(success, total);
//		return 1.96 * Math.sqrt(p1*(1-p1)/(total+4));
//	}	
	
	// see paper: p'
	public static double p1(int success, int total) {
		return (success+2)/(double)(total+4);
	}
	
	/**
	 * @return the classToDescribe
	 */
	public OWLClass getClassToDescribe() {
		return classToDescribe;
	}

	public void setClassToDescribe(OWLClass classToDescribe) {
		this.classToDescribe = classToDescribe;
	}
	
	public void setClassToDescribe(IRI classIRI) {
		this.classToDescribe = df.getOWLClass(classIRI);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#evaluate(org.dllearner.core.owl.Description)
	 */
	@Override
	public EvaluatedDescriptionClass evaluate(OWLClassExpression description, double noise) {
		ClassScore score = computeScore(description, noise);
		return new EvaluatedDescriptionClass(description, score);
	}

	/**
	 * @return the isConsistent
	 */
	public boolean isConsistent(OWLClassExpression description) {
		OWLAxiom axiom;
		if(equivalence) {
			axiom = df.getOWLEquivalentClassesAxiom(classToDescribe, description);
		} else {
			axiom = df.getOWLSubClassOfAxiom(classToDescribe, description);
		}
		return getReasoner().remainsSatisfiable(axiom);
	}
	
	public boolean followsFromKB(OWLClassExpression description) {
		return equivalence ? getReasoner().isEquivalentClass(description, classToDescribe) : getReasoner().isSuperClassOf(description, classToDescribe);
	}

	public int getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}

	public boolean isEquivalence() {
		return equivalence;
	}

	public void setEquivalence(boolean equivalence) {
		this.equivalence = equivalence;
	}

	public boolean isUseApproximations() {
		return useApproximations;
	}

	public void setUseApproximations(boolean useApproximations) {
		this.useApproximations = useApproximations;
	}

	public HeuristicType getHeuristic() {
		return heuristic;
	}

	public void setHeuristic(HeuristicType heuristic) {
		this.heuristic = heuristic;
		
		if(heuristic == HeuristicType.AMEASURE) {
			accuracyMethod = "standard";
		} else if(heuristic == HeuristicType.FMEASURE) {
			accuracyMethod = "fmeasure";
		} else if(heuristic == HeuristicType.GEN_FMEASURE) {
			accuracyMethod = "generalised_fmeasure";
		} else if(heuristic == HeuristicType.JACCARD) {
			accuracyMethod = "jaccard";
		} else if(heuristic == HeuristicType.PRED_ACC) {
			accuracyMethod = "pred_acc";
		} 
	}

	public double getApproxDelta() {
		return approxDelta;
	}

	public void setApproxDelta(double approxDelta) {
		this.approxDelta = approxDelta;
	}

	public double getBetaSC() {
		return betaSC;
	}

	public void setBetaSC(double betaSC) {
		this.betaSC = betaSC;
	}

	public double getBetaEq() {
		return betaEq;
	}

	public void setBetaEq(double betaEq) {
		this.betaEq = betaEq;
	}

	public boolean isCheckConsistency() {
		return checkConsistency;
	}

	public void setCheckConsistency(boolean checkConsistency) {
		this.checkConsistency = checkConsistency;
	}

	public String getAccuracyMethod() {
		return accuracyMethod;
	}

	public void setAccuracyMethod(String accuracyMethod) {
		this.accuracyMethod = accuracyMethod;
	}
	
	/**
	 * @param useInstanceChecks the useInstanceChecks to set
	 */
	public void setUseInstanceChecks(boolean useInstanceChecks) {
		this.useInstanceChecks = useInstanceChecks;
	}
	
	/**
	 * @return the useInstanceChecks
	 */
	public boolean isUseInstanceChecks() {
		return useInstanceChecks;
	}
}
