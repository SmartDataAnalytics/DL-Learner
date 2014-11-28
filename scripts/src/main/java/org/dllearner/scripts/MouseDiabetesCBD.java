package org.dllearner.scripts;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.dllearner.examples.MouseDiabetes;
import org.dllearner.utilities.owl.OWLAxiomCBDGenerator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MouseDiabetesCBD {
    private static final Logger logger = LoggerFactory.getLogger(MouseDiabetesCBD.class);
    private static String cbdFilePath = "/tmp/smallis/kb_cbd_05.owl";
    private static int cbdDepth = 5;
    private static List<String> exampleUris = new ArrayList<String>(Arrays.asList(
            // positive
            "http://dl-learner.org/smallis/allelic_info00001",
            "http://dl-learner.org/smallis/allelic_info00006",
            "http://dl-learner.org/smallis/allelic_info00009",
            "http://dl-learner.org/smallis/allelic_info00010",
            "http://dl-learner.org/smallis/allelic_info00013",
            "http://dl-learner.org/smallis/allelic_info00018",
            "http://dl-learner.org/smallis/allelic_info00022",
            "http://dl-learner.org/smallis/allelic_info00024",
            "http://dl-learner.org/smallis/allelic_info00027",
            "http://dl-learner.org/smallis/allelic_info00031",
            "http://dl-learner.org/smallis/allelic_info00036",
            "http://dl-learner.org/smallis/allelic_info00040",
            "http://dl-learner.org/smallis/allelic_info00045",
            "http://dl-learner.org/smallis/allelic_info00051",
            "http://dl-learner.org/smallis/allelic_info00054",
            "http://dl-learner.org/smallis/allelic_info00060",
            "http://dl-learner.org/smallis/allelic_info00063",
            "http://dl-learner.org/smallis/allelic_info00064",
            "http://dl-learner.org/smallis/allelic_info00067",
            "http://dl-learner.org/smallis/allelic_info00072",
            // negative
            "http://dl-learner.org/smallis/allelic_info00077",
            "http://dl-learner.org/smallis/allelic_info00080",
            "http://dl-learner.org/smallis/allelic_info00081",
            "http://dl-learner.org/smallis/allelic_info00082",
            "http://dl-learner.org/smallis/allelic_info00083",
            "http://dl-learner.org/smallis/allelic_info00087",
            "http://dl-learner.org/smallis/allelic_info00088",
            "http://dl-learner.org/smallis/allelic_info00091",
            "http://dl-learner.org/smallis/allelic_info00093",
            "http://dl-learner.org/smallis/allelic_info00094",
            "http://dl-learner.org/smallis/allelic_info00096",
            "http://dl-learner.org/smallis/allelic_info00099",
            "http://dl-learner.org/smallis/allelic_info00100",
            "http://dl-learner.org/smallis/allelic_info00102",
            "http://dl-learner.org/smallis/allelic_info00103",
            "http://dl-learner.org/smallis/allelic_info00105",
            "http://dl-learner.org/smallis/allelic_info00108",
            "http://dl-learner.org/smallis/allelic_info00109",
            "http://dl-learner.org/smallis/allelic_info00111",
            "http://dl-learner.org/smallis/allelic_info00114"
            ));

    public static void main (String[] args) throws Exception {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = man.getOWLDataFactory();
        logger.info("Loading ontology files...");
        OWLOntology ontology = MouseDiabetes.readDumpFiles();
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

        man.saveOntology(cbdOnt, new RDFXMLOntologyFormat(), new FileOutputStream(new File(cbdFilePath)));
    }
}
