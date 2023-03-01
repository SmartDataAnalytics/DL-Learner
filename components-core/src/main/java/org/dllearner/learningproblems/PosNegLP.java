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
import com.google.common.collect.Sets.SetView;
import org.apache.log4j.Logger;
import org.dllearner.accuracymethods.AccMethodApproximate;
import org.dllearner.accuracymethods.AccMethodPredAcc;
import org.dllearner.accuracymethods.AccMethodTwoValued;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.Helper;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Jens Lehmann
 *
 */
public abstract class PosNegLP extends AbstractClassExpressionLearningProblem<ScorePosNeg<OWLNamedIndividual>> {
	protected static final Logger logger = Logger.getLogger(PosNegLP.class);

	@ConfigOption(description = "list of positive examples", required = true)
	protected Set<OWLIndividual> positiveExamples = new TreeSet<>();
	@ConfigOption(description = "list of negative examples", required = true)
	protected Set<OWLIndividual> negativeExamples = new TreeSet<>();
	protected Set<OWLIndividual> allExamples = new TreeSet<>();

	@ConfigOption(description = "list of positive testing examples")
	protected Set<OWLIndividual> positiveTestExamples = new TreeSet<>();
	@ConfigOption(description = "list of negative testing examples")
	protected Set<OWLIndividual> negativeTestExamples = new TreeSet<>();
	protected Set<OWLIndividual> allTestExamples = new TreeSet<>();

	@ConfigOption(description = "\"Specifies whether to use retrieval or instance checks for testing a concept. - NO LONGER FULLY SUPPORTED.",defaultValue = "false")
    private boolean useRetrievalForClassification = false;
    @ConfigOption(description = "Percent Per Length Unit", defaultValue = "0.05", required = false)
    private double percentPerLengthUnit = 0.05;

	@ConfigOption(description = "Specifies, which method/function to use for computing accuracy. Available measues are \"PRED_ACC\" (predictive accuracy), \"FMEASURE\" (F measure), \"GEN_FMEASURE\" (generalised F-Measure according to Fanizzi and d'Amato).",
			defaultValue = "PRED_ACC")
	protected AccMethodTwoValued accuracyMethod;

    public PosNegLP(){

    }

