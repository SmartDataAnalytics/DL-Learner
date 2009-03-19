/**
 * Copyright (C) 2007-2009, Jens Lehmann
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
package org.dllearner.learningproblems;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.algorithms.EvaluatedDescriptionClass;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.configurators.ClassLearningProblemConfigurator;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.StringConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.utilities.Helper;

/**
 * The problem of learning the description of an existing class
 * in an OWL ontology.
 * 
 * @author Jens Lehmann
 *
 */
public class ClassLearningProblem extends LearningProblem {

	private NamedClass classToDescribe;
	private Set<Individual> classInstances;
	private boolean equivalence = true;
	private ClassLearningProblemConfigurator configurator;
	
	// factor for higher weight on coverage (needed for subclass learning)
	private double coverageFactor;
	
	// instances of super classes excluding instances of the class itself
	private List<Individual> superClassInstances;
	
	@Override
	public ClassLearningProblemConfigurator getConfigurator(){
		return configurator;
	}	
	
	public ClassLearningProblem(ReasonerComponent reasoner) {
		super(reasoner);
		configurator = new ClassLearningProblemConfigurator(this);
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new StringConfigOption("classToDescribe", "class of which a description should be learned", null, true, false));
		StringConfigOption type = new StringConfigOption("type", "Whether to learn an equivalence class or super class axiom or domain/range of a property.","equivalence");
		type.setAllowedValues(new String[] {"equivalence", "superClass", "domain", "range"});
		options.add(type);		
		return options;
	}

	public static String getName() {
		return "class learning problem";
	}	
	
	@Override
	public void init() throws ComponentInitException {
		classToDescribe = new NamedClass(configurator.getClassToDescribe());
		if(!reasoner.getNamedClasses().contains(classToDescribe)) {
			throw new ComponentInitException("The class \"" + configurator.getClassToDescribe() + "\" does not exist. Make sure you spelled it correctly.");
		}
		
		classInstances = reasoner.getIndividuals(classToDescribe);
		equivalence = (configurator.getType().equals("equivalence"));
		
		if(equivalence) {
			coverageFactor = 1;
		} else {
			coverageFactor = 3;
		}
		
		// we compute the instances of the super class to perform
		// optimisations later on
		Set<Description> superClasses = reasoner.getClassHierarchy().getSuperClasses(classToDescribe);
		TreeSet<Individual> superClassInstancesTmp = new TreeSet<Individual>(reasoner.getIndividuals());
		for(Description superClass : superClasses) {
			superClassInstancesTmp.retainAll(reasoner.getIndividuals(superClass));
		}
		superClassInstancesTmp.removeAll(classInstances);
		// since we use the instance list for approximations, we want to avoid
		// any bias through URI names, so we shuffle the list once pseudo-randomly
		superClassInstances = new LinkedList<Individual>(superClassInstancesTmp);
		Random rand = new Random(1);
		Collections.shuffle(superClassInstances, rand);
	}
	
	/**
	 * Computes the fraction of the instances of the class to learn, which 
	 * is covered by the given description.
	 * @param description The description for which to compute coverage.
	 * @return The class coverage (between 0 and 1).
	 */
