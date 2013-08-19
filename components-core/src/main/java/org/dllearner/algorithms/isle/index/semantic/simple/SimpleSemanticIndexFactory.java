/**
 *
 */
package org.dllearner.algorithms.isle.index.semantic.simple;

import org.dllearner.algorithms.isle.index.syntactic.SyntacticIndex;
import org.dllearner.algorithms.isle.index.semantic.SemanticIndex;
import org.dllearner.algorithms.isle.index.semantic.SemanticIndexFactory;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;

/**
 * This gets a syntactic index and returns a semantic index by applying WSD etc.
 *
 * @author Lorenz Buehmann
 * @author Daniel Fleischhacker
 */
public class SimpleSemanticIndexFactory implements SemanticIndexFactory {
    private OWLOntology ontology;
    private SyntacticIndex syntacticIndex;

    /**
     * Initializes a semantic index factory for creating simple semantic indexes. Simple semantic indexes use
     * the labels assigned to an entity in {@code ontology} as its surface forms and return the all documents
     * from the given syntactic index which contain at least one of these surface forms.
     *
     * @param syntacticIndex the syntactic index in which occurrences of the labels are searched
     * @param ontology       the ontology retrieve the entities' labels from
     */
    public SimpleSemanticIndexFactory(SyntacticIndex syntacticIndex, OWLOntology ontology) {
        this.syntacticIndex = syntacticIndex;
        this.ontology = ontology;
    }

    @Override
    public SemanticIndex createIndex(File inputDirectory) {
        return new SimpleSemanticIndex(ontology, syntacticIndex);
    }
}
