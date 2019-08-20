package org.dllearner.core.search.old2;

import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dllearner.accuracymethods.AccMethodPredAcc;
import org.dllearner.core.*;
import org.dllearner.core.search.Beam;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.*;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.ClosedWorldReasonerFast;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.refinementoperators.RefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.ConsoleProgressMonitor;
import org.dllearner.utilities.owl.OWLClassExpressionLengthCalculator;
import org.dllearner.utilities.owl.OWLClassExpressionMinimizer;
import org.dllearner.utilities.owl.SimpleOWLEntityChecker;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

/**
 * Reference implementation of stochastic beam search for OWL class expressions using
 * - class learning problem
 * - refinement operator rho
 * -
 *
 * @author Lorenz Buehmann
 */
public class SimpleStochasticBeamSearch2
        extends StochasticBeamSearch<OWLClassExpression, Score, EvaluatedDescription<Score>> {

    private RefinementOperator op;
    private AbstractClassExpressionLearningProblem<? extends Score> lp;
    private AbstractReasonerComponent reasoner;

    private long maxExecutionTimeMs = 10_000;

    private OWLClassExpressionMinimizer minimizer;

    public SimpleStochasticBeamSearch2(int beamSize,
                                       Set<OWLClassExpression> startHypotheses,
                                       AbstractReasonerComponent reasoner,
                                       RefinementOperator op,
                                       AbstractClassExpressionLearningProblem<? extends Score> lp) {
        super(beamSize, startHypotheses);
        this.reasoner = reasoner;
        this.op = op;
        this.lp = lp;

        minimizer = new OWLClassExpressionMinimizer(OWLManager.getOWLDataFactory(), reasoner);
    }

    @Override
    protected Set<OWLClassExpression> refine(OWLClassExpression hypothesis) {
        return ((RhoDRDown) op).refine(hypothesis, 25);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected EvaluatedDescription<Score> evaluate(OWLClassExpression hypothesis) {
        if(lengthCalculator.getLength(hypothesis) > 25) throw new RuntimeException("too long: " + hypothesis);
//        System.out.println(lp.evaluate(hypothesis, 0.2));
        return (EvaluatedDescription<Score>) lp.evaluate(hypothesis, 0.2);
    }

    @Override
    protected boolean isSolution(EvaluatedDescription<Score> hypothesis) {
//        System.out.println(hypothesis + "-->" + minimizer.minimize(hypothesis.getDescription()));
        // it can only be a solution if it's better than the min. accuracy
        return hypothesis.getAccuracy() >= minQuality
                && isAllowedClassExpression(minimizer.minimize(hypothesis.getDescription()))
                ;
    }

    @Override
    protected boolean isCandidate(EvaluatedDescription<Score> hypothesis) {
        double mas = getMaximumAchievableScore(hypothesis);
//        System.out.println(hypothesis + ":" + mas);
        return hypothesis.getAccuracy() < minQuality
                && getMaximumAchievableScore(hypothesis) >= minQuality
                && isAllowedClassExpression(hypothesis.getDescription());
    }

    @Override
    protected boolean terminationCriteriaSatisfied() {
        return false;
    }

    @Override
    protected boolean addSolution(EvaluatedDescription<Score> hypothesis) {
        // rewrite before added to solution set
//        System.out.println(hypothesis + " -> " + minimizer.minimize(hypothesis.getDescription()));
        hypothesis.setDescription(minimizer.minimize(hypothesis.getDescription()));
//        hypothesis.setDescription(hypothesis.getDescription());
        return super.addSolution(hypothesis);
    }

    @Override
    protected double utility(EvaluatedDescription<Score> hypothesis, EvaluatedDescription<Score> parent) {
        // quality gain
        double qualityGain = quality(hypothesis) - quality(parent);

        // max. achievable quality
        double maq = getMaximumAchievableScore(hypothesis);

        // complexity is the length
        double complexity = complexity(hypothesis);

        double utility = quality(hypothesis) + 0.5 * qualityGain + 0.2 * maq - 0.001 * complexity;

        return utility;
    }

    @Override
    protected double quality(EvaluatedDescription<Score> hypothesis) {
        return hypothesis.getAccuracy();
    }

    private OWLClassExpressionLengthCalculator lengthCalculator = new OWLClassExpressionLengthCalculator();
    @Override
    protected double complexity(EvaluatedDescription<Score> hypothesis) {
        return lengthCalculator.getLength(hypothesis.getDescription());
    }

    @Override
    protected void repopulateBeam(Beam<BeamNode<EvaluatedDescription<Score>>> beam,
                                  SortedSet<BeamNode<EvaluatedDescription<Score>>> candidates) {
        System.out.println("|beam| = " + beam.size());
        System.out.println("|candidates| = " + candidates.size());
        super.repopulateBeam(beam, candidates);
    }

    private double getMaximumAchievableScore(EvaluatedDescription<Score> ece) {
        Score score = ece.getScore();
        if(score instanceof ClassScore) {
            ClassScore cScore = ((ClassScore) score);
            return getMaximumAchievableScore(
                    cScore.getCoveredInstances().size(),
                    cScore.getNotCoveredNegInstances().size(),
                    cScore.getAdditionalInstances().size(),
                    cScore.getNotCoveredInstances().size());
        } else if(score instanceof ScorePosNeg) {
            ScorePosNeg cScore = ((ScorePosNeg) score);
            return getMaximumAchievableScore(
                    cScore.getCoveredPositives().size(),
                    cScore.getNotCoveredNegatives().size(),
                    cScore.getCoveredNegatives().size(),
                    cScore.getNotCoveredPositives().size());

        } else {
            throw new RuntimeException("score type not supported yet");
        }

    }

    private double getMaximumAchievableScore(int tp, int tn, int fp, int fn) {
        // due to downward refinement, the number of covered instances can only be reduced
        // thus, the upper bound should be the accuracy when we do not cover any neg. examples, i.e. fp = 0
        double mas = 1;
        if(lp instanceof ClassLearningProblem) {
            mas = ((AccMethodPredAcc)((ClassLearningProblem) lp).getAccuracyMethod()).getAccOrTooWeak2(tp, fn, 0, fn, 1.0);
        } else if(lp instanceof PosNegLP) {
            mas = ((AccMethodPredAcc)((PosNegLP) lp).getAccuracyMethod()).getAccOrTooWeak2(tp, fn, 0, fn, 1.0);
        }
//        double mas = ((AccMethodPredAcc)lp.getAccuracyMethod()).getAccOrTooWeak2(tp, fn, 0, fn, 1.0);

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
        if(lp instanceof ClassExpressionLearningProblem) {
            OWLClassExpression ce = ((ClassExpressionLearningProblem) lp).getClassExpressionToDescribe();
            if(description.equals(ce)) {
                return false;
            }

            if(isEquivalenceProblem) {
                // the class to learn must not appear on the outermost property level
                if(occursOnFirstLevel(description, ce)) {
                    return false;
                }
            } else {
                // none of the superclasses of the class to learn must appear on the
                // outermost property level
                TreeSet<OWLClassExpression> toTest = new TreeSet<>();
                toTest.add(ce);
                while(!toTest.isEmpty()) {
                    OWLClassExpression d = toTest.pollFirst();
                    if(occursOnFirstLevel(description, d)) {
                        return false;
                    }
                    toTest.addAll(reasoner.getClassHierarchy().getSuperClasses(d));
                }
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
        String path = args[0];
        int beamSize = Integer.parseInt(args[1]);
        String startClass = args.length == 3 ? args[2] : null;

        ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());

//        pelletBug();

//        swore();
//        poker();
        poker2(path, beamSize, startClass);
    }

    static void pelletBug() throws Exception {
        OWLOntology ont = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File("/tmp/poker_straight_flush_p50-n50.owl"));

        PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ont);
        reasoner.prepareReasoner();

        OWLClassExpression ce;

        ce = parseClassExpression(ont, "hasRank some {seven}");
//        reasoner.getSubClasses(ce, true);

        ce = parseClassExpression(ont, "hasCard max 4 (hasRank some {seven})");
        reasoner.getSubClasses(ce, true);


    }

    static void swore() throws Exception {
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

//        SimpleOWLEntityChecker entityChecker = new SimpleOWLEntityChecker(rc);
//        entityChecker.setAllowShortForm(true);
//        OWLClassExpression ce = new ManchesterOWLSyntaxClassExpressionParser(OWLManager.getOWLDataFactory(), entityChecker)
//                .parse("Requirement and isCreatedBy some Thing");
//
//        EvaluatedDescription<ClassScore> ec = lp.evaluate(ce);
//        System.out.println(ec);
//        ce = new ManchesterOWLSyntaxClassExpressionParser(OWLManager.getOWLDataFactory(), entityChecker)
//                .parse("Requirement and (CustomerRequirement or TextualScenario)");
//        OWLClassExpressionMinimizer minimizer = new OWLClassExpressionMinimizer(OWLManager.getOWLDataFactory(), rc);
//        ce = minimizer.minimize(ce);
//
//        ec = lp.evaluate(ce);
//        System.out.println(ec);
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

        SimpleStochasticBeamSearch2 alg = new SimpleStochasticBeamSearch2(50, Sets.newHashSet(startClass), rc, op, lp);
        alg.search();
        System.out.println("solutions:");
        alg.getSolutions().forEach(System.out::println);
    }

    static void poker() throws Exception {

            File file = new File("../examples/poker/pair50.owl");
            OWLClass startClass = OWLManager.getOWLDataFactory().getOWLThing();

            AbstractKnowledgeSource ks = OWLAPIOntology.fromFile(file);
            ks.init();

            OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
            baseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
            baseReasoner.init();
            ClosedWorldReasoner rc = new ClosedWorldReasoner(ks);
            rc.setReasonerComponent(baseReasoner);
            rc.init();

            RhoDRDown op = new RhoDRDown();
            op.setStartClass(startClass);
            op.setReasoner(rc);
            op.setUseNegation(false);
            op.setUseHasValueConstructor(false);
            op.setUseCardinalityRestrictions(true);
            op.setUseExistsConstructor(true);
            op.setUseAllConstructor(false);
            op.init();

        OWLDataFactory df = OWLManager.getOWLDataFactory();

            Set<OWLIndividual> posExamples = Lists.newArrayList("kb:hand9", "kb:hand13",
                    "kb:hand18",
                    "kb:hand19",
                    "kb:hand21",
                    "kb:hand22",
                    "kb:hand23",
                    "kb:hand24",
                    "kb:hand25",
                    "kb:hand26",
                    "kb:hand29",
                    "kb:hand35",
                    "kb:hand36",
                    "kb:hand38",
                    "kb:hand39",
                    "kb:hand40",
                    "kb:hand41",
                    "kb:hand43",
                    "kb:hand47",
                    "kb:hand48").stream()
                    .map(s -> s.replace("kb:", "http://localhost/foo#"))
                    .map(IRI::create)
                    .map(df::getOWLNamedIndividual)
                    .collect(Collectors.toSet());

            Set<OWLIndividual> negExamples = Lists.newArrayList("kb:hand0",
                    "kb:hand1",
                    "kb:hand2",
                    "kb:hand3",
                    "kb:hand4",
                    "kb:hand5",
                    "kb:hand6",
                    "kb:hand7",
                    "kb:hand8",
                    "kb:hand10",
                    "kb:hand11",
                    "kb:hand12",
                    "kb:hand14",
                    "kb:hand15",
                    "kb:hand16",
                    "kb:hand17",
                    "kb:hand20",
                    "kb:hand27",
                    "kb:hand28",
                    "kb:hand30",
                    "kb:hand31",
                    "kb:hand32",
                    "kb:hand33",
                    "kb:hand34",
                    "kb:hand37",
                    "kb:hand42",
                    "kb:hand44",
                    "kb:hand45",
                    "kb:hand46").stream()
                    .map(s -> s.replace("kb:", "http://localhost/foo#"))
                    .map(IRI::create)
                    .map(df::getOWLNamedIndividual)
                    .collect(Collectors.toSet());

            PosNegLP lp = new PosNegLPStandard(rc);
            lp.setPositiveExamples(posExamples);
            lp.setNegativeExamples(negExamples);
            lp.setAccuracyMethod(new AccMethodPredAcc());
            lp.init();


             SimpleStochasticBeamSearch2 alg = new SimpleStochasticBeamSearch2(20, Sets.newHashSet(startClass), rc, op, lp);
            alg.search();
            System.out.println("solutions:");
            alg.getSolutions().forEach(System.out::println);
    }

    static void poker2(String path, int beamSize, String startClassExpressionString) throws Exception {

        File file = new File(path);

        OWLAPIOntology ks = OWLAPIOntology.fromFile(file);
        ks.init();

        OWLClassExpression startClass = startClassExpressionString == null
                                                ? OWLManager.getOWLDataFactory().getOWLThing()
                                                : parseClassExpression(ks.getOntology(), startClassExpressionString);

        OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
        baseReasoner.setReasonerImplementation(ReasonerImplementation.PELLET);
        baseReasoner.init();
//        ClosedWorldReasonerFast rc = new ClosedWorldReasonerFast(ks);
        ClosedWorldReasoner rc = new ClosedWorldReasoner(ks);
        rc.setReasonerComponent(baseReasoner);
        rc.init();

        RhoDRDown op = new RhoDRDown();
        op.setStartClass(startClass);
        op.setReasoner(rc);
        op.setUseNegation(true);
        op.setUseHasValueConstructor(true);
        op.setFrequencyThreshold(10);
        op.setUseObjectValueNegation(true);
        op.setUseCardinalityRestrictions(true);
        op.setUseExistsConstructor(true);
        op.setUseAllConstructor(false);
        op.setCardinalityLimit(5);
        op.init();

        OWLDataFactory df = OWLManager.getOWLDataFactory();

        final String targetClass = "straight_flush";

        OWLClass hand = df.getOWLClass(IRI.create("http://dl-learner.org/examples/uci/poker#Hand"));

        Set<OWLIndividual> allIndividuals = Sets.newHashSet(ks.getOntology().getIndividualsInSignature());
        Set<OWLIndividual> posExamples = allIndividuals.stream()
                .filter(ind -> ks.getOntology().getAnnotationAssertionAxioms(ind.asOWLNamedIndividual().getIRI()).stream()
                        .map(OWLAnnotationAssertionAxiom::annotationValue)
                        .map(OWLAnnotationValue::asLiteral)
                        .anyMatch(lit -> lit.isPresent() && lit.get().getLiteral().equals(targetClass)))
                .collect(Collectors.toSet());

        Set<OWLIndividual> negExamples = ks.getOntology().getABoxAxioms(Imports.INCLUDED).stream()
                .filter(ax -> ax.isOfType(AxiomType.CLASS_ASSERTION))
                .map(ax -> (OWLClassAssertionAxiom)ax)
                .filter(ax -> ax.getClassExpression().equals(hand))
                .map(OWLClassAssertionAxiom::getIndividual)
                .filter(ind -> ks.getOntology().getAnnotationAssertionAxioms(ind.asOWLNamedIndividual().getIRI()).stream()
                        .map(OWLAnnotationAssertionAxiom::annotationValue)
                        .map(OWLAnnotationValue::asLiteral)
                        .anyMatch(lit -> lit.isPresent() && !lit.get().getLiteral().equals(targetClass)))
                .collect(Collectors.toSet());

//        negExamples = Sets.difference(allIndividuals, posExamples);

        PosNegLP lp = new PosNegLPStandard(rc);
        lp.setPositiveExamples(posExamples);
        lp.setNegativeExamples(negExamples);
        lp.setAccuracyMethod(new AccMethodPredAcc());
        lp.init();

        System.out.println("#pos ex. = " + posExamples.size());
        System.out.println("#neg ex. = " + negExamples.size());

        OWLClassExpression target = parseClassExpression(ks.getOntology(),
                "Hand and hasCard some ( Card and sameSuit min 4 ( Card and nextRank some ( Card and hasRank some (not {ace}))))");

        EvaluatedDescription<ScorePosNeg<OWLNamedIndividual>> targetED = lp.evaluate(target);

        System.out.println(targetED);
        System.out.println(targetED.getScore().printConfusionMatrix());
        System.out.println(targetED.getScore().getNotCoveredPositives());




        SimpleStochasticBeamSearch2 alg = new SimpleStochasticBeamSearch2(beamSize, Sets.newHashSet(startClass), rc, op, lp);
        alg.setProgressMonitor(new ConsoleProgressMonitor());
//        alg.setMinQuality(0.8);
        alg.search();
        System.out.println("solutions:");
        alg.getSolutions().forEach(System.out::println);
    }

    private static OWLClassExpression parseClassExpression(OWLOntology rootOntology, String s) {
        ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
        parser.setDefaultOntology(rootOntology);
        parser.setStringToParse(s);
        parser.setOWLEntityChecker(new ShortFormEntityChecker(
                new BidirectionalShortFormProviderAdapter(
                        rootOntology.getOWLOntologyManager(),
                        rootOntology.getImportsClosure(),
                        new SimpleShortFormProvider())));
        return parser.parseClassExpression();
    }
}
