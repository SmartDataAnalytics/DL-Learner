package org.dllearner.algorithms.probabilistic.structure.unife.leap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import org.dllearner.algorithms.probabilistic.parameter.unife.edge.AbstractEDGE;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dllearner.core.probabilistic.unife.StructureLearningException;
import org.dllearner.exceptions.UnsupportedLearnedAxiom;

import org.dllearner.utils.unife.OWLUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import unife.bundle.exception.InconsistencyException;
import unife.bundle.utilities.BundleUtilities;
import unife.math.utilities.MathUtilities;
import static unife.utilities.GeneralUtils.safe;

@ComponentAnn(name = "LEAP", shortName = "leap", version = 1.0)
public class LEAP extends AbstractLEAP {

    private static final Logger logger = LoggerFactory.getLogger(LEAP.class);

    public LEAP() {

    }

    public LEAP(AbstractCELA cela, AbstractEDGE lpr) {
        super(cela, lpr);
    }

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
        //edge.start();

//        logger.debug("First EDGE cycle terminated.");
        //logger.debug("Initial Log-likelihood: " + edge.getLL());
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
        cela.getReasoner().releaseKB();
        celaTimeMills = System.currentTimeMillis() - celaTimeMills;
        // get the best class expressions
        NavigableSet<? extends EvaluatedDescription> evaluatedDescriptions = cela.getCurrentlyBestEvaluatedDescriptions();
        // convert the class expressions into axioms
//        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntologyManager manager = edge.getSourcesOntology().getOWLOntologyManager();
        List<? extends OWLAxiom> candidateAxioms;
        if (getClassAxiomType().equalsIgnoreCase("subClassOf") || getClassAxiomType().equalsIgnoreCase("both")) {
            candidateAxioms = convertIntoSubClassOfAxioms(manager, evaluatedDescriptions);
        } else {
            candidateAxioms = convertIntoEquivalentClassesAxioms(manager, evaluatedDescriptions);
        }
        // perform a greedy search 
        logger.info("Start greedy search");
        // temporaneo i raffinamenti dopo dovranno essere assegnati ad ogni processo
        Set<OWLAxiom> learnedAxioms = null;
        try {
            learnedAxioms = greedySearch(candidateAxioms);
        } catch (UnsupportedLearnedAxiom ex) {
            logger.error(ex.getMessage());
            System.exit(-1);
        }
        logger.info("Greedy search finished");

