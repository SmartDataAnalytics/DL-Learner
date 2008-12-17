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

import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.Score;
import org.dllearner.core.configurators.ComponentFactory;
import org.dllearner.core.configurators.PosNegInclusionLPConfigurator;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Negation;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.SetManipulation;

/**
 * The aim of this learning problem is to find an appropriate inclusion axiom
 * given positive and negative examples. 
 * 
 * This is similar to the definition learning problem, but here the positive 
 * and negative examples usually do not follow when the inclusion is added to 
 * the knowledge base. This raises the question how the quality of a concept
 * with respect to this learning problem can be measured. Due to the fact that
 * the inclusion does not entail examples, we have to look at the negation of
 * the concept we are looking at. For a good solution it is the case that
 * no positive examples follow from the negated concept, the negative 
 * examples follow from it. This means that the standard definition learning
 * problem and the inclusion learning problem can be seen as two possible
 * weakenings of the strict definition learning problem. (Of course, both problems
 * can yield the same solution.) 
 * 
 * @author Jens Lehmann
 *
 */
public class PosNegInclusionLP extends PosNegLP implements InclusionLP {

	private PosNegDefinitionLP definitionLP;
	private PosNegInclusionLPConfigurator configurator;
	
	@Override
	public PosNegInclusionLPConfigurator getConfigurator(){
		return configurator;
	}
	
	public PosNegInclusionLP(ReasonerComponent reasoningService) {
		super(reasoningService);
		configurator = new PosNegInclusionLPConfigurator(this); 
	}
/*
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new StringSetConfigOption("positiveExamples",
				"positive examples"));
		options.add(new StringSetConfigOption("negativeExamples",
				"negative examples"));
		options.add(new BooleanConfigOption("useRetrievalForClassficiation", 
				"Specifies whether to use retrieval or instance checks for testing a concept."));
		return options;
	}
*/
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#getName()
	 */
	public static String getName() {
		return "two valued inclusion learning problem";
	}

		
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		super.init();
		definitionLP = ComponentFactory.getPosNegDefinitionLP(
				reasoner, 
				SetManipulation.indToString(negativeExamples), 
				SetManipulation.indToString(positiveExamples));
		//definitionLP = new PosNegDefinitionLP(reasoningService, negativeExamples, positiveExamples);
		// TODO: we must make sure that the problem also gets the same 
		// reasoning options (i.e. options are the same up to reversed example sets)
		definitionLP.init();
	}
	
	/**
	 * See the documentation of <code>coveredNegativeExamplesOrTooWeak</code> in
	 * the definition learning problem case. This method works differently:
	 * First, it tests whether none of the positive examples is covered by the
	 * negation of the given concept. Should this be the case, it returns
	 * too weak. If this is not the case, the method returns the number of
	 * negative examples, which do not follow from the negation of the input
	 * concept. Thus, this methods uses a different notion of coverage than
	 * the one for the standard definition learning problem.
	 * 
	 * @see org.dllearner.learningproblems.PosNegLP.UseMultiInstanceChecks
	 * @param concept
	 *            The concept to test.
	 * @return -1 if concept is too weak and the number of covered negative
	 *         examples otherwise.
	 */
	@Override
	public int coveredNegativeExamplesOrTooWeak(Description concept) {

		if (useRetrievalForClassification) {
			SortedSet<Individual> inNegatedConcept = reasoner.getIndividuals(new Negation(concept));

			for (Individual posExample : positiveExamples) {
				// if any positive example follows from the negation, then
				// the concept is "too weak"
				if (inNegatedConcept.contains(posExample))
					return -1;
			}

			// number of covered negatives
			// cover = neg. example does not belong to the negated concept
			SortedSet<Individual> negExInNegatedConcept = Helper.intersection(negativeExamples, inNegatedConcept);			
			return (negativeExamples.size() - negExInNegatedConcept.size());
		} else {
			if (useMultiInstanceChecks != UseMultiInstanceChecks.NEVER) {
				// two checks
				if (useMultiInstanceChecks == UseMultiInstanceChecks.TWOCHECKS) {
					Set<Individual> posExInNegatedConcept = reasoner.hasType(new Negation(concept), positiveExamples);
					
					if(posExInNegatedConcept.size()>0) {
						return -1;
					} else {
						Set<Individual> negExInNegatedConcept = reasoner.hasType(new Negation(concept), negativeExamples);
						return (negativeExamples.size() - negExInNegatedConcept.size());
					}
						
					// one check
				} else {
					Set<Individual> inNegatedConcept = reasoner.hasType(new Negation(concept), allExamples);
					
					for(Individual i : positiveExamples) {
						if(inNegatedConcept.contains(i))
							return -1;
					}
					
					// we can now be sure that inNegatedConcept contains only
					// negative examples
					return (negativeExamples.size() - inNegatedConcept.size());
					
				}
			// single instance checks
			} else {
				int coverCount = negativeExamples.size();

				for (Individual example : positiveExamples) {
					if (reasoner.hasType(new Negation(concept), example))
						return -1;
				}
				for (Individual example : negativeExamples) {
					if (!reasoner.hasType(new Negation(concept), example))
						coverCount--;
				}

				return coverCount;
			}
		}
	}
	
	/** 
	 * Calls the same method on the standard definition learning problem, but 
	 * negates the concept before and permutes positive and negative examples.
	 * 
	 * @see org.dllearner.core.LearningProblem#computeScore(org.dllearner.core.owl.Description)
	 */
	@Override
	public Score computeScore(Description concept) {
		// FastInstanceChecker supports only negation normal form, so we have to make
		// sure to convert the description before
//		if(reasoner instanceof FastInstanceChecker) {
//			return definitionLP.computeScore(ConceptTransformation.transformToNegationNormalForm(new Negation(concept)));
//		}
		return definitionLP.computeScore(new Negation(concept));
	}

}
