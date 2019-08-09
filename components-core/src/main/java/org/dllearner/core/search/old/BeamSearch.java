package org.dllearner.core.search.old;

import java.util.*;
import java.util.stream.Collectors;

import org.dllearner.core.EvaluatedHypothesisOWL;
import org.dllearner.core.Score;
import org.dllearner.core.search.Beam;
import org.dllearner.core.search.BoundedTreeSet;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public abstract class BeamSearch<H extends OWLObject, S extends Score, EH extends EvaluatedHypothesisOWL<H, S>> {

    private static final Logger log = LoggerFactory.getLogger(BeamSearch.class);

    protected int beamSize;

    protected int maxNrOfResults = 100;

    private SortedSet<EH> solutions;

    private Set<H> startHypotheses;

    protected long startTime;
    protected double minAccuracy;

    public BeamSearch(int beamSize, Set<H> startHypotheses) {
        if (beamSize <= 0) {
            throw new IllegalArgumentException("beamSize = " + beamSize + "; expected a positive integer.");
        }
        Objects.requireNonNull(startHypotheses, "start hypotheses must not be null");
        this.beamSize = beamSize;
        this.startHypotheses = startHypotheses;
    }

    public BeamSearch(int beamSize) {
        this(beamSize, Collections.emptySet());
    }

    public void search() {
        startTime = System.currentTimeMillis();

        solutions = new BoundedTreeSet<>(maxNrOfResults);

        // init beam
        Beam<EH> beam = new Beam<>(beamSize, Comparator.naturalOrder());

        // populate beam with start hypotheses
        startHypotheses.stream().map(this::evaluate).forEach(beam::add);

        int i = 1;
        while(!beam.isEmpty() && !terminationCriteriaSatisfied()) {
            log.info("iteration " + i++);

            SortedSet<EH> candidates = new TreeSet<>();

            // process each element of the beam
            for (EH h : beam) {
                // compute refinements
                Set<H> refinements = refine(h.getDescription());

                // evaluate refinements
                Set<EH> evaluatedRefinements = evaluate(refinements);

                for (EH ref : evaluatedRefinements) {
                    if(isSolution(ref)) { // refinement is already a solution? TODO add option to refine solutions
                        addSolution(ref);
                    } else if(isCandidate(ref)){ // refinement is at least "good" enough being a candidate
                        candidates.add(ref);
                    }
                }
            }

            // re-populate the beam
            repopulateBeam(beam, candidates);
        }

    }

    /**
     * Refine a single hypothesis.
     *
     * @param hypothesis the hypothesis to refine
     * @return set of more specific hypothesis
     */
    protected abstract Set<H> refine(H hypothesis);

    /**
     * Evaluate a single hypothesis.
     *
     * @param hypothesis the hypothesis to evaluate
     * @return the evaluated hypothesis
     */
    protected abstract EH evaluate(H hypothesis);

    /**
     * Evaluate a set of hypotheses.
     *
     * @param hypotheses the hypotheses to evaluate
     * @return the evaluated hypotheses
     */
    protected Set<EH> evaluate(Set<H> hypotheses) {
        return hypotheses.stream().map(this::evaluate).collect(Collectors.toSet());
    }

    /**
     * Check if the given hypothesis is a solution.
     *
     * @param hypothesis the hypothesis to check
     * @return <code>true</code> if hypothesis is solution, otherwise <code>false</code>
     */
    protected abstract boolean isSolution(EH hypothesis);

    /**
     * Check if the given hypothesis is a sufficient candidate.
     *
     * @param hypothesis the candidate hypothesis to check
     * @return <code>true</code> if hypothesis is a candidate, otherwise <code>false</code>
     */
    protected abstract boolean isCandidate(EH hypothesis);

    /**
     * After all elements of the beam have been processed, the beam has to rebuild based
     * on the set of candidates that have been discovered during refinement.
     *
     * @param beam the beam that will be populated
     * @param candidates the set of candidates
     */
    protected abstract void repopulateBeam(Beam<EH> beam, SortedSet<EH> candidates);

    /**
     * Called to add solution candidate to solution set. Can be overridden to modify resp. transform solution before
     * added to final solution set.
     *
     * @param hypothesis
     * @return
     */
    protected boolean addSolution(EH hypothesis) {
        return solutions.add(hypothesis);
    }

    protected abstract boolean terminationCriteriaSatisfied();

    /**
     * Set the hypotheses to start with.
     *
     * @param startHypotheses
     */
    public void setStartHypotheses(Set<H> startHypotheses) {
        this.startHypotheses = startHypotheses;
    }

    /**
     * The maximum number of learned hypothesis maintained during learning and returned as final result.
     *
     * @param maxNrOfResults
     */
    public void setMaxNrOfResults(int maxNrOfResults) {
        this.maxNrOfResults = maxNrOfResults;
    }

    /**
     * Returns the solutions found so far, i.e. it can also be used as online algorithm.
     *
     * @return
     */
    public SortedSet<EH> getSolutions() {
        return solutions;
    }

    /**
     * Set the size of the beam maintained during learning.
     *
     * @param beamSize the size of the beam
     */
    public void setBeamSize(int beamSize) {
        this.beamSize = beamSize;
    }

    static class BeamNode<T, U extends Utility, Q extends Quality> implements Comparable<BeamNode<T, U, Q>> {

        private final BeamNode<T, U, Q> parent;

        private Q quality;
        private U utility;

        BeamNode(BeamNode<T, U, Q> parent) {
            this.parent = parent;
        }

        BeamNode(BeamNode<T, U, Q> parent, Q quality, U utility) {
            this.parent = parent;
            this.quality = quality;
            this.utility = utility;
        }

        public boolean isRoot() {
            return parent == null;
        }

        public BeamNode<T, U, Q> getParent() {
            return parent;
        }

        public Q getQuality() {
            return quality;
        }

        public U getUtility() {
            return utility;
        }

        @Override
        public int compareTo(@NotNull BeamNode<T, U , Q> other) {
            return utility.compareTo(other.utility);
        }
    }

    static abstract class Quality implements Comparable<Quality> {

    }

    static abstract class Utility<T extends Utility> implements Comparable<T> {

    }

    static class SimpleUtility extends Utility<SimpleUtility> {

        final double value;

        SimpleUtility(double value) {
            this.value = value;
        }

        @Override
        public int compareTo(@NotNull SimpleUtility other) {
            return Double.compare(this.value, other.value);
        }
    }


}
