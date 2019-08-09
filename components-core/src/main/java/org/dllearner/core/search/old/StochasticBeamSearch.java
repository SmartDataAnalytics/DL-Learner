package org.dllearner.core.search.old;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.Iterables;
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
public abstract class StochasticBeamSearch<H extends OWLObject, S extends Score, EH extends EvaluatedHypothesisOWL<H, S>>
        extends BeamSearch<H, S, EH> {

    public StochasticBeamSearch(int beamSize) {
        this(beamSize, Collections.emptySet());
    }

    public StochasticBeamSearch(int beamSize, Set<H> startHypotheses) {
        super(beamSize, startHypotheses);
    }

    @Override
    protected void repopulateBeam(Beam<EH> beam, SortedSet<EH> candidates) {
        beam.clear();

        Iterables.limit(candidates, beamSize).forEach(beam::add);
    }
}
