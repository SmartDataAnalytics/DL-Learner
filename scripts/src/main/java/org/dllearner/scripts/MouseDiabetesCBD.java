package org.dllearner.scripts;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dllearner.core.owl.Individual;
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
    public static String cbdFilePath = "/tmp/smallis/kb_cbd_05.owl";
    private static int cbdDepth = 5;
    
	public static int nrOfPosExamples = 5;
	public static int nrOfNegExamples = 5;
	
	private static List<Individual> exampleUris = new ArrayList<Individual>();

    public static void main (String[] args) throws Exception {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = man.getOWLDataFactory();
        
        logger.info("Loading ontology files...");
        OWLOntology ontology = MouseDiabetes.readDumpFiles();
        logger.info("Done");
        OWLAxiomCBDGenerator cbdGenartor = new OWLAxiomCBDGenerator(ontology);
        cbdGenartor.setFetchCompleteRelatedTBox(true);
        
        List<Individual> posExamples = new ArrayList<Individual>(MouseDiabetes.loadPosExamples());
        exampleUris.addAll(posExamples.subList(0, nrOfPosExamples ));
        List<Individual> negExamples = new ArrayList<Individual>(MouseDiabetes.loadNegExamples());
        exampleUris.addAll(negExamples.subList(0, nrOfNegExamples ));

        OWLOntology cbdOnt = man.createOntology();

        for (Individual ind : exampleUris) {
            logger.info("Creating cbd for " + ind + "...");
            Set<OWLAxiom> cbdAxioms = cbdGenartor.getCBD(factory.getOWLNamedIndividual(IRI.create(ind.getName())), cbdDepth);
            logger.info("  Done. Adding {} axioms to main CBD dataset...", cbdAxioms.size());
            man.addAxioms(cbdOnt, cbdAxioms);
            logger.info("  Also done");
        }

        man.saveOntology(cbdOnt, new RDFXMLOntologyFormat(), new FileOutputStream(new File(cbdFilePath)));
    }
}
