package org.dllearner.core.search;

import java.util.*;

import org.dllearner.core.EvaluatedHypothesis;
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
public abstract class BeamSearch<H extends OWLObject, S extends Score, EH extends EvaluatedHypothesis<H, S>> {

    protected final int beamSize;

    private SortedSet<EH> solutions;

    private Set<EH> startHypotheses;

    public BeamSearch(int beamSize, Set<EH> startHypotheses) {
        Objects.requireNonNull(startHypotheses, "start hypotheses must not be null");
        this.beamSize = beamSize;
        this.startHypotheses = startHypotheses;
    }

    public BeamSearch(int beamSize) {
        this(beamSize, Collections.emptySet());
    }

    public void setStartHypotheses(Set<EH> startHypotheses) {
        this.startHypotheses = startHypotheses;
    }

    public void search() {
        solutions = new TreeSet<>();

        // init beam
        Beam<EH> beam = new Beam<EH>(beamSize, Comparator.naturalOrder());

        // populate beam with start hypotheses
        beam.addAll(startHypotheses);

        while(!beam.isEmpty() && !terminationCriteriaSatisfied()) {

            SortedSet<EH> candidates = new TreeSet<>();

            // process each element of the beam
            for (EH h : beam) {
                // compute refinements
                Set<EH> refinements = refine(h);

                for (EH ref : refinements) {
                    if(isSolution(ref)) { // refinement is already a solution? TODO add option to refine solutions
                        solutions.add(ref);
                    } else if(isCandidate(ref)){ // refinement is at least "good" enough being a candidate
                        refinements.add(ref);
                    }
                }
                candidates.addAll(refinements);
            }

            // re-populate the beam
            repopulateBeam(beam, candidates);
        }

    }

    abstract Set<EH> refine(EH hypothesis);

    /**
     * Check if the given hypothesis is a solution.
     *
     * @param hypothesis the hypothesis to check
     * @return <code>true</code> if hypothesis is solution, otherwise <code>false</code>
     */
    abstract boolean isSolution(EH hypothesis);

    /**
     * Check if the given hypothesis is a sufficient candidate.
     *
     * @param hypothesis the candidate hypothesis to check
     * @return <code>true</code> if hypothesis is a candidate, otherwise <code>false</code>
     */
    abstract boolean isCandidate(EH hypothesis);

    /**
     * After all elements of the beam have been processed, the beam has to rebuild based
     * on the set of candidates that have been discovered during refinement.
     *
     * @param beam the beam that will be populated
     * @param candidates the set of candidates
     */
    abstract void repopulateBeam(Beam<EH> beam, SortedSet<EH> candidates);

    abstract boolean terminationCriteriaSatisfied();

    public SortedSet<EH> getSolutions() {
        return solutions;
    }


}
