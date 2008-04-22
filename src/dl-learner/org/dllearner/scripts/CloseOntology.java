package org.dllearner.scripts;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.OntologyCloserOWLAPI;

/**
 * Script for closing an ontology OWLAPI produces extensive filesizes, when
 * exporting output file ist named like input file, but recieves a
 * "_closedConcise" at the end.
 * 
 * Counts all roles of individuals and adds an Intersection (Concise) of
 * ExactCardinalityRestriction to the ABox
 * 
 */
public class CloseOntology {

	/**
	 * @param argument0
	 *            simply the path to the owl ontology "examples/test.owl"
	 */
	public static void main(String[] args) {
		String ontopath=""; 
		//ontopath="examples/carcinogenesis/carcinogenesis.owl";
		// inputURI
		//ontopath = args[0];
		File file = new File(ontopath);
		URI inputURI = file.toURI();

		// outputURI
		String ending = ontopath.substring(ontopath.lastIndexOf(".") + 1);
		ontopath = ontopath.replace("." + ending, "_closedConcise." + ending);
		file = new File(ontopath);
		URI outputURI = file.toURI();

		try {
			// initializing reasoner
			OWLFile owlFile = new OWLFile();
			owlFile.setURL(inputURI.toURL());
			Set<KnowledgeSource> ks = new HashSet<KnowledgeSource>();
			ks.add(owlFile);
			OWLAPIReasoner owlapireasoner = new OWLAPIReasoner(ks);
			owlapireasoner.init();

			// close
			OntologyCloserOWLAPI oc = new OntologyCloserOWLAPI(owlapireasoner);
			oc.testForTransitiveProperties(true);
			System.out.println("Attempting to close");
			oc.applyNumberRestrictionsConcise();
			System.out.println("Finished, preparing output");

			// save
			oc.writeOWLFile(outputURI);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
