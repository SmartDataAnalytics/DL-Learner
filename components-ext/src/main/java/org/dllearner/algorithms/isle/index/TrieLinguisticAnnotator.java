package org.dllearner.algorithms.isle.index;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Annotates a document using a prefix trie.
 *
 * @author Andre Melo
 */
public class TrieLinguisticAnnotator implements LinguisticAnnotator {
    EntityCandidatesTrie candidatesTrie;
    private boolean normalizeWords = true;
    
    private boolean ignoreStopWords = true;

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
    public Set<Annotation> annotate(TextDocument document) {
        Set<Annotation> annotations = new HashSet<>();
        
        List<Token> matchedTokens;
        for (Token token : document) {
        	if(!(token.isPunctuation() ||token.isStopWord())){
        		matchedTokens = candidatesTrie.getLongestMatchingText(document.getTokensStartingAtToken(token, true));
            	if(matchedTokens != null && !matchedTokens.isEmpty()){
            		Annotation annotation = new Annotation(document, matchedTokens);
                    annotations.add(annotation);
            	}
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
