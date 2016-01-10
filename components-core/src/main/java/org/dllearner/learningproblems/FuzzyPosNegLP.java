package org.dllearner.learningproblems;

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
		fuzzyExamples = new TreeSet<>();

		for (OWLIndividual i : fuzzyEx.keySet()) {
			this.fuzzyExamples.add(new FuzzyIndividual(i.toStringID(), fuzzyEx.get(i).doubleValue()));
		}
	}

	protected boolean useRetrievalForClassification = false;
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