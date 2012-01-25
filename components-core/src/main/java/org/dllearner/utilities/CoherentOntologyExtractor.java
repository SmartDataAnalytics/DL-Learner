package org.dllearner.utilities;

import org.semanticweb.owlapi.model.OWLOntology;

public interface CoherentOntologyExtractor {
	
	OWLOntology getCoherentOntology(OWLOntology incoherentOntology);
	
	OWLOntology getCoherentOntology(OWLOntology ontology, boolean preferRoots);

}
