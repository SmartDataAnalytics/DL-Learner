package org.dllearner.algorithms.isle.index;

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Provides methods to annotate documents.
 *
 * @author Daniel Fleischhacker
 */
public class SemanticAnnotator {
	
    OWLOntology ontology;

    /**
     * Initialize this semantic annotator to use the entities from the provided ontology.
     *
     * @param ontology the ontology to use entities from
     */
    public SemanticAnnotator(OWLOntology ontology) {
        this.ontology = ontology;
    }

    /**
     * Processes the given document and returns the annotated version of this document.
     *
     * @param document the document to annotate
     * @return the given document extended with annotations
     */
    public AnnotatedDocument processDocument(Document document){
    	return null;
    }
}
