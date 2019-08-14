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

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.fuzzydll.FuzzyIndividual;
import org.dllearner.utilities.Helper;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * @author Jens Lehmann
 *
 */
public abstract class FuzzyPosNegLP extends AbstractClassExpressionLearningProblem<ScorePosNeg<OWLNamedIndividual>> {
	
	@ConfigOption()
	protected SortedSet<OWLIndividual> positiveExamples;
	@ConfigOption()
	protected SortedSet<OWLIndividual> negativeExamples;

	protected SortedSet<OWLIndividual> allExamples;
	@ConfigOption()
	protected SortedSet<FuzzyIndividual> fuzzyExamples;

	protected double percentPerLengthUnit = 0.05;
	protected double totalTruth = 0;
	
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
	public void init() throws ComponentInitException {
		// commented by Josue as now there's no need of + and - examples (more code need to be deleted in this sense)
		// allExamples = Helper.union(positiveExamples, negativeExamples);

		// sanity check whether examples are contained in KB
		Helper.checkIndividuals(reasoner, Sets.union(Sets.union(positiveExamples, negativeExamples), fuzzyExamples));

		initialized = true;
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

	public double getPercentPerLengthUnit() {
		return percentPerLengthUnit;
	}

	public SortedSet<FuzzyIndividual> getFuzzyExamples() {
		return fuzzyExamples;
	}

	public void setFuzzyExamples(SortedSet<FuzzyIndividual> fuzzyExamples) {
		this.fuzzyExamples = fuzzyExamples;
	}

	public void setFuzzyExamples(Map<OWLIndividual, Double> fuzzyEx) {
		fuzzyExamples = fuzzyEx.entrySet().stream()
				.map(e -> new FuzzyIndividual(e.getKey().toStringID(), e.getValue()))
				.collect(Collectors.toCollection(TreeSet::new));
	}


	
}