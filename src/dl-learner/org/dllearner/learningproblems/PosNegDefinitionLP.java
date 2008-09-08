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
package org.dllearner.learningproblems;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.utilities.Helper;

/**
 * The aim of this learning problem is to learn a concept definition such that
 * the positive examples and the negative examples do not follow. It is
 * 2-valued, because we only distinguish between covered and non-covered
 * examples. (A 3-valued problem distinguishes between covered examples,
 * examples covered by the negation of the concept, and all other examples.) The
 * 2-valued learning problem is often more useful for Description Logics due to
 * (the Open World Assumption and) the fact that negative knowledge, e.g. that a
 * person does not have a child, is or cannot be expressed.
 * 
 * @author Jens Lehmann
 * 
 */
public class PosNegDefinitionLP extends PosNegLP implements DefinitionLP {

		
	public PosNegDefinitionLP(ReasoningService reasoningService) {
		super(reasoningService);
	}

	public PosNegDefinitionLP(ReasoningService reasoningService, SortedSet<Individual> positiveExamples, SortedSet<Individual> negativeExamples) {
		super(reasoningService);
		this.positiveExamples = positiveExamples;
		this.negativeExamples = negativeExamples;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#getName()
	 */
	public static String getName() {
		return "two valued definition learning problem";
	}
	

	public static Collection<ConfigOption<?>> createConfigOptions() {
		return PosNegLP.createConfigOptions();
	}
	
	/**
	 * This method computes (using the reasoner) whether a concept is too weak.
	 * If it is not weak, it returns the number of covered negative example. It
	 * can use retrieval or instance checks for classification.
	 * 
	 * @see org.dllearner.learningproblems.PosNegLP.MultiInstanceChecks
	 * TODO: Performance could be slightly improved by counting the number of
	 *       covers instead of using sets and counting their size.
	 * @param concept
	 *            The concept to test.
	 * @return -1 if concept is too weak and the number of covered negative
	 *         examples otherwise.
	 */
	@Override
	public int coveredNegativeExamplesOrTooWeak(Description concept) {

		if (useRetrievalForClassification) {
			SortedSet<Individual> posClassified = reasoningService.retrieval(concept);
			SortedSet<Individual> negAsPos = Helper.intersection(negativeExamples, posClassified);
			SortedSet<Individual> posAsNeg = new TreeSet<Individual>();

			// the set is constructed piecewise to avoid expensive set
			// operations
			// on a large number of individuals
			for (Individual posExample : positiveExamples) {
				if (!posClassified.contains(posExample))
					posAsNeg.add(posExample);
			}

			// too weak
			if (posAsNeg.size() > 0)
				return -1;
			// number of covered negatives
			else
				return negAsPos.size();
		} else {
			if (useMultiInstanceChecks != UseMultiInstanceChecks.NEVER) {
				// two checks
				if (useMultiInstanceChecks == UseMultiInstanceChecks.TWOCHECKS) {
					Set<Individual> s = reasoningService.instanceCheck(concept, positiveExamples);
					// if the concept is too weak, then do not query negative
					// examples
					if (s.size() != positiveExamples.size())
						return -1;
					else {
						s = reasoningService.instanceCheck(concept, negativeExamples);
						return s.size();
					}
					// one check
				} else {
					Set<Individual> s = reasoningService.instanceCheck(concept, allExamples);
					// test whether all positive examples are covered
					if (s.containsAll(positiveExamples))
						return s.size() - positiveExamples.size();
					else
						return -1;
				}
			} else {
				// SortedSet<Individual> posAsNeg = new TreeSet<Individual>();
				SortedSet<Individual> negAsPos = new TreeSet<Individual>();

				for (Individual example : positiveExamples) {
					if (!reasoningService.instanceCheck(concept, example))
						return -1;
					// posAsNeg.add(example);
				}
				for (Individual example : negativeExamples) {
					if (reasoningService.instanceCheck(concept, example))
						negAsPos.add(example);
				}

				return negAsPos.size();
			}
		}
	}

	/**
	 * Computes score of a given concept using the reasoner. Either retrieval or
	 * instance check are used. For the latter, this method treats
	 * <code>UseMultiInstanceChecks.TWO_CHECKS</code> as if it were 
	 * <code>UseMultiInstanceChecks.ONE_CHECKS</code> (it does not make much sense
	 * to implement TWO_CHECKS in this function, because we have to test all
	 * examples to create a score object anyway).
	 * 
	 * @see org.dllearner.learningproblems.PosNegLP.MultiInstanceChecks
	 * @param concept
	 *            The concept to test.
	 * @return Corresponding Score object.
	 */
	@Override
	public Score computeScore(Description concept) {
		if (useRetrievalForClassification) {
			SortedSet<Individual> posClassified = reasoningService.retrieval(concept);
			SortedSet<Individual> posAsPos = Helper.intersection(positiveExamples, posClassified);
			SortedSet<Individual> negAsPos = Helper.intersection(negativeExamples, posClassified);
			SortedSet<Individual> posAsNeg = new TreeSet<Individual>();

			// piecewise set construction
			for (Individual posExample : positiveExamples) {
				if (!posClassified.contains(posExample))
					posAsNeg.add(posExample);
			}
			SortedSet<Individual> negAsNeg = new TreeSet<Individual>();
			for (Individual negExample : negativeExamples) {
				if (!posClassified.contains(negExample))
					negAsNeg.add(negExample);
			}
			return new ScoreTwoValued(concept.getLength(), percentPerLengthUnit, posAsPos, posAsNeg, negAsPos, negAsNeg);
		// instance checks for classification
		} else {		
			SortedSet<Individual> posAsPos = new TreeSet<Individual>();
			SortedSet<Individual> posAsNeg = new TreeSet<Individual>();
			SortedSet<Individual> negAsPos = new TreeSet<Individual>();
			SortedSet<Individual> negAsNeg = new TreeSet<Individual>();
			
			if (useMultiInstanceChecks != UseMultiInstanceChecks.NEVER) {
				SortedSet<Individual> posClassified = reasoningService.instanceCheck(concept,
						allExamples);
				SortedSet<Individual> negClassified = Helper.difference(allExamples,
						posClassified);
				posAsPos = Helper.intersection(positiveExamples, posClassified);
				posAsNeg = Helper.intersection(positiveExamples, negClassified);
				negAsPos = Helper.intersection(negativeExamples, posClassified);
				negAsNeg = Helper.intersection(negativeExamples, negClassified);
				
				// System.out.println("pos classified: " + posClassified);
				
				return new ScoreTwoValued(concept.getLength(), percentPerLengthUnit, posAsPos, posAsNeg, negAsPos,
						negAsNeg);
			} else {
				System.out.println("TEST");
				
				for (Individual example : positiveExamples) {
					if (reasoningService.instanceCheck(concept, example)) {
						posAsPos.add(example);
					} else {
						posAsNeg.add(example); System.out.println(concept + " " + example);
					}
				}
				for (Individual example : negativeExamples) {
					if (reasoningService.instanceCheck(concept, example))
						negAsPos.add(example);
					else
						negAsNeg.add(example);
				}
				return new ScoreTwoValued(concept.getLength(), percentPerLengthUnit, posAsPos, posAsNeg, negAsPos,
						negAsNeg);
			}
		}
	}

}
