/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.unife.probabilistic.structure.algorithms;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.dllearner.core.*;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.unife.probabilistic.core.AbstractPSLA;
import org.dllearner.unife.probabilistic.core.StructureLearningException;
import org.dllearner.unife.probabilistic.parameter.algorithms.AbstractEDGE;
import org.dllearner.unife.utils.ReflectionHelper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import unife.bundle.exception.InconsistencyException;
import unife.bundle.utilities.BundleUtilities;

import java.util.*;

/**
 *
 * @author Giuseppe Cota <giuseta@gmail.com>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
public abstract class AbstractLEAP extends AbstractPSLA {

    private static final Logger logger = Logger.getLogger(LEAP.class.getName());

    @ConfigOption(name = "dummyClass", defaultValue = "owl:learnedClass", description = "You can specify a start class for the algorithm. To do this, you have to use Manchester OWL syntax without using prefixes.")
    private OWLClass dummyClass;

    @ConfigOption(name = "accuracy", description = "accuracy used during the computation of the probabilistic values (number of digital places)", defaultValue = "5")
    protected int accuracy = 5;

    protected AbstractEDGE edge;

    @Override
    public void init() throws ComponentInitException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // create dummy class
        if (dummyClass == null) {
            dummyClass = manager.getOWLDataFactory().getOWLClass(IRI.create("owl:learnedClass"));
        }

        logger.debug("getting the individuals");
        Set<OWLIndividual> positiveIndividuals;
        Set<OWLIndividual> negativeIndividuals;
        AbstractClassExpressionLearningProblem learningProblem = cela.getLearningProblem();
        if (learningProblem instanceof PosNegLP) {
            positiveIndividuals = ((PosNegLP) learningProblem).getPositiveExamples();
            negativeIndividuals = ((PosNegLP) learningProblem).getNegativeExamples();
        } else if (learningProblem instanceof PosOnlyLP) {
            positiveIndividuals = ((PosOnlyLP) learningProblem).getPositiveExamples();
            // use pseudo-negative individuals
            negativeIndividuals = Sets.difference(learningProblem.getReasoner().getIndividuals(), positiveIndividuals);
        } else if (learningProblem instanceof ClassLearningProblem) {
            // Java Reflection has been used to get values from private fields. 
            //It's neither a conventional way nor the universally suggested idea,
            // but in this case is the only way to extract positive and negative individuals
            // without modifing the DLLearner code (the creation of a plugin is the objective)
            try {
                List<OWLIndividual> positiveIndividualsList = ReflectionHelper.getPrivateField(learningProblem, "classInstances");
                positiveIndividuals = new TreeSet<>(positiveIndividualsList);
                negativeIndividuals = new TreeSet<>((List<OWLIndividual>) ReflectionHelper.getPrivateField(learningProblem, "superClassInstances"));
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                String msg = "Cannot extract the individuals from"
                        + " learning problem: " + e.getMessage();
                logger.error(msg);
                throw new ComponentInitException(msg);
            }

        } else {
            try {
                throw new LearningProblemUnsupportedException(learningProblem.getClass(), this.getClass());
            } catch (LearningProblemUnsupportedException e) {
                throw new ComponentInitException(e.getMessage());
            }
        }
        // convert the individuals into assertional axioms
        logger.debug("convert the individuals into assertional axioms");
        OWLDataFactory owlFactory = manager.getOWLDataFactory();
        Set<OWLAxiom> positiveExamples = new HashSet<>();
        for (OWLIndividual ind : positiveIndividuals) {
            OWLAxiom axiom = owlFactory.getOWLClassAssertionAxiom(dummyClass, ind);
            positiveExamples.add(axiom);
        }

        Set<OWLAxiom> negativeExamples = new HashSet<>();
        for (OWLIndividual ind : negativeIndividuals) {
            OWLAxiom axiom = owlFactory.getOWLClassAssertionAxiom(dummyClass, ind);
            negativeExamples.add(axiom);
        }

        edge.setPositiveExampleAxioms(positiveExamples);
        edge.setNegativeExampleAxioms(negativeExamples);

    }

    protected void printTimings(long totalTimeMills, long celaTimeMills, Map<String, Long> timeMap) {
        logger.info("Main: " + totalTimeMills + " ms");
        logger.info("CELOE: " + celaTimeMills + " ms");
        logger.info("EDGE: " + (timeMap.get("EM") + timeMap.get("Bundle")) + " ms");
        logger.info("\tBundle: " + timeMap.get("Bundle") + " ms");
        logger.info("\tEM: " + timeMap.get("EM") + " ms");
        long timeOther = totalTimeMills - celaTimeMills - (timeMap.get("EM") + timeMap.get("Bundle"));
        logger.info("Other: " + timeOther + " ms");
        logger.info("Program client: execution successfully terminated");
    }

    /**
     * @return the dummyClass
     */
    public OWLClass getDummyClass() {
        return dummyClass;
    }

    /**
     * @param dummyClass the dummyClass to set
     */
    public void setDummyClass(OWLClass dummyClass) {
        this.dummyClass = dummyClass;
    }

    /**
     * @return the edge
     */
    public AbstractEDGE getEdge() {
        return edge;
    }

    /**
     * @param edge the edge to set
     */
    @Autowired
    public void setEdge(AbstractEDGE edge) {
        this.edge = edge;
    }

    /**
     * @return the accuracy
     */
    public int getAccuracy() {
        return accuracy;
    }

    /**
     * @param accuracy the accuracy to set
     */
    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    protected LinkedHashSet<OWLSubClassOfAxiom> convertIntoAxioms(OWLOntologyManager manager, NavigableSet<? extends EvaluatedDescription<? extends Score>> evaluatedDescriptions) {
        LinkedHashSet<OWLSubClassOfAxiom> axioms = new LinkedHashSet<>(evaluatedDescriptions.size());
        OWLDataFactory factory = manager.getOWLDataFactory();
        for (EvaluatedDescription<? extends Score> description : evaluatedDescriptions.descendingSet()) {
            OWLAnnotation annotation = factory.
                    getOWLAnnotation(BundleUtilities.PROBABILISTIC_ANNOTATION_PROPERTY, factory.getOWLLiteral(description.getAccuracy()));
            OWLSubClassOfAxiom axiom = factory.
                    getOWLSubClassOfAxiom(description.getDescription(), dummyClass, Collections.singleton(annotation));
            axioms.add(axiom);
        }
        return axioms;
    }

    protected OWLOntology replaceSuperClass(OWLOntology finalOntology, Set<OWLSubClassOfAxiom> learnedAxioms) {
        logger.debug("Replacing super class \"dummyClass\" with \"classToDescribe\"");
        ClassLearningProblem clp = (ClassLearningProblem) cela.getLearningProblem();
        OWLOntologyManager man = finalOntology.getOWLOntologyManager();
        OWLDataFactory df = man.getOWLDataFactory();
        int numInitialAxioms = finalOntology.getAxiomCount();
        // remove the learned Axioms
        //man.removeAxiom(finalOntology, learnedAxioms.iterator().next());
        Set<OWLSubClassOfAxiom> learnedAxiomsCopy = new LinkedHashSet<>(learnedAxioms);
        for (OWLAxiom axiom : finalOntology.getAxioms(AxiomType.SUBCLASS_OF)) {
            for (OWLAxiom axiomToRemove : learnedAxiomsCopy) {
                // conviene usare una copia di probAddedAxioms 
                //in maniera tale da eliminare gli assiomi gi� trovati durante la ricerca e 
                //quindi ridurre il numero di check
                //logger.debug("Learned axiom to remove: " + BundleUtilities.getManchesterSyntaxString(axiomToRemove));
                if (axiomToRemove.equalsIgnoreAnnotations(axiom)) {
                    man.removeAxiom(finalOntology, axiom);
                    learnedAxiomsCopy.remove(axiomToRemove);
                    break;
                }
            }
        }
        int numAxiomsAfterRemove = finalOntology.getAxiomCount();
        // check if correctly removed
        if (numAxiomsAfterRemove != numInitialAxioms - learnedAxioms.size()) {
            String msg = "Error during the replacement of super class: "
                    + "Axiom remotion was incorrect. "
                    + "numAxiomsAfterRemove: " + numAxiomsAfterRemove
                    + " numInitialAxioms: " + numInitialAxioms
                    + " numAxioms to remove: " + learnedAxioms.size()
                    + " numAxioms removed: " + (numInitialAxioms - numAxiomsAfterRemove);
            logger.error(msg);
            throw new StructureLearningException(msg);
        }
        LinkedHashSet<OWLSubClassOfAxiom> newAxioms = new LinkedHashSet<>();
        for (OWLSubClassOfAxiom axiom : learnedAxioms) {
            OWLSubClassOfAxiom newAxiom = df.getOWLSubClassOfAxiom(axiom.getSubClass(),
                    clp.getClassToDescribe(), axiom.getAnnotations());
            newAxioms.add(newAxiom);
            logger.info("Learned Axiom: " + newAxiom);
        }
        man.addAxioms(finalOntology, newAxioms);
        // check if correctly added
        if (finalOntology.getAxiomCount() != numAxiomsAfterRemove + learnedAxioms.size()) {
            String msg = "Error during the replacement of super class: "
                    + "Axiom addition was incorrect."
                    + " numAxiomsAfterRemove: " + numAxiomsAfterRemove
                    + " numAxioms to add: " + learnedAxioms.size()
                    + " numAxioms added: " + (finalOntology.getAxiomCount() - numAxiomsAfterRemove);;
            logger.error(msg);
            throw new StructureLearningException(msg);
        }
        logger.debug("Replaced all the super classes");
        return finalOntology;
    }

    protected void addAxiom(OWLOntology ontology, OWLAxiom axiom) throws InconsistencyException {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        manager.addAxiom(ontology, axiom);
        PelletReasoner pelletReasoner = new PelletReasonerFactory().createReasoner(ontology);
        if (!pelletReasoner.isConsistent()) {
            String message = "The axiom will make the KB inconsistent.\n"
                    + "It will NOT be added";
            logger.warn(message);
            manager.removeAxiom(ontology, axiom);
            throw new InconsistencyException(message);
        }
    }

    protected void removeAxiom(OWLOntology ontology, OWLAxiom axiom) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        manager.removeAxiom(ontology, axiom);
    }

}
