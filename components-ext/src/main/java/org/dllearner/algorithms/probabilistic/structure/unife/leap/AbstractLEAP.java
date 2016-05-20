/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.algorithms.probabilistic.structure.unife.leap;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.core.probabilistic.unife.AbstractPSLA;
import org.dllearner.core.probabilistic.unife.StructureLearningException;
import org.dllearner.algorithms.probabilistic.parameter.unife.edge.AbstractEDGE;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.probabilistic.unife.AbstractParameterLearningAlgorithm;
import org.dllearner.exceptions.UnsupportedLearnedAxiom;
import org.dllearner.utils.unife.ReflectionHelper;
import org.dllearner.utilities.Helper;
import org.dllearner.utils.unife.OWLUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import unife.bundle.exception.InconsistencyException;
import unife.bundle.utilities.BundleUtilities;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
public abstract class AbstractLEAP extends AbstractPSLA {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLEAP.class);

    @ConfigOption(defaultValue = "owl:learnedClass", description = "You can "
            + "specify a start class for the algorithm. To do this, you have to "
            + "use Manchester OWL syntax without using prefixes.")
    private OWLClass dummyClass;

    @ConfigOption(description = "accuracy used during the computation of the "
            + "probabilistic values (number of digital places)", defaultValue = "5")
    protected int accuracy = 5;

    @ConfigOption(description = "This is used to set the type of class axiom to learn. Accepted values (case insensitive): 'subClassOf', 'equivalentClasses', 'both'",
            required = false,
            defaultValue = "subClassOf")
    private String classAxiomType = "subClassOf";

    protected AbstractEDGE edge;

    public AbstractLEAP() {

    }

    public AbstractLEAP(AbstractCELA cela, AbstractParameterLearningAlgorithm pla) {
        super(cela, pla);
    }

    @Override
    public void init() throws ComponentInitException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // create dummy class
        if (dummyClass == null) {
            dummyClass = manager.getOWLDataFactory().getOWLClass(IRI.create("https://sites.google.com/a/unife.it/ml/disponte:learnedClass"));
        }

        logger.debug("getting the individuals");
        Set<OWLIndividual> positiveIndividuals;
        Set<OWLIndividual> negativeIndividuals;
        if (learningProblem == null) {
            learningProblem = cela.getLearningProblem();
        }
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
                throw new LearningProblemUnsupportedException(((AbstractClassExpressionLearningProblem) learningProblem).getClass(), this.getClass());
            } catch (LearningProblemUnsupportedException e) {
                throw new ComponentInitException(e.getMessage());
            }
        }
        // convert the individuals into assertional axioms
        logger.debug("convert the individuals into assertional axioms");
//        OWLDataFactory owlFactory = manager.getOWLDataFactory();
        Set<OWLAxiom> positiveExamples = OWLUtils.convertIndividualsToAssertionalAxioms(positiveIndividuals, dummyClass);
//        Set<OWLAxiom> positiveExamples = new HashSet<>();
//        for (OWLIndividual ind : positiveIndividuals) {
//            OWLAxiom axiom = owlFactory.getOWLClassAssertionAxiom(dummyClass, ind);
//            positiveExamples.add(axiom);
//        }

        Set<OWLAxiom> negativeExamples = OWLUtils.convertIndividualsToAssertionalAxioms(negativeIndividuals, dummyClass);
