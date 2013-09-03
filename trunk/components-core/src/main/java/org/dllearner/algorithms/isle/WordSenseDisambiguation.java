package org.dllearner.algorithms.isle;

import org.dllearner.algorithms.isle.index.Annotation;
import org.dllearner.algorithms.isle.index.Document;
import org.dllearner.algorithms.isle.index.SemanticAnnotation;
import org.dllearner.core.owl.Entity;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

/**
 * Abstract class for
 *
 * @author Daniel Fleischhacker
 */
public abstract class WordSenseDisambiguation {
    OWLOntology ontology;

    /**
     * Initialize the word sense disambiguation to use the given ontology.
     *
     * @param ontology the ontology to disambiguate on
     */
    public WordSenseDisambiguation(OWLOntology ontology) {
        this.ontology = ontology;
    }

    /**
     * Chooses the correct entity for the given annotation from a set of candidate enties.
     *
     * @param annotation        the annotation to find entity for
     * @param candidateEntities the set of candidate entities
     * @return semantic annotation containing the given annotation and the chosen entity
     */
    public abstract SemanticAnnotation disambiguate(Annotation annotation, Set<Entity> candidateEntities);
}