	public PosNegLP(AbstractReasonerComponent reasoningService) {
		super(reasoningService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		ExampleLoader exampleLoaderHelper = this.getExampleLoaderHelper();
		if (exampleLoaderHelper != null && !exampleLoaderHelper.isInitialized()) {
			logger.info("Loading examples by expression");
			exampleLoaderHelper.setPosNegLP(this);
			exampleLoaderHelper.init();
		}
		// check if some positive examples have been set
		if(positiveExamples.isEmpty()) {
			throw new ComponentInitException("No positive examples have been set.");
		}
		
		// check if some negative examples have been set and give warning if not
		if(negativeExamples.isEmpty()) {
			logger.warn("No negative examples have been set, but you decided to use the positive-negative learning"
					+ "problem. We recommend to use the positive-only learning problem for the case of no negative examples instead.");
		}
		
		// check if there is some overlap between positive and negative examples and give warning
		// in that case
		SetView<OWLIndividual> overlap = Sets.intersection(positiveExamples, negativeExamples);
		if(!overlap.isEmpty()) {
			logger.warn("You declared some individuals as both positive and negative examples.");
		}
		
		allExamples = Sets.union(positiveExamples, negativeExamples);
		
		if (accuracyMethod == null) {
			accuracyMethod = new AccMethodPredAcc(true);
		}
		if (accuracyMethod instanceof AccMethodApproximate) {
			((AccMethodApproximate)accuracyMethod).setReasoner(reasoner);
		}
		
		// sanity check whether examples are contained in KB
		Helper.checkIndividuals(reasoner, allExamples);

		if(positiveTestExamples.isEmpty()) {
			logger.warn("No positive testing examples have been set.");
		}

		// check if some negative examples have been set and give warning if not
		if(negativeTestExamples.isEmpty()) {
			logger.warn("No negative testing examples have been set.");
		}

		// check if there is some overlap between positive and negative examples and give warning
		// in that case
		SetView<OWLIndividual> testOverlap = Sets.intersection(positiveTestExamples, negativeTestExamples);
		if(!testOverlap.isEmpty()) {
			logger.warn("You declared some individuals as both positive and negative testing examples.");
		}

		allTestExamples = Sets.union(positiveTestExamples, negativeTestExamples);

		// sanity check whether examples are contained in KB
		Helper.checkIndividuals(reasoner, allTestExamples);
		
		initialized = true;
	}

	public double getTestAccuracyOrTooWeak(OWLClassExpression description, double noise) {
		return reasoningUtil.getAccuracyOrTooWeak2(accuracyMethod, description, positiveTestExamples, negativeTestExamples, noise);
	}

	public void printTestEvaluation(OWLClassExpression description) {
		Set<OWLIndividual> tp = reasoner.hasType(description, positiveTestExamples);
		Set<OWLIndividual> fn = new TreeSet<>(positiveTestExamples);
		fn.removeAll(tp);

		Set<OWLIndividual> fp = reasoner.hasType(description, negativeTestExamples);
		Set<OWLIndividual> tn = new TreeSet<>(negativeTestExamples);

		for (OWLIndividual ex : fp) {
			logger.info("False positive: " + ex.toStringID());
			tn.remove(ex);
		}

		double acc = (tp.size() + tn.size()) / (double) (positiveTestExamples.size() + negativeTestExamples.size());
		double precision = tp.size() / (double) (tp.size() + fp.size());
		double rec = tp.size() / (double) (tp.size() + fn.size());
		double spec = tn.size() / (double) (fp.size() + tn.size());
		double fpr = fp.size() / (double) (fp.size() + tn.size());
		double fm = 2 / ((1 / precision) + (1 / rec));

		logger.info("======================================================");
		logger.info("Test evaluation.");
		logger.info("Concept: " + description);
		logger.info("True positives: " + tp.size());
		logger.info("True negatives: " + tn.size());
		logger.info("False positives: " + fp.size());
		logger.info("False negatives: " + fn.size());

		logger.info("Accuracy: " + acc);
		logger.info("Precission: " + precision);
		logger.info("Recall: " + rec);
		logger.info("Specificity: " + spec);
		logger.info("FP rate: " + fpr);
		logger.info("F-measure: " + fm);
	}

	public Set<OWLIndividual> getNegativeExamples() {
		return negativeExamples;
	}

	public Set<OWLIndividual> getPositiveExamples() {
		return positiveExamples;
	}
	
	public void setNegativeExamples(Set<OWLIndividual> set) {
		this.negativeExamples=set;
	}

	public void setPositiveExamples(Set<OWLIndividual> set) {
		this.positiveExamples=set;
	}

	public Set<OWLIndividual> getNegativeTestExamples() {
		return negativeTestExamples;
	}

	public Set<OWLIndividual> getPositiveTestExamples() {
		return positiveTestExamples;
	}

	public void setNegativeTestExamples(Set<OWLIndividual> negativeTestExamples) {
		this.negativeTestExamples = negativeTestExamples;
	}

	public void setPositiveTestExamples(Set<OWLIndividual> positiveTestExamples) {
		this.positiveTestExamples = positiveTestExamples;
	}

	public double getPercentPerLengthUnit() {
		return percentPerLengthUnit;
	}

    public void setPercentPerLengthUnit(double percentPerLengthUnit) {
        this.percentPerLengthUnit = percentPerLengthUnit;
    }

    public boolean isUseRetrievalForClassification() {
        return useRetrievalForClassification;
    }

    public void setUseRetrievalForClassification(boolean useRetrievalForClassification) {
        this.useRetrievalForClassification = useRetrievalForClassification;
    }

	public AccMethodTwoValued getAccuracyMethod() {
	    return accuracyMethod;
	}

	@Autowired(required=false)
	public void setAccuracyMethod(AccMethodTwoValued accuracyMethod) {
	    this.accuracyMethod = accuracyMethod;
	}

}