        OWLOntology finalOntology = edge.getSourcesOntology();
        // In case replace super class
        if (cela.getLearningProblem() instanceof ClassLearningProblem) {
            try {
                finalOntology = replaceDummyClass(finalOntology, learnedAxioms);
            } catch (UnsupportedLearnedAxiom ex) {
                logger.error(ex.getMessage());
                System.exit(-1);
            }
        } else {
            for (OWLAxiom axiom : safe(learnedAxioms)) {
                logger.info("Learned Axiom: " + axiom);
            }
        }
        // final step save the ontology
        try {
            logger.info("Saving the learned ontology");
            OWLUtils.saveOntology(finalOntology, outputFile, outFormat);
        } catch (OWLOntologyStorageException e) {
            String msg = "Cannot save the learned ontology: " + e.getMessage();
            throw new StructureLearningException(msg);
        }
        totalTimeMills = System.currentTimeMillis() - totalTimeMills;
        printTimings(totalTimeMills, celaTimeMills, timers);
    }

    /**
     * Returns the name of the algorithm.
     *
     * @return "LEAP"
     */
    public static String getName() {
        return "LEAP";
    }

    /**
     * This method performs a greedy search in the space of Theories. Given a
     * set of axioms it executes a loop for each axiom. For each iteration it
     * adds an axiom into the knowledge base and if the log-likelihood (LL)
     * increases the axiom is kept otherwise it is removed from the ontology.
     * For EquivalentClassesAxiom: if we keep an EquivalentClassesAxiom because
     * the LL has increased but a SubClassOfAxiom with the same classes has been
     * already added, the SubClassOfAxiom is removed.
     *
     * @param candidateAxioms the set of candidate axiom that we would like to
     * add in the knowledge base.
     * @return the set of axioms added in the knowledge base.
     */
    private Set<OWLAxiom> greedySearch(List<? extends OWLAxiom> candidateAxioms) throws UnsupportedLearnedAxiom {
        BigDecimal bestLL;
        if (edge instanceof AbstractEDGE) {
            bestLL = ((AbstractEDGE) edge).getLOGZERO().multiply(
                    new BigDecimal(edge.getPositiveExampleAxioms().size()));
            bestLL = bestLL.setScale(accuracy, RoundingMode.HALF_UP);
        } else {
            bestLL = BigDecimal.ZERO.multiply(
                    new BigDecimal(edge.getPositiveExampleAxioms().size()));
            bestLL = bestLL.setScale(accuracy, RoundingMode.HALF_UP);
        }
        logger.debug("Initial Log-likelihood: " + bestLL.toString());
//        BigDecimal bestLL = edge.getLL();
//        logger.debug("Resetting EDGE");
//        edge.reset();
        OWLOntology ontology = edge.getLearnedOntology();
        edge.changeSourcesOntology(ontology); // queste operazioni fanno perdere tempo, sono da ottimizzare
        LinkedHashSet<OWLAxiom> learnedAxioms = new LinkedHashSet<>();
        OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
        String infoMsg = "Type of axiom to learn: ";
        switch (getClassAxiomType().toLowerCase()) {
            case "subclassof":
                infoMsg += "subClassOf axioms";
                break;
            case "equivalentclasses":
                infoMsg += "equivalentClasses axioms";
                break;
            case "both":
                throw new UnsupportedLearnedAxiom("LEAP cannot learn this type of axioms: " + getClassAxiomType());
//                infoMsg += "subClassOf and equivalentClasses axioms";
//                break;
            default:
                throw new UnsupportedLearnedAxiom("LEAP cannot learn this type of axioms: " + getClassAxiomType());
        }
        logger.info(infoMsg);
//        int i = 0;
        int numChunks = (int) Math.ceil((double) candidateAxioms.size() / blockSizeGreedySearch);
        logger.info("number of axiom chunks: " + numChunks);
//        for (OWLAxiom axiom : candidateAxioms) {
        for (int i = 0; i < numChunks; i++) {
            int lastIndex = i < numChunks - 1 ? (i + 1) * blockSizeGreedySearch : candidateAxioms.size();
            List<? extends OWLAxiom> axioms = candidateAxioms.subList(i * blockSizeGreedySearch, lastIndex);
            if (i >= 0) {
                for (OWLAxiom axiom : axioms) {
                    logger.info("Adding axiom: " + axiom);
                }
                try {
                    addAxioms(ontology, axioms);
                } catch (InconsistencyException iex) {
                    logger.info(iex.getMessage());
                    logger.info("Trying with the next class expression");
                    continue;
                }
                logger.info("Running parameter learner");
                edge.start();
                BigDecimal currLL = edge.getLL();
                logger.info("Current Log-Likelihood: " + currLL);
                if (getClassAxiomType().equalsIgnoreCase("both")) {
                    // Not supported yet
                    // TO DO: I need to save the probabilistic values before adding 
                    // the equivalentClasses axiom. Because if I add the equivalentClasses axiom
                    // and the resulting Log-likelihood is worse than adding the 
                    // subClassOf axiom, then I need to recover the previously 
                    // computated probabilistic values
                    // (without running EDGE again)
                    throw new UnsupportedOperationException("Not supported yet.");
                    /* To uncomment in the future after the modifications of EDGE
                     logger.info("Trying to add the corresponding equivalent axiom");
                     logger.debug("but first I remove the axiom: " + axiom);
                     removeAxiom(ontology, axiom);
                     // here i need to sava the probabilistic values. A solution could be
                     // to modify EDGE. It should update the PMap object when it learns the
                     // parameters and EDGE should read it when the learned ontology
                     // is requested
                     OWLEquivalentClassesAxiom equivAxiom = OWLUtils.convertSubClassOfIntoEquivalentClassesAxiom((OWLSubClassOfAxiom) axiom);
                     logger.debug("Adding axiom: " + equivAxiom);
                     try {
                     addAxiom(ontology, axiom);
                     } catch (InconsistencyException iex) {
                     logger.info(iex.getMessage());
                     logger.info("Trying with the next class expression");
                     continue;
                     }
                     logger.info("Running parameter learner");
                     edge.start();
                     BigDecimal currLL2 = edge.getLL();
                     // if adding the equivalentClasses axioms leads to better log-likelihood I keep it
                     if (currLL2.compareTo(currLL) > 0) {
                     currLL = currLL2;
                     axiom = equivAxiom;
                     } else {
                     removeAxiom(ontology, equivAxiom);
                     ontology.getOWLOntologyManager().addAxiom(ontology, axiom);
                     }*/
                }
                if (currLL.compareTo(bestLL) > 0) {
                    logger.info("Log-Likelihood enhanced. Updating ontologies...");
                    // I recover the annotation containing the learned probabilistic values

                    for (OWLAxiom axiom : axioms) {
                        OWLAxiom updatedAxiom;

                        OWLAnnotation annotation = df.
                                getOWLAnnotation(BundleUtilities.PROBABILISTIC_ANNOTATION_PROPERTY,
                                        df.getOWLLiteral(edge.getParameter(axiom).doubleValue()));

                        if (axiom.isOfType(AxiomType.SUBCLASS_OF)) {
                            updatedAxiom = df.getOWLSubClassOfAxiom(
                                    ((OWLSubClassOfAxiom) axiom).getSubClass(),
                                    ((OWLSubClassOfAxiom) axiom).getSuperClass(),
                                    Collections.singleton(annotation));
                        } else {
                            if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {

                                // I have to remove the subsumption a
//                        for (OWLClass subClass : ((OWLEquivalentClassesAxiom) axiom).getNamedClasses()) {
//                            if (subClass.compareTo(getDummyClass()) != 0) {
////                                subClass = c;
//                                for (OWLAxiom lax : learnedAxioms) {
//                                    
//                                    if (lax.isOfType(AxiomType.SUBCLASS_OF)
//                                            && ((OWLSubClassOfAxiom) lax).getSubClass().compareTo(subClass) == 0) {
//                                        logger.in removeAxiom(ontology, lax
//                                        );
//                                        learnedAxioms.remove(lax);
//                                        break;
//                                    }
//                                }
//                                break;
//                            }
//                        }
                                updatedAxiom = df.getOWLEquivalentClassesAxiom(
                                        ((OWLEquivalentClassesAxiom) axiom).getClassExpressions(),
                                        Collections.singleton(annotation));
                            } else {
                                throw new UnsupportedLearnedAxiom("The axiom to add is not supported: "
                                        + BundleUtilities.getManchesterSyntaxString(axiom));
                            }
                        }
                        learnedAxioms.add(updatedAxiom);
                    }
                    updateOntology(); // queste operazioni fanno perdere tempo, sono da ottimizzare
                    bestLL = currLL;
                } else {
                    logger.info("Log-Likelihood worsened. Removing Last Axioms...");
                    removeAxioms(ontology, axioms);
                }
                for (Map.Entry<String, Long> timer : edge.getTimeMap().entrySet()) {
                    Long previousValue = timers.get(timer.getKey());
                    if (previousValue == null) {
                        previousValue = 0L;
                    }
                    timers.put(timer.getKey(), previousValue + timer.getValue());
                }
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
