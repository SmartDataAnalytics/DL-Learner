package org.dllearner.scripts;

import org.dllearner.examples.MouseDiabetes;
import org.dllearner.utilities.owl.OWLAxiomCBDGenerator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MouseDiabetesCBD {
    private static final Logger logger = LoggerFactory.getLogger(MouseDiabetesCBD.class);
    public static String cbdFilePath = "/tmp/smallis/kb_cbd_05.owl";
    private static int cbdDepth = 5;
    
	public static int nrOfPosExamples = 5;
	public static int nrOfNegExamples = 5;
	
	private static List<OWLIndividual> exampleUris = new ArrayList<>();

    public static void main (String[] args) throws Exception {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = man.getOWLDataFactory();
        
        logger.info("Loading ontology files...");
        OWLOntology ontology = MouseDiabetes.readDumpFiles();
        logger.info("Done");
        OWLAxiomCBDGenerator cbdGenartor = new OWLAxiomCBDGenerator(ontology);
        cbdGenartor.setFetchCompleteRelatedTBox(true);
        
        List<OWLIndividual> posExamples = new ArrayList<>(MouseDiabetes.loadPosExamples());
        exampleUris.addAll(posExamples.subList(0, nrOfPosExamples ));
        List<OWLIndividual> negExamples = new ArrayList<>(MouseDiabetes.loadNegExamples());
        exampleUris.addAll(negExamples.subList(0, nrOfNegExamples ));

        OWLOntology cbdOnt = man.createOntology();

        for (OWLIndividual ind : exampleUris) {
            logger.info("Creating cbd for " + ind + "...");
            Set<OWLAxiom> cbdAxioms = cbdGenartor.getCBD(factory.getOWLNamedIndividual(IRI.create(ind.toStringID())), cbdDepth);
            logger.info("  Done. Adding {} axioms to main CBD dataset...", cbdAxioms.size());
            man.addAxioms(cbdOnt, cbdAxioms);
            logger.info("  Also done");
        }

        man.saveOntology(cbdOnt, new RDFXMLDocumentFormat(), new FileOutputStream(new File(cbdFilePath)));
    }
}
