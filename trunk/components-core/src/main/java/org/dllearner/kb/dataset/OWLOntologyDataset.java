package org.dllearner.kb.dataset;

import java.io.File;
import java.util.Collection;

import org.semanticweb.owlapi.model.OWLOntology;

public interface OWLOntologyDataset {
	
	static File datasetDirectory = new File("dataset");
	
	Collection<OWLOntology> loadOntologies();
	Collection<OWLOntology> loadIncoherentOntologies();
	Collection<OWLOntology> loadInconsistentOntologies();

}
