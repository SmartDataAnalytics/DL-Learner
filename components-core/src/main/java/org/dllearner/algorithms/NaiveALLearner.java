package org.dllearner.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.ScorePosNeg;

/**
 * Simple example learning algorithm exhaustively creating complex class
 * expressions of the AL description logic.
 */
@ComponentAnn(name = "Naive ALC Learner", shortName = "naiveALCLearner", version = 0.1)
public class NaiveALLearner extends AbstractCELA{

    private Map<Integer, List<Description>> generatedDescriptions;
    private boolean running = false;
    private int maxLength = 4;
    private static final Description top = new Thing();
    private static final Description bottom = new Nothing();
    private Description bestDescription;
    private ScorePosNeg bestScore;

    public NaiveALLearner(AbstractLearningProblem lp, AbstractReasonerComponent reasoner) {
        super(lp, reasoner);
    }
    @Override
    public void start() {
        running = true;

        // first generate all possible complex class expressions up to a length
        // of maxLength
        for (int i=1; i<=maxLength; i++) {
            generateDescriptions(i);
        }

        // now evaluate these expressions to find a description that best
        // describes and distinguishes positive and negative examples
        evaluateGeneratedDefinition();

        System.out.println("Best description: " + bestDescription);
        System.out.println("Best score: " + bestScore);
        stop();
    }

    @Override
    public void init() throws ComponentInitException {
        generatedDescriptions = new HashMap<Integer, List<Description>>();
        bestDescription = top;
        bestScore = null;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public Description getCurrentlyBestDescription() {
        return bestDescription;
    }

    @Override
    public EvaluatedDescription     getCurrentlyBestEvaluatedDescription() {
        return new EvaluatedDescriptionPosNeg(bestDescription,bestScore);
    }

    /**
     * Generates new descriptions (i.e. concepts) of the length `length`.
     * - `length` == 1:
     *      - all atomic classes are added to the set `generatedDescriptions`
     *      - the concepts owl:Thing and owl:Nothing are added
     * - `length` == 2:
     *      - negations of all atomic classes are added
     * - `length` == 3:
     *      - all atomic roles are added as limited existential quantification/
     *        value restriction
     *      - all combinations of intersections of atomic classes are added
     * - `length` >= 3:
     *      - all concepts of length `length`-1 are extended by intersection
     *        with an atomic class
     *      - all concepts of length `length`-2 are extended by limited
     *        existential quantification/value restriction
     *
     * @param length
     */
    private void generateDescriptions(int length) {
        generatedDescriptions.put(length, new ArrayList<Description>());
        List<Description> thisLenDescriptions = generatedDescriptions.get(length);

        if (length == 1) {
            // add atomic classes
            thisLenDescriptions.add(top);
            thisLenDescriptions.add(bottom);

            for (Description atomicClass : reasoner.getAtomicConceptsList()) {
                thisLenDescriptions.add(atomicClass);
            }
        }

        if (length == 2) {
            // add negation of atomic classes
            for (Description atomicClass : reasoner.getAtomicConceptsList()) {
                thisLenDescriptions.add(new Negation(atomicClass));
            }
        }

        if (length == 3) {
            // add limited existential quantification/value restriction
            for (ObjectProperty prop : reasoner.getObjectProperties()) {
                thisLenDescriptions.add(new ObjectSomeRestriction(prop, top));
                thisLenDescriptions.add(new ObjectAllRestriction(prop, top));

                for (Description atomicClass : reasoner.getAtomicConceptsList()) {
                    thisLenDescriptions.add(new ObjectAllRestriction(prop, atomicClass));
                }
            }

            // add intersections of atomic concepts
            for (Description leftAtomicConcept : reasoner.getAtomicConceptsList()) {
                for(Description rightAtomicConcept : reasoner.getAtomicConceptsList()) {
                    thisLenDescriptions.add(new Intersection(
                            Arrays.asList(leftAtomicConcept, rightAtomicConcept)));
                }
            }
        }

        if (length > 3) {
            // add ALL <objectProperty>.<generatedConcept> for all concepts of length
            // `length`-2
            for (ObjectProperty objProp : reasoner.getObjectProperties()) {
                for (Description description : generatedDescriptions.get(length-2)) {
                    thisLenDescriptions.add(new ObjectAllRestriction(objProp, description));
                }
            }
            // add <generatedConcept> INTERSECT <atomicClass> for all concepts
            // of length `length`-1
            for (Description atomicConcept : reasoner.getAtomicConceptsList()) {
                for (Description concept : generatedDescriptions.get(length-1)) {
                    thisLenDescriptions.add(new Intersection(
                            Arrays.asList(concept, atomicConcept)));
                }
            }
        }
    }

    public void setMaxLength(int length) {
        maxLength = length;
    }

    public int getMaxLength() {
        return maxLength;
    }

    private void evaluateGeneratedDefinition() {
        double bestScoreVal = Double.NEGATIVE_INFINITY;
        double tmpScoreVal;
        ScorePosNeg tmpScore;
        for (int i=1; i<=maxLength; i++) {
            for (Description description : generatedDescriptions.get(i)) {
                tmpScore = (ScorePosNeg) learningProblem.computeScore(description);
                tmpScoreVal = tmpScore.getScoreValue();
                if (tmpScoreVal > bestScoreVal) {
                    bestDescription = description;
                    bestScore = tmpScore;
                    bestScoreVal = tmpScoreVal;
                }
            }
        }
    }
}
