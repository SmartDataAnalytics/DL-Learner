package org.dllearner.examples;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassAsInstanceLearningProblem;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.refinementoperators.RhoDRDown;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class MouseDiabetesExp3CaI {

    private static final Logger logger = Logger.getLogger(MouseDiabetesExp3CaI.class);
    private static final String dir = "/tmp/";
    private static final String kbFilePath = dir + "go.owl";
    // http://pwestphal.aksw.org/smallis/telco_20150429/pos_ont_cai_intersection_curated.xml
    private static final String posOntFilePath = dir + "pos_ont_cai_intersection_curated.xml";
    // http://pwestphal.aksw.org/smallis/telco_20150429/neg_ont_cai_intersection_curated.xml
    private static final String negOntFilePath = dir + "neg_ont_cai_intersection_curated.xml";
    // http://pwestphal.aksw.org/smallis/telco_20150429/pos_uris_cai_intersection_curated.txt
    public static final String posExamplesFilePath = dir + "pos_uris_cai_intersection_curated.txt";
    // http://pwestphal.aksw.org/smallis/telco_20150429/neg_uris_cai_intersection_curated.txt
    public static final String negExamplesFilePath = dir + "neg_uris_cai_intersection_curated.txt";

    static boolean useCBD = false;

    public static void main(String[] args) throws Exception {
        setUp();
        logger.info("starting...");
        OWLOntology ontology;
        ontology = readDumpFiles();

        logger.info("reading positive and negative examples...");
        Set<OWLClass> posExamples = readExamples(posExamplesFilePath);
        Set<OWLClass> negExamples = readExamples(negExamplesFilePath);
        logger.info("finished reading examples");

        logger.info("Building locality based module...");
        /* This is done to narrow down the search space and avoid infeasible
         * number of concept combinations*/
        OWLOntology module = buildLBM(posExamples, negExamples, ontology);
        logger.info("-Done-");

        logger.info("initializing knowledge source...");
        KnowledgeSource ks = new OWLAPIOntology(module);
        ks.init();
        logger.info("finished initializing knowledge source");

        logger.info("initializing reasoner...");
        OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
        baseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        baseReasoner.setUseFallbackReasoner(true);
        baseReasoner.init();
        logger.info("finished initializing reasoner");

        logger.info("initializing learning problem...");
        ClassAsInstanceLearningProblem lp = new ClassAsInstanceLearningProblem();
        lp.setNegativeExamples(negExamples);
        lp.setPositiveExamples(posExamples);
        lp.setReasoner(baseReasoner);
        lp.init();
        logger.info("finished initializing learning problem");

        logger.info("initializing learning algorithm...");
        CELOE la;
        OEHeuristicRuntime heuristic = new OEHeuristicRuntime();
        heuristic.setExpansionPenaltyFactor(0.01);

        la = new CELOE(lp, baseReasoner);
        la.setHeuristic(heuristic);
        la.setMaxExecutionTimeInSeconds(36000);
        la.setMaxDepth(30);
        la.setNoisePercentage(20);
        la.setMaxNrOfResults(70);
        la.setWriteSearchTree(true);
        la.setReplaceSearchTree(true);
        la.setSearchTreeFile("log/mouse-diabetis.log");
//        OWLDataFactory factory = OWLManager.getOWLDataFactory();
//        OWLClassExpression startClass = factory.getOWLObjectUnionOf(
//                new OWLClassImpl(IRI.create("http://purl.obolibrary.org/obo/GO_0003674")),
//                new OWLClassImpl(IRI.create("http://purl.obolibrary.org/obo/GO_0005575")),
//                new OWLClassImpl(IRI.create("http://purl.obolibrary.org/obo/GO_0008150"))
//                );
//        OWLClassExpression startClass = new OWLClassImpl(IRI.create("http://dl-learner.org/smallis/Sample"));
//        OWLClassExpression startClass = new OWLClassImpl(IRI.create("http://purl.obolibrary.org/obo/GO_TOP"));

//        OWLClassExpression startClass = factory.getOWLObjectIntersectionOf(
//              new OWLClassImpl(IRI.create("http://dl-learner.org/smallis/Sample")),
//              new OWLObjectSomeValuesFromImpl(
//                      new OWLObjectPropertyImpl(IRI.create("http://dl-learner.org/smallis/has_gene_association")),
//                      factory.getOWLThing()));
//      la.setStartClass(startClass);
        logger.info("finished initializing learning algorithm");

        logger.info("initializing operator...");
        RhoDRDown op = new RhoDRDown();
        op.setInstanceBasedDisjoints(true);
        op.setUseNegation(false);
        op.setUseHasValueConstructor(true);
        op.setUseAllConstructor(false);
//        op.setStartClass(startClass);
        op.setReasoner(baseReasoner);
        op.setSubHierarchy(baseReasoner.getClassHierarchy());
        op.setObjectPropertyHierarchy(baseReasoner.getObjectPropertyHierarchy());
        op.setDataPropertyHierarchy(baseReasoner.getDatatypePropertyHierarchy());
        op.init();
        logger.info("finished initializing operator");
        la.setOperator(op);

        la.init();
        la.start();
    }

    public static Set<OWLClass> loadPosExamples() throws IOException {
        return readExamples(posExamplesFilePath);
    }

    public static Set<OWLClass> loadNegExamples() throws IOException {
        return readExamples(negExamplesFilePath);
    }

    public static Set<OWLClass> readExamples(String filePath) throws IOException {
        Set<OWLClass> classes = new TreeSet<>();
        try(BufferedReader buffRead = new BufferedReader(new FileReader(new File(filePath)))){
            String line;
            while ((line = buffRead.readLine()) != null) {
                line = line.trim();
//              line = line.substring(1, line.length()-1);  // strip off angle brackets
                classes.add(new OWLClassImpl(IRI.create(line)));
            }
        }
        return classes;
    }

    private static void setUp() {
        logger.setLevel(Level.DEBUG);
//        Logger.getLogger(AbstractReasonerComponent.class).setLevel(Level.OFF);
        StringRenderer.setRenderer(Rendering.DL_SYNTAX);
    }

    public static OWLOntology readDumpFiles() throws
            Exception {

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ont = man.createOntology();

        logger.info("reading main knowledge base (" + kbFilePath + ")...");
        OWLOntology kb = readOWLOntology(kbFilePath);

        logger.info("reading positive examples data (" + posOntFilePath + ")...");
        OWLOntology posExmpls = readOWLOntology(posOntFilePath);

        logger.info("reading negative examples data (" + negOntFilePath + ")...");
        OWLOntology negExmpls = readOWLOntology(negOntFilePath);

        logger.info("finished reading files");

        logger.info("Merging ontologies...");
        man.addAxioms(ont, kb.getAxioms());
        man.addAxioms(ont, posExmpls.getAxioms());
        man.addAxioms(ont, negExmpls.getAxioms());
        logger.info("finished conversion");

        // sanity check
        if(containsErrorNamedClasses(ont)){
            logger.fatal("Ontology contains errors!!! Exiting...");
            System.exit(0);
        }

        return ont;
    }

    private static boolean containsErrorNamedClasses(OWLOntology ontology){
        for (OWLClass cls : ontology.getClassesInSignature()) {
            if(cls.toStringID().startsWith("http://org.semanticweb.owlapi/error#")){
                return true;
            }
        }
        return false;
    }

    public static OWLOntology readOWLELOntology(String filePath) throws
            OWLOntologyCreationException {

        OWLOntology ont = readOWLOntology(filePath);
        OWLOntologyManager elMan = OWLManager.createOWLOntologyManager();

        OWLOntology elOnt = elMan.createOntology();

        OWL2ELProfile el = new OWL2ELProfile();
        OWLProfileReport report = el.checkOntology(ont);
        HashSet<OWLAxiom> violatingAxioms = new HashSet<>();

        for (OWLProfileViolation violation : report.getViolations()) {
            violatingAxioms.add(violation.getAxiom());
        }

        for (OWLAxiom axiom : ont.getLogicalAxioms()) {
            if (!violatingAxioms.contains(axiom)) {
                elMan.addAxiom(elOnt, axiom);
            }
        }
//        IRI physicalModuleIRI = IRI.create(new File("/tmp/smallis/el_module.owl"));
//        elMan.saveOntology(elOnt, new RDFXMLOntologyFormat(), physicalModuleIRI);
        logger.info("-Done-");
        return elOnt;
    }

    public static OWLOntology readOWLOntology(String filePath) throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File(filePath));

        return ontology;
    }

    private static OWLOntology buildLBM(Set<OWLClass> posSamples,
            Set<OWLClass> negSamples, OWLOntology ont) throws OWLOntologyCreationException {

        Set<OWLEntity> allClasses = new HashSet<>();
        allClasses.addAll(posSamples);
        allClasses.addAll(negSamples);

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        SyntacticLocalityModuleExtractor extractor =
                new SyntacticLocalityModuleExtractor(man, ont, ModuleType.BOT);

        IRI moduleIRI = IRI.create("http://dl-learner.org/modules/mouse_diabetes/exp3.owl");
//        IRI moduleIRI = IRI.create(new File("/tmp/module.owl"));
        OWLOntology module = extractor.extractAsOntology(allClasses, moduleIRI);

        return module;
    }
}
