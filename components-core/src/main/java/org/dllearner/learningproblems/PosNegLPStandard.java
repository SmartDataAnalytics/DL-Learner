package org.dllearner.learningproblems;

import java.util.SortedSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.utilities.ReasoningUtils.Coverage;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

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
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#getName()
	 */
	public static String getName() {
		return "pos neg learning problem";
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

		Coverage[] cc = getReasoningUtil().getCoverage(concept, positiveExamples, negativeExamples);

		// TODO: this computes accuracy twice - more elegant method should be implemented
		double accuracy = accuracyMethod.getAccOrTooWeak2(cc[0].trueCount, cc[0].falseCount, cc[1].trueCount, cc[1].falseCount, noise);

		return new ScoreTwoValued(
				OWLClassExpressionUtils.getLength(concept),
				getPercentPerLengthUnit(),
				cc[0].trueSet, cc[0].falseSet, cc[1].trueSet, cc[1].falseSet,
				accuracy);

	}

	@Override
	public double getAccuracyOrTooWeak(OWLClassExpression description, double noise) {
		return getReasoningUtil().getAccuracyOrTooWeak2(accuracyMethod, description, positiveExamples, negativeExamples, noise);
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
    	return new PosNegLPStandard(this);
    }
}