/**
 *
 */
package org.dllearner.algorithms.isle.index.semantic.simple;

import org.dllearner.algorithms.isle.index.Document;
import org.dllearner.algorithms.isle.index.syntactic.SyntacticIndex;
import org.dllearner.algorithms.isle.index.semantic.SemanticIndex;
import org.dllearner.algorithms.isle.textretrieval.RDFSLabelEntityTextRetriever;
import org.dllearner.core.owl.Entity;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A semantic index which returns all documents which contain at least one of the labels assigned to a specific
 * entity in a provided ontology.
 *
 * @author Lorenz Buehmann
 */
public class SimpleSemanticIndex implements SemanticIndex {
    private SyntacticIndex syntacticIndex;
    private RDFSLabelEntityTextRetriever labelRetriever;

    /**
     * Initializes the semantic index to use {@code ontology} for finding all labels of an entity and
     * {@code syntacticIndex} to query for documents containing these labels.
     *
     * @param ontology       ontology to retrieve entity labels from
     * @param syntacticIndex index to query for documents containing the labels
     */
    public SimpleSemanticIndex(OWLOntology ontology, SyntacticIndex syntacticIndex) {
        this.syntacticIndex = syntacticIndex;
        labelRetriever = new RDFSLabelEntityTextRetriever(ontology);
    }

    /* (non-Javadoc)
     * @see org.dllearner.algorithms.isle.SemanticIndex#getDocuments(org.dllearner.core.owl.Entity)
     */
    @Override
    public Set<Document> getDocuments(Entity entity) {
        Set<Document> documents = new HashSet<Document>();
        Map<String, Double> relevantText = labelRetriever.getRelevantText(entity);

        for (Entry<String, Double> entry : relevantText.entrySet()) {
            String label = entry.getKey();
            documents.addAll(syntacticIndex.getDocuments(label));
        }

        return documents;
    }

    /* (non-Javadoc)
     * @see org.dllearner.algorithms.isle.SemanticIndex#count(java.lang.String)
     */
    @Override
    public int count(Entity entity) {
        return getDocuments(entity).size();
    }

    /* (non-Javadoc)
     * @see org.dllearner.algorithms.isle.SemanticIndex#getSize()
     */
    @Override
    public int getSize() {
        return syntacticIndex.getSize();
    }


}
