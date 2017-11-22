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

import com.google.common.collect.Sets;
import org.dllearner.accuracymethods.*;
import org.dllearner.core.*;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.utilities.ReasoningUtils;
import org.dllearner.utilities.ReasoningUtils.Coverage;
import org.dllearner.utilities.ReasoningUtilsCLP;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;

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
	@ConfigOption(defaultValue = "10",
	              description = "Maximum execution time in seconds")
	private int maxExecutionTimeInSeconds = 10;

	@ConfigOption(description = "class of which an OWL class expression should be learned",
	              required = true)
	private OWLClass classToDescribe;

	private List<OWLIndividual> classInstances;
	private TreeSet<OWLIndividual> classInstancesSet;
	@ConfigOption(defaultValue = "true",
	              description = "Whether this is an equivalence problem (or superclass learning problem)")
	private boolean equivalence = true;

	@ConfigOption(description = "beta index for F-measure in super class learning",
	              required = false,
	              defaultValue = "3.0")
	private double betaSC = 3.0;

	@ConfigOption(description = "beta index for F-measure in definition learning",
	              required = false,
	              defaultValue = "1.0")
	private double betaEq = 1.0;

	// instances of super classes excluding instances of the class itself
	private List<OWLIndividual> superClassInstances;
	// instances of super classes including instances of the class itself
	private List<OWLIndividual> classAndSuperClassInstances;
	// specific variables for generalised F-measure
	private TreeSet<OWLIndividual> negatedClassInstances;

	@ConfigOption(description = "Specifies, which method/function to use for computing accuracy. Available measues are \"pred_acc\" (predictive accuracy), \"fmeasure\" (F measure), \"generalised_fmeasure\" (generalised F-Measure according to Fanizzi and d'Amato).",
	              defaultValue = "PRED_ACC")
	protected AccMethod accuracyMethod;

	@ConfigOption(description = "whether to check for consistency of suggestions (when added to ontology)",
	              required = false,
	              defaultValue = "true")
	private boolean checkConsistency = true;

	private OWLDataFactory df = new OWLDataFactoryImpl();

	public ClassLearningProblem() {

	}

	@Override
	protected ReasoningUtils newReasoningUtils(AbstractReasonerComponent reasoner) {
		return new ReasoningUtilsCLP(this, reasoner);
	}

	public ClassLearningProblem(AbstractReasonerComponent reasoner) {
		super(reasoner);
	}

	@Override
	public void init() throws ComponentInitException {

		if (accuracyMethod != null && accuracyMethod instanceof AccMethodPredAccApprox) {
			logger.warn("Approximating predictive accuracy is an experimental feature. USE IT AT YOUR OWN RISK. If you consider to use it for anything serious, please extend the unit tests at org.dllearner.test.junit.HeuristicTests first to verify that it works.");
		}

		if (!getReasoner().getClasses().contains(classToDescribe)) {
			throw new ComponentInitException("The class \"" + classToDescribe + "\" does not exist. Make sure you spelled it correctly.");
		}

		classInstances = new LinkedList<>(getReasoner().getIndividuals(classToDescribe));
		// sanity check
		if (classInstances.size() == 0) {
			throw new ComponentInitException("Class " + classToDescribe + " has 0 instances according to \"" + AnnComponentManager.getName(getReasoner().getClass()) + "\". Cannot perform class learning with 0 instances.");
		}

		classInstancesSet = new TreeSet<>(classInstances);

		double coverageFactor;
		if (equivalence) {
			coverageFactor = betaEq;
		} else {
			coverageFactor = betaSC;
		}

		// we compute the instances of the super class to perform
		// optimisations later on
		Set<OWLClassExpression> superClasses = getReasoner().getSuperClasses(classToDescribe);
		TreeSet<OWLIndividual> superClassInstancesTmp = new TreeSet<>(getReasoner().getIndividuals());
		for (OWLClassExpression superClass : superClasses) {
			superClassInstancesTmp.retainAll(getReasoner().getIndividuals(superClass));
		}
		// we create one list, which includes instances of the class (an instance of the class is also instance of all super classes) ...
		classAndSuperClassInstances = new LinkedList<>(superClassInstancesTmp);
		// ... and a second list not including them
		superClassInstancesTmp.removeAll(classInstances);
		// since we use the instance list for approximations, we want to avoid
		// any bias through URI names, so we shuffle the list once pseudo-randomly
		superClassInstances = new LinkedList<>(superClassInstancesTmp);
		Random rand = new Random(1);
		Collections.shuffle(classInstances, rand);
		Collections.shuffle(superClassInstances, rand);

		if (accuracyMethod == null) {
			accuracyMethod = new AccMethodPredAcc(true);
		}
		if (accuracyMethod instanceof AccMethodApproximate) {
			((AccMethodApproximate) accuracyMethod).setReasoner(getReasoner());
		}
		if (accuracyMethod instanceof AccMethodThreeValued) {
			Coverage[] cc = reasoningUtil.getCoverage(df.getOWLObjectComplementOf(classToDescribe), superClassInstances);
			negatedClassInstances = Sets.newTreeSet(cc[0].trueSet);
//			System.out.println("negated class instances: " + negatedClassInstances);
		}
		if (accuracyMethod instanceof AccMethodWithBeta) {
			((AccMethodWithBeta)accuracyMethod).setBeta(coverageFactor);
		}

//		System.out.println(classInstances.size() + " " + superClassInstances.size());
		
		initialized = true;
	}

	@Override
	public ClassScore computeScore(OWLClassExpression description, double noise) {

		// TODO: reuse code to ensure that we never return inconsistent results
		// between getAccuracy, getAccuracyOrTooWeak and computeScore
		Coverage[] cc = ((ReasoningUtilsCLP)reasoningUtil).getCoverageCLP(description, classInstances, superClassInstances);

		double recall = Heuristics.divideOrZero(cc[0].trueCount, classInstances.size()); // tp / (tp+fn)
		double precision = Heuristics.divideOrZero(cc[0].trueCount, cc[0].trueCount + cc[1].trueCount); // tp / (tp+fp)
		// for each OWLClassExpression with less than 100% coverage, we check whether it is
		// leads to an inconsistent knowledge base

		double acc;
		if (accuracyMethod instanceof AccMethodTwoValued) {
			acc = reasoningUtil.getAccuracyOrTooWeakExact2((AccMethodTwoValued) accuracyMethod, cc, noise);
		} else if (accuracyMethod instanceof AccMethodThreeValued) {
			acc = ((ReasoningUtilsCLP)reasoningUtil).getAccuracyOrTooWeakExact3((AccMethodThreeValued) accuracyMethod, description, classInstances, superClassInstances, negatedClassInstances, noise);
		} else {
			throw new RuntimeException();
		}

		if (checkConsistency) {

			// we check whether the axiom already follows from the knowledge base
			boolean followsFromKB = followsFromKB(description);

			// workaround due to a bug (see http://sourceforge.net/tracker/?func=detail&aid=2866610&group_id=203619&atid=986319)
			// (if the axiom follows, then the knowledge base remains consistent)
			boolean isConsistent = followsFromKB || isConsistent(description);

			return new ClassScore(cc[0].trueSet, cc[0].falseSet, recall, cc[1].trueSet, precision, acc, isConsistent, followsFromKB);

		} else {
			return new ClassScore(cc[0].trueSet, cc[0].falseSet, recall, cc[1].trueSet, precision, acc);
		}
	}

	public boolean isEquivalenceProblem() {
		return equivalence;
	}

	@Override
	public double getAccuracyOrTooWeak(OWLClassExpression description, double noise) {
		nanoStartTime = System.nanoTime();
		if (accuracyMethod instanceof AccMethodThreeValued) {
			return ((ReasoningUtilsCLP)reasoningUtil).getAccuracyOrTooWeak3((AccMethodThreeValued) accuracyMethod, description, classInstances, superClassInstances, negatedClassInstances, noise);
		} else if (accuracyMethod instanceof  AccMethodTwoValued) {
			return reasoningUtil.getAccuracyOrTooWeak2((AccMethodTwoValued) accuracyMethod, description, classInstances, superClassInstances, noise);
		} else {
			throw new RuntimeException();
		}
	}

	/**
	 * @return whether the description test should be aborted because time expired
	 */
	public boolean terminationTimeExpired() {
		boolean val = ((System.nanoTime() - nanoStartTime) >= (maxExecutionTimeInSeconds * 1000000000L));
		if (val) {
			logger.warn("Description test aborted, because it took longer than " + maxExecutionTimeInSeconds + " seconds.");
		}
		return val;
	}

	// see http://sunsite.informatik.rwth-aachen.de/Publications/CEUR-WS/Vol-426/swap2008_submission_14.pdf
	// for all methods below (currently dummies)

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
		if (equivalence) {
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

	public AccMethod getAccuracyMethod() {
		return accuracyMethod;
	}

	@Autowired(required = false)
	public void setAccuracyMethod(AccMethod accuracyMethod) {
		this.accuracyMethod = accuracyMethod;
	}

	public double getRecall(OWLClassExpression description) {
		ReasoningUtils.CoverageCount[] cc = reasoningUtil.getCoverageCount(description, classInstancesSet);
		if (cc == null) {
			return 0;
		}
		return cc[0].trueCount/(double)cc[0].total;
	}
}

