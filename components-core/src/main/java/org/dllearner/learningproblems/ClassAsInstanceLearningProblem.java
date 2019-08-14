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
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.*;
import org.dllearner.core.StringRenderer.Rendering;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.Heuristics.HeuristicType;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.io.File;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A learning problem in which positive and negative examples are classes, i.e.
 * the whole learning is done on the schema level.
 * 
 * Instead of doing instance checks to compute the quality of a given class
 * expression, we
 * check for subclass relationship.
 * 
 * @author Lorenz Buehmann
 *
 */
@ComponentAnn(name = "Class as Instance LP", shortName = "classasinstance", version = 0.1)
public class ClassAsInstanceLearningProblem extends AbstractClassExpressionLearningProblem<ScorePosNeg<OWLClass>> {

	private static final Logger logger = LoggerFactory.getLogger(ClassAsInstanceLearningProblem.class);

	@ConfigOption(description = "Percent Per Length Unit", defaultValue = "0.05", required = false)
	private double percentPerLengthUnit = 0.05;

	private HeuristicType heuristic = HeuristicType.PRED_ACC;

	@ConfigOption()
	protected Set<OWLClass> positiveExamples = new TreeSet<>();
	@ConfigOption()
	protected Set<OWLClass> negativeExamples = new TreeSet<>();
	
	

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		if (positiveExamples.isEmpty()) {
			logger.warn("No positive examples set");
		}
		if (negativeExamples.isEmpty()) {
			logger.warn("No negative examples set");
		}
		if (reasoner != null) {
			Set<OWLClass> allClasses = reasoner.getClasses();
			Set<OWLClass> allExamples = Sets.union(positiveExamples, negativeExamples);
			if (!allClasses.containsAll(allExamples)) {
				Set<OWLClass> missing = Sets.difference(allExamples, allClasses);
				double percentage = (double) missing.size() / allExamples.size();
				percentage = Math.round(percentage * 1000.0) / 1000.0;
				String str =
						"The examples (" + (percentage * 100) + " % of total) " +
								"below are not contained in the knowledge base " +
								"(check spelling and prefixes)\n";
				str += missing.toString();

				if(missing.size()==allExamples.size())    {
					throw new ComponentInitException(str);
				} else if(percentage < 0.10) {
					logger.warn(str);
				} else {
					logger.error(str);
				}
			}
		}