//	public double getCoverage(Description description) {
//		int instancesCovered = 0;
//		for(Individual instance : classInstances) {
//			if(reasoner.hasType(description, instance)) {
//				instancesCovered++;
//			}
//		}
//		return instancesCovered/(double)classInstances.size();
//	}
	
	@Override
	public ClassScore computeScore(Description description) {
		Set<Individual> retrieval = reasoner.getIndividuals(description);
		
		Set<Individual> coveredInstances = new TreeSet<Individual>();
		
		int instancesCovered = 0;
		
		for(Individual ind : classInstances) {
			if(retrieval.contains(ind)) {
				instancesCovered++;
				coveredInstances.add(ind);
			}
		}
		
		Set<Individual> additionalInstances = Helper.difference(retrieval, coveredInstances);		
		
		double coverage = instancesCovered/(double)classInstances.size();
		double protusion = retrieval.size() == 0 ? 0 : instancesCovered/(double)retrieval.size();
//		double accuracy = coverage + Math.sqrt(protusion);
		
		return new ClassScore(coveredInstances, coverage, additionalInstances, protusion, getAccuracy(coverage, protusion));
	}	
	
	public boolean isEquivalenceProblem() {
		return equivalence;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#getAccuracy(org.dllearner.core.owl.Description)
	 */
	@Override
	public double getAccuracy(Description description) {
		Set<Individual> retrieval = reasoner.getIndividuals(description);
		int instancesCovered = 0;
		
		for(Individual ind : classInstances) {
			if(retrieval.contains(ind)) {
				instancesCovered++;
			}
		}
		
		double coverage = instancesCovered/(double)classInstances.size();
//		double protusion = instancesCovered/(double)retrieval.size();
		double protusion = retrieval.size() == 0 ? 0 : instancesCovered/(double)retrieval.size();
//				
		
//		return (coverageFactor * coverage + protusion) / (coverageFactor + 1);
		return getAccuracy(coverage, protusion);
	}

	@Override
	public double getAccuracyOrTooWeak(Description description, double minAccuracy) {
		// instead of using the standard operation, we use optimisation
		// and approximation here
		
		// we abort when there are too many uncovered positives
		int maxNotCovered = (int) Math.ceil(minAccuracy*classInstances.size());
		int instancesCovered = 0;
		int instancesNotCovered = 0;
		
		for(Individual ind : classInstances) {
			if(reasoner.hasType(description, ind)) {
				instancesCovered++;
//				System.out.println("covered");
			} else {
//				System.out.println(ind + " not covered.");
				instancesNotCovered ++;
				if(instancesNotCovered > maxNotCovered) {
					return -1;
				}
			}
		}	
		
		double coverage = instancesCovered/(double)classInstances.size();
		
		// we know that a definition candidate is always subclass of the
		// intersection of all super classes, so we test only the relevent instances
		// (leads to undesired effects for descriptions not following this rule,
		// but improves performance a lot);
		// for learning a superclass of a defined class, similar observations apply;

		// we only test 10 * instances covered; while this is only an
		// approximation, it is unlikely that further tests will have any
		// significant impact on the overall accuracy
		int maxTests = 10 * instancesCovered;
//		int tests = Math.min(maxTests, superClassInstances.size());
		int testsPerformed = 0;
		int instancesDescription = 0;
		
		for(Individual ind : superClassInstances) {
			
//			System.out.println(ind);
			
			if(reasoner.hasType(description, ind)) {
//				System.out.println("ind: " + ind);
				instancesDescription++;
			}
			
			testsPerformed++;
			
			if(testsPerformed > maxTests) {
//				System.out.println(testsPerformed);
//				System.out.println("estimating accuracy by random sampling");
				// estimate for the number of instances of the description
				instancesDescription = (int) (instancesDescription/(double)testsPerformed * superClassInstances.size());
				break;
			}
		}
		
//		System.out.println(description);
//		System.out.println("A and C: " + instancesCovered);
//		System.out.println("instances description: " + instancesDescription);
		
		// since we measured/estimated accuracy only on instances outside A (superClassInstances
		// does not include instances of A), we need to add it in the denominator
		double protusion = instancesCovered/(double)(instancesDescription+instancesCovered);
		if(instancesCovered + instancesDescription == 0) {
			protusion = 0;
		}
		
//		System.out.println(description);
//		System.out.println(instancesDescription);
//		System.out.println("prot: " + protusion);
		
//		double acc = (coverageFactor * coverage + protusion) / (coverageFactor + 1);
		
//		System.out.println("acc: " + acc);
		
		return getAccuracy(coverage, protusion);
	}	
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#getAccuracyOrTooWeak(org.dllearner.core.owl.Description, double)
	 */
	public double getAccuracyOrTooWeakStandard(Description description, double minAccuracy) {
		// since we have to perform a retrieval operation anyway, we cannot easily
		// get a benefit from the accuracy limit
		double accuracy = getAccuracy(description);
		if(accuracy >= minAccuracy) {
			return accuracy;
		} else {
			return -1;
		}
	}

	private double getAccuracy(double coverage, double protusion) {
		return (coverageFactor * coverage + Math.sqrt(protusion)) / (coverageFactor + 1);
	}
	
	/**
	 * @return the classToDescribe
	 */
	public NamedClass getClassToDescribe() {
		return classToDescribe;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#evaluate(org.dllearner.core.owl.Description)
	 */
	@Override
	public EvaluatedDescription evaluate(Description description) {
		ClassScore score = computeScore(description);
		return new EvaluatedDescriptionClass(description, score);
	}
}
