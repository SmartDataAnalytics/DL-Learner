/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.utils.unife;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.probabilistic.parameter.distributed.unife.edge.EDGEDistributedDynamic;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLOntologyKnowledgeSource;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.*;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
public class OWLUtils {

    private static Logger logger = Logger.getLogger(EDGEDistributedDynamic.class);

    /**
     * prefix used for the dummy class.
     */
    //protected static final String PREFIX = "https://sites.google.com/a/unife.it/ml/leap";
    /**
     * This method merges all the input knowledge sources and returns the
     * filename of the new ontology.
     *
     * @param sources set of knowledge bases
     * @return the ontology obtained from the merging of {@code sources}
     * @throws org.dllearner.core.ComponentInitException
     */
    public static OWLOntology mergeOntologies(Set<KnowledgeSource> sources) throws ComponentInitException {
        logger.info("Number of sources: " + sources.size());
        logger.info("creating ontology through merging of the sources");
        // list of source ontologies
        List<OWLOntology> owlAPIOntologies = new LinkedList<>();

        Set<OWLImportsDeclaration> directImports = new HashSet<>();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        for (KnowledgeSource source : sources) {
            OWLOntology ontology;
            if (source instanceof OWLOntologyKnowledgeSource) {
                ontology = ((OWLOntologyKnowledgeSource) source).createOWLOntology(manager);
                owlAPIOntologies.add(ontology);
            } else {
                //This parameter learner requires an ontology to process
                String message = "EDGE Parameter Learner Requires an OWLKnowledgeSource.  Received a KS of type: " + source.getClass().getName();
                logger.error(message);
                throw new ComponentInitException(message);
            }

            directImports.addAll(ontology.getImportsDeclarations());
        }

        //Now merge all of the knowledge sources into one ontology instance.
        try {
            logger.info("Merging the ontologies...");
            //The following line illustrates a problem with using different OWLOntologyManagers.  This can manifest itself if we have multiple sources who were created with different manager instances.
            //ontology = OWLManager.createOWLOntologyManager().createOntology(IRI.create("http://dl-learner/all"), new HashSet<OWLOntology>(owlAPIOntologies));
//            OWLOntology allOntology = manager.createOntology(IRI.create("http://dl-learner/all"), new HashSet<OWLOntology>(owlAPIOntologies));
            OWLOntology allOntology = manager.createOntology(IRI.generateDocumentIRI(), new HashSet<OWLOntology>(owlAPIOntologies));
            //we have to add all import declarations manually here, because this are no axioms
            List<OWLOntologyChange> addImports = new ArrayList<>();
            for (OWLImportsDeclaration i : directImports) {
                addImports.add(new AddImport(allOntology, i));
            }
            manager.applyChanges(addImports);
            logger.info("Ontologies merged. Complete ontology created");
            return allOntology;
        } catch (OWLOntologyCreationException e1) {
            String message = "OWLOntologyCreationException complete ontology NOT created. ";
            logger.error(message + e1.getMessage());
            throw new ComponentInitException(message);
        }

    }

    public static OWLClass createDummyClass(IRI dummyClassIRI) {
        // create dummy class
//            if (classToDescribe != null) {
//                logger.debug("Creating dummy class for " + ((OWLClass) classToDescribe).toStringID());
//            } else {
        logger.debug("Creating dummy class");

//            }
        // It must be very rare that a class in an ontology has the IRI defined by PREFIX#learnedClass
        // but let's make it sure
        int i = 0;
        //String dummyClassStringIRI = PREFIX + "#dummyClass";
//            String num = "";
//            while (ontologyOWL.containsClassInSignature(IRI.create(dummyClassStringIRI + num))) {
//                num = "" + i;
//                i++;
//            }
//            dummyClass = manager.getOWLDataFactory().getOWLClass(IRI.create(dummyClassStringIRI + num));
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//            OWLClass dummyClass = manager.getOWLDataFactory().getOWLClass(IRI.create(dummyClassStringIRI));
        OWLClass dummyClass = manager.getOWLDataFactory().getOWLClass(dummyClassIRI);
        logger.debug("Dummy class created");
        return dummyClass;
    }

    public static void saveOntology(OWLOntology resultOntology, String outputFile, String outFormat)
            throws OWLOntologyStorageException {
        OWLDocumentFormat formatter;
        switch (outFormat) {
            case "OWLXML":
                formatter = new OWLXMLDocumentFormat();
                break;
            case "OWLFUNCTIONAL":
                formatter = new FunctionalSyntaxDocumentFormat();
                break;
            default:
                formatter = new OWLXMLDocumentFormat();
                break;
        }
        resultOntology.getOWLOntologyManager().saveOntology(resultOntology, formatter, IRI.create(new File(outputFile)));
    }

    public static void saveAxioms(Set<OWLAxiom> axioms, String outputFile, String outFormat)
            throws OWLOntologyStorageException, OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology resultOntology = man.createOntology();
        man.addAxioms(resultOntology, axioms);
        OWLDocumentFormat formatter;
        switch (outFormat) {
            case "OWLXML":
                formatter = new OWLXMLDocumentFormat();
                break;
            case "OWLFUNCTIONAL":
                formatter = new FunctionalSyntaxDocumentFormat();
                break;
            default:
                formatter = new OWLXMLDocumentFormat();
                break;
        }
        resultOntology.getOWLOntologyManager().saveOntology(resultOntology, formatter, IRI.create(new File(outputFile)));
    }

    public static Set<OWLAxiom> convertIndividualsToAssertionalAxioms(
            Set<OWLIndividual> individuals, OWLClassExpression ce) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory owlFactory = manager.getOWLDataFactory();
        Set<OWLAxiom> assertionalAxioms = new HashSet<>();
        for (OWLIndividual ind : individuals) {
            OWLAxiom axiom = owlFactory.getOWLClassAssertionAxiom(ce, ind);
            assertionalAxioms.add(axiom);
        }
        return assertionalAxioms;
    }

    public static OWLEquivalentClassesAxiom convertSubClassOfIntoEquivalentClassesAxiom(OWLSubClassOfAxiom axiom) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLClassExpression class1 = axiom.getSubClass();
        OWLClassExpression class2 = axiom.getSuperClass();
        Set<OWLAnnotation> annotations = axiom.getAnnotations();
        OWLEquivalentClassesAxiom equivAxiom = df.getOWLEquivalentClassesAxiom(
                class2, class2, annotations);
        return equivAxiom;
    }
}
