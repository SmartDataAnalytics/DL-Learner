/**
 *
 */
package org.dllearner.algorithms.isle.index.semantic.simple;

import org.dllearner.algorithms.isle.index.SemanticAnnotator;
import org.dllearner.algorithms.isle.index.SimpleEntityCandidatesTrie;
import org.dllearner.algorithms.isle.index.TrieEntityCandidateGenerator;
import org.dllearner.algorithms.isle.index.TrieLinguisticAnnotator;
import org.dllearner.algorithms.isle.index.semantic.SemanticIndex;
import org.dllearner.algorithms.isle.index.syntactic.SyntacticIndex;
import org.dllearner.algorithms.isle.textretrieval.RDFSLabelEntityTextRetriever;
import org.dllearner.algorithms.isle.wsd.SimpleWordSenseDisambiguation;
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
     * {@code syntacticIndex} to query for documents containing these labels. This consutrctor initializes with
     * full lemmatizing enabled.
     *
     * @param ontology       ontology to retrieve entity labels from
     * @param syntacticIndex index to query for documents containing the labels
     */
    public SimpleSemanticIndex(OWLOntology ontology, SyntacticIndex syntacticIndex) {
        this(ontology, syntacticIndex, true);
    }

    /**
     * Initializes the semantic index to use {@code ontology} for finding all labels of an entity and
     * {@code syntacticIndex} to query for documents containing these labels.
     *
     * @param ontology       ontology to retrieve entity labels from
     * @param syntacticIndex index to query for documents containing the labels
     * @param useWordNormalization    whether word normalization should be used or not
     */
    public SimpleSemanticIndex(OWLOntology ontology, SyntacticIndex syntacticIndex, boolean useWordNormalization) {
        super(ontology);
        SimpleEntityCandidatesTrie trie;
        if (useWordNormalization) {
            trie = new SimpleEntityCandidatesTrie(new RDFSLabelEntityTextRetriever(ontology),
                    ontology, new SimpleEntityCandidatesTrie.LemmatizingWordNetNameGenerator(5));
        }
        else {
            trie = new SimpleEntityCandidatesTrie(new RDFSLabelEntityTextRetriever(ontology),
                    ontology, new SimpleEntityCandidatesTrie.DummyNameGenerator());
        }
//        trie.printTrie();
        TrieLinguisticAnnotator linguisticAnnotator = new TrieLinguisticAnnotator(trie);
        linguisticAnnotator.setNormalizeWords(useWordNormalization);
        setSemanticAnnotator(new SemanticAnnotator(
                new SimpleWordSenseDisambiguation(ontology),
                new TrieEntityCandidateGenerator(ontology, trie),
                linguisticAnnotator));

    }

}
