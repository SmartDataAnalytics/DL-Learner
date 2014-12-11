package org.dllearner.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ExistentialRestrictionMaterialization;
import org.dllearner.reasoning.MaterializableFastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.owl.DLSyntaxObjectRenderer;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;


public class ReactomeMinimal {
    private static final Logger logger = Logger.getLogger(ReactomeMinimal.class);
    private static final String kbPathStr = "/tmp/tr_cbd.owl";
    private static final List<String> posExampleUris = new ArrayList<String>(Arrays.asList(
            "http://www.reactome.org/biopax/48887#BiochemicalReaction670",
            "http://www.reactome.org/biopax/48887#BiochemicalReaction1968",
            "http://www.reactome.org/biopax/48887#BiochemicalReaction1331",
            "http://www.reactome.org/biopax/48887#BiochemicalReaction3743",
            "http://www.reactome.org/biopax/48887#BiochemicalReaction3244"
        ));
    private static final List<String> negExampleUris = new ArrayList<String>(Arrays.asList(
            "http://www.reactome.org/biopax/48887#BiochemicalReaction2588",
            "http://www.reactome.org/biopax/48887#BiochemicalReaction4826",
            "http://www.reactome.org/biopax/48887#Degradation10",
            "http://www.reactome.org/biopax/48887#BiochemicalReaction2187",
            "http://www.reactome.org/biopax/48887#BiochemicalReaction1273"
        ));

    public static void main(String[] args) throws Exception {
        setUp();
        run();
    }

    private static void run() throws OWLOntologyCreationException, ComponentInitException {
        logger.debug("Starting...");

        logger.debug("creating positive and negative examples...");
        Set<Individual> posExamples = makeIndividuals(posExampleUris);
        Set<Individual> negExamples = makeIndividuals(negExampleUris);
        logger.debug("finished creating positive and negative examples");

        logger.debug("reading ontology...");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File(kbPathStr));
        logger.debug("read " + ontology.getAxiomCount() + " axioms");
        logger.debug("finished reading the ontology");
        
        ExistentialRestrictionMaterialization mat = new ExistentialRestrictionMaterialization(ontology);
        System.out.println(mat.materialize("http://purl.obolibrary.org/obo/CHEBI_33560"));

        logger.debug("initializing knowledge source...");
        KnowledgeSource ks = new OWLAPIOntology(ontology);
        ks.init();
        logger.debug("finished initializing knowledge source");

        logger.debug("initializing reasoner...");
        OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
        baseReasoner.setReasonerTypeString("pellet");
        baseReasoner.setUseFallbackReasoner(true);
        baseReasoner.init();
        Logger.getLogger(ElkReasoner.class).setLevel(Level.OFF);
       
        MaterializableFastInstanceChecker cwReasoner = new MaterializableFastInstanceChecker(ks);
        cwReasoner.setReasonerComponent(baseReasoner);
        cwReasoner.setHandlePunning(false);
        cwReasoner.setUseMaterializationCaching(false);
        cwReasoner.setMaterializeExistentialRestrictions(true);
        cwReasoner.init();
        logger.debug("finished initializing reasoner component");

        AbstractReasonerComponent rc = cwReasoner;

        logger.debug("initializing learning problem...");
        PosNegLPStandard lp = new PosNegLPStandard(rc);
        lp.setPositiveExamples(posExamples);
        lp.setNegativeExamples(negExamples);
        lp.init();
        logger.debug("finished initializing learning problem");
        
        Description d = new Intersection(
        		new NamedClass("http://purl.obolibrary.org/obo/GO_0016773"),
        		new ObjectSomeRestriction(new ObjectProperty("http://purl.obolibrary.org/obo/RO_0002233"), Thing.instance)
        		);
        System.out.println(d + ":" + lp.getAccuracyOrTooWeak(d, 1.0));
        
        logger.debug("initializing learning algorithm...");
        AbstractCELA la;

        OEHeuristicRuntime heuristic = new OEHeuristicRuntime();
        heuristic.setExpansionPenaltyFactor(0.001);

        CELOE celoe = new CELOE(lp, rc);
        celoe.setHeuristic(heuristic);
        celoe.setMaxExecutionTimeInSeconds(60*60*12);
        celoe.setNoisePercentage(80);
        celoe.setMaxNrOfResults(50);
        celoe.setSearchTreeFile("log/reactome-minimal.log");
//        celoe.setWriteSearchTree(true);
        celoe.setReplaceSearchTree(true);

//        ELLearningAlgorithm elLa = new ELLearningAlgorithm(lp, rc);
//        elLa.setNoisePercentage(1.0);
//        elLa.setWriteSearchTree(true);
//        elLa.setReplaceSearchTree(true);
//        la = elLa; // celoe;

        la = celoe;
//        Description startClass = new NamedClass("http://dl-learner.org/smallis/Allelic_info");
        logger.debug("finished initializing learning algorithm");
        logger.debug("initializing operator...");
        RhoDRDown op = new RhoDRDown();
        op.setInstanceBasedDisjoints(true);
        op.setUseNegation(false);
//        op.setStartClass(new NamedClass("http://dl-learner.org/smallis/Allelic_info"));
        op.setUseHasValueConstructor(false);
        op.setUseAllConstructor(false);
        op.setReasoner(rc);
        op.setSubHierarchy(rc.getClassHierarchy());
        op.setObjectPropertyHierarchy(rc.getObjectPropertyHierarchy());
        op.setDataPropertyHierarchy(rc.getDatatypePropertyHierarchy());
        op.init();
        Set<Description> refinements = op.refine(d, 5);
        for (Description ref : refinements) {
			System.out.println(ref + ":" + lp.getAccuracyOrTooWeak(ref, 1.0));
		}
        logger.debug("finished initializing operator");
        if(la instanceof CELOE)
        	((CELOE) la).setOperator(op);

        la.init();
        la.start();

        logger.debug("Finished");
    }

    private static void setUp() {
        logger.setLevel(Level.DEBUG);
        Logger.getLogger(AbstractReasonerComponent.class).setLevel(Level.OFF);
        ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
    }


    private static Set<Individual> makeIndividuals(List<String> uris) {
        Set<Individual> individuals = new HashSet<Individual>();
        for (String uri : uris) {
            individuals.add(new Individual(uri));
        }

        return individuals;
    }
}