package org.dllearner.scripts;

import org.dllearner.utilities.owl.OWLAxiomCBDGenerator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ReactomeCBD {
    private static final Logger logger = LoggerFactory.getLogger(ReactomeCBD.class);
    private static String dumpFilePath = "/tmp/tr.owl";
    private static String cbdFilePath = "/tmp/tr_cbd.owl";
    private static int cbdDepth = 30;
    private static List<String> exampleUris = new ArrayList<>(Arrays.asList(
            // positive
            "http://www.reactome.org/biopax/48887#BiochemicalReaction670",
            "http://www.reactome.org/biopax/48887#BiochemicalReaction1968",
            "http://www.reactome.org/biopax/48887#BiochemicalReaction1331",
            "http://www.reactome.org/biopax/48887#BiochemicalReaction3743",
            "http://www.reactome.org/biopax/48887#BiochemicalReaction3244",
            // negative
            "http://www.reactome.org/biopax/48887#BiochemicalReaction2588",
            "http://www.reactome.org/biopax/48887#BiochemicalReaction4826",
            "http://www.reactome.org/biopax/48887#Degradation10",
            "http://www.reactome.org/biopax/48887#BiochemicalReaction2187",
            "http://www.reactome.org/biopax/48887#BiochemicalReaction1273"
    ));

    public static void main (String[] args) throws Exception {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = man.getOWLDataFactory();
        logger.info("Loading ontology...");
        OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File(dumpFilePath));
        logger.info("Done");
        OWLAxiomCBDGenerator cbdGenartor = new OWLAxiomCBDGenerator(ontology);
        cbdGenartor.setFetchCompleteRelatedTBox(true);

        OWLOntology cbdOnt = man.createOntology();

        for (String uri : exampleUris) {
            logger.info("Creating cbd for " + uri + "...");
            Set<OWLAxiom> cbdAxioms = cbdGenartor.getCBD(factory.getOWLNamedIndividual(IRI.create(uri)), cbdDepth);
            logger.info("  Done. Adding {} axioms to main CBD dataset...", cbdAxioms.size());
            man.addAxioms(cbdOnt, cbdAxioms);
            logger.info("  Also done");
        }

        man.saveOntology(cbdOnt, new RDFXMLDocumentFormat(), new FileOutputStream(new File(cbdFilePath)));
    }
}
