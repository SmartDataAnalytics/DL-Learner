/**
 *
 */
package org.dllearner.algorithms.isle.index.semantic.simple;

import org.dllearner.algorithms.isle.SimpleWordSenseDisambiguation;
import org.dllearner.algorithms.isle.index.SimpleEntityCandidateGenerator;
import org.dllearner.algorithms.isle.index.SimpleEntityCandidatesTrie;
import org.dllearner.algorithms.isle.index.SimpleLinguisticAnnotator;
import org.dllearner.algorithms.isle.index.TrieEntityCandidateGenerator;
import org.dllearner.algorithms.isle.index.semantic.SemanticIndex;
import org.dllearner.algorithms.isle.index.syntactic.SyntacticIndex;
import org.dllearner.algorithms.isle.textretrieval.AnnotationEntityTextRetriever;
import org.dllearner.algorithms.isle.textretrieval.RDFSLabelEntityTextRetriever;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * A semantic index which returns all documents which contain at least one of the labels assigned to a specific
 * entity in a provided ontology.
 *
 * @author Lorenz Buehmann
 */
public class SimpleSemanticIndex extends SemanticIndex {

    /**
     * Initializes the semantic index to use {@code ontology} for finding all labels of an entity and
     * {@code syntacticIndex} to query for documents containing these labels.
     *
     * @param ontology       ontology to retrieve entity labels from
     * @param syntacticIndex index to query for documents containing the labels
     */
    public SimpleSemanticIndex(OWLOntology ontology, SyntacticIndex syntacticIndex) {
        super(ontology,
                syntacticIndex,
                new SimpleWordSenseDisambiguation(ontology),
                new TrieEntityCandidateGenerator(ontology, new SimpleEntityCandidatesTrie(new RDFSLabelEntityTextRetriever(ontology), ontology)),
                new SimpleLinguisticAnnotator());

    }

}
