package org.dllearner.core.search;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.Iterables;
import org.dllearner.core.EvaluatedHypothesis;
import org.dllearner.core.Score;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Classical local beam search which picks the k best successors (refinements) from the list of candidates
 * when re-populating the beam.
 *
 * @author Lorenz Buehmann
 */
public abstract class LocalBeamSearch<H extends OWLObject, S extends Score, EH extends EvaluatedHypothesis<H, S>>
        extends BeamSearch<H, S, EH> {

    public LocalBeamSearch(int beamSize) {
        this(beamSize, Collections.emptySet());
    }

    public LocalBeamSearch(int beamSize, Set<H> startHypotheses) {
        super(beamSize, startHypotheses);
    }

    @Override
    protected void repopulateBeam(Beam<EH> beam, SortedSet<EH> candidates) {
        beam.clear();

        // candidates are already sorted, thus, just take as many elements as fit into the beam
        Iterables.limit(candidates, beamSize).forEach(beam::add);
    }
}
