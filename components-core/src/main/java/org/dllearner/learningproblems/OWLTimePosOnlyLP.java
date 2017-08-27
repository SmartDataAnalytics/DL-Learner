package org.dllearner.learningproblems;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.TemporalOWLReasoner;
import org.dllearner.sampling.temporal.TemporalSamplingStrategy;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import uk.ac.manchester.cs.owl.owlapi.OWLClassAssertionAxiomImpl;

/**
 * Learning problem for positive instant based learning with negative instant sampling(?)
 */
public class OWLTimePosOnlyLP extends
		PosOnlyLP {
//		AbstractClassExpressionLearningProblem<ScorePosOnly<OWLNamedIndividual>> {
	private SortedSet<OWLIndividual> posExamples;
	private Set<OWLIndividual> negExamples;
	private TemporalSamplingStrategy samplingStrategy = null;
	private TemporalOWLReasoner reasoner;
	private double threshold = 0.3;

	@Override
	public void init() throws ComponentInitException {
		if (samplingStrategy == null)
			throw new ComponentInitException("No sampling strategy set");
		if (reasoner == null)
			throw new ComponentInitException("No time reasoner set");
		if (posExamples == null)
			throw new ComponentInitException("No positive examples set");

		samplingStrategy.setPosExamples(posExamples);
		samplingStrategy.setReasoner(reasoner);
		samplingStrategy.init();

		negExamples = samplingStrategy.getNegativeExamples();
	}


	@Override
	public ScorePosOnly<OWLNamedIndividual> computeScore(OWLClassExpression hypothesis, double noise) {
		Set<OWLIndividual> retrieval = reasoner.getIndividuals(hypothesis);

		Set<OWLIndividual> instancesCovered = new TreeSet<>();
		Set<OWLIndividual> instancesNotCovered = new TreeSet<>();
		for(OWLIndividual ind : posExamples) {
			if(retrieval.contains(ind)) {
				instancesCovered.add(ind);
			} else {
				instancesNotCovered.add(ind);
			}
		}

		double coverage = instancesCovered.size() / (double) posExamples.size();
		double protusion = retrieval.size() == 0 ? 0 : instancesCovered.size() / (double) retrieval.size();

		// pass only additional instances to score object
		retrieval.removeAll(instancesCovered);

		double accuracy = 0.5 * (coverage + Math.sqrt(protusion));

		return new ScorePosOnly(instancesCovered, instancesNotCovered, coverage, retrieval, protusion, accuracy);
	}

	@Override
	public double getAccuracyOrTooWeak(OWLClassExpression hypothesis, double noise) {
		System.out.println(hypothesis);
		int tp = 0;
		int tn = 0;
		int fp = 0;
		int fn = 0;
		Set<OWLAnnotation> annotations = new HashSet<>();

		OWLAPIReasoner rc = reasoner.getReasonerComponent();
		OWLClassAssertionAxiom classAssertion;

		for (OWLIndividual p : posExamples) {
			classAssertion = new OWLClassAssertionAxiomImpl(
					p, hypothesis, annotations);

			if (rc.isEntailed(classAssertion)) tp++;
//			else fn++;
		}

		for (OWLIndividual n : negExamples) {
			classAssertion = new OWLClassAssertionAxiomImpl(
					n, hypothesis, annotations);

			if (rc.isEntailed(classAssertion)) fp++;
			else tn++;
		}

		double acc = (tp + tn) / ((double) posExamples.size() + negExamples.size());

		if (acc < threshold) return -1;
		else return acc;
	}

	@Override
	public void setPositiveExamples(SortedSet<OWLIndividual> posExamples) {
		this.posExamples = posExamples;
	}

	@Override
	public SortedSet<OWLIndividual> getPositiveExamples() {
		return posExamples;
	}

	@Override
	public void setReasoner(AbstractReasonerComponent reasoner) {
		if (!(reasoner instanceof TemporalOWLReasoner)) {
			throw new RuntimeException(
					"Expecting something that implements " +
							TemporalOWLReasoner.class.getName());
		}
		this.reasoner = (TemporalOWLReasoner) reasoner;
	}

	@Override
	public AbstractReasonerComponent getReasoner() {
		return (AbstractReasonerComponent) reasoner;
	}

	public void setSamplingStrategy(TemporalSamplingStrategy samplingStrategy) {
		this.samplingStrategy = samplingStrategy;
	}

	public TemporalSamplingStrategy getSamplingStrategy() {
		return samplingStrategy;
	}

	@Override
	public EvaluatedDescription<ScorePosOnly<OWLNamedIndividual>> evaluate(OWLClassExpression description, double noise) {
		ScorePosOnly<OWLNamedIndividual> score = computeScore(description, noise);
		return new EvaluatedDescriptionPosOnly(description, score);
	}
}
