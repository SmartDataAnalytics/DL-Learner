package org.dllearner.core.search.old;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Sets;
import org.dllearner.accuracymethods.AccMethodPredAcc;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassExpressionLearningProblem;
import org.dllearner.learningproblems.ClassScore;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.refinementoperators.RefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.owl.OWLClassExpressionMinimizer;
import org.dllearner.utilities.owl.SimpleOWLEntityChecker;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxClassExpressionParser;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

/**
 * Reference implementation of local beam search for OWL class expressions using
 * - class learning problem
 * - refinement operator rho
 * -
 *
 * @author Lorenz Buehmann
 */
public class SimpleLocalBeamSearch extends LocalBeamSearch<OWLClassExpression, ClassScore, EvaluatedDescriptionClass> {

    private RefinementOperator op;
    private ClassExpressionLearningProblem lp;
    private AbstractReasonerComponent reasoner;

    private double minAccuracy = 0.7;
    private long maxExecutionTimeMs = 10_000;

    private OWLClassExpressionMinimizer minimizer;

    public SimpleLocalBeamSearch(int beamSize,
                                 Set<OWLClassExpression> startHypotheses,
                                 AbstractReasonerComponent reasoner,
                                 RefinementOperator op,
                                 ClassExpressionLearningProblem lp) {
        super(beamSize, startHypotheses);
        this.reasoner = reasoner;
        this.op = op;
        this.lp = lp;

        minimizer = new OWLClassExpressionMinimizer(OWLManager.getOWLDataFactory(), reasoner);
    }

    @Override
    protected Set<OWLClassExpression> refine(OWLClassExpression hypothesis) {
        return ((RhoDRDown)op).refine(hypothesis, 5);
    }

    @Override
    protected EvaluatedDescriptionClass evaluate(OWLClassExpression hypothesis) {
        return lp.evaluate(hypothesis, 0.2);
    }

    @Override
    protected boolean isSolution(EvaluatedDescriptionClass hypothesis) {
//        System.out.println(hypothesis + "-->" + minimizer.minimize(hypothesis.getDescription()));
        // it can only be a solution if it's better than the min. accuracy
        return hypothesis.getAccuracy() >= minAccuracy
                && isAllowedClassExpression(minimizer.minimize(hypothesis.getDescription()))
                ;
    }

    @Override
    protected boolean isCandidate(EvaluatedDescriptionClass hypothesis) {
        double mas = getMaximumAchievableScore(hypothesis);
//        System.out.println(hypothesis + ":" + mas);
        return hypothesis.getAccuracy() < minAccuracy
                && getMaximumAchievableScore(hypothesis) >= minAccuracy
                && isAllowedClassExpression(hypothesis.getDescription());
    }

    @Override
    protected boolean terminationCriteriaSatisfied() {
        return false;
    }

    @Override
    protected boolean addSolution(EvaluatedDescriptionClass hypothesis) {
        // rewrite before added to solution set
        hypothesis.setDescription(minimizer.minimize(hypothesis.getDescription()));
        return super.addSolution(hypothesis);
    }

    private double getMaximumAchievableScore(EvaluatedDescriptionClass ece) {
        return getMaximumAchievableScore(
                ece.getScore().getCoveredInstances().size(),
                ece.getScore().getNotCoveredNegInstances().size(),
                ece.getAdditionalInstances().size(),
                ece.getNotCoveredInstances().size());
    }

    private double getMaximumAchievableScore(int tp, int tn, int fp, int fn) {
        // due to downward refinement, the number of covered instances can only be reduced
        // thus, the upper bound should be the accuracy when we do not cover any neg. examples, i.e. fp = 0
        double mas = ((AccMethodPredAcc)lp.getAccuracyMethod()).getAccOrTooWeak2(tp, fn, 0, fn, 1.0);

        double posExamplesWeight = 0.8;
        // (tn + tp) / (tn + fp + fn + tp) -> (tn + (tp + fn)) / (tn + fp + fn + tp)
        //			mas = (posExamplesWeight * tp + tn - fp) / (posExamplesWeight * (tp + fn) + tn + fp);
        // due to refinement, the only space for improvement is fn becoming tp
//        double mas = (posExamplesWeight * (tp + fn) + tn) / (posExamplesWeight * (tp + fn) + tn + fp);
        return mas;
    }

    // checks whether the class expression is allowed
    boolean isEquivalenceProblem = true;
    private boolean isAllowedClassExpression(OWLClassExpression description) {
        if(description.equals(lp.getClassExpressionToDescribe())) {
            return false;
        }

        if(isEquivalenceProblem) {
            // the class to learn must not appear on the outermost property level
            if(occursOnFirstLevel(description, lp.getClassExpressionToDescribe())) {
                return false;
            }
        } else {
            // none of the superclasses of the class to learn must appear on the
            // outermost property level
            TreeSet<OWLClassExpression> toTest = new TreeSet<>();
            toTest.add(lp.getClassExpressionToDescribe());
            while(!toTest.isEmpty()) {
                OWLClassExpression d = toTest.pollFirst();
                if(occursOnFirstLevel(description, d)) {
                    return false;
                }
                toTest.addAll(reasoner.getClassHierarchy().getSuperClasses(d));
            }
        }
        return true;
    }

    private boolean occursOnFirstLevel(OWLClassExpression description, OWLClassExpression cls) {
        return !cls.isOWLThing() &&
                (description instanceof OWLNaryBooleanClassExpression &&
                ((OWLNaryBooleanClassExpression) description).getOperands().contains(cls));
    }

    public static void main(String[] args) throws Exception {
        ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
//        File file = new File("../examples/family/father_oe.owl");
//        OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://example.com/father#father"));
//        OWLClass startClass = new OWLClassImpl(IRI.create("http://example.com/father#male"));

        File file = new File("../examples/swore/swore.rdf");
        OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://ns.softwiki.de/req/CustomerRequirement"));
        OWLClass startClass = new OWLClassImpl(IRI.create("http://ns.softwiki.de/req/AbstractRequirement"));



        AbstractKnowledgeSource ks = OWLAPIOntology.fromFile(file);
        ks.init();

        OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
        baseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        baseReasoner.init();
        ClosedWorldReasoner rc = new ClosedWorldReasoner(ks);
        rc.setReasonerComponent(baseReasoner);
        rc.init();

        ClassExpressionLearningProblem lp = new ClassExpressionLearningProblem(rc);
        lp.setAccuracyMethod(new AccMethodPredAcc());
//		lp.setEquivalence(false);
        lp.setClassExpressionToDescribe(classToDescribe);
        lp.init();

        SimpleOWLEntityChecker entityChecker = new SimpleOWLEntityChecker(rc);
        entityChecker.setAllowShortForm(true);
        OWLClassExpression ce = new ManchesterOWLSyntaxClassExpressionParser(OWLManager.getOWLDataFactory(), entityChecker)
                .parse("Requirement and isCreatedBy some Thing");

        EvaluatedDescription<ClassScore> ec = lp.evaluate(ce);
        System.out.println(ec);
//        System.exit(0);

        RhoDRDown op = new RhoDRDown();
        op.setStartClass(startClass);
        op.setReasoner(rc);
        op.setUseNegation(false);
        op.setUseHasValueConstructor(false);
        op.setUseCardinalityRestrictions(true);
        op.setUseExistsConstructor(true);
        op.setUseAllConstructor(true);
        op.init();

        SimpleLocalBeamSearch alg = new SimpleLocalBeamSearch(100, Sets.newHashSet(startClass), rc, op, lp);
        alg.search();
        alg.getSolutions().forEach(System.out::println);


    }
}
