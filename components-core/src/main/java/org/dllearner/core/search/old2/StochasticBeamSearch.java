package org.dllearner.core.search.old2;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.dllearner.core.EvaluatedHypothesis;
import org.dllearner.core.EvaluatedHypothesisOWL;
import org.dllearner.core.Score;
import org.dllearner.core.search.Beam;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Beam search with difference in re-populating the beam after each iteration:
 *
 * Instead of choosing the best k from the pool of candidate successors stochastic beam search
 * chooses k successors at random.
 * Probability of choosing a given successor being an increasing function of its value.
 *
 * @author Lorenz Buehmann
 */
public abstract class StochasticBeamSearch< H extends OWLObject,
                                            S extends Score,
                                            EH extends EvaluatedHypothesis<H, S>>
        extends BeamSearch<H, S, EH> {

    private RandomGenerator rng = new JDKRandomGenerator();

    public StochasticBeamSearch(int beamSize) {
        this(beamSize, Collections.emptySet());
    }

    public StochasticBeamSearch(int beamSize, Set<H> startHypotheses) {
        super(beamSize, startHypotheses);
    }

    @Override
    protected void repopulateBeam(Beam<BeamNode<EH>> beam, SortedSet<BeamNode<EH>> candidates) {
        beam.clear();

        // if there are less candidates than the size of the beam, just use all of them and return
        if(candidates.size() < beamSize) {
            beam.addAll(candidates);
            return;
        }

        // we do stochastic sampling based on the utility
        // get min/max first for normalization
        double minUtility = candidates.last().getUtility();
        double maxUtility = candidates.first().getUtility();

        // compute probability for each node
        List<Pair<BeamNode<EH>, Double>> nodes2Prob = candidates.stream()
                .map(c -> Pair.create(c, probability(normalize(c.getUtility(), minUtility, maxUtility))))
                .collect(Collectors.toList());

        // reset the random generator
        rng.setSeed(123);

        // create discrete probability distribution (this class does normalization to sum of 1 internally)
        EnumeratedDistribution<BeamNode<EH>> distribution = new EnumeratedDistribution<>(rng, nodes2Prob);

        // fill beam
        while(beam.size() < beamSize) {
            // compute a sample
            BeamNode<EH> sampleNode = distribution.sample();

            // add to beam set
            beam.add(sampleNode);
        }

    }

    double temperature = 1.0;
    double decay = 0.95;

    @Override
    protected void nextIterationStarted(int i) {
        super.nextIterationStarted(i);
        temperature *= decay;
    }

    /*
     * Boltzmann distribution exp^(-1 * (val/T))
     *
     * If temperature is high, weaker candidates will have a higher probabilities, but once temperatures getting
     * lower, the stronger candidates will be preferred.
     *
     * Note, for proper distribution, the values have to be normalized to sum_1..n over p_i = 1
     *
     */
    private double probability(double value) {
        return Math.exp(- value / temperature);
    }

    /*
     standard min/max normalization
     1 - (val - min) / (max - min)
     values close to 0 are considered stronger than values close to 1
     */
    private double normalize(double value, double min, double max) {
        return (min == max) ? 0 : 1 - (value - min)/(max - min);
    }
}