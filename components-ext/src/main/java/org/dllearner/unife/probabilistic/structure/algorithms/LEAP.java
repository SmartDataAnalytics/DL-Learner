package org.dllearner.unife.probabilistic.structure.algorithms;

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.Score;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.unife.probabilistic.core.StructureLearningException;
import org.dllearner.unife.utils.OWLUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unife.bundle.exception.InconsistencyException;
import unife.bundle.utilities.BundleUtilities;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.NavigableSet;
import java.util.Set;

@ComponentAnn(name = "LEAP", shortName = "leap", version = 1.0)
public class LEAP extends AbstractLEAP {

    private static final Logger logger = LoggerFactory.getLogger(LEAP.class);

    @Override
    public void init() throws ComponentInitException {
        super.init();
    }

    @Override
    public void start() {
        stop = false;
        isRunning = true;

        long totalTimeMills = System.currentTimeMillis();
        long celaTimeMills = 0;
        //AbstractEDGEDistributed edge = (AbstractEDGEDistributed) pla;
        // First step: run Distributed EDGE
        edge.start();

        logger.debug("First EDGE cycle terminated.");
        logger.debug("Initial Log-likelihood: " + edge.getLL());
        //OWLOntology originalOntology = edge.getLearnedOntology();

        logger.debug("Starting structure learner LEAP");
//            Set<KnowledgeSource> newSources = Collections.singleton((KnowledgeSource) new OWLAPIOntology(ontology));
//            AbstractReasonerComponent reasoner = cela.getReasoner();
//            reasoner.changeSources(newSources);
//            try {
//                reasoner.init();
//                cela.init();
//            } catch (ComponentInitException cie) {
//                logger.error("Error: " + cie.getMessage());
//                throw new StructureLearningException(cie);
//            }
        // start class expression learning algorithm
        celaTimeMills = System.currentTimeMillis();
        cela.start();
        celaTimeMills = System.currentTimeMillis() - celaTimeMills;
        // get the best class expressions
        NavigableSet<? extends EvaluatedDescription<? extends Score>> evaluatedDescriptions = cela.getCurrentlyBestEvaluatedDescriptions();
        // convert the class expressions into axioms
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        LinkedHashSet<OWLSubClassOfAxiom> candidateAxioms = convertIntoAxioms(manager, evaluatedDescriptions);
        // perform a greedy search 
        logger.debug("Start greedy search");
        // temporaneo i raffinamenti dopo dovranno essere assegnati ad ogni processo
        Set<OWLSubClassOfAxiom> learnedAxioms = greedySearch(candidateAxioms);
        logger.debug("Greedy search finished");

        OWLOntology finalOntology = edge.getSourcesOntology();
        // In case replace super class
        if (cela.getLearningProblem() instanceof ClassLearningProblem) {
            finalOntology = replaceSuperClass(finalOntology, learnedAxioms);
        } else {
            for (OWLAxiom axiom : learnedAxioms) {
                logger.info("Learned Axiom: " + axiom);
            }
        }
        // final step save the ontology
        try {
            OWLUtils.saveOntology(finalOntology, outputFile, outFormat);
        } catch (OWLOntologyStorageException e) {
            String msg = "Cannot save the learned ontology: " + e.getMessage();
            throw new StructureLearningException(msg);
        }
        totalTimeMills = System.currentTimeMillis() - totalTimeMills;
        printTimings(totalTimeMills, celaTimeMills, edge.getTimeMap());
    }

    public static String getName() {
        return "LEAP";
    }

    private Set<OWLSubClassOfAxiom> greedySearch(LinkedHashSet<OWLSubClassOfAxiom> candidateAxioms) {
        BigDecimal bestLL = edge.getLL();
        logger.debug("Resetting EDGE");
        edge.reset();
        OWLOntology ontology = edge.getLearnedOntology();
        edge.changeSourcesOntology(ontology);
        LinkedHashSet<OWLSubClassOfAxiom> learnedAxioms = new LinkedHashSet<>();
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        for (OWLSubClassOfAxiom axiom : candidateAxioms) {
            logger.debug("Adding axiom: " + axiom);
            try {
                addAxiom(ontology, axiom);
            } catch (InconsistencyException iex) {
                logger.info(iex.getMessage());
                logger.info("Trying with the next class expression");
                continue;
            }
            logger.info("Axiom added.");
            logger.info("Running parameter learner");
            edge.start();
            BigDecimal currLL = edge.getLL();
            logger.info("Current Log-Likelihood: " + currLL);
            if (currLL.compareTo(bestLL) > 0) {
                logger.info("Log-Likelihood enhanced. Updating ontologies...");
                OWLAnnotation annotation = df.
                        getOWLAnnotation(BundleUtilities.PROBABILISTIC_ANNOTATION_PROPERTY,
                                df.getOWLLiteral(edge.getParameter(axiom).doubleValue()));
                OWLSubClassOfAxiom updatedAxiom = df.getOWLSubClassOfAxiom(axiom.getSubClass(),
                        axiom.getSuperClass(), Collections.singleton(annotation));
                learnedAxioms.add(updatedAxiom);
                updateOntology();
                bestLL = currLL;
            } else {
                logger.info("Log-Likelihood worsened. Removing Last Axiom...");
                removeAxiom(ontology, axiom);
            }
        }
        return learnedAxioms;
    }

    private void updateOntology() {
        logger.debug("Updating ontology");
        OWLOntology ontology = edge.getLearnedOntology();
        edge.changeSourcesOntology(ontology);
        logger.debug("Ontology Updated");
    }

}
