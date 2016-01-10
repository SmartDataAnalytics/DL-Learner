package org.dllearner.test;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/**
 * @author Jens Lehmann
 *
 */
public class PelletPerformanceProblem {

	public static void main(String[] args) throws OWLOntologyCreationException {
		Logger pelletLogger = Logger.getLogger("org.mindswap.pellet");
		pelletLogger.setLevel(Level.WARN);		
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        File f = new File("examples/epc/conf/sap_modell_komplett_2.owl");
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
        OWLReasoner reasoner = new PelletReasonerFactory().createReasoner(ontology, new SimpleConfiguration());
        System.out.println("ontology loaded");
        
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS);
	}
	
}
