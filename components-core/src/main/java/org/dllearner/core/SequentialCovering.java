package org.dllearner.core;

import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 */
public abstract class SequentialCovering<E extends Example, H extends EvaluatedHypothesisOWL> {

    private final Set<E> examples;

    public SequentialCovering(Set<E> examples) {
        this.examples = examples;
    }

    public abstract H learnOneHypothesis(Set<E> examples);

    public abstract boolean goodPerformance(H hypothesis);

    public abstract Set<E> coveredExamples(H hypothesis);

    public void run() {
        Set<H> learnedHypotheses = new TreeSet<>();

        Set<E> currentExamples = Sets.newHashSet(examples);

        // learn a single hypothesis
        H hypothesis = learnOneHypothesis(currentExamples);


        while(goodPerformance(hypothesis)) {
            learnedHypotheses.add(hypothesis);

            // removed covered examples from examples
            currentExamples = Sets.difference(currentExamples, coveredExamples(hypothesis));

            // learn another hypothesis on remaining examples
            hypothesis = learnOneHypothesis(currentExamples);
        }
    }



}
