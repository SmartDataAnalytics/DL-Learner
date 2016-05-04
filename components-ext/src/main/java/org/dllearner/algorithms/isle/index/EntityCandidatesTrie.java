package org.dllearner.algorithms.isle.index;

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;

public interface EntityCandidatesTrie {
	
	/**
	 * Adds an entity to the set of candidates of a string
	 * @param s
	 * @param e
	 */
	void addEntry(List<Token> s, OWLEntity e);
	
	
	/**
	 * Gets set of candidate entities for a list of tokens
	 * @return
	 */
	Set<EntityScorePair> getCandidateEntities(List<Token> tokens);


	/**
	 * Returns the string on which this entry is based on. This is used e.g. for storing the original
     * ontology string when the parameter string has been added to the trie after generation by using
     * WordNet or other additional methods.
     *
	 * @return string generating the path of the longest match in the trie
	 */
	List<Token> getGeneratingStringForLongestMatch(List<Token> tokens);

    /**
     * Gets the longest matching string
     * @return
     */
    List<Token> getLongestMatchingText(List<Token> tokens);
}
