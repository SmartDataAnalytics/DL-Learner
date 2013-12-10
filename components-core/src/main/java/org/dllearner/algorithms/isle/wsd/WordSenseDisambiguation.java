package org.dllearner.algorithms.isle.wsd;

import org.dllearner.algorithms.isle.index.Annotation;
import org.dllearner.algorithms.isle.index.EntityScorePair;
import org.dllearner.algorithms.isle.index.SemanticAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

/**
 * Abstract class for the word sense disambiguation component.
 *
 * @author Daniel Fleischhacker
 */
public abstract class WordSenseDisambiguation {
    OWLOntology ontology;

    /**
     * Initializes the word sense disambiguation to use the given ontology.
     *
     * @param ontology the ontology to disambiguate on
     */
    public WordSenseDisambiguation(OWLOntology ontology) {
        this.ontology = ontology;
    }

    /**
     * Chooses the correct entity for the given annotation from a set of candidate entities.
     *
     *
     * @param annotation        the annotation to find entity for
     * @param candidateEntities the set of candidate entities
     * @return semantic annotation containing the given annotation and the chosen entity
     */
    public abstract SemanticAnnotation disambiguate(Annotation annotation, Set<EntityScorePair> candidateEntities);
}
