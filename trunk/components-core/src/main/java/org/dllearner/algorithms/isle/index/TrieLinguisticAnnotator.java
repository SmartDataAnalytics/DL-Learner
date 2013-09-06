package org.dllearner.algorithms.isle.index;

import java.util.HashSet;
import java.util.Set;

/**
 * Annotates a document using a prefix trie
 *
 * @author Andre Melo
 */
public class TrieLinguisticAnnotator implements LinguisticAnnotator {
    EntityCandidatesTrie candidatesTrie;

    public TrieLinguisticAnnotator(EntityCandidatesTrie candidatesTrie) {
        this.candidatesTrie = candidatesTrie;
    }

    /**
     * Generates annotation based on trie's longest matching strings
     *
     * @param document the document to get annotations for
     * @return the set of annotation for the given document
     */
    @Override
    public Set<Annotation> annotate(Document document) {
        String content = document.getContent();
        Set<Annotation> annotations = new HashSet<Annotation>();
        for (int i = 0; i < content.length(); i++) {
            String unparsed = content.substring(i);
            String match = candidatesTrie.getLongestMatch(unparsed);
            if (match != null && !match.isEmpty()) {
                Annotation annotation = new Annotation(document, i, match.length());
                annotations.add(annotation);
                i += match.length() - 1;
            }
        }
        return annotations;
    }

}
