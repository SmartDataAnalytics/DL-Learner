package org.dllearner.kb.dataset;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Lorenz Buehmann
 */
public class SimpleFileOWLOntologyDataset implements OWLOntologyDataset{

	private final File directory;
	private final OWLOntologyManager man = OWLManager.createOWLOntologyManager();

	public SimpleFileOWLOntologyDataset(File directory) {
		this.directory = directory;
	}

	@Override
	public Collection<OWLOntology> loadOntologies() {
		Collection<OWLOntology> ontologies = new ArrayList<>();

		int i = 0;
		for (File file : directory.listFiles()) {
			try {
				OWLOntology ontology = man.loadOntologyFromOntologyDocument(file);
				ontologies.add(ontology);
				if(i == 10) break;
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}
		}
		return ontologies;
	}
}
