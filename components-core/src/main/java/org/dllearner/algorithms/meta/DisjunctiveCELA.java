package org.dllearner.algorithms.meta;

import com.google.common.collect.Iterables;
import org.dllearner.core.*;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.utilities.Helper;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A meta algorithm that combines the (partial) solutions of multiple calls of the base class learning algorithm LA
 * into a disjunction.
 * In particular, a partial solution is computed by running LA for a given time only on examples of the learning
 * problem that aren't already covered by previously computed solutions.
 *
 * @author Lorenz Buehmann
 */
public class DisjunctiveCELA extends AbstractCELA {

    private static final Logger log = LoggerFactory.getLogger(DisjunctiveCELA.class);

    @ConfigOption(defaultValue = "0.1", description="Specifies the min accuracy for a partial solution.")
    private double minAccuracyPartialSolution = 0.1;

    @ConfigOption(defaultValue = "10", description="Specifies how long the algorithm should search for a partial solution.")
    private int partialSolutionSearchTimeSeconds = 10;

    @ConfigOption(defaultValue = "false", description = "If yes, then the algorithm tries to cover all positive examples. " +
            "Note that while this improves accuracy on the testing set, it may lead to overfitting.")
    private boolean tryFullCoverage = false;

    @ConfigOption(defaultValue="false", description="algorithm will terminate immediately when a correct definition is found")
    private boolean stopOnFirstDefinition = false;

    @ConfigOption(defaultValue="0.0", description="the (approximated) percentage of noise within the examples")
    private double noisePercentage = 0.0;

    // the class with which we start the refinement process
    @ConfigOption(defaultValue="owl:Thing", description="You can specify a start class for the algorithm. " +
            "To do this, you have to use Manchester OWL syntax without using prefixes.")
    private OWLClassExpression startClass;


    // the core learning algorithm
    private final AbstractCELA la;
    private final AbstractClassExpressionLearningProblem<? extends Score> lp;

    private Set<OWLIndividual> currentPosExamples;
    private Set<OWLIndividual> currentNegExamples;

    private Set<OWLIndividual> initialPosExamples;

    private List<EvaluatedDescription<? extends Score>> partialSolutions = new ArrayList<>();

    /**
     * @param la the basic learning algorithm
     */
    public DisjunctiveCELA(AbstractCELA la) {
        this.la = la;
        this.lp = la.getLearningProblem();
    }

    @Override
    public void init() throws ComponentInitException {
        la.setMaxExecutionTimeInSeconds(partialSolutionSearchTimeSeconds);

        reset();
        initialized = true;
    }

    @Override
    public void start() {
        nanoStartTime = System.nanoTime();

        while(!stop && !stoppingCriteriaSatisfied()) {

            // compute next partial solution
            EvaluatedDescription<? extends Score> partialSolution = computePartialSolution();

            // add to global solution if criteria are satisfied
            if(addPartialSolution(partialSolution)) {
                log.info("new partial solution found: {}", partialSolution);

                // update the learning problem
                updateLearningProblem(partialSolution);
            }

        }
        log.info("finished computation in {}.\n top 10 solutions:\n{}",
                Helper.prettyPrintMilliSeconds(getCurrentRuntimeInMilliSeconds()),
                getSolutionString());

    }

    private void reset() {
        currentPosExamples = new TreeSet<>(((PosNegLP) la.getLearningProblem()).getPositiveExamples());
        currentNegExamples = new TreeSet<>(((PosNegLP) la.getLearningProblem()).getNegativeExamples());

        // keep copy of the initial pos examples
        initialPosExamples = new TreeSet<>(currentPosExamples);
    }

    private EvaluatedDescription<? extends Score> computePartialSolution() {
        log.info("computing next partial solution...");
        la.start();
        EvaluatedDescription<? extends Score> partialSolution = la.getCurrentlyBestEvaluatedDescription();
        return partialSolution;
    }

    private boolean addPartialSolution(EvaluatedDescription<? extends Score> partialSolution) {
        // check whether partial solution follows criteria (currently only accuracy threshold)
        if(Double.compare(partialSolution.getAccuracy(), minAccuracyPartialSolution) > 0) {
            partialSolutions.add(partialSolution);

            // create combined solution
            OWLObjectUnionOf combinedCE = dataFactory.getOWLObjectUnionOf(
                                                            partialSolutions.stream()
                                                                            .map(EvaluatedHypothesis::getDescription)
                                                                            .collect(Collectors.toSet()));
            // evalute combined solution
            EvaluatedDescription<? extends Score> combinedSolution = lp.evaluate(combinedCE);
            bestEvaluatedDescriptions.add(combinedSolution);

            return true;
        }
        return false;
    }

    private void updateLearningProblem(EvaluatedDescription<? extends Score> partialSolution) {
        // get individuals covered by the solution
        SortedSet<OWLIndividual> coveredExamples = la.getReasoner().getIndividuals(partialSolution.getDescription());

        // remove from pos examples as those are already covered
        currentPosExamples.removeAll(coveredExamples);

        // remove from neg examples as those will always be covered in the combined solution
        currentNegExamples.removeAll(coveredExamples);

        // update the learning problem itself // TODO do we need some re-init of the lp afterwards?
        if(lp instanceof PosNegLP) {
            ((PosNegLP) la.getLearningProblem()).setPositiveExamples(currentPosExamples);
            ((PosNegLP) la.getLearningProblem()).setNegativeExamples(currentNegExamples);
        } else if(lp instanceof PosOnlyLP) {
            ((PosOnlyLP) la.getLearningProblem()).setPositiveExamples(currentPosExamples);
        } else if(lp instanceof ClassLearningProblem){
            // TODO
        }

    }

    private boolean stoppingCriteriaSatisfied() {
        // global time expired
        if(isTimeExpired()) {
            return true;
        }

        // stop if there are no more positive examples to cover
        if(stopOnFirstDefinition && currentPosExamples.size()==0) {
            return true;
        }

        // we stop when the score of the last tree added is too low
        // (indicating that the algorithm could not find anything appropriate
        // in the timeframe set)
        EvaluatedDescription<? extends Score> lastPartialSolution = Iterables.getLast(partialSolutions, null);
        if(lastPartialSolution != null && Double.compare(lastPartialSolution.getAccuracy(), minAccuracyPartialSolution) <= 0) {
            return true;
        }

        // stop when almost all positive examples have been covered
        if(tryFullCoverage) {
            return false;
        } else {
            int maxPosRemaining = (int) Math.ceil(initialPosExamples.size() * 0.05d);
            return (currentPosExamples.size()<=maxPosRemaining);
        }
    }

    @Override
    public void stop() {
        // we also have to stop the underlying learning algorithm
        la.stop();
        super.stop();
    }

    /**
     * Sets the max. execution time of the whole algorithm. Note, this values should always be higher
     * than the max. execution time to compute a partial solution.
     *
     * @param maxExecutionTimeInSeconds the overall the max. execution time
     */
    @Override
    public void setMaxExecutionTimeInSeconds(long maxExecutionTimeInSeconds) {
        super.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
    }

    public void setTryFullCoverage(boolean tryFullCoverage) {
        this.tryFullCoverage = tryFullCoverage;
    }

    public void setMinAccuracyPartialSolution(double minAccuracyPartialSolution) {
        this.minAccuracyPartialSolution = minAccuracyPartialSolution;
    }

    public void setPartialSolutionSearchTimeSeconds(int partialSolutionSearchTimeSeconds) {
        this.partialSolutionSearchTimeSeconds = partialSolutionSearchTimeSeconds;
    }
}
