package org.dllearner.learningproblems;

import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * @author Jens Lehmann
 *
 */
public abstract class PosNegLP extends AbstractClassExpressionLearningProblem<ScorePosNeg<OWLNamedIndividual>> {
	protected static Logger logger = Logger.getLogger(PosNegLP.class);

	protected Set<OWLIndividual> positiveExamples = new TreeSet<>();
	protected Set<OWLIndividual> negativeExamples = new TreeSet<>();
	protected Set<OWLIndividual> allExamples = new TreeSet<>();

    @org.dllearner.core.config.ConfigOption(name = "useRetrievalForClassification", description = "\"Specifies whether to use retrieval or instance checks for testing a concept. - NO LONGER FULLY SUPPORTED.",defaultValue = "false")
    private boolean useRetrievalForClassification = false;
    @org.dllearner.core.config.ConfigOption(name = "percentPerLengthUnit", description = "Percent Per Length Unit", defaultValue = "0.05", required = false)
    private double percentPerLengthUnit = 0.05;

	@ConfigOption(
			name = "accuracyMethod",
			description = "Specifies, which method/function to use for computing accuracy. Available measues are \"PRED_ACC\" (predictive accuracy), \"FMEASURE\" (F measure), \"GEN_FMEASURE\" (generalised F-Measure according to Fanizzi and d'Amato).",
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
		if(reasoner != null && !reasoner.getIndividuals().containsAll(allExamples) && !reasoner.getClass().isAssignableFrom(SPARQLReasoner.class)) {
            Set<OWLIndividual> missing = Sets.difference(allExamples, reasoner.getIndividuals());
            double percentage = missing.size()/allExamples.size();
            percentage = Math.round(percentage * 1000) / 1000;
			String str = "The examples (" + (percentage * 100) + " % of total) below are not contained in the knowledge base (check spelling and prefixes)\n";
			str += missing.toString();
            if(missing.size()==allExamples.size())    {
                throw new ComponentInitException(str);
            } if(percentage < 0.10) {
                logger.warn(str);
            } else {
                logger.error(str);
            }
		}
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