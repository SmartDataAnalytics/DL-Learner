package org.dllearner.core.search.old2;

import java.util.Set;

import org.dllearner.core.EvaluatedHypothesis;
import org.dllearner.core.EvaluatedHypothesisOWL;
import org.dllearner.core.Score;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Beam search that keeps k states opposed to one state like in e.g. classic hill-climbing.
 *
 * General steps:
 *
 * - Begins with k randomly generated states
 * - At each step all successors of all k states are generated
 * - If any one is a goal, then algorithm stops
 * - Otherwise, it selects the k best successors from the complete list and repeats
 *
 * @param <H> hypothesis type
 * @param <S> score object type
 * @param <EH> evaluated hypothesis type
 *
 * @author Lorenz Buehmann
 */
public abstract class BeamSearchOWL<H extends OWLObject,
                                    S extends Score,
                                    EH extends EvaluatedHypothesis<H, S>>
        extends BeamSearch<H, S, EH> {

    public BeamSearchOWL(int beamSize, Set<H> startHypotheses) {
        super(beamSize, startHypotheses);
    }

    public BeamSearchOWL(int beamSize) {
        super(beamSize);
    }


}
