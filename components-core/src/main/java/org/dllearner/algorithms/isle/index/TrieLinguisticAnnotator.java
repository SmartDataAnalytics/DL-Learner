package org.dllearner.algorithms.isle.index;

import java.util.HashSet;
import java.util.Set;

/**
 * Annotates a document using a prefix trie.
 *
 * @author Andre Melo
 */
public class TrieLinguisticAnnotator implements LinguisticAnnotator {
    EntityCandidatesTrie candidatesTrie;
    private boolean normalizeWords = true;

    public TrieLinguisticAnnotator(EntityCandidatesTrie candidatesTrie) {
        this.candidatesTrie = candidatesTrie;
    }

    /**
     * Generates annotation based on trie's longest matching strings. By default, the document's contents are
     * normalized using a lemmatizer. The normalization step can be disabled using the
     *
     * @param document the document to get annotations for
     * @return the set of annotation for the given document
     */
    @Override
    public Set<Annotation> annotate(Document document) {
        String content = document.getContent();
        Set<Annotation> annotations = new HashSet<Annotation>();
        for (int i = 0; i < content.length(); i++) {
            if (Character.isWhitespace(content.charAt(i))) {
                continue;
            }
            String unparsed = content.substring(i);
            if (normalizeWords) {
                unparsed = LinguisticUtil.getInstance().getNormalizedForm(unparsed);
            }
            String match = candidatesTrie.getLongestMatch(unparsed);
            if (match != null && !match.isEmpty()) {
                Annotation annotation = new Annotation(document, i, match.length());
                annotations.add(annotation);
                i += match.length() - 1;
            }
        }
        return annotations;
    }

    /**
     * Sets whether the document's contents should be normalized or not.
     * @param enabled if true normalizing is enabled, otherwise disabled
     */
    public void setNormalizeWords(boolean enabled) {
        normalizeWords = enabled;
    }
}
