/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.fuzzydll.FuzzyIndividual;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * @author Jens Lehmann
 *
 */
public abstract class FuzzyPosNegLP extends AbstractClassExpressionLearningProblem<ScorePosNeg<OWLNamedIndividual>> {
	
	protected SortedSet<OWLIndividual> positiveExamples;
	protected SortedSet<OWLIndividual> negativeExamples;
	protected SortedSet<OWLIndividual> allExamples;
	
	protected SortedSet<FuzzyIndividual> fuzzyExamples;

	protected Map<OWLIndividual,Double> fuzzyEx;
	
	public Map<OWLIndividual, Double> getFuzzyEx() {
		return fuzzyEx;
	}

	public void setFuzzyEx(Map<OWLIndividual, Double> fuzzyEx) {
		fuzzyExamples = new TreeSet<FuzzyIndividual>();
		
		Iterator<OWLIndividual> it = fuzzyEx.keySet().iterator();		
		
		while (it.hasNext()) {
			OWLIndividual i = it.next();
			this.fuzzyExamples.add(new FuzzyIndividual(i.toStringID(), fuzzyEx.get(i).doubleValue()));
		}
	}

	protected boolean useRetrievalForClassification = false;
	protected UseMultiInstanceChecks useMultiInstanceChecks = UseMultiInstanceChecks.TWOCHECKS;
	protected double percentPerLengthUnit = 0.05;
	protected double totalTruth = 0;

	/**
	 * If instance checks are used for testing concepts (e.g. no retrieval), then
	 * there are several options to do this. The enumeration lists the supported
	 * options. These options are only important if the reasoning mechanism 
	 * supports sending several reasoning requests at once as it is the case for
	 * DIG reasoners.
	 * 
	 * @author Jens Lehmann
	 *
	 */
	public enum UseMultiInstanceChecks {
		/**
		 * Perform a separate instance check for each example.
		 */
		NEVER,
		/**
		 * Perform one instance check for all positive and one instance check
		 * for all negative examples.
		 */
		TWOCHECKS,
		/**
		 * Perform all instance checks at once.
		 */
		ONECHECK
	};
	
	public FuzzyPosNegLP() {}
	
	public FuzzyPosNegLP(AbstractReasonerComponent reasoningService) {
		super(reasoningService);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		// commented by Josue as now there's no need of + and - examples (more code need to be deleted in this sense)
		// allExamples = Helper.union(positiveExamples, negativeExamples);
	}
	
	public SortedSet<OWLIndividual> getNegativeExamples() {
		return negativeExamples;
	}

	public SortedSet<OWLIndividual> getPositiveExamples() {
		return positiveExamples;
	}
	
	public void setNegativeExamples(SortedSet<OWLIndividual> set) {
		this.negativeExamples=set;
	}

	public void setPositiveExamples(SortedSet<OWLIndividual> set) {
		this.positiveExamples=set;
	}
	
	public abstract int coveredNegativeExamplesOrTooWeak(org.semanticweb.owlapi.model.OWLClassExpression concept);

	public double getPercentPerLengthUnit() {
		return percentPerLengthUnit;
	}

	public SortedSet<FuzzyIndividual> getFuzzyExamples() {
		return fuzzyExamples;
	}

	public void setFuzzyExamples(SortedSet<FuzzyIndividual> fuzzyExamples) {
		this.fuzzyExamples = fuzzyExamples;
	}
	
}