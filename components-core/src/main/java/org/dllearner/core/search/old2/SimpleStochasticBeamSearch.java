//package org.dllearner.core.search.old2;
//
//import java.io.File;
//import java.util.Set;
//import java.util.SortedSet;
//import java.util.TreeSet;
//
//import com.google.common.collect.Sets;
//import org.dllearner.accuracymethods.AccMethodPredAcc;
//import org.dllearner.core.*;
//import org.dllearner.core.search.Beam;
//import org.dllearner.kb.OWLAPIOntology;
//import org.dllearner.learningproblems.*;
//import org.dllearner.reasoning.ClosedWorldReasoner;
//import org.dllearner.reasoning.OWLAPIReasoner;
//import org.dllearner.reasoning.ReasonerImplementation;
//import org.dllearner.refinementoperators.RefinementOperator;
//import org.dllearner.refinementoperators.RhoDRDown;
//import org.dllearner.utilities.owl.OWLClassExpressionMinimizer;
//import org.dllearner.utilities.owl.SimpleOWLEntityChecker;
//import org.semanticweb.owlapi.apibinding.OWLManager;
//import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
//import org.semanticweb.owlapi.io.ToStringRenderer;
//import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxClassExpressionParser;
//import org.semanticweb.owlapi.model.IRI;
//import org.semanticweb.owlapi.model.OWLClass;
//import org.semanticweb.owlapi.model.OWLClassExpression;
//import org.semanticweb.owlapi.model.OWLNaryBooleanClassExpression;
//import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
//
///**
// * Reference implementation of stochastic beam search for OWL class expressions using
// * - class learning problem
// * - refinement operator rho
// * -
// *
// * @author Lorenz Buehmann
// */
//public class SimpleStochasticBeamSearch<S extends Score>
//        extends StochasticBeamSearch<OWLClassExpression, S, EvaluatedDescription<S>> {
//
//    private RefinementOperator op;
//    private AbstractClassExpressionLearningProblem<? extends Score> lp;
//    private AbstractReasonerComponent reasoner;
//
//    private long maxExecutionTimeMs = 10_000;
//
//    private OWLClassExpressionMinimizer minimizer;
//
//    public SimpleStochasticBeamSearch(int beamSize,
//                                      Set<OWLClassExpression> startHypotheses,
//                                      AbstractReasonerComponent reasoner,
//                                      RefinementOperator op,
//                                      AbstractClassExpressionLearningProblem<? extends Score> lp) {
//        super(beamSize, startHypotheses);
//        this.reasoner = reasoner;
//        this.op = op;
//        this.lp = lp;
//
//        minimizer = new OWLClassExpressionMinimizer(OWLManager.getOWLDataFactory(), reasoner);
//    }
//
//    @Override
//    protected Set<OWLClassExpression> refine(OWLClassExpression hypothesis) {
//        return ((RhoDRDown)op).refine(hypothesis, 5);
//    }
//
//    @Override
//    protected EvaluatedDescription<? extends Score> evaluate(OWLClassExpression hypothesis) {
//        return lp.evaluate(hypothesis, 0.2);
//    }
//
//    @Override
//    protected boolean isSolution(EvaluatedDescription<? extends Score> hypothesis) {
////        System.out.println(hypothesis + "-->" + minimizer.minimize(hypothesis.getDescription()));
//        // it can only be a solution if it's better than the min. accuracy
//        return hypothesis.getAccuracy() >= minQuality
//                && isAllowedClassExpression(minimizer.minimize(hypothesis.getDescription()))
//                ;
//    }
//
//    @Override
//    protected boolean isCandidate(EvaluatedDescription<? extends Score> hypothesis) {
//        double mas = getMaximumAchievableScore(hypothesis);
////        System.out.println(hypothesis + ":" + mas);
//        return hypothesis.getAccuracy() < minQuality
//                && getMaximumAchievableScore(hypothesis) >= minQuality
//                && isAllowedClassExpression(hypothesis.getDescription());
//    }
//
//    @Override
//    protected boolean terminationCriteriaSatisfied() {
//        return false;
//    }
//
//    @Override
//    protected boolean addSolution(EvaluatedDescription<? extends Score> hypothesis) {
//        // rewrite before added to solution set
////        System.out.println(hypothesis + " -> " + minimizer.minimize(hypothesis.getDescription()));
//        hypothesis.setDescription(minimizer.minimize(hypothesis.getDescription()));
////        hypothesis.setDescription(hypothesis.getDescription());
//        return super.addSolution(hypothesis);
//    }
//
//    @Override
//    protected double utility(EvaluatedDescription<? extends Score> hypothesis, EvaluatedDescription<? extends Score> parent) {
//        // quality gain
//        double qualityGain = quality(hypothesis) - quality(parent);
//
//        // max. achievable quality
//        double maq = getMaximumAchievableScore(hypothesis);
//
//        double utility = quality(hypothesis) + 0.5 * qualityGain + 0.2 * maq;
//        return utility;
//    }
//
//    @Override
//    protected double quality(EvaluatedDescription<? extends Score> hypothesis) {
//        return hypothesis.getAccuracy();
//    }
//
//    @Override
//    protected void repopulateBeam(Beam<BeamNode<EvaluatedDescription<? extends Score>>> beam,
//                                  SortedSet<BeamNode<EvaluatedDescription<? extends Score>>> candidates) {
//        System.out.println("|beam| = " + beam.size());
//        System.out.println("|candidates| = " + candidates.size());
//        super.repopulateBeam(beam, candidates);
//    }
//
//    private double getMaximumAchievableScore(EvaluatedDescription<? extends Score> ece) {
//        Score score = ece.getScore();
//        if(score instanceof ClassScore) {
//            ClassScore cScore = ((ClassScore) score);
//            return getMaximumAchievableScore(
//                    cScore.getCoveredInstances().size(),
//                    cScore.getNotCoveredNegInstances().size(),
//                    cScore.getAdditionalInstances().size(),
//                    cScore.getNotCoveredInstances().size());
//        } else if(score instanceof ScorePosNeg) {
//            ScorePosNeg cScore = ((ScorePosNeg) score);
//            return getMaximumAchievableScore(
//                    cScore.getCoveredPositives().size(),
//                    cScore.getNotCoveredNegatives().size(),
//                    cScore.getCoveredNegatives().size(),
//                    cScore.getNotCoveredPositives().size());
//
//        } else {
//            throw new RuntimeException("score type not supported yet");
//        }
//
//    }
//
//    private double getMaximumAchievableScore(int tp, int tn, int fp, int fn) {
//        // due to downward refinement, the number of covered instances can only be reduced
//        // thus, the upper bound should be the accuracy when we do not cover any neg. examples, i.e. fp = 0
//        double mas = 1;
//        if(lp instanceof ClassLearningProblem) {
//            mas = ((AccMethodPredAcc)((ClassLearningProblem) lp).getAccuracyMethod()).getAccOrTooWeak2(tp, fn, 0, fn, 1.0);
//        } else if(lp instanceof PosNegLP) {
//            mas = ((AccMethodPredAcc)((PosNegLP) lp).getAccuracyMethod()).getAccOrTooWeak2(tp, fn, 0, fn, 1.0);
//        }
////        double mas = ((AccMethodPredAcc)lp.getAccuracyMethod()).getAccOrTooWeak2(tp, fn, 0, fn, 1.0);
//
//        double posExamplesWeight = 0.8;
//        // (tn + tp) / (tn + fp + fn + tp) -> (tn + (tp + fn)) / (tn + fp + fn + tp)
//        //			mas = (posExamplesWeight * tp + tn - fp) / (posExamplesWeight * (tp + fn) + tn + fp);
//        // due to refinement, the only space for improvement is fn becoming tp
////        double mas = (posExamplesWeight * (tp + fn) + tn) / (posExamplesWeight * (tp + fn) + tn + fp);
//        return mas;
//    }
//
//    // checks whether the class expression is allowed
//    boolean isEquivalenceProblem = true;
//    private boolean isAllowedClassExpression(OWLClassExpression description) {
//        if(lp instanceof ClassExpressionLearningProblem) {
//            OWLClassExpression ce = ((ClassExpressionLearningProblem) lp).getClassExpressionToDescribe();
//            if(description.equals(ce)) {
//                return false;
//            }
//
//            if(isEquivalenceProblem) {
//                // the class to learn must not appear on the outermost property level
//                if(occursOnFirstLevel(description, ce)) {
//                    return false;
//                }
//            } else {
//                // none of the superclasses of the class to learn must appear on the
//                // outermost property level
//                TreeSet<OWLClassExpression> toTest = new TreeSet<>();
//                toTest.add(ce);
//                while(!toTest.isEmpty()) {
//                    OWLClassExpression d = toTest.pollFirst();
//                    if(occursOnFirstLevel(description, d)) {
//                        return false;
//                    }
//                    toTest.addAll(reasoner.getClassHierarchy().getSuperClasses(d));
//                }
//            }
//        }
//
//        return true;
//    }
//
//    private boolean occursOnFirstLevel(OWLClassExpression description, OWLClassExpression cls) {
//        return !cls.isOWLThing() &&
//                (description instanceof OWLNaryBooleanClassExpression &&
//                ((OWLNaryBooleanClassExpression) description).getOperands().contains(cls));
//    }
//
//    public static void main(String[] args) throws Exception {
//        ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
////        File file = new File("../examples/family/father_oe.owl");
////        OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://example.com/father#father"));
////        OWLClass startClass = new OWLClassImpl(IRI.create("http://example.com/father#male"));
//
//        File file = new File("../examples/swore/swore.rdf");
//        OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://ns.softwiki.de/req/CustomerRequirement"));
//        OWLClass startClass = new OWLClassImpl(IRI.create("http://ns.softwiki.de/req/AbstractRequirement"));
//
//
//
//        AbstractKnowledgeSource ks = OWLAPIOntology.fromFile(file);
//        ks.init();
//
//        OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
//        baseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
//        baseReasoner.init();
//        ClosedWorldReasoner rc = new ClosedWorldReasoner(ks);
//        rc.setReasonerComponent(baseReasoner);
//        rc.init();
//
//        ClassExpressionLearningProblem lp = new ClassExpressionLearningProblem(rc);
//        lp.setAccuracyMethod(new AccMethodPredAcc());
////		lp.setEquivalence(false);
//        lp.setClassExpressionToDescribe(classToDescribe);
//        lp.init();
//
////        SimpleOWLEntityChecker entityChecker = new SimpleOWLEntityChecker(rc);
////        entityChecker.setAllowShortForm(true);
////        OWLClassExpression ce = new ManchesterOWLSyntaxClassExpressionParser(OWLManager.getOWLDataFactory(), entityChecker)
////                .parse("Requirement and isCreatedBy some Thing");
////
////        EvaluatedDescription<ClassScore> ec = lp.evaluate(ce);
////        System.out.println(ec);
////        ce = new ManchesterOWLSyntaxClassExpressionParser(OWLManager.getOWLDataFactory(), entityChecker)
////                .parse("Requirement and (CustomerRequirement or TextualScenario)");
////        OWLClassExpressionMinimizer minimizer = new OWLClassExpressionMinimizer(OWLManager.getOWLDataFactory(), rc);
////        ce = minimizer.minimize(ce);
////
////        ec = lp.evaluate(ce);
////        System.out.println(ec);
////        System.exit(0);
//
//        RhoDRDown op = new RhoDRDown();
//        op.setStartClass(startClass);
//        op.setReasoner(rc);
//        op.setUseNegation(false);
//        op.setUseHasValueConstructor(false);
//        op.setUseCardinalityRestrictions(true);
//        op.setUseExistsConstructor(true);
//        op.setUseAllConstructor(true);
//        op.init();
//
//        SimpleStochasticBeamSearch alg = new SimpleStochasticBeamSearch(20, Sets.newHashSet(startClass), rc, op, lp);
//        alg.search();
//        System.out.println("solutions:");
//        alg.getSolutions().forEach(System.out::println);
//
//
//    }
//}