//        Set<OWLAxiom> negativeExamples = new HashSet<>();
//        for (OWLIndividual ind : negativeIndividuals) {
//            OWLAxiom axiom = owlFactory.getOWLClassAssertionAxiom(dummyClass, ind);
//            negativeExamples.add(axiom);
//        }

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

    protected LinkedHashSet<OWLSubClassOfAxiom> convertIntoSubClassOfAxioms(OWLOntologyManager manager, NavigableSet<? extends EvaluatedDescription> evaluatedDescriptions) {
        LinkedHashSet<OWLSubClassOfAxiom> axioms = new LinkedHashSet<>(evaluatedDescriptions.size());
        OWLDataFactory factory = manager.getOWLDataFactory();
        for (EvaluatedDescription description : evaluatedDescriptions.descendingSet()) {
            OWLAnnotation annotation = factory.
                    getOWLAnnotation(BundleUtilities.PROBABILISTIC_ANNOTATION_PROPERTY, factory.getOWLLiteral(description.getAccuracy()));
//            if (classAxiomType.equalsIgnoreCase("subClassOf") || classAxiomType.equalsIgnoreCase("both")) {
            OWLSubClassOfAxiom axiom = factory.
                    getOWLSubClassOfAxiom((OWLClassExpression) description.getDescription(), dummyClass, Collections.singleton(annotation));
            axioms.add(axiom);
//            }
//            if (classAxiomType.equalsIgnoreCase("equivalentClasses") || classAxiomType.equalsIgnoreCase("both")) {
//                OWLEquivalentClassesAxiom axiom = factory.getOWLEquivalentClassesAxiom((OWLClassExpression) description.getDescription(), dummyClass, Collections.singleton(annotation));
//                axioms.add(axiom);
//            }
        }
        return axioms;
    }

    protected LinkedHashSet<OWLEquivalentClassesAxiom> convertIntoEquivalentClassesAxioms(OWLOntologyManager manager, NavigableSet<? extends EvaluatedDescription> evaluatedDescriptions) {
        LinkedHashSet<OWLEquivalentClassesAxiom> axioms = new LinkedHashSet<>(evaluatedDescriptions.size());
        OWLDataFactory factory = manager.getOWLDataFactory();
        for (EvaluatedDescription description : evaluatedDescriptions.descendingSet()) {
            OWLAnnotation annotation = factory.
                    getOWLAnnotation(BundleUtilities.PROBABILISTIC_ANNOTATION_PROPERTY, factory.getOWLLiteral(description.getAccuracy()));

            OWLEquivalentClassesAxiom axiom = factory.getOWLEquivalentClassesAxiom((OWLClassExpression) description.getDescription(), dummyClass, Collections.singleton(annotation));
            axioms.add(axiom);

        }
        return axioms;
    }

    /**
     *
     * @param finalOntology
     * @param learnedAxioms
     * @return
     */
    protected OWLOntology replaceDummyClass(OWLOntology finalOntology, Set<OWLAxiom> learnedAxioms) throws UnsupportedLearnedAxiom {
        logger.debug("Replacing super class \"dummyClass\" with \"classToDescribe\"");
        ClassLearningProblem clp = (ClassLearningProblem) cela.getLearningProblem();
        OWLOntologyManager man = finalOntology.getOWLOntologyManager();
        OWLDataFactory df = man.getOWLDataFactory();
        int numInitialAxioms = finalOntology.getLogicalAxiomCount();
        // remove the learned Axioms
        //man.removeAxiom(finalOntology, learnedAxioms.iterator().next());
        Set<OWLAxiom> learnedAxiomsCopy = new LinkedHashSet<>(learnedAxioms);
        for (OWLAxiom axiom : finalOntology.getLogicalAxioms(Imports.EXCLUDED)) {
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
        int numAxiomsAfterRemove = finalOntology.getLogicalAxiomCount();
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
        LinkedHashSet<OWLAxiom> newAxioms = new LinkedHashSet<>();
        for (OWLAxiom axiom : learnedAxioms) {
            OWLAxiom newAxiom;
            if (axiom.isOfType(AxiomType.SUBCLASS_OF)) {
                newAxiom = df.getOWLSubClassOfAxiom(
                        ((OWLSubClassOfAxiom) axiom).getSubClass(),
                        clp.getClassToDescribe(),
                        axiom.getAnnotations());
            } else if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
                OWLClassExpression clazz = null;
                for (OWLClassExpression c : ((OWLEquivalentClassesAxiom) axiom).getClassExpressions()){

                    if (c.compareTo(getDummyClass()) != 0) {
                        clazz = c;
                        break;
                    }
                }
                if (clazz == null) {
                    throw new UnsupportedLearnedAxiom("The learned axiom " + axiom
                            + "has a null class");
                }
                newAxiom = df.getOWLEquivalentClassesAxiom(
                        clazz,
                        clp.getClassToDescribe(),
                        axiom.getAnnotations());
            } else {
                throw new UnsupportedLearnedAxiom("The learned axiom " + axiom
                        + "is not supported");
            }
            newAxioms.add(newAxiom);
            logger.info("Learned Axiom: " + newAxiom);
        }
        man.addAxioms(finalOntology, newAxioms);
        // check if correctly added
        if (numInitialAxioms != numAxiomsAfterRemove + learnedAxioms.size()) {
            String msg = "Error during the replacement of super class: "
                    + "Axiom addition was incorrect."
                    + " numAxiomsAfterRemove: " + numAxiomsAfterRemove
                    + " numAxioms to add: " + learnedAxioms.size()
                    + " numAxioms added: " + (numInitialAxioms - numAxiomsAfterRemove);;
            logger.error(msg);
            throw new StructureLearningException(msg);
        }
        logger.debug("Replaced all the super classes");
        return finalOntology;
    }

    /**
     * It tries to add the axiom into the ontology. If there is an inconsistency
     * after adding the axiom the axiom is removed from the ontology and an
     * InconsistencyException is thrown.
     *
     * @param ontology ontology to modify
     * @param axiom axiom to add
     * @throws InconsistencyException if adding the exceptions leads to an
     * inconsistency
     */
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

    /**
     * @return the classAxiomType
     */
    public String getClassAxiomType() {
        return classAxiomType;
    }

    /**
     * @param classAxiomType the classAxiomType to set
     */
    @Autowired
    public void setClassAxiomType(String classAxiomType) {
        this.classAxiomType = classAxiomType;
    }

}
