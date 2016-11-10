package org.dllearner.algorithms.qtl;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningProblem;

import java.util.List;
import java.util.SortedSet;

/**
 * @author Lorenz Buehmann
 */
public class SPARQLQueryLearningProblemPosNeg extends SPARQLQueryLearningProblem {

	private SortedSet<List<String>> posExamples;
	private SortedSet<List<String>> negExamples;


	@Override
	public void init() throws ComponentInitException {

	}

	public SortedSet<List<String>> getPosExamples() {
		return posExamples;
	}

	public void setPosExamples(SortedSet<List<String>> posExamples) {
		this.posExamples = posExamples;
	}

	public SortedSet<List<String>> getNegExamples() {
		return negExamples;
	}

	public void setNegExamples(SortedSet<List<String>> negExamples) {
		this.negExamples = negExamples;
	}
}
