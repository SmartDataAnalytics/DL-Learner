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

import org.dllearner.core.*;
import org.dllearner.utilities.CoverageAdapter;
import org.dllearner.utilities.ReasoningUtils.Coverage;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.util.SortedSet;

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
@ComponentAnn(name = "PosNegLPStandard", shortName = "posNegStandard", version = 0.8)
public class PosNegLPStandard extends PosNegLP implements Cloneable{
	

    public PosNegLPStandard() {
	}

    public PosNegLPStandard(AbstractReasonerComponent reasoningService){
        super(reasoningService);
    }
    
    /**
     * Copy constructor
     * @param lp the learning problem
     */
    public PosNegLPStandard(PosNegLP lp) {
    	this.positiveExamples = lp.getPositiveExamples();
    	this.negativeExamples = lp.getNegativeExamples();
    	
    	this.reasoner = lp.getReasoner();
    	this.accuracyMethod = lp.getAccuracyMethod();
    	setUseRetrievalForClassification(lp.isUseRetrievalForClassification());
	}

	public PosNegLPStandard(AbstractReasonerComponent reasoningService, SortedSet<OWLIndividual> positiveExamples, SortedSet<OWLIndividual> negativeExamples) {
		this.setReasoner(reasoningService);
		this.positiveExamples = positiveExamples;
		this.negativeExamples = negativeExamples;
	}

	@Override
	public void init() throws ComponentInitException {
		super.init();
		
		initialized = true;
	}

	/**
	 * Computes score of a given concept using the reasoner.
	 * 
	 * @param concept
	 *            The concept to test.
	 * @return Corresponding Score object.
	 */
	@Override
	public ScorePosNeg computeScore(OWLClassExpression concept, double noise) {

		Coverage[] cc = reasoningUtil.getCoverage(concept, positiveExamples, negativeExamples);

		// TODO: this computes accuracy twice - more elegant method should be implemented
		double accuracy = reasoningUtil.getAccuracyOrTooWeakExact2(accuracyMethod, cc, noise);
		CoverageAdapter.CoverageAdapter2 c2 = new CoverageAdapter.CoverageAdapter2(cc);

		return new ScoreTwoValued(
				OWLClassExpressionUtils.getLength(concept),
				getPercentPerLengthUnit(),
				c2.posAsPos(), c2.posAsNeg(), c2.negAsPos(), c2.negAsNeg(),
				accuracy);

	}

	@Override
	public double getAccuracyOrTooWeak(OWLClassExpression description, double noise) {
		return reasoningUtil.getAccuracyOrTooWeak2(accuracyMethod, description, positiveExamples, negativeExamples, noise);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#evaluate(org.dllearner.core.owl.Description)
	 */
	@Override
	public EvaluatedDescription evaluate(OWLClassExpression description, double noise) {
		ScorePosNeg score = computeScore(description, noise);
		return new EvaluatedDescriptionPosNeg(description, score);
	}

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
    	super.clone();
    	return new PosNegLPStandard(this);
    }
}