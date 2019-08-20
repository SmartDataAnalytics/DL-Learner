package org.dllearner.core.search.old2;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ComparisonChain;
import org.dllearner.core.EvaluatedHypothesis;
import org.dllearner.core.EvaluatedHypothesisOWL;
import org.dllearner.core.Score;
import org.dllearner.core.search.Beam;
import org.dllearner.core.search.BoundedTreeSet;
import org.dllearner.utilities.ProgressMonitor;
import org.dllearner.utilities.SilentProgressMonitor;
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
public abstract class BeamSearch<
        H extends OWLObject,
        S extends Score,
        EH extends EvaluatedHypothesis<H, S>>
         {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // the hypotheses to start with
    private Set<H> startHypotheses;

    // the max. size of the beam
    protected int beamSize;

    // the max. number of solutions that will be returned and also maintained during the algorithm
    protected int maxNrOfSolutions = 10;

    // the (bounded) set of solutions
    private SortedSet<EH> solutions;

    // denotes the min. quality of a solution //TODO make it an object with lower and upper bound?
    protected double minQuality = 0.7;

    // start time of the algorithm
    protected long startTime;

    protected ProgressMonitor progressMonitor = new SilentProgressMonitor();


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

        solutions = new BoundedTreeSet<>(maxNrOfSolutions, Comparator.reverseOrder());

        // init beam
        Beam<BeamNode<EH>> beam = new Beam<>(beamSize, Comparator.naturalOrder());

        // populate beam with start hypotheses
        startHypotheses.stream().map(this::evaluate).map(BeamNode::new).forEach(beam::add);

        // best quality so far is min. quality
        double bestQuality = minQuality;

        int i = 1;
        while(!beam.isEmpty() && !terminationCriteriaSatisfied()) {
            nextIterationStarted(i++);

            Set<H> candidateHypotheses = new HashSet<>();// beam.stream().flatMap(node -> refine(node.hypothesis.getDescription()).stream()).collect(Collectors.toSet());

            SortedSet<BeamNode<EH>> candidates = new TreeSet<>();

            // process each element of the beam
            int done = 0;
            int total = beam.size();
            for (BeamNode<EH> node : beam) {
                progressMonitor.updateProgress(++done, total, "processing node " + node);
                // compute refinements
                Set<H> refinements = refine(node.hypothesis.getDescription());
                refinements.removeAll(candidateHypotheses);

                // evaluate refinements
                Set<EH> evaluatedRefinements = evaluate(refinements);

                for (EH ref : evaluatedRefinements) {
                    if(isSolution(ref)) { // refinement is already a solution?
                        // we update the min. quality value once a new solution was added to the solutions
                        if(addSolution(ref)) {
                            minQuality = quality(solutions.last());
                            // notify if new best solution was found
                            if(quality(solutions.first()) > bestQuality) {
                                log.info("\nbetter solution found: " + ref);
                                bestQuality = quality(solutions.first());
                            }
                        }
                        // TODO add option to refine solutions
                        if(quality(ref) < 1.0) {
                            candidates.add(new BeamNode<>(ref, node, utility(ref, node.hypothesis)));
                        }
                    } else if(isCandidate(ref)){ // refinement is at least "good" enough being a candidate for populating the beam
                        candidates.add(new BeamNode<>(ref, node, utility(ref, node.hypothesis)));
                    }
                }

                candidateHypotheses.addAll(refinements);
            }

            // re-populate the beam
//            log.info("\ntop candidates:\n" + candidates.stream().limit(beamSize).map(BeamNode::toString).collect(Collectors.joining("\n")));
            repopulateBeam(beam, candidates);
//            log.info("\nbeam:\n" + beam.stream().map(BeamNode::toString).collect(Collectors.joining("\n")));

            // TODO add option to either clear the candidates or consider them for re-populating the beam in next iteration
            candidates.clear();
        }

    }

    /**
     * Method is called when a new iteration in beam search is started. Can be used for logging, adapting parameters or just for keeping
     * track of statistics w.r.t. the current iteration.
     *
     * @param i the iteration number
     */
    protected void nextIterationStarted(int i){
        log.info("iteration " + i++);
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
    protected boolean isSolution(EH hypothesis) {
        return quality(hypothesis) > minQuality;
    };

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
    protected abstract void repopulateBeam(Beam<BeamNode<EH>> beam, SortedSet<BeamNode<EH>> candidates);

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

    /**
     * Computes the utility of a hypothesis when used in the beam search. This value is used to guide the beam search.
     *
     * @param hypothesis the hypothesis
     * @param parent the parent hypothesis
     * @return the utility value, higher is better
     */
    protected double utility(EH hypothesis, EH parent) {
        // quality gain
        double qualityGain = quality(hypothesis) - quality(parent);

        double utility = quality(hypothesis) + 0.5 * qualityGain - 0.05 * complexity(hypothesis);
        return utility;
    }

    /**
     * Computes the quality of a hypothesis. This value is used to decide on whether a hypothesis is a solution
     * or just candidate which will be further decided on in {@link #isCandidate(EvaluatedHypothesis) isCandidate}
     * method.
     * The default implementation of {@link #utility(EvaluatedHypothesis, EvaluatedHypothesis) utility} does use
     * the quality value.
     *
     * @param hypothesis the hypothesis
     * @return the quality value, higher is better
     */
    protected double quality(EH hypothesis) {
        return hypothesis.getAccuracy();
    }

    /**
     * Computes the complexity of a hypothesis. This value is considered when computing the utility of a hypothesis.
     *
     * The default implementation of {@link #utility(EvaluatedHypothesis, EvaluatedHypothesis) utility} does use
     * the complexity value as a negative factor based on the assumption the less complex solutions might be better.
     *
     * Defaults to 0.0
     *
     * @param hypothesis the hypothesis
     * @return the complexity value, higher is better
     */
    protected double complexity(EH hypothesis) {
        return 0.0;
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
     * @param maxNrOfSolutions
     */
    public void setMaxNrOfSolutions(int maxNrOfSolutions) {
        this.maxNrOfSolutions = maxNrOfSolutions;
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

    /**
     * Set the min. value of quality a solution has to fulfill
     *
     * @param minQuality the min. quality
     */
    public void setMinQuality(double minQuality) {
        this.minQuality = minQuality;
    }

    public void setProgressMonitor(ProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
    }


    static class BeamNode<T extends EvaluatedHypothesis> implements Comparable<BeamNode<T>> {

        private final T hypothesis;
        private final BeamNode<T> parent;
        private final double utility;

        /**
         * Creates a root node having no parent. All root nodes will be given the same utility value of 1.0
         * @param hypothesis the hypothesis
         */
        BeamNode(T hypothesis) {
            this(hypothesis, null, 1.0);
        }

        BeamNode(T hypothesis, BeamNode<T> parent, double utility) {
            this.hypothesis = hypothesis;
            this.parent = parent;
            this.utility = utility;
        }

        public boolean isRoot() {
            return parent == null;
        }

        public BeamNode<T> getParent() {
            return parent;
        }

        public double getUtility() {
            return utility;
        }

        public T getHypothesis() {
            return hypothesis;
        }

        @Override
        public int compareTo(@NotNull BeamNode<T> other) {
            return -1 * ComparisonChain.start()
                    .compare(utility, other.utility)
                    .compare(hypothesis.getAccuracy(), other.hypothesis.getAccuracy())
                    .compare(hypothesis.getDescription(), other.hypothesis.getDescription())
                    .result();
        }

        @Override
        public String toString() {
            return hypothesis + "\t(u(h)=" + utility + ")";
        }
    }

}