		initialized = true;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractLearningProblem#computeScore(org.dllearner.core.owl.Description)
	 */
	@Override
	public ScorePosNeg<OWLClass> computeScore(OWLClassExpression description, double noise) {
		SortedSet<OWLClass> posAsPos = new TreeSet<>();
		SortedSet<OWLClass> posAsNeg = new TreeSet<>();
		SortedSet<OWLClass> negAsPos = new TreeSet<>();
		SortedSet<OWLClass> negAsNeg = new TreeSet<>();

		// for each positive example, we check whether it is a subclass of the given concept
		for (OWLClass example : positiveExamples) {
			if (getReasoner().isSuperClassOf(description, example)) {
				posAsPos.add(example);
			} else {
				posAsNeg.add(example);
			}
		}
		// for each negative example, we check whether it is not a subclass of the given concept
		for (OWLClass example : negativeExamples) {
			if (getReasoner().isSuperClassOf(description, example)) {
				negAsPos.add(example);
			} else {
				negAsNeg.add(example);
			}
		}

		// compute the accuracy
		double accuracy = getAccuracyOrTooWeak(description);

		return new ScoreTwoValued<>(OWLClassExpressionUtils.getLength(description), percentPerLengthUnit, posAsPos, posAsNeg,
				negAsPos, negAsNeg, accuracy);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractLearningProblem#evaluate(org.dllearner.core.owl.Description)
	 */
	@Override
	public EvaluatedDescription evaluate(OWLClassExpression description) {
		ScorePosNeg<OWLClass> score = computeScore(description);
		return new EvaluatedDescriptionPosNeg(description, score);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractLearningProblem#getAccuracyOrTooWeak(org.dllearner.core.owl.Description, double)
	 */
	@Override
	public double getAccuracyOrTooWeak(OWLClassExpression description, double noise) {
		return getAccuracyOrTooWeakExact(description, noise);
	}

	public double getAccuracyOrTooWeakExact(OWLClassExpression description, double noise) {
		switch (heuristic) {
		case PRED_ACC:
			return getPredAccuracyOrTooWeakExact(description, noise);
		case FMEASURE:
			return getFMeasureOrTooWeakExact(description, noise);
		default:
			throw new Error("Heuristic " + heuristic + " not implemented.");
		}
	}

	public double getPredAccuracyOrTooWeakExact(OWLClassExpression description, double noise) {

		int maxNotCovered = (int) Math.ceil(noise * positiveExamples.size());

		int notCoveredPos = 0;
		int notCoveredNeg = 0;

		for (OWLClass example : positiveExamples) {
			if (!getReasoner().isSuperClassOf(description, example)) {
				notCoveredPos++;

				if (notCoveredPos >= maxNotCovered) {
					return -1;
				}
			}
		}
		for (OWLClass example : negativeExamples) {
			if (!getReasoner().isSuperClassOf(description, example)) {
				notCoveredNeg++;
			}
		}

		int tp = positiveExamples.size() - notCoveredPos;
		int tn = notCoveredNeg;
		int fp = notCoveredPos;
		int fn = negativeExamples.size() - notCoveredNeg;

		return (tp + tn) / (double) (tp + fp + tn + fn);
	}

	public double getFMeasureOrTooWeakExact(OWLClassExpression description, double noise) {
		int additionalInstances = 0;
		for (OWLClass example : negativeExamples) {
			if (getReasoner().isSuperClassOf(description, example)) {
				additionalInstances++;
			}
		}

		int coveredInstances = 0;
		for (OWLClass example : positiveExamples) {
			if (getReasoner().isSuperClassOf(description, example)) {
				coveredInstances++;
			}
		}

		double recall = coveredInstances / (double) positiveExamples.size();

		if (recall < 1 - noise) {
			return -1;
		}

		double precision = (additionalInstances + coveredInstances == 0) ? 0 : coveredInstances
				/ (double) (coveredInstances + additionalInstances);

		return Heuristics.getFScore(recall, precision);
	}

	/**
	 * @param positiveExamples the positiveExamples to set
	 */
	public void setPositiveExamples(Set<OWLClass> positiveExamples) {
		this.positiveExamples = positiveExamples;
	}

	/**
	 * @return the positiveExamples
	 */
	public Set<OWLClass> getPositiveExamples() {
		return positiveExamples;
	}

	/**
	 * @param negativeExamples the negativeExamples to set
	 */
	public void setNegativeExamples(Set<OWLClass> negativeExamples) {
		this.negativeExamples = negativeExamples;
	}

	/**
	 * @return the negativeExamples
	 */
	public Set<OWLClass> getNegativeExamples() {
		return negativeExamples;
	}

	/**
	 * @return the percentPerLengthUnit
	 */
	public double getPercentPerLengthUnit() {
		return percentPerLengthUnit;
	}

	/**
	 * @param percentPerLengthUnit the percentPerLengthUnit to set
	 */
	public void setPercentPerLengthUnit(double percentPerLengthUnit) {
		this.percentPerLengthUnit = percentPerLengthUnit;
	}
	
	public static void main(String[] args) throws Exception{
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		File file = new File("../examples/father.owl");
		OWLClass cls1 = new OWLClassImpl(IRI.create("http://example.com/father#male"));
		OWLClass cls2 = new OWLClassImpl(IRI.create("http://example.com/father#female"));
		
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);
		
		AbstractKnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		
		OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
		baseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        baseReasoner.init();
		ClosedWorldReasoner rc = new ClosedWorldReasoner(ks);
		rc.setReasonerComponent(baseReasoner);
		rc.init();
		
		ClassAsInstanceLearningProblem lp = new ClassAsInstanceLearningProblem();
		lp.setPositiveExamples(Sets.newHashSet(cls1, cls2));
		lp.setReasoner(rc);
		lp.init();
		
		CELOE alg = new CELOE(lp, rc);
		alg.setMaxExecutionTimeInSeconds(10);
		alg.setWriteSearchTree(true);
		alg.setSearchTreeFile("log/search-tree.log");
		alg.setReplaceSearchTree(true);
		alg.init();
		
		alg.start();
		
	}

}
