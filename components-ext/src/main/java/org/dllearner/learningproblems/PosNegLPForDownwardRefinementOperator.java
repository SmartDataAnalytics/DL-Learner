package org.dllearner.learningproblems;

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.util.SortedSet;

/**
 * Main idea of this learning problem is that one can consider a lower threshold
 * w.r.t. a class expressions accuracy and return 'too weak' if this is not met.
 *
 * Since we are assuming a downward refinement operator, further refinements
 * can only decrease the number of instances of the given class expression. In
 * the best case further refinements will eliminate all covered negative
 * examples, i.e. the false positives, s.t. the best possible accuracy will be
 *
 *  number of covered positive examples + number of all negative examples
 *  ---------------------------------------------------------------------
 *                         number of all examples
 *
 * If we are interested in the, say, 10 best learned class expressions and we
 * know
 *
 * - that the currently 10th best class expression has an accuracy of 0.65,
 * - we have a set of 12 positive examples, and
 * - we have a set of 10 negative examples
 *
 * the minimum number of examples a given class expression to calculate the
 * accuracy for has to classify correctly is CEIL(0.65 * 22) = 15. If we
 * optimistically assume that further refinements will classify all 10 negative
 * examples correctly there must be at least 5 correctly classified positive
 * examples to reach an accuracy better than the currently 10th best learned
 * class expression. If the input class expression only covers, let's say, 3
 * positive examples we can already mark it as too weak and thus effectively
 * remove it from the search tree.
 */
public class PosNegLPForDownwardRefinementOperator extends PosNegLPStandard {

    public double getAccuracyOrTooWeak(
            OWLClassExpression ce, double noise, double lowAccThreshold) {

        int numExamples = positiveExamples.size() + negativeExamples.size();
        int minNrOfCorrectlyClassifiedExamplesToNotBeTooWeak =
                (int) Math.ceil(numExamples * lowAccThreshold);

        // Assuming the ideal case that all covered negative examples will not
        // be covered after applying a certain number of future refinement steps
        int minNrOfPosExamplesToCoverToNotBeTooWeak =
                minNrOfCorrectlyClassifiedExamplesToNotBeTooWeak - negativeExamples.size();

        // If we had a noise value of 0.2, another 20% of the positive examples
        // may be not covered (then considered as noise we have to accept) and
        // the result may still be considered OK
        // --> reduce minNrOfPosExamplesToCoverToNotBeTooWeak by this 20%
        // (i.e. set minNrOfPosExamplesToCoverToNotBeTooWeak to
        // minNrOfPosExamplesToCoverToNotBeTooWeak * 0.8)
        minNrOfPosExamplesToCoverToNotBeTooWeak =
                (int) Math.ceil((1-noise) * minNrOfPosExamplesToCoverToNotBeTooWeak);

        SortedSet<OWLIndividual> allCoveredIndividuals = reasoner.getIndividuals(ce);
        if (allCoveredIndividuals == null) {
            return -1.0;
        }

        int numCoveredPosExamples =
                Sets.intersection(allCoveredIndividuals, positiveExamples).size();

        if (numCoveredPosExamples < minNrOfPosExamplesToCoverToNotBeTooWeak) {
            return -1.0;

        } else {

            int maxNotCovered = (int) Math.ceil(noise * positiveExamples.size());

            int numNotCoveredPosExamples =
                    positiveExamples.size() - numCoveredPosExamples;

            if (numNotCoveredPosExamples != 0 &&
                    numNotCoveredPosExamples >= maxNotCovered) {
                return -1;
            }

            int numCoveredNegExamples =
                    Sets.intersection(allCoveredIndividuals, negativeExamples).size();
            int numNotCoveredNegExamples = negativeExamples.size() - numCoveredNegExamples;

            return (numNotCoveredNegExamples + numCoveredPosExamples) / numExamples;
        }
    }
}
