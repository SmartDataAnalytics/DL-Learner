 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.algorithms.probabilistic.structure.unife.leap;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.collect.Sets;
import edu.stanford.nlp.util.Factory;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
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
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.probabilistic.unife.AbstractParameterLearningAlgorithm;
import org.dllearner.exceptions.UnsupportedLearnedAxiom;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.utils.unife.OWLClassExpressionSimplifierVisitorImpl;
import org.dllearner.utils.unife.ReflectionHelper;
import org.dllearner.utils.unife.OWLUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import unife.bundle.exception.InconsistencyException;
import unife.bundle.utilities.BundleUtilities;
import unife.constants.UniFeIRI;

/**
 * There could be different version of LEAP (sequential, multi-thread,
 * distributed), so we need an abstract class.
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
public abstract class AbstractLEAP extends AbstractPSLA {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLEAP.class);

    @ConfigOption(defaultValue = "owl:learnedClass", description = "You can "
            + "specify a start class for the algorithm. To do this, you have to "
            + "use Manchester OWL syntax without using prefixes.")
    protected OWLClass dummyClass;

    @ConfigOption(description = "accuracy used during the computation of the "
            + "probabilistic values (number of digital places)", defaultValue = "5")
    protected int accuracy = 5;

    @ConfigOption(description = "This is used to set the type of class axiom to learn. Accepted values (case insensitive): 'subClassOf', 'equivalentClasses', 'both'",
            required = false,
            defaultValue = "subClassOf")
    protected String classAxiomType = "subClassOf";

    @ConfigOption(defaultValue = "10", description = "maximum execution of the algorithm in seconds")
    protected int maxExecutionTimeInSeconds = 10; // TO DO: stop when execution time is over

    @ConfigOption(defaultValue = "1",
            required = false,
            description = "the number of probabilistic axioms that LEAP tries to "
            + "add into the ontology at each iteration of the greedy search")
    protected int blockSizeGreedySearch = 1;

    protected TreeMap<String, Long> timers;

    protected AbstractEDGE edge;

    public AbstractLEAP() {

    }

    public AbstractLEAP(AbstractCELA cela, AbstractParameterLearningAlgorithm pla) {
        super(cela, pla);
    }

    @Override
    public void init() throws ComponentInitException {

        timers = new TreeMap<>();

        //OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntologyManager manager = edge.getSourcesOntology().getOWLOntologyManager();
        // create dummy class
        if (dummyClass == null) {
            OWLDataFactory owlFactory = manager.getOWLDataFactory();
            dummyClass = owlFactory.getOWLClass(IRI.create(UniFeIRI.DISPONTE + "/learnedClass"));
            OWLSubClassOfAxiom axiom = owlFactory.
                    getOWLSubClassOfAxiom(dummyClass, owlFactory.getOWLThing());
            manager.addAxiom(edge.getSourcesOntology(), axiom);
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

    protected void printTimings(long totalTimeMills, long celaTimeMills, TreeMap<String, Long> timeMap) {
        logger.info("Main: " + totalTimeMills + " ms");
        logger.info("CELOE: " + celaTimeMills + " ms");
        long timeOther = totalTimeMills - celaTimeMills;
        for (Entry<String, Long> time : timeMap.entrySet()) {
            String names[] = time.getKey().split("\\.");
            if (names.length == 1) {
                timeOther -= time.getValue();
//                logger.info(timeMap.subMap(names[0], names[0] + Character.MAX_VALUE).toString());
            }

            String output = StringUtils.repeat("\t", names.length - 1);
            output += names[names.length - 1] + ": " + time.getValue() + " ms";
            logger.info(output);

        }

//        logger.info("EDGE: " + (timeMap.get("EM") + timeMap.get("Bundle")) + " ms");
//        logger.info("\tBundle: " + timeMap.get("Bundle") + " ms");
//        logger.info("\tEM: " + timeMap.get("EM") + " ms");
//        long timeOther = totalTimeMills - celaTimeMills - (timeMap.get("EM") + timeMap.get("Bundle"));
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

    private <T extends OWLAxiom> List<T> convertIntoAxioms(Class<T> type, OWLOntologyManager manager,
            NavigableSet<? extends EvaluatedDescription> evaluatedDescriptions) {
        List<T> axioms = new LinkedList<>();
        OWLDataFactory factory = manager.getOWLDataFactory();
        for (EvaluatedDescription description : evaluatedDescriptions.descendingSet()) {
            OWLClassExpression ce = (OWLClassExpression) description.getDescription();
            ce = OWLClassExpressionSimplifierVisitorImpl.getOWLClassExpression(ce, manager);
            OWLAnnotation annotation = factory.
                    getOWLAnnotation(BundleUtilities.PROBABILISTIC_ANNOTATION_PROPERTY, factory.getOWLLiteral(description.getAccuracy()));
//            if (classAxiomType.equalsIgnoreCase("subClassOf") || classAxiomType.equalsIgnoreCase("both")) {
            T axiom;
            if (type == OWLEquivalentClassesAxiom.class) {
                axiom = (T) factory.getOWLEquivalentClassesAxiom(ce, dummyClass, Collections.singleton(annotation));

            } else if (type == OWLSubClassOfAxiom.class) {
                axiom = (T) factory.getOWLSubClassOfAxiom(ce, dummyClass, Collections.singleton(annotation));
            } else {
                throw new RuntimeException("convertIntoAxioms only works with "
                        + "equivalent and subclassOf axioms");
            }
            axioms.add(axiom);
        }
        return axioms;
    }

    protected List<OWLSubClassOfAxiom> convertIntoSubClassOfAxioms(OWLOntologyManager manager, NavigableSet<? extends EvaluatedDescription> evaluatedDescriptions) {
        List<OWLSubClassOfAxiom> axioms = convertIntoAxioms(OWLSubClassOfAxiom.class, manager, evaluatedDescriptions);
        return axioms;
    }

    protected List<OWLEquivalentClassesAxiom> convertIntoEquivalentClassesAxioms(OWLOntologyManager manager, NavigableSet<? extends EvaluatedDescription> evaluatedDescriptions) {
        List<OWLEquivalentClassesAxiom> axioms = convertIntoAxioms(OWLEquivalentClassesAxiom.class, manager, evaluatedDescriptions);
        return axioms;
        
    }

    /*
     protected List<OWLSubClassOfAxiom> convertIntoSubClassOfAxioms(OWLOntologyManager manager, NavigableSet<? extends EvaluatedDescription> evaluatedDescriptions) {
     List<OWLSubClassOfAxiom> axioms = new LinkedList<>();
     OWLDataFactory factory = manager.getOWLDataFactory();
     for (EvaluatedDescription description : evaluatedDescriptions.descendingSet()) {
     OWLClassExpression ce = (OWLClassExpression) description.getDescription();
     ce = OWLClassExpressionSimplifierVisitorImpl.getOWLClassExpression(ce, manager);
     OWLAnnotation annotation = factory.
     getOWLAnnotation(BundleUtilities.PROBABILISTIC_ANNOTATION_PROPERTY, factory.getOWLLiteral(description.getAccuracy()));
     OWLSubClassOfAxiom axiom = factory.
     getOWLSubClassOfAxiom(ce, dummyClass, Collections.singleton(annotation));
     axioms.add(axiom);
     }
     return axioms;
     }

     protected LinkedHashSet<OWLEquivalentClassesAxiom> convertIntoEquivalentClassesAxioms(OWLOntologyManager manager, NavigableSet<? extends EvaluatedDescription> evaluatedDescriptions) {
     LinkedHashSet<OWLEquivalentClassesAxiom> axioms = new LinkedHashSet<>(evaluatedDescriptions.size());
     OWLDataFactory factory = manager.getOWLDataFactory();
     for (EvaluatedDescription description : evaluatedDescriptions.descendingSet()) {
     OWLClassExpression ce = (OWLClassExpression) description.getDescription();
     ce = OWLClassExpressionSimplifierVisitorImpl.getOWLClassExpression(ce, manager);
     OWLAnnotation annotation = factory.
     getOWLAnnotation(BundleUtilities.PROBABILISTIC_ANNOTATION_PROPERTY, factory.getOWLLiteral(description.getAccuracy()));

     OWLEquivalentClassesAxiom axiom = factory.getOWLEquivalentClassesAxiom(ce, dummyClass, Collections.singleton(annotation));
     axioms.add(axiom);

     }
     return axioms;
     }
     */
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
                for (OWLClassExpression c : ((OWLEquivalentClassesAxiom) axiom).getClassExpressions()) {

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
     * It tries to add a set of axioms into the ontology. If there is an inconsistency
     * after adding the axiom the axiom is removed from the ontology and an
     * InconsistencyException is thrown.
     *
     * @param ontology ontology to modify
     * @param axioms axioms to add
     * @throws InconsistencyException if adding the exceptions leads to an
     * inconsistency
     */
    protected void addAxioms(OWLOntology ontology, List<? extends OWLAxiom> axioms) throws InconsistencyException {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        manager.addAxioms(ontology, new HashSet<>(axioms));
//        PelletReasoner pelletReasoner = new PelletReasonerFactory().createReasoner(ontology);
        PelletReasoner pelletReasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontology);
        if (!pelletReasoner.isConsistent()) {
            String message = "The axiom will make the KB inconsistent.\n"
                    + "It will NOT be added";
            logger.warn(message);
            manager.removeAxioms(ontology, new HashSet<>(axioms));
            pelletReasoner.dispose();
            throw new InconsistencyException(message);
        }
        pelletReasoner.dispose();
    }

    protected void removeAxioms(OWLOntology ontology, List<? extends OWLAxiom> axioms) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        manager.removeAxioms(ontology, new HashSet<>(axioms));
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
    public void setClassAxiomType(String classAxiomType) {
        this.classAxiomType = classAxiomType;
    }

    /**
     * @param maxExecutionTimeInSeconds the maxExecutionTimeInSeconds to set
     */
    public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
        this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
    }

    /**
     * @param blockSizeGreedySearch the blockSizeGreedySearch to set
     */
    public void setBlockSizeGreedySearch(int blockSizeGreedySearch) {
        this.blockSizeGreedySearch = blockSizeGreedySearch;
    }

}
