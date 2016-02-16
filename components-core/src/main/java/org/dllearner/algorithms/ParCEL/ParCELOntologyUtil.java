package org.dllearner.algorithms.ParCEL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 * Some utility functions for ontology manipulation
 * 
 * @author An C. Tran
 *
 */

public class ParCELOntologyUtil {

	/**
	 * ==========================================================================================
	 * Get ontology given its file path
	 * 
	 * @param ontologyFilePath
	 * 
	 * @return Opened ontology
	 * @throws OWLOntologyCreationException
	 */
	public static OWLOntology loadOntology(String ontologyFilePath)
			throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology;

		String flash = (System.getProperty("os.name").contains("Windows")) ? "/" : "";

		File f = new File(ontologyFilePath);

		if (!ontologyFilePath.contains("file:"))
			ontologyFilePath = "file:" + flash + f.getAbsolutePath();

		ontologyFilePath = ontologyFilePath.replace('\\', '/');

		ontology = manager.loadOntology(IRI.create(ontologyFilePath));

		return ontology;
	}

	/**
	 * ============================================================================================
	 * Persist the ontology
	 * 
	 * @param ontology
	 *            Ontology which need to be persisted
	 * 
	 * @throws OWLOntologyStorageException
	 */
	public static void persistOntology(OWLOntology ontology) throws OWLOntologyStorageException {
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		manager.saveOntology(ontology);
	}

	/**
	 * ============================================================================================
	 * Persist ontology to another ontology
	 * 
	 * @param ontology
	 *            Ontology contains changes
	 * @param newFilePath
	 *            Path to the new ontology file
	 * 
	 * @throws OWLOntologyStorageException
	 * @throws IOException
	 */
	public static void persistOntology(OWLOntology ontology, String newFilePath)
			throws OWLOntologyStorageException, IOException {
		OWLOntologyManager manager = ontology.getOWLOntologyManager();

		File f = new File(newFilePath);
		FileOutputStream fo = new FileOutputStream(f);

		manager.saveOntology(ontology, fo);
		fo.close();
	}

}
