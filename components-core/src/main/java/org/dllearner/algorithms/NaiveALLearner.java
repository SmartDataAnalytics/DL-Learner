package org.dllearner.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.OWLObjectIntersectionOfImplExt;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.ScorePosNeg;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * Simple example learning algorithm exhaustively creating complex class
 * expressions of the AL description logic.
 */
@ComponentAnn(name = "Naive AL Learner", shortName = "naiveALLearner", version = 0.1)
public class NaiveALLearner extends AbstractCELA{

    private Map<Integer, List<OWLClassExpression>> generatedDescriptions;
    
    private boolean running = false;
    @ConfigOption(defaultValue = "4", description = "maximum length of class expression")
    private int maxLength = 4;
    
    private OWLClassExpression bestDescription;
    private ScorePosNeg bestScore;

    public NaiveALLearner() {
    }

    public NaiveALLearner(AbstractClassExpressionLearningProblem lp, AbstractReasonerComponent reasoner) {
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
        generatedDescriptions = new HashMap<Integer, List<OWLClassExpression>>();
        
        // start with owl:Thing
        bestDescription = OWL_THING;
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
    public OWLClassExpression getCurrentlyBestDescription() {
        return bestDescription;
    }

    @Override
    public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
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
     * - `length` > 3:
     *      - all concepts of length `length`-1 are extended by intersection
     *        with an atomic class
     *      - all concepts of length `length`-2 are extended by limited
     *        existential quantification/value restriction
     *
     * @param length
     */
    private void generateDescriptions(int length) {
        generatedDescriptions.put(length, new ArrayList<OWLClassExpression>());
        List<OWLClassExpression> thisLenDescriptions = generatedDescriptions.get(length);

        if (length == 1) {
            // add atomic classes
            thisLenDescriptions.add(OWL_THING);
            thisLenDescriptions.add(OWL_NOTHING);

            for (OWLClassExpression atomicClass : reasoner.getAtomicConceptsList()) {
                thisLenDescriptions.add(atomicClass);
            }
        }

        if (length == 2) {
            // add negation of atomic classes
            for (OWLClassExpression atomicClass : reasoner.getAtomicConceptsList()) {
                thisLenDescriptions.add(dataFactory.getOWLObjectComplementOf(atomicClass));
            }
        }

        if (length == 3) {
            // add limited existential quantification/value restriction
            for (OWLObjectProperty prop : reasoner.getObjectProperties()) {
                thisLenDescriptions.add(dataFactory.getOWLObjectSomeValuesFrom(prop, OWL_THING));
                thisLenDescriptions.add(dataFactory.getOWLObjectAllValuesFrom(prop, OWL_THING));

                for (OWLClassExpression atomicClass : reasoner.getAtomicConceptsList()) {
                    thisLenDescriptions.add(dataFactory.getOWLObjectAllValuesFrom(prop, atomicClass));
                }
            }

            // add intersections of atomic concepts
            for (OWLClassExpression leftAtomicConcept : reasoner.getAtomicConceptsList()) {
                for(OWLClassExpression rightAtomicConcept : reasoner.getAtomicConceptsList()) {
                    thisLenDescriptions.add(new OWLObjectIntersectionOfImplExt(
                            Arrays.asList(leftAtomicConcept, rightAtomicConcept)));
                }
            }
        }

        if (length > 3) {
            // add ALL <objectProperty>.<generatedConcept> for all concepts of length
            // `length`-2
            for (OWLObjectProperty objProp : reasoner.getObjectProperties()) {
                for (OWLClassExpression description : generatedDescriptions.get(length-2)) {
                    thisLenDescriptions.add(dataFactory.getOWLObjectAllValuesFrom(objProp, description));
                }
            }
            // add <generatedConcept> INTERSECT <atomicClass> for all concepts
            // of length `length`-1
            for (OWLClassExpression atomicConcept : reasoner.getAtomicConceptsList()) {
                for (OWLClassExpression concept : generatedDescriptions.get(length-1)) {
                    thisLenDescriptions.add(new OWLObjectIntersectionOfImplExt(
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
            for (OWLClassExpression description : generatedDescriptions.get(i)) {
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